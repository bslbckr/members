package de.guc.dto;

import java.time.LocalDateTime;

public record HistoryEntryDto(LocalDateTime date, String user){
}
