package de.guc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.RestPath;

import de.guc.dto.MemberDto;
import de.guc.entities.EmailUpdateEntity;
import de.guc.entities.MemberEntity;
import de.guc.entities.MemberOverview;
import de.guc.entities.ResourcesEntity;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("v1/member")
@Authenticated
public class MemberResource {
    /**
     * StateChanged instance for a change of member state
     */
    private static TemplateInstance changedState(String givenName, String name, String oldState, String newState, String dateEffective) {
        return new StateChangeOrCancellation(givenName, name, oldState, newState, dateEffective);
    }
    
    /**
     * StateChanged instance for a cancellation
     */
    private static TemplateInstance cancellation(String givenName, String name, String dateEffective) {
        return new StateChangeOrCancellation(givenName, name, "ignored", "ignored", dateEffective);
        }
        
    @Inject
    JsonWebToken accessToken;
    @Inject ObjectMapper mapper;

    @Inject Mailer mailer;
    
    record StateChangeOrCancellation(String givenName,
                        String name,
                        String oldState,
                        String newState,
                        String dateEffective) implements TemplateInstance {};

    record NewMemberRegistered(String givenName, String name, String newState, String dateEffective, String email) implements TemplateInstance {};
    
    @ConfigProperty(name = "guc.member.mail.state-changed")
    String toAddress;
    
    @GET
    @Path("{id}")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public MemberDto getMemberById(@RestPath("id") String memberId) {
        checkAuthorization(UUID.fromString(memberId), true);
        final var member = MemberEntity.byId(UUID.fromString(memberId));
        final var dto = MemberDto.fromEntity(member.orElseThrow(() -> new NotFoundException()));
        return dto;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Constants.ROLE_BOARD_MEMBER)
    public List<MemberOverview> getAllActiveMembers() {
        return MemberEntity.activeMembers();
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("{id}")
    public MemberDto updateMember(@RestPath("id") String memberId, @Valid MemberDto member) {
        if (!UUID.fromString(memberId).equals(member.getId())) {
            Log.warnf("Attempt to store a member-object with id %s under id %s", member.getId(), memberId);
            throw new BadRequestException();
        }
        final var existingMember = MemberEntity.byId(UUID.fromString(memberId));
        if (existingMember.isEmpty()) {
            Log.warnf("Did not find member with id %s. Returning 404", memberId);
            throw new NotFoundException();
        }
        this.checkAuthorization(UUID.fromString(memberId), false);
        final var entityToUpdate = existingMember.get();
        this.checkEmailUpdate(entityToUpdate, member);
        this.checkStateUpdate(entityToUpdate, member);
        this.updateMember(entityToUpdate, member);
        entityToUpdate.persistAndFlush();
        return MemberDto.fromEntity(entityToUpdate);
    }

    private void checkAuthorization(final UUID resourceId, final boolean  read) {
        final String subject = this.accessToken.getSubject();
        ResourcesEntity.hasAccess(UUID.fromString(subject), resourceId).orElseThrow(() -> new UnauthorizedException());
    }

    private void checkEmailUpdate(@Nonnull MemberEntity member, @Nonnull MemberDto dto) {
        final var updates = new ArrayList<EmailUpdateEntity>();
         
       if (!member.email.equals(dto.getEmail())) {
            // email differs
           if (member.emailList) {
               final var removeOldAddress = new EmailUpdateEntity();
               removeOldAddress.emailAddress = member.email;
               removeOldAddress.change = EmailUpdateEntity.ChangeType.REMOVE;
               updates.add(removeOldAddress);
           }
           if(dto.isEmailList()){
               final var insertNewAddress = new EmailUpdateEntity();
               insertNewAddress.emailAddress = dto.getEmail();
               insertNewAddress.change = EmailUpdateEntity.ChangeType.INSERT;
               updates.add(insertNewAddress);
           }
       } else if (member.emailList != dto.isEmailList()) {
           if(member.emailList) {
               final var removeOldAddress = new EmailUpdateEntity();
               removeOldAddress.emailAddress = member.email;
               removeOldAddress.change = EmailUpdateEntity.ChangeType.REMOVE;
               updates.add(removeOldAddress);
           } else {
               final var insertNewAddress = new EmailUpdateEntity();
               insertNewAddress.emailAddress = dto.getEmail();
               insertNewAddress.change = EmailUpdateEntity.ChangeType.INSERT;
               updates.add(insertNewAddress);
           } 
       }

       updates.stream().forEach(e -> e.persist());
    }

    private void checkStateUpdate(@Nonnull MemberEntity member, @Nonnull MemberDto dto) {
        TemplateInstance mailBody;
        String subject;

        if (member.state == null) {
             mailBody = new NewMemberRegistered(dto.getGivenName(), dto.getName(), dto.getState(), dto.getEntryDate().toString(), dto.getEmail());
             subject = String.format("[GUC/Mitglieder] Neues Mitglied %s %s", dto.getGivenName(), dto.getName());
        }
        else if (!member.state.equals(dto.getState()) || (member.exitDate == null && dto.getExitDate() != null)) {
            if (dto.getExitDate() == null) {
                Log.infof("Member %1$s changed state from %2$s to %3$s", member.id, member.state, dto.getState());
                mailBody = changedState(member.givenName, member.name, member.state, dto.getState(), dto.getStateEffective().toString());
                subject = String.format("[GUC/Mitglieder] Statuswechsel %s %s", member.givenName, member.name);
            } else {
                Log.infof("Member %1$s cancelled its membership", member.id, member.state, dto.getState());
                mailBody = cancellation(member.givenName, member.name, dto.getExitDate().toString());
                subject = String.format("[GUC/Mitglieder] Kündigung %s %s", member.givenName, member.name);
            }
        } else {
            return;
        }
        final var mail = Mail.withText(this.toAddress, subject, mailBody.render());
        this.mailer.send(mail);
    }

    private void updateMember(@Nonnull MemberEntity member, @Nonnull MemberDto dto) {
        // we don't update id, dfvNumber, and entryDate for a reason.
        member.city = dto.getCity();
        member.dayOfBirth = dto.getDayOfBirth();
        member.dse = dto.isDse();
        member.email = dto.getEmail();
        member.emailList = dto.isEmailList();
        if (member.exitDate == null && dto.getExitDate() != null) {
            member.exitDate = dto.getExitDate();
        }
        member.gender = dto.getGender();
        member.givenName = dto.getGivenName();
        member.mobile = dto.getMobile();
        member.name = dto.getName();
        member.rabatt = dto.isDfvDiscount();
        member.street = dto.getStreet();
        if (!dto.getState().equals(member.state)) {
            // only update stateEffective, iff the state has changed.
            member.stateEffective = dto.getStateEffective();
        }
        member.state = dto.getState();
        member.zipCode = dto.getZipCode();
    }
}
