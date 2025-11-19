package de.guc;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

import de.guc.dto.StateChangeDto;
import de.guc.entities.EntryOrCancellation;
import de.guc.entities.MemberEntity;
import de.guc.services.HistoryService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("v1/changes")
@Authenticated
public class ChangeResource {

    private final HistoryService service;

    public ChangeResource(HistoryService histService) {
        this.service = Objects.requireNonNull(histService);
    }

    @GET
    @Path("state")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Constants.ROLE_BOARD_MEMBER)
    @Transactional
    public Collection<StateChangeDto> getStateChanges() {
        final var changes = this.service.stateChanges();
        return changes.stream().map(StateChangeDto::fromStateChange).toList();
    }

    @GET
    @Path("cancellation")
    @RolesAllowed(Constants.ROLE_BOARD_MEMBER)
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<EntryOrCancellation> getCancellations() {
        return this.service.cancellations(LocalDate.now().minusMonths(18));
    }

    @GET
    @Path("entries")
    @RolesAllowed(Constants.ROLE_BOARD_MEMBER)
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<EntryOrCancellation> getEntries() {
        return MemberEntity.entriesSinceDate(LocalDate.now().minusMonths(18));
    }
}
