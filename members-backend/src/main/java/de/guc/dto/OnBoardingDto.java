package de.guc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

@ValidOnBoarding
public record OnBoardingDto(@NotEmpty String firstName,
                @NotEmpty String name,
                @NotEmpty @Email String email,
                @NotEmpty String login,
                boolean memberIsChild,
                String memberName,
                String memberFirstName) {

    public String consolidatedName() {
        if (this.memberIsChild) {
            return this.memberName;
        }
        return this.name;
    }

    public String consolidatedFirstName() {
        if (this.memberIsChild) {
            return this.memberFirstName;
        }
        return this.firstName;
    }
}
