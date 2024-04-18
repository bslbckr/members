package de.guc.entities;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_update")
public class EmailUpdateEntity extends PanacheEntity {
    public static enum ChangeType {
        INSERT,
        REMOVE
    }

    @Column(name = "email_address")
    public String emailAddress;
    @Column(name="\"change\"")
    public ChangeType change;
    @Column(name="inserted_at")
    public LocalDateTime insertedAt = LocalDateTime.now(ZoneOffset.UTC);
}
