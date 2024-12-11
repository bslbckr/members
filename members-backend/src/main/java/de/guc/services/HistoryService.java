package de.guc.services;

import java.util.UUID;
import java.util.Collection;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface HistoryService {
    public static record StateChange(UUID id, String givenName, String name, String oldState, String newState, LocalDate stateEffective, LocalDateTime changedAt){}

    public Collection<StateChange> stateChanges();
}
