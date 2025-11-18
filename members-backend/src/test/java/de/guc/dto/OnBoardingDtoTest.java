package de.guc.dto;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OnBoardingDtoTest {

        private static final OnBoardingDto onBoardingChild = new OnBoardingDto("a", "b", "c", "d", true, "e", "f");
        private static final OnBoardingDto onBoardingAdult = new OnBoardingDto("a", "b", "c", "d", false, null, null);

    static List<Arguments> memberFirstNameSource = Arrays.asList(
                        arguments(onBoardingAdult, "a"),
                        arguments(onBoardingChild, "f"));


    static List<Arguments> memberNameSource = Arrays.asList(
                        arguments(onBoardingAdult, "b"),
                        arguments(onBoardingChild, "e"));

        @ParameterizedTest
        @FieldSource("memberNameSource")
        public void testConsolidatedName(OnBoardingDto dto, String expected) {
                assertEquals(dto.consolidatedName(), expected);
        }

        @ParameterizedTest
        @FieldSource("memberFirstNameSource")
        public void testConsolidatedFirstName(OnBoardingDto dto, String expected) {
                assertEquals(dto.consolidatedFirstName(), expected);
        }

}
