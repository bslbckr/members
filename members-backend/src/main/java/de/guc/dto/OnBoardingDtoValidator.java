package de.guc.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OnBoardingDtoValidator implements ConstraintValidator<ValidOnBoarding, OnBoardingDto> {

    @Override
    public boolean isValid(OnBoardingDto value, ConstraintValidatorContext context) {
        if (value != null) {
            if (value.memberIsChild()) {
                if (value.memberName() == null || value.memberName().isEmpty())
                    return false;
                if (value.memberFirstName() == null || value.memberFirstName().isEmpty())
                    return false;
                return true;
           } else {
                return true;
            }
        }
        return false;
    }
}

    
