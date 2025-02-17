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
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.guc.dto.MemberWithLoginDto;
import de.guc.dto.OnBoardingDto;
import de.guc.entities.EmailUpdateEntity;
import de.guc.entities.MemberEntity;
import de.guc.entities.ResourcesEntity;
import de.guc.entities.ResourcesEntity.ResourceType;
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

@Path("v1/onboarding")
@RolesAllowed("onboarding")
public class OnBoardingResource {

        private final static Logger LOGGER = LoggerFactory.getLogger(OnBoardingResource.class);
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

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @Transactional
        public void OnBoardNewMember(@Valid OnBoardingDto newMember) {
                var context = this.registerUserAtKeycloak(newMember);

                context = this.storeNewMember(newMember, context);
                this.storeAccessResource(context);

                final var mailTemplate = new welcome(newMember.firstName(), newMember.login(), context.password());

                final var mail = Mail.withText(newMember.email(),
                                "Herzlich Willkommen beim Goldfingers Ultimate Club e.V.",
                                mailTemplate.render());
                this.mailer.send(mail);

        }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("import")
    public void ImportExistingMembers(@Valid @NotEmpty List<MemberWithLoginDto> importedMembers) {
        importedMembers.stream()
            .map(mwl -> {
                    final boolean isChild = mwl.isChild();
                    final OnBoardingDto dto = new OnBoardingDto(isChild ? mwl.getParentFirstName() : mwl.getGivenName(),
                                                                isChild ? mwl.getParentLastName() : mwl.getName(),
                                                                mwl.getEmail(),
                                                                mwl.getLogin(), isChild, "", "");
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
                    final var templ = new imported(ctxt.member.givenName, ctxt.login, ctxt.password);
                    return Mail.withText(ctxt.member.email, "GUC Mitgliederverwaltung - Neues Konto angelegt", templ.render());
                })
            .forEach(mail -> this.mailer.send(mail));
    }

        private void storeAccessResource(OnBoardingContext ctxt) {
                final var access = new ResourcesEntity();
                access.identity = UUID.fromString(ctxt.keycloakId());
                access.member = ctxt.member();
                access.role = ctxt.memberIsChild() ? "PARENT" : "SELF";
                access.rType = ResourceType.MEMBER;
                access.name = ctxt.member().givenName;
                access.persist();

        }

        private OnBoardingContext storeNewMember(OnBoardingDto newMember, OnBoardingContext ctxt) {
                final var member = new MemberEntity();

                if (newMember.memberIsChild()) {
                        member.name = newMember.memberName();
                        member.givenName = newMember.memberFirstName();
                } else {
                        member.name = newMember.name();
                        member.givenName = newMember.firstName();
                }
                member.email = newMember.email();
                member.entryDate = LocalDate.now();
                member.persist();

                if (!newMember.memberIsChild()) {
                        final var emailUpdate = new EmailUpdateEntity();
                        emailUpdate.emailAddress = newMember.email();
                        emailUpdate.change = EmailUpdateEntity.ChangeType.INSERT;
                        emailUpdate.persist();
                }

                return ctxt.withMember(member, newMember.memberIsChild());
        }

        private OnBoardingContext registerUserAtKeycloak(OnBoardingDto dto) {

                final var userResource = this.adminClient.realm("guc").users();
                final var existingUsers = userResource.searchByEmail(dto.email(), true);
                if (existingUsers.isEmpty()){
                    // no user for given email exists. Thus, we create a new one.
                    final var ur = this.mapDto(dto);
                    final var response = userResource.create(ur);
                    final var userId = CreatedResponseUtil.getCreatedId(response);
                    final var loadedUser = userResource.get(userId);
                    return this.setInitialPassword(loadedUser);
                } else {
                    // a user with the given email exists in keycloak.
                    final var existingUser = existingUsers.get(0);
                    final var resources = ResourcesEntity.resourcesForIdentity(existingUser.getId());
                    final var existingUserIsMember = resources.stream().anyMatch(r -> "SELF".equals(r.role));
                    if (dto.memberIsChild() || !existingUserIsMember) {
                        existingUser.setEnabled(true);
                        if (!existingUser.getGroups().contains("GUC Members")) {
                            existingUser.getGroups().add("GUC Members");
                        }
                        return new OnBoardingContext(existingUser.getId(), "UNCHANGED", existingUser.getUsername());
                    }
                }
                throw new WebApplicationException("User with email exists as member", Status.BAD_REQUEST);
        }

        private OnBoardingContext setInitialPassword(UserResource ur) {
                final CredentialRepresentation credentials = new CredentialRepresentation();
                credentials.setTemporary(true);
                credentials.setType(CredentialRepresentation.PASSWORD);
                final String password = this.generateSecurePassword();
                credentials.setValue(password);
                ur.resetPassword(credentials);
                final var representation = ur.toRepresentation();
                return new OnBoardingContext(representation.getId(), password, representation.getUsername());
        }

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

        private UserRepresentation mapDto(OnBoardingDto d) {
                final UserRepresentation newUser = new UserRepresentation();
                newUser.setEnabled(true);
                newUser.setFirstName(d.firstName());
                newUser.setLastName(d.name());
                newUser.setEmail(d.email());
                newUser.setEmailVerified(false);
                newUser.setUsername(d.login());
                newUser.setGroups(Arrays.asList("GUC Members"));
                LOGGER.info("built UserRepresentation");
                return newUser;
        }
}
