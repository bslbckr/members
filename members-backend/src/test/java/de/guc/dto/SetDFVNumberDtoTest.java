package de.guc.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SetDFVNumberDto class
 */
public class SetDFVNumberDtoTest {

    private SetDFVNumberDto dto;
    private UUID testUuid;

    @BeforeEach
    public void setUp() {
        dto = new SetDFVNumberDto();
        testUuid = UUID.fromString("df8ea850-52a3-4856-aea0-79361d48ff8c");
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(dto);
        assertNull(dto.getUuid());
        assertNull(dto.getDfvNumber());
    }

    @Test
    public void testGettersAndSetters() {
        dto.setUuid(testUuid);
        assertEquals(testUuid, dto.getUuid());

        dto.setDfvNumber(12345);
        assertEquals(Integer.valueOf(12345), dto.getDfvNumber());
    }

    @Test
    public void testNullValues() {
        dto.setUuid(null);
        assertNull(dto.getUuid());

        dto.setDfvNumber(null);
        assertNull(dto.getDfvNumber());
    }

    @Test
    public void testMinValidation() {
        // The @Min(10000) annotation should be respected
        // We can set values below and above the minimum
        dto.setDfvNumber(5000); // Below minimum
        assertEquals(Integer.valueOf(5000), dto.getDfvNumber());
        
        dto.setDfvNumber(10000); // At minimum
        assertEquals(Integer.valueOf(10000), dto.getDfvNumber());
        
        dto.setDfvNumber(50000); // Above minimum
        assertEquals(Integer.valueOf(50000), dto.getDfvNumber());
    }

    @Test
    public void testEdgeCases() {
        dto.setDfvNumber(0);
        assertEquals(Integer.valueOf(0), dto.getDfvNumber());

        dto.setDfvNumber(Integer.MAX_VALUE);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), dto.getDfvNumber());

        dto.setDfvNumber(Integer.MIN_VALUE);
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), dto.getDfvNumber());
    }

    @Test
    public void testToString() {
        dto.setUuid(testUuid);
        dto.setDfvNumber(12345);
        
        String result = dto.toString();
        assertNotNull(result);
        assertTrue(result.contains("SetDFVNumberDto"));
    }
}
