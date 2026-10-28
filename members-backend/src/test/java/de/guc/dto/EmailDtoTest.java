package de.guc.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailDto class
 */
public class EmailDtoTest {

    private EmailDto emailDto;

    @BeforeEach
    public void setUp() {
        emailDto = new EmailDto();
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(emailDto);
        assertNull(emailDto.getSubject());
        assertNull(emailDto.getBody());
    }

    @Test
    public void testGettersAndSetters() {
        String testSubject = "Test Subject";
        String testBody = "Test Body Content";

        emailDto.setSubject(testSubject);
        assertEquals(testSubject, emailDto.getSubject());

        emailDto.setBody(testBody);
        assertEquals(testBody, emailDto.getBody());
    }

    @Test
    public void testEmptyStrings() {
        emailDto.setSubject("");
        assertEquals("", emailDto.getSubject());

        emailDto.setBody("");
        assertEquals("", emailDto.getBody());
    }

    @Test
    public void testNullValues() {
        emailDto.setSubject(null);
        assertNull(emailDto.getSubject());

        emailDto.setBody(null);
        assertNull(emailDto.getBody());
    }

    @Test
    public void testSpecialCharacters() {
        String specialSubject = "Test with \"quotes\" and \n newlines";
        String specialBody = "Body with \t tabs and \u00e4 umlauts";

        emailDto.setSubject(specialSubject);
        assertEquals(specialSubject, emailDto.getSubject());

        emailDto.setBody(specialBody);
        assertEquals(specialBody, emailDto.getBody());
    }

    @Test
    public void testLongContent() {
        StringBuilder longSubject = new StringBuilder();
        StringBuilder longBody = new StringBuilder();
        
        for (int i = 0; i < 1000; i++) {
            longSubject.append("a");
            longBody.append("b");
        }

        emailDto.setSubject(longSubject.toString());
        assertEquals(1000, emailDto.getSubject().length());

        emailDto.setBody(longBody.toString());
        assertEquals(1000, emailDto.getBody().length());
    }

    @Test
    public void testToString() {
        emailDto.setSubject("Test Subject");
        emailDto.setBody("Test Body");
        
        String result = emailDto.toString();
        assertNotNull(result);
        assertTrue(result.contains("EmailDto"));
    }
}
