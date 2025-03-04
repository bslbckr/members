package de.guc.entities;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.envers.Audited;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;

@Entity
@Table(name="members")
@Audited(withModifiedFlag = true)
public class MemberEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;
    public String name;
    @Column(name="given_name")
    public String givenName;
    @Column(name="entry_date")
    public LocalDate entryDate;
    @Column(name="day_of_birth")
    public LocalDate dayOfBirth;
    public String gender;
    public String state;
    @Column(name="state_effective")
    public LocalDate stateEffective;
    @Column(name="dfv_number")
    public int dfvNumber;
    public boolean dse;
    public boolean rabatt;
    public String street;
    @Column(name="zip_code")
    public String zipCode;
    public String city;
    public String mobile;
    public String email;
    @Column(name="email_list")
    public boolean emailList;
    @Column(name="exit_date")
    public LocalDate exitDate;


    public List<MemberEntity> all() {
        return listAll();
    }

    public static Optional<MemberEntity> byId(UUID id) {
        return byId(id, false);
    }

    public static Optional<MemberEntity> byId(UUID id, boolean locked) {
        final LockModeType lmt = locked ? LockModeType.PESSIMISTIC_WRITE : LockModeType.NONE;
        return findByIdOptional(id, lmt); 
    }

    public static List<MemberOverview> activeMembers() {
        return find("select m.name, m.givenName, m.state, m.stateEffective, m.entryDate, m.exitDate from MemberEntity m where m.exitDate is null or m.exitDate >= ?1", LocalDate.now())
            .project(MemberOverview.class).list();
    }
}
