package de.guc.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;

import de.guc.entities.MemberEntity;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;

@RequestScoped
public class HistoryServiceImpl implements HistoryService {

    private static record EnversResult<T>(T entity, DefaultRevisionEntity revision, RevisionType revType){
        public static <T> EnversResult<T> fromEnvers(Object result, Class<T> cls) {
            final Object[] a = (Object[]) result;
            return new EnversResult<T>((T) a[0], (DefaultRevisionEntity) a[1], (RevisionType) a[2]);
        }
    }

    private static EnversResult<MemberEntity> toResult(Object o) {
        return EnversResult.fromEnvers(o, MemberEntity.class);
    }
    
    private final EntityManager entityManager; 

    public HistoryServiceImpl(EntityManager em) {
        this.entityManager = Objects.requireNonNull(em);
    }
    
    public Collection<StateChange> stateChanges() {
        Log.debug("searching state-changes");
        final AuditReader ar = AuditReaderFactory.get(this.entityManager);
        final var resultList = (List<Object>) ar.createQuery()
            .forRevisionsOfEntity(MemberEntity.class, false, true)
            .add(AuditEntity.property("state").hasChanged())
            .getResultList();
        Log.info(String.format("found %d change entries", resultList.size()));
       final var convertedResult = resultList.stream()
            
           .map(HistoryServiceImpl::toResult)
            .toList();
        
        final var changesPerUser = convertedResult.stream().collect(Collectors.groupingBy(er -> er.entity.id));
        List<StateChange> result = new ArrayList<>();
        for (var entry : changesPerUser.entrySet()) {
            final var value = entry.getValue();
            if (value.size() > 1) {
                value.sort(HistoryServiceImpl::compareAuditResults);
                for (int i = value.size() - 1; i > 0; i--) {
                    final var newEntity = value.get(i).entity();
                    final var oldEntity = value.get(i - 1).entity();
                    final long changedAt = value.get(i).revision().getTimestamp();
                    final StateChange change = new StateChange(newEntity.id,
                                                               newEntity.givenName,
                                                               newEntity.name,
                                                               oldEntity.state,
                                                               newEntity.state,
                                                               newEntity.stateEffective,
                                                               LocalDateTime.ofInstant(Instant.ofEpochMilli(changedAt), ZoneOffset.UTC) );
                    result.add(change);
                }
            }
        }
        Log.info(String.format("returning %d state changes", result.size()));
        return result;
    }

    private static int compareAuditResults(EnversResult<MemberEntity> a, EnversResult<MemberEntity> b) {
        final long timestampA = a.revision().getTimestamp();
        final long timestampB = b.revision().getTimestamp();
        return Long.compare(timestampA, timestampB);
    }
}
