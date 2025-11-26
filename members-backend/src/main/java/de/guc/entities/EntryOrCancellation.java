package de.guc.entities;

import java.time.LocalDate;

public record EntryOrCancellation(String name, String givenName, LocalDate date) {}
