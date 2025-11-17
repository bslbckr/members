package de.guc.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class OnBoardingDtoValidatorTest {

    private final OnBoardingDtoValidator validator = new OnBoardingDtoValidator();

    @Test
    public void testAdultValidatedCorrectly() {
        final var dto = new OnBoardingDto("a","b","c","d",false,null,null);
        assertTrue(validator.isValid(dto, null));
    }

    @Test
    public void testChildWithoutMemberNameIsInvalid() {
        final var dto = new OnBoardingDto("a","b","c","d",true,null,null);
        assertFalse(validator.isValid(dto, null));
    }

    @Test
    public void testChildWithoutMemberFirstNameIsInvalid() {
        final var dto = new OnBoardingDto("a","b","c","d",true,"foo",null);
        assertFalse(validator.isValid(dto, null));
    }

    @Test
    public void testChildWithEmptyMemberFirstNameIsInvalid() {
        final var dto = new OnBoardingDto("a","b","c","d",true,"foo","");
        assertFalse(validator.isValid(dto, null));
    }

    @Test
    public void testChildWithEmptyMemberNameIsInvalid() {
        final var dto = new OnBoardingDto("a","b","c","d",true,"","");
        assertFalse(validator.isValid(dto, null));
    }

    @Test
    public void testNullValueIsInvalid() {
        assertFalse(this.validator.isValid(null,null));
    }

    @Test
    public void testCompletelyFilledChildIsValid() {
        final var dto = new OnBoardingDto("a","b","c","d",true,"foo","bar");
        assertTrue(validator.isValid(dto, null));

    }
}
