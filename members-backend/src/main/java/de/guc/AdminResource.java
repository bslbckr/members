package de.guc;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.guc.dto.CancelMembershipDto;
import de.guc.dto.SetDFVNumberDto;
import de.guc.entities.MemberEntity;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("v1/admin")
@RolesAllowed("onboarding")
public class AdminResource {

    @POST
    @Path("updateDfv")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateDfvNumbers(@Valid List<SetDFVNumberDto> updatedNumbers) {
        final var overall = updatedNumbers.stream().map(this::updateMemberDfv)
            .reduce(true, this::accumulate);
        Response result;
        if(overall) {
            result =  Response.ok().build();
        } else {
            result = Response.serverError().build();
        }
        return result;
    }

    private Boolean accumulate(Boolean acc, Boolean cur) {
        return !cur ? Boolean.FALSE : acc;
    }
    
    private Boolean updateMemberDfv(SetDFVNumberDto dto) {
        boolean result = false;
        final var member = MemberEntity.byId(dto.getUuid());
        if (member.isPresent()) {
            final var entity = member.get();
            entity.dfvNumber = dto.getDfvNumber();
            entity.persist();
            result = true;
            Log.infof("Set dfvNumber of member %s to %d", dto.getUuid(), dto.getDfvNumber());
        } else {
            Log.warnf("Member with id %s could not be found", dto.getUuid());
        }
            
        return result;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("cancelMembership")
    @Transactional
    public void cancelMemberships(@Valid CancelMembershipDto cancelled) {
        final var cancelledEntities = cancelled.ids().stream()
            .<Optional<MemberEntity>>map(MemberEntity::byId)
            .filter(Optional::isPresent)
            .<MemberEntity>map(Optional::get)
            .peek(e -> e.exitDate = cancelled.cancellationDate())
            .peek(this::sendNotificationMails)
            ;
        MemberEntity.persist(cancelledEntities);
    }
    @ConfigProperty(name = "guc.member.mail.state-changed")
    String toAddress;

    @Inject Mailer mailer;

    public static TemplateInstance membershipCancellation(MemberEntity entity) {
        return new MemberResource.StateChangeOrCancellation(entity.givenName, entity.name, "ignored", "ignored", entity.exitDate.toString());
    }

    private void sendNotificationMails(MemberEntity entity) {
        final TemplateInstance boardTemplate  = membershipCancellation(entity);
        final String boardMailSubject = String.format("[GUC/Mitglieder] Kündigung %s %s", entity.givenName, entity.name);
        final Mail boardMail = Mail.withText(this.toAddress, boardMailSubject, boardTemplate.render());

        this.mailer.send(boardMail);
    }
}
