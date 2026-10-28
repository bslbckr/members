package de.guc.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CancelMembershipDto record class
 */
public class CancelMembershipDtoTest {

    @Test
    public void testRecordCreation() {
        LocalDate cancellationDate = LocalDate.of(2024, 12, 31);
        List<UUID> ids = List.of(
            UUID.fromString("df8ea850-52a3-4856-aea0-79361d48ff8c"),
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        );

        CancelMembershipDto dto = new CancelMembershipDto(cancellationDate, ids);

        assertNotNull(dto);
        assertEquals(cancellationDate, dto.cancellationDate());
        assertEquals(ids, dto.ids());
    }

    @Test
    public void testEmptyList() {
        LocalDate cancellationDate = LocalDate.of(2024, 12, 31);
        List<UUID> emptyList = List.of();

        CancelMembershipDto dto = new CancelMembershipDto(cancellationDate, emptyList);

        assertNotNull(dto);
        assertEquals(cancellationDate, dto.cancellationDate());
        assertTrue(dto.ids().isEmpty());
    }

    @Test
    public void testSingleId() {
        LocalDate cancellationDate = LocalDate.of(2024, 6, 15);
        UUID singleId = UUID.fromString("df8ea850-52a3-4856-aea0-79361d48ff8c");

        CancelMembershipDto dto = new CancelMembershipDto(cancellationDate, List.of(singleId));

        assertEquals(1, dto.ids().size());
        assertEquals(singleId, dto.ids().get(0));
    }

    @Test
    public void testEquality() {
        LocalDate date1 = LocalDate.of(2024, 12, 31);
        LocalDate date2 = LocalDate.of(2024, 12, 31);
        List<UUID> ids = List.of(UUID.fromString("df8ea850-52a3-4856-aea0-79361d48ff8c"));

        CancelMembershipDto dto1 = new CancelMembershipDto(date1, ids);
        CancelMembershipDto dto2 = new CancelMembershipDto(date2, ids);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    public void testInequality_DifferentDates() {
        LocalDate date1 = LocalDate.of(2024, 12, 31);
        LocalDate date2 = LocalDate.of(2025, 1, 1);
        List<UUID> ids = List.of(UUID.fromString("df8ea850-52a3-4856-aea0-79361d48ff8c"));

        CancelMembershipDto dto1 = new CancelMembershipDto(date1, ids);
        CancelMembershipDto dto2 = new CancelMembershipDto(date2, ids);

        assertNotEquals(dto1, dto2);
    }

    @Test
    public void testInequality_DifferentIds() {
        LocalDate date = LocalDate.of(2024, 12, 31);
        UUID id1 = UUID.fromString("df8ea850-52a3-4856-aea0-79361d48ff8c");
        UUID id2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        CancelMembershipDto dto1 = new CancelMembershipDto(date, List.of(id1));
        CancelMembershipDto dto2 = new CancelMembershipDto(date, List.of(id2));

        assertNotEquals(dto1, dto2);
    }

    @Test
    public void testToString() {
        LocalDate cancellationDate = LocalDate.of(2024, 12, 31);
        List<UUID> ids = List.of(UUID.fromString("df8ea850-52a3-4856-aea0-79361d48ff8c"));

        CancelMembershipDto dto = new CancelMembershipDto(cancellationDate, ids);

        String result = dto.toString();
        assertNotNull(result);
        assertTrue(result.contains("CancelMembershipDto"));
    }

    @Test
    public void testNullCancellationDate_ThrowsException() {
        List<UUID> ids = List.of(UUID.fromString("df8ea850-52a3-4856-aea0-79361d48ff8c"));

        assertThrows(NullPointerException.class, () -> {
            new CancelMembershipDto(null, ids);
        });
    }

    @Test
    public void testNullIds_ThrowsException() {
        LocalDate cancellationDate = LocalDate.of(2024, 12, 31);

        assertThrows(NullPointerException.class, () -> {
            new CancelMembershipDto(cancellationDate, null);
        });
    }
}
