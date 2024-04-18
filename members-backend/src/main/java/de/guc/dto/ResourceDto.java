package de.guc.dto;

public class ResourceDto {
    private String name;
    private String memberId;
    private String owner;

    public ResourceDto(String n, String u, String o) {
        this.name = n;
        this.memberId = u;
        this.owner = o;
    }

    public String getName() {
        return this.name;
    }
    public String getMemberId(){
        return this.memberId;
    }
    public String getOwner(){
        return this.owner;
    }
}
