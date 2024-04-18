package de.guc.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Parameters;

@Entity(name = "resource_access")
public class ResourcesEntity extends PanacheEntity {
    public static enum ResourceType {
        MEMBER,
        URL
    }
    @ManyToOne
    @JoinColumn(name="member")
    public MemberEntity member;
    public UUID identity;
    public String role;
    public String name;
    @Column(name="type")
    public ResourceType rType;

    public static List<ResourcesEntity> resourcesForIdentity(final String identity) {
        final var uuid = UUID.fromString(identity);
        return ResourcesEntity.list("identity", uuid);
    }

    public static Optional<ResourcesEntity> hasAccess(final UUID identity, final UUID resource) {
        final Map<String, UUID> parameters = new HashMap<>();
        parameters.put("identity", identity);
        parameters.put("resource", resource);
        final var paramMap = Parameters.with("identity", identity).and("resource", resource).map();
        final Optional<ResourcesEntity> resEntity = ResourcesEntity.find("identity = :identity and member.id = :resource",paramMap)
            .singleResultOptional();
        return resEntity;
    }
}
