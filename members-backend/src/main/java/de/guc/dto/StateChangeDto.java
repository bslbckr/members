package de.guc.dto;

import java.time.format.DateTimeFormatter;

import de.guc.services.HistoryService.StateChange;

public record StateChangeDto(String id, String givenName, String name, String oldState, String newState, String stateEffective, String changedAt) {
    public static StateChangeDto fromStateChange(StateChange sc) {
        return new StateChangeDto(sc.id().toString(), sc.givenName(), sc.name(), sc.oldState(), sc.newState(), sc.stateEffective().toString(), sc.changedAt().format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
