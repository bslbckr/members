package de.guc.dto;

import de.guc.services.HistoryService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StateChangeDto record class
 */
public class StateChangeDtoTest {

    @Test
    public void testRecordCreation() {
        String id = "df8ea850-52a3-4856-aea0-79361d48ff8c";
        String givenName = "John";
        String name = "Doe";
        String oldState = "aktiv";
        String newState = "passiv";
        String stateEffective = "2024-01-01";
        String changedAt = "2024-01-01T12:00:00";

        StateChangeDto dto = new StateChangeDto(
            id, givenName, name, oldState, newState, stateEffective, changedAt
        );

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(givenName, dto.givenName());
        assertEquals(name, dto.name());
        assertEquals(oldState, dto.oldState());
        assertEquals(newState, dto.newState());
        assertEquals(stateEffective, dto.stateEffective());
        assertEquals(changedAt, dto.changedAt());
    }

    @Test
    public void testFromStateChange() {
        UUID id = UUID.fromString("df8ea850-52a3-4856-aea0-79361d48ff8c");
        LocalDate stateEffectiveDate = LocalDate.of(2024, 1, 1);
        LocalDateTime changedAtDateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        HistoryService.StateChange stateChange = new HistoryService.StateChange(
            id, "John", "Doe", "aktiv", "passiv", stateEffectiveDate, changedAtDateTime
        );

        StateChangeDto dto = StateChangeDto.fromStateChange(stateChange);

        assertNotNull(dto);
        assertEquals(id.toString(), dto.id());
        assertEquals("John", dto.givenName());
        assertEquals("Doe", dto.name());
        assertEquals("aktiv", dto.oldState());
        assertEquals("passiv", dto.newState());
        assertEquals("2024-01-01", dto.stateEffective());
        assertEquals("2024-01-01T12:00:00", dto.changedAt());
    }

    @Test
    public void testFromStateChange_WithNull_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            StateChangeDto.fromStateChange(null);
        });
    }

    @Test
    public void testEquality() {
        StateChangeDto dto1 = new StateChangeDto(
            "id1", "John", "Doe", "aktiv", "passiv", "2024-01-01", "2024-01-01T12:00:00"
        );
        StateChangeDto dto2 = new StateChangeDto(
            "id1", "John", "Doe", "aktiv", "passiv", "2024-01-01", "2024-01-01T12:00:00"
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    public void testInequality() {
        StateChangeDto dto1 = new StateChangeDto(
            "id1", "John", "Doe", "aktiv", "passiv", "2024-01-01", "2024-01-01T12:00:00"
        );
        StateChangeDto dto2 = new StateChangeDto(
            "id2", "Jane", "Smith", "passiv", "aktiv", "2024-02-01", "2024-02-01T12:00:00"
        );

        assertNotEquals(dto1, dto2);
    }

    @Test
    public void testToString() {
        StateChangeDto dto = new StateChangeDto(
            "df8ea850-52a3-4856-aea0-79361d48ff8c",
            "John", "Doe", "aktiv", "passiv", "2024-01-01", "2024-01-01T12:00:00"
        );

        String result = dto.toString();
        assertNotNull(result);
        assertTrue(result.contains("StateChangeDto"));
    }

    @Test
    public void testEmptyStrings() {
        StateChangeDto dto = new StateChangeDto("", "", "", "", "", "", "");

        assertEquals("", dto.id());
        assertEquals("", dto.givenName());
        assertEquals("", dto.name());
        assertEquals("", dto.oldState());
        assertEquals("", dto.newState());
        assertEquals("", dto.stateEffective());
        assertEquals("", dto.changedAt());
    }
}
