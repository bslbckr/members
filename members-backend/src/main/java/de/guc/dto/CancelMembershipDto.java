package de.guc.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CancelMembershipDto(@NotNull LocalDate cancellationDate, @NotEmpty List<UUID> ids){}
