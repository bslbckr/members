package de.guc.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.ConstraintTarget;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = OnBoardingDtoValidator.class)
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOnBoarding {
    //ConstraintTarget validationAppliesTo() default ConstraintTarget.IMPLICIT;
    String message() default "If the new member is a 'child' the fields memberFirstName and memberName must not be empty";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
