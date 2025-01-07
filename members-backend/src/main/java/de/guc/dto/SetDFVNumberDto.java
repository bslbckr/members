package de.guc.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public class SetDFVNumberDto{

    @NotNull
    private UUID uuid;

    @Min(10000)
    private Integer dfvNumber;

    public void setUuid(UUID value) {
        this.uuid = value;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setDfvNumber(Integer value) {
        this.dfvNumber = value;
    }

    public Integer getDfvNumber() {
        return this.dfvNumber;
    }
}
