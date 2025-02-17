package de.guc.entities;

import java.time.LocalDate;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class MemberOverview {
    public String givenName;
    public String name;
    public String state;
    public LocalDate stateEffective;
    public LocalDate entryDate;
    public LocalDate exitDate;
}
