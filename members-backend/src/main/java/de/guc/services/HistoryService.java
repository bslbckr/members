package de.guc.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import de.guc.entities.EntryOrCancellation;

public interface HistoryService {
    public static record StateChange(UUID id, String givenName, String name, String oldState, String newState, LocalDate stateEffective, LocalDateTime changedAt){}

    public Collection<StateChange> stateChanges();

    public List<EntryOrCancellation> cancellations(LocalDate after);
}
