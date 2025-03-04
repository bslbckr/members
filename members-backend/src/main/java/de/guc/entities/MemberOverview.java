package de.guc.entities;

import java.time.LocalDate;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record MemberOverview (String givenName,
                              String name,
                              String state,
                              LocalDate stateEffective,
                              LocalDate entryDate,
                              LocalDate exitDate) {}
