package de.guc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;

import org.apache.commons.lang3.RandomStringUtils;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import de.guc.dto.MemberWithLoginDto;
import de.guc.dto.OnBoardingDto;
import de.guc.entities.EmailUpdateEntity;
import de.guc.entities.MemberEntity;
import de.guc.entities.ResourcesEntity;
import de.guc.entities.ResourcesEntity.ResourceType;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

/**
 * REST resource for managing member onboarding operations.
 * 
 * <p>This resource handles the onboarding of new members into the Goldfingers Ultimate Club (GUC)
 * system by:</p>
 * <ul>
 *   <li>Creating and registering users in Keycloak</li>
 *   <li>Storing member data in the database</li>
 *   <li>Granting access to member resources</li>
 *   <li>Sending welcome emails to new members</li>
 * </ul>
 * 
 * <p>Two types of onboarding are supported:</p>
 * <ol>
 *   <li><strong>New Member Onboarding</strong>: Creates a completely new member
 *       with a generated temporary password</li>
 *   <li><strong>Import of Existing Members</strong>: Imports members who already
 *       exist in Keycloak (e.g., child accounts, transferred members)</li>
 * </ol>
 * 
 * <p>Keycloak users are automatically enabled and assigned to the "GUC Members"
 * group once onboarding is successful.</p>
 * 
 * @see OnBoardingDto
 * @see OnBoardingDtoValidator
 */
@Path("v1/onboarding")
@RolesAllowed(Constants.ROLE_ON_BOARDING)
public class OnBoardingResource {

    // private final static Logger LOGGER =
    // LoggerFactory.getLogger(OnBoardingResource.class);
    private final static Pattern passwordPattern = Pattern
        .compile("(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[\\Q!\"§$%&/()?#+*,.;:\\E])");

    private static record OnBoardingContext(String keycloakId, String password, String login, MemberEntity member,
        boolean memberIsChild) {
        OnBoardingContext(String keycloakId, String password, String login) {
            this(keycloakId, password, login, null, false);
        }

        OnBoardingContext withMember(MemberEntity member) {
            return this.withMember(member, false);
        }

        OnBoardingContext withMember(MemberEntity member, boolean isChild) {
            return new OnBoardingContext(this.keycloakId, this.password, login, member, isChild);
        }
    }

    private static record ImportPair(MemberWithLoginDto member, OnBoardingContext context) {
    };

    @Inject
    Keycloak adminClient;
    @Inject
    Mailer mailer;

    record welcome(String firstname, String login, String password) implements TemplateInstance {
    };

    record imported(String firstname, String login, String password) implements TemplateInstance {
    };

    /**
     * Creates a new member by onboarding them into the GUC system.
     * 
     * <p>This endpoint processes a onboarding request for a new member by:</p>
     * <ol>
     *   <licreate the user in Keycloak with a generated temporary password</li>
     *   <li>Creating a {@link MemberEntity} record in the database</li>
     *   <li>Creating a resource access entry for the new member</li>
     *   <li>Sending a welcome email with login credentials to the member</li>
     * </ol>
     * 
     * <p>For child members, their parent's personal data is stored; for adult members,
     * their own data is stored. Email changes are recorded in {@link EmailUpdateEntity}.</p>
     * 
     * <p>If a Keycloak user with the given email already exists, the system:</p>
     * <ul>
     *   <li>Enables the user if they are not yet a member</li>
     *   <li>Adds them to the "GUC Members" group</li>
     *   <li>Reports the existing password as "UNCHANGED"</li>
     *   <li>Warns if attempting to create two members for the same user</li>
     * </ul>
     * 
     * <p>Only allowed to users with the {@link Constants#ROLE_ON_BOARDING} role.</p>
     * 
     * @param newMember The validated onboarding request containing member details,
     *                  email address, login, and birth date
     * @throws jakarta.ws.rs.WebApplicationException if a user with the given email
     *                                               already exists as a member
     * @throws jakarta.ws.rs.ValidationException     if the request fails validation
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public void OnBoardNewMember(@Valid OnBoardingDto newMember) {
        Log.infof("Starting on-boarding of %s %s", newMember.memberFirstName(), newMember.memberName());
        var context = this.registerUserAtKeycloak(newMember);

        context = this.storeNewMember(newMember, context);
        this.storeAccessResource(context);

        final var mailTemplate = new welcome(newMember.firstName(), newMember.login(), context.password());

        final var mail = Mail.withText(newMember.email(),
            "Herzlich Willkommen beim Goldfingers Ultimate Club e.V.",
            mailTemplate.render());
        this.mailer.send(mail);
    }

    /**
     * Imports multiple existing members from external sources into the GUC system.
     * 
     * <p>This endpoint accepts a list of member DTOs representing users who already
     * exist in Keycloak (e.g., child accounts imported along with their parent,
     * or members transferred from another system). For each member, the operation:</p>
     * <ol>
     *   <licreate the member entity with provided data</li>
     *   <li>Stores a resource access entry linking the user</li>
     *   <li>Sending a confirmation email to the member</li>
     * </ol>
     * 
     * <p>Child members are processed with their parent's personal data while maintaining
     * the child's login and email. DSE (disabled student evidence) status and DFV numbers
     * are preserved from the original member if applicable.</p>
     * 
     * <p>All imported members are enabled in Keycloak and granted "GUC Members" group
     * membership if not already assigned.</p>
     * 
     * @param importedMembers The list of members to import into the system
     *                        (must contain at least one member)
     * @throws jakarta.ws.rs.WebApplicationException if duplicate members are being
     *                                               imported (one user, two "SELF" roles)
     * @throws jakarta.ws.rs.ValidationException     if the request list is empty
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("import")
    public void ImportExistingMembers(@Valid @NotEmpty List<MemberWithLoginDto> importedMembers) {
        importedMembers.stream()
            .map(mwl -> {
            final boolean isChild = mwl.isChild();
            final OnBoardingDto dto = new OnBoardingDto(
                isChild ? mwl.getParentFirstName() : mwl.getGivenName(),
                isChild ? mwl.getParentLastName() : mwl.getName(),
                mwl.getEmail(),
                mwl.getLogin(),
                mwl.getDayOfBirth(),
                isChild, "", "");
            return new ImportPair(mwl, this.registerUserAtKeycloak(dto));
        })
            .map(pair -> {
            final MemberEntity member = new MemberEntity();
            member.city = pair.member.getCity();
            member.givenName = pair.member.getGivenName();
            member.name = pair.member.getName();
            member.gender = pair.member.getGender();
            member.dayOfBirth = pair.member.getDayOfBirth();
            member.entryDate = pair.member.getEntryDate();
            member.state = pair.member.getState();
            member.stateEffective = pair.member.getStateEffective();
            member.dfvNumber = pair.member.getDfvNumber();
            member.dse = pair.member.isDse();
            member.rabatt = pair.member.isDfvDiscount();
            member.street = pair.member.getStreet();
            member.zipCode = pair.member.getZipCode();
            member.mobile = pair.member.getMobile();
            member.email = pair.member.getEmail();
            member.emailList = pair.member.isEmailList();

            member.persist();
            return pair.context.withMember(member, pair.member.isChild());
        })
            .map(ctxt -> {
            this.storeAccessResource(ctxt);
            return ctxt;
        })
            .map(ctxt -> {
            final var templ = new imported(ctxt.member.givenName, ctxt.login,
                ctxt.password);
            return Mail.withText(ctxt.member.email,
                "GUC Mitgliederverwaltung - Neues Konto angelegt",
                templ.render());
        })
            .forEach(mail -> this.mailer.send(mail));
    }


    /**
     * Creates and persists a member access resource entry.
     * 
     * <p>This method creates a {@link ResourcesEntity} that associates a member with
     * their Keycloak user identity. The entry tracks:</p>
     * <ul>
     *   <li>The Keycloak user ID for future lookups</li>
     *   <li>The associated {@link MemberEntity}</li>
     *   <li>The role type ("SELF" or "PARENT")</li>
     *   <li>The member's given name</li>
     * </ul>
     * 
     * @param ctxt The onboarding context containing the Keycloak ID, member entity,
     *            and role information
     */
    private void storeAccessResource(OnBoardingContext ctxt) {
        final var access = new ResourcesEntity();
        access.identity = UUID.fromString(ctxt.keycloakId());
        access.member = ctxt.member();
        access.role = ctxt.memberIsChild() ? "PARENT" : "SELF";
        access.rType = ResourceType.MEMBER;
        access.name = ctxt.member().givenName;
        access.persist();
    }

    /**
     * Stores a new member entity and returns an updated context with the member attached.
     * 
     * <p>Creates a {@link MemberEntity} with the provided data and sets the entry date
     * to the current moment. Optionally records an {@link EmailUpdateEntity} entry if
     * this is not a child member (for email change history tracking).</p>
     * 
     * @param newMember The onboarding DTO containing member personal data
     * @param ctxt The existing onboarding context containing Keycloak information
     * @return A new context with the newly created member attached and role flag
     */
    private OnBoardingContext storeNewMember(OnBoardingDto newMember, OnBoardingContext ctxt) {
        final var member = new MemberEntity();

        member.name = newMember.consolidatedName();
        member.givenName = newMember.consolidatedFirstName();
        member.email = newMember.email();
        member.entryDate = LocalDate.now();
        member.dayOfBirth = newMember.dob();
        member.persist();

        if (!newMember.memberIsChild()) {
            final var emailUpdate = new EmailUpdateEntity();
            emailUpdate.emailAddress = newMember.email();
            emailUpdate.change = EmailUpdateEntity.ChangeType.INSERT;
            emailUpdate.persist();
        }

        return ctxt.withMember(member, newMember.memberIsChild());
    }

    /**
     * Registers a new member at Keycloak and returns their initial context.
     * 
     * <p>Checks if a user with the given email already exists in Keycloak:</p>
     * <ul>
     *   <licreate the user in Keycloak by registering a new UserRepresentation, creating at
     *       Keycloak, resetting with a temporary password, and loading the registered user</li>
     *   <li>If user exists: enable the user, check group membership, add to "GUC Members"
     *       group if needed, and return unchanged password indicator</li>
     * </ul>
     * 
     * <p>Throws a {@link WebApplicationException} if a user exists and would create
     * duplicate member records (one user with two SELF roles).</p>
     * 
     * @param dto The onboarding DTO with member email and login name
     * @return OnBoardingContext with Keycloak ID, password, and login information
     *         and optionally attached member entity
     * @throws WebApplicationException if duplicate member creation would occur
     */
    private OnBoardingContext registerUserAtKeycloak(OnBoardingDto dto) {

        final var userResource = this.adminClient.realm("guc").users();
        final var existingUsers = userResource.searchByEmail(dto.email(), true);
        if (existingUsers.isEmpty()) {
            // no user for given email exists. Thus, we create a new one.
            Log.infof("no user for email %s could be found. Creating one.", dto.email());
            final var ur = this.mapDto(dto);
            final var response = userResource.create(ur);
            final var userId = CreatedResponseUtil.getCreatedId(response);
            Log.debugf("user created with id %s", userId);
            final var loadedUser = userResource.get(userId);
            return this.setInitialPassword(loadedUser);
        } else {
            // a user with the given email exists in keycloak.
            Log.infof("User with email %s already exists. Using this one", dto.email());
            final var existingUser = existingUsers.get(0);
            final var resources = ResourcesEntity.resourcesForIdentity(existingUser.getId());
            final var existingUserIsMember = resources.stream().anyMatch(r -> "SELF".equals(r.role));
            if (dto.memberIsChild() || !existingUserIsMember) {
                Log.info("User is not a GUC-Member himself or new member is its child, hence we can continue");
                existingUser.setEnabled(true);
                final var existingUserResource = userResource.get(existingUser.getId());
                final boolean isInMembersGroup = existingUserResource.groups()
                    .stream()
                    .map(GroupRepresentation::getName)
                    .anyMatch(g -> Constants.GROUP_GUC_MEMBERS.equals(g));
                if (!isInMembersGroup) {
                    Log.info("added existing user to GUC Members group");
                    existingUser.getGroups().add(Constants.GROUP_GUC_MEMBERS);
                }
                existingUserResource.update(existingUser);
                return new OnBoardingContext(existingUser.getId(), "UNCHANGED",
                    existingUser.getUsername());
            } else {
                Log.warnf("Cannot create two Members for one user with role SELF. dto.memberIsChild=%b, existingUserIsMember=%b", dto.memberIsChild(), existingUserIsMember);
            }
                    
        }
        throw new WebApplicationException("User with email exists as member", Status.BAD_REQUEST);
    }

    /**
     * Resets a registered Keycloak user's password to a securely generated temporary value.
     * 
     * <p>Generates a 16-character password containing at least one digit, one uppercase
     * letter, one lowercase letter, and one special character from a predefined set.
     * The password is marked as temporary in Keycloak to require a change at first login.</p>
     * 
     * @param ur The Keycloak user resource to reset the password on
     * @return OnBoardingContext with the user's ID, username, and generated temporary password
     */
    private OnBoardingContext setInitialPassword(UserResource ur) {
        final CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setTemporary(true);
        credentials.setType(CredentialRepresentation.PASSWORD);
        final String password = this.generateSecurePassword();
        credentials.setValue(password);
        ur.resetPassword(credentials);
        final var representation = ur.toRepresentation();
        Log.debug("set temporary password for user");
        return new OnBoardingContext(representation.getId(), password, representation.getUsername());
    }

    /**
     * Generates a cryptographically secure temporary password for new Keycloak users.
     * 
     * <p>Repeatedly generates 16-character random strings until one satisfies the
     * required complexity pattern: contains digits, uppercase, lowercase, and special
     * characters. The character set includes printable ASCII and common special symbols.</p>
     * 
     * <p>The returned password is intended to be a temporary password requiring
     * initial change upon first login (configured via temporary flag in Keycloak).</p>
     * 
     * @return A secure temporary password with minimum complexity requirements
     */
    private String generateSecurePassword() {
        String password;
        Matcher matcher;
        do {
            password = RandomStringUtils.random(16,
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!\"§$%&/()?#+*,.;:");
            matcher = passwordPattern.matcher(password);
        } while (!matcher.find());
        return password;
    }

    /**
     * Converts an OnBoardingDto to a Keycloak UserRepresentation for user registration.
     * 
     * <p>Mappings between DTO and Keycloak fields:</p>
     * <ul>
     *   <li><code>firstName</code> → Keycloak first name</li>
     *   <li><code>name</code> → Keycloak last name</li>
     *   <li><code>email</code> → Keycloak email address</li>
     *   <li><code>login</code> → Keycloak username</li>
     *   <li>Groups set to "GUC Members" (case-sensitive)</li>
     * </ul>
     * 
     * <p>The user is enabled and email is marked as verified in Keycloak after mapping.</p>
     * 
     * @param d The onboarding DTO to map to Keycloak representation
     * @return A UserRepresentation ready for Keycloak registration
     */
    private UserRepresentation mapDto(OnBoardingDto d) {
        final UserRepresentation newUser = new UserRepresentation();
        newUser.setEnabled(true);
        newUser.setFirstName(d.firstName());
        newUser.setLastName(d.name());
        newUser.setEmail(d.email());
        newUser.setEmailVerified(true);
        newUser.setUsername(d.login());
        newUser.setGroups(Arrays.asList("GUC Members"));
        Log.info("built UserRepresentation");
        return newUser;
    }
}
