package de.guc.dto;

import de.guc.entities.MemberEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;
import java.time.LocalDate;
import java.util.Objects;

public class MemberDto {
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public LocalDate getExitDate() {
        return exitDate;
    }
    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
    }
    public boolean isEmailList() {
        return emailList;
    }
    public void setEmailList(boolean emailList) {
        this.emailList = emailList;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public boolean isDfvDiscount() {
        return dfvDiscount;
    }
    public void setRabatt(boolean rabatt) {
        this.dfvDiscount = rabatt;
    }
    public boolean isDse() {
        return dse;
    }
    public void setDse(boolean dse) {
        this.dse = dse;
    }
    public int getDfvNumber() {
        return dfvNumber;
    }
    public void setDfvNumber(int dfvNumber) {
        this.dfvNumber = dfvNumber;
    }
    public LocalDate getStateEffective() {
        return stateEffective;
    }
    public void setStateEffective(LocalDate stateEffective) {
        this.stateEffective = stateEffective;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public LocalDate getDayOfBirth() {
        return dayOfBirth;
    }
    public void setDayOfBirth(LocalDate dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }
    public LocalDate getEntryDate() {
        return entryDate;
    }
    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }
    public String getGivenName() {
        return givenName;
    }
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    //@NotEmpty
    //@org.hibernate.validator.constraints.UUID(allowNil = false, allowEmpty = false, version =4)
    @NotNull
    private UUID id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String givenName;
    @NotNull
    private LocalDate entryDate;
    @NotNull
    private LocalDate dayOfBirth;
    @NotEmpty
    private String gender;
    @NotEmpty
    @Pattern(regexp = "passiv|ermäßigt|berufstätig|jugendliche", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String state;
    @NotNull
    private LocalDate stateEffective;
    private int dfvNumber;
    @NotNull
    private boolean dse;
    @NotNull
    private boolean dfvDiscount;
    @NotEmpty
    private String street;
    @NotEmpty
    private String zipCode;
    @NotEmpty
    private String city;
    @NotEmpty
    private String mobile;
    @NotEmpty
    @Email
    private String email;
    @NotNull
    private boolean emailList;
    private LocalDate exitDate;

    public static MemberDto fromEntity(MemberEntity entity) {
        Objects.requireNonNull(entity);
        final MemberDto result = new MemberDto();
        result.id = entity.id;
        result.name = entity.name;
        result.givenName = entity.givenName;
        result.dayOfBirth = entity.dayOfBirth;
        result.entryDate = entity.entryDate;
        result.exitDate = entity.exitDate;
        result.gender = entity.gender;
        result.state = entity.state;
        result.stateEffective = entity.stateEffective;
        result.dfvNumber = entity.dfvNumber;
        result.dse = entity.dse;
        result.dfvDiscount = entity.rabatt;
        result.street = entity.street;
        result.city = entity.city;
        result.zipCode = entity.zipCode;
        result.email = entity.email;
        result.emailList = entity.emailList;
        result.mobile = entity.mobile;
        return result;
    }
}
