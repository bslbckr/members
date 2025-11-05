package de.guc;

import java.util.stream.Collectors;

import de.guc.dto.EmailDto;
import de.guc.entities.MemberEntity;
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
        final var emails = MemberEntity.activeMemberEmails();
        final var mails = emails.stream()
            .map(receipient -> Mail.withText(receipient,
                                             email.getSubject(),
                                             email.getBody()))
            .toArray(Mail[]::new);
        this.mailer.send(mails);
    }
}
