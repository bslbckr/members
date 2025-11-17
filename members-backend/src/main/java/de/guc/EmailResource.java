package de.guc;

import de.guc.dto.EmailDto;
import de.guc.entities.MemberEntity;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Path("v1/email")
@Authenticated
public class EmailResource {

    @Inject Mailer mailer;
    
    @POST
    @RolesAllowed("board-member")
    @Consumes(MediaType.APPLICATION_JSON)
    public void send(@Valid EmailDto email) {
        Log.infof("Start sending mail with subjet: %s", email.getSubject());
        final var emails = MemberEntity.activeMemberEmails();
        Log.infof("Sending mail to %d receipients", emails.size());
        final var mails = emails.stream()
            .map(receipient -> Mail.withText(receipient,
                                             email.getSubject(),
                                             email.getBody()))
            .toArray(Mail[]::new);
        Log.debug("Built all mails. Start sending them");
        this.mailer.send(mails);
        Log.debug("Sending all mails completed.");
    }
}
