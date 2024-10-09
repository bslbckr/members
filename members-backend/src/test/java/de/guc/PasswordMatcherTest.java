package de.guc;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PasswordMatcherTest {
    private Pattern passwordPattern = //Pattern.compile("(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=[,\\.;:!\"#@\\+\\-\\?])");
        Pattern.compile("(?=.*\\d)(?=.*[A-Z])");

    static Stream<Arguments> testPatternInput() {
        return Stream.of(
                         arguments("aaaaaaaa", false),
                         arguments("aaaaaaaA", false),
                         arguments("aaaaaaA8", true),
                         arguments("aaaaaA8.",true),
                         arguments("BBBBB8a", true),
                         arguments("7Ba", true)
        );
    }
    
    @ParameterizedTest
    @MethodSource("testPatternInput")
    public void testPattern(String input, boolean match) {
        final var matcher = passwordPattern.matcher(input);
        assertEquals(match, matcher.find());
    }
}
