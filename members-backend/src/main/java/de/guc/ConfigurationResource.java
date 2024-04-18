package de.guc;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import de.guc.dto.ResourceDto;
import de.guc.entities.ResourcesEntity;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("v1/configuration")
@Authenticated
//@RolesAllowed("guc-members")
public class ConfigurationResource {

    @Inject
    JsonWebToken accessToken;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public  List<ResourceDto> getAccessibleResource() {
        final String subject = this.accessToken.getSubject();
        final var resources = ResourcesEntity.resourcesForIdentity(subject);
        return resources
            .stream()
            .map(res -> new ResourceDto(String.format("%s %s",res.member.givenName, res.member.name),
                                        String.format("%s", res.member.id),
                                        res.role))
            .toList();
    }

}
