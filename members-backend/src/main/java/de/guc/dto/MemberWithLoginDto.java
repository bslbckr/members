package de.guc.dto;

public class MemberWithLoginDto extends MemberDto {
    private String login;
    private String parentFirstName;
    private String parentLastName;
    
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getParentFirstName() {
        return this.parentFirstName;
    }

    public void setParentFirstName(String firstName) {
        this.parentFirstName = firstName;
    }

    public String getParentLastName(){
        return this.parentLastName;
    }
    public void setParentLastName(String lastName) {
        this.parentLastName = lastName;
    }

    public boolean isChild() {
        return (this.parentFirstName != null && !this.parentFirstName.isEmpty())
            || (this.parentLastName != null && !this.parentLastName.isEmpty());
    }
}
