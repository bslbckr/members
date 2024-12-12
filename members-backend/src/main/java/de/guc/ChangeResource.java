package de.guc;

import java.util.Collection;
import java.util.Objects;

import de.guc.dto.StateChangeDto;
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
    @RolesAllowed("board-member")
    @Transactional
    public Collection<StateChangeDto> getStateChanges() {
        final var changes = this.service.stateChanges();
        return changes.stream().map(StateChangeDto::fromStateChange).toList();
    }
}
