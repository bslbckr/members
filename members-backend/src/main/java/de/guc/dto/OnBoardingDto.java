package de.guc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

@ValidOnBoarding
public record OnBoardingDto(@NotEmpty String  firstName,
                            @NotEmpty String name,
                            @NotEmpty @Email String email,
                            @NotEmpty String login,
                            boolean memberIsChild,
                            String memberName,
                            String memberFirstName) {
}
