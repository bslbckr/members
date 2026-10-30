package de.guc.service;

import de.guc.services.HistoryService;
import de.guc.entities.MemberEntity;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for HistoryServiceImpl
 * Tests state change and cancellation history tracking
 */
@QuarkusTest
@TestTransaction
@QuarkusTestResource(H2DatabaseTestResource.class)
public class HistoryServiceImplTest {

    @Inject
    HistoryService historyService;

    private UUID testMemberId;

    @BeforeEach
    public void setUp() {
        // Create a test member
        MemberEntity member = new MemberEntity();
        member.name = "Doe";
        member.givenName = "John";
        member.entryDate = LocalDate.of(2020, 1, 15);
        member.dayOfBirth = LocalDate.of(1990, 5, 20);
        member.gender = "male";
        member.state = "aktiv";
        member.stateEffective = LocalDate.of(2020, 1, 15);
        member.dfvNumber = 12345;
        member.dse = true;
        member.rabatt = false;
        member.street = "Main Street";
        member.zipCode = "12345";
        member.city = "Berlin";
        member.mobile = "+123456789";
        member.email = "john.doe@example.com";
        member.emailList = true;
        member.persist();
        testMemberId = member.id;
    }

    @Test
    public void testStateChanges_WithNoChanges() {
        Collection<HistoryService.StateChange> stateChanges = historyService.stateChanges();
        
        assertNotNull(stateChanges);
        // No state changes yet
        assertTrue(stateChanges.isEmpty());
    }

    @Test
    public void testStateChanges_WithStateChange() {
        // Change the member's state
        MemberEntity member = MemberEntity.findById(testMemberId);
        member.state = "passiv";
        member.stateEffective = LocalDate.now();
        member.persist();

        Collection<HistoryService.StateChange> stateChanges = historyService.stateChanges();
        
        assertNotNull(stateChanges);
        // Should have at least one state change
        assertTrue(stateChanges.size() >= 1);
        
        // Check that the state change is for our member
        boolean hasOurMember = stateChanges.stream()
            .anyMatch(sc -> testMemberId.equals(sc.id()));
        assertTrue(hasOurMember);
    }

    @Test
    public void testStateChanges_MultipleChanges() {
        // Change the member's state multiple times
        MemberEntity member = MemberEntity.findById(testMemberId);
        
        member.state = "passiv";
        member.stateEffective = LocalDate.now().minusDays(10);
        member.persist();
        
        member.state = "berufst\u00e4tig";
        member.stateEffective = LocalDate.now().minusDays(5);
        member.persist();
        
        member.state = "aktiv";
        member.stateEffective = LocalDate.now();
        member.persist();
        Panache.getEntityManager().flush();

        Collection<HistoryService.StateChange> stateChanges = historyService.stateChanges();
        
        assertNotNull(stateChanges);
        // Should have at least 3 state changes
        assertTrue(stateChanges.size() >= 3);
    }

    @Test
    public void testCancellations_WithNoCancellations() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(18);
        
        Collection<de.guc.entities.EntryOrCancellation> cancellations = 
            historyService.cancellations(cutoffDate);
        
        assertNotNull(cancellations);
        // No cancellations yet
        assertTrue(cancellations.isEmpty());
    }

    @Test
    public void testCancellations_WithCancellation() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(18);
        
        // Cancel the member
        MemberEntity member = MemberEntity.findById(testMemberId);
        member.exitDate = LocalDate.now().minusDays(1);
        member.persist();

        Collection<de.guc.entities.EntryOrCancellation> cancellations = 
            historyService.cancellations(cutoffDate);
        
        assertNotNull(cancellations);
        // Should have at least one cancellation
        assertTrue(cancellations.size() >= 1);
        
        // Check that the cancellation is for our member
        boolean hasOurMember = cancellations.stream()
            .anyMatch(c -> "Doe".equals(c.name()) && "John".equals(c.givenName()));
        assertTrue(hasOurMember);
    }

    @Test
    public void testCancellations_WithOldCancellation() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(6);
        
        // Cancel the member with an old date (before cutoff)
        MemberEntity member = MemberEntity.findById(testMemberId);
        member.exitDate = LocalDate.now().minusMonths(12);
        member.persist();

        Collection<de.guc.entities.EntryOrCancellation> cancellations = 
            historyService.cancellations(cutoffDate);
        
        assertNotNull(cancellations);
        // Old cancellation should be included (cutoff is 6 months ago)
        assertTrue(cancellations.size() >= 1);
    }

    @Test
    public void testCancellations_WithVeryOldCancellation() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(6);
        
        // Cancel the member with a very old date (before cutoff)
        MemberEntity member = MemberEntity.findById(testMemberId);
        member.exitDate = LocalDate.now().minusMonths(24);
        member.persist();

        Collection<de.guc.entities.EntryOrCancellation> cancellations = 
            historyService.cancellations(cutoffDate);
        
        assertNotNull(cancellations);
        // Very old cancellation should NOT be included (cutoff is 6 months ago)
        assertTrue(cancellations.isEmpty());
    }

    @Test
    public void testStateChange_RecordStructure() {
        // Change the member's state
        MemberEntity member = MemberEntity.findById(testMemberId);
        String oldState = member.state;
        member.state = "passiv";
        member.stateEffective = LocalDate.now();
        member.persist();

        Collection<HistoryService.StateChange> stateChanges = historyService.stateChanges();
        
        assertNotNull(stateChanges);
        assertFalse(stateChanges.isEmpty());
        
        HistoryService.StateChange change = stateChanges.iterator().next();
        assertNotNull(change.id());
        assertNotNull(change.givenName());
        assertNotNull(change.name());
        assertNotNull(change.oldState());
        assertNotNull(change.newState());
        assertNotNull(change.stateEffective());
        assertNotNull(change.changedAt());
    }

    @Test
    public void testStateChange_OldAndNewStates() {
        MemberEntity member = MemberEntity.findById(testMemberId);
        String oldState = member.state;
        String newState = "passiv";
        
        member.state = newState;
        member.stateEffective = LocalDate.now();
        member.persist();

        Collection<HistoryService.StateChange> stateChanges = historyService.stateChanges();
        
        assertNotNull(stateChanges);
        assertFalse(stateChanges.isEmpty());
        
        HistoryService.StateChange change = stateChanges.iterator().next();
        assertEquals(oldState, change.oldState());
        assertEquals(newState, change.newState());
    }

    @Test
    public void testMultipleMembers_StateChanges() {
        // Create another member
        MemberEntity member2 = new MemberEntity();
        member2.name = "Smith";
        member2.givenName = "Jane";
        member2.entryDate = LocalDate.of(2020, 2, 20);
        member2.dayOfBirth = LocalDate.of(1992, 3, 10);
        member2.gender = "female";
        member2.state = "aktiv";
        member2.stateEffective = LocalDate.of(2020, 2, 20);
        member2.persist();
        
        // Change both members' states
        MemberEntity member1 = MemberEntity.findById(testMemberId);
        member1.state = "passiv";
        member1.stateEffective = LocalDate.now();
        member1.persist();
        
        member2.state = "berufst\u00e4tig";
        member2.stateEffective = LocalDate.now();
        member2.persist();

        Collection<HistoryService.StateChange> stateChanges = historyService.stateChanges();
        
        assertNotNull(stateChanges);
        // Should have at least 2 state changes
        assertTrue(stateChanges.size() >= 2);
    }

    @Test
    public void testStateChangesForMember() {
        // Change the member's state
        MemberEntity member = MemberEntity.findById(testMemberId);
        member.state = "passiv";
        member.stateEffective = LocalDate.now();
        member.persist();

        // Note: The current HistoryService implementation doesn't have
        // getStateChangesForMember method, so we test the general method
        Collection<HistoryService.StateChange> stateChanges = historyService.stateChanges();
        
        assertNotNull(stateChanges);
        assertFalse(stateChanges.isEmpty());
        
        // Filter for our member
        Collection<HistoryService.StateChange> ourChanges = stateChanges.stream()
            .filter(sc -> testMemberId.equals(sc.id()))
            .toList();
        
        assertFalse(ourChanges.isEmpty());
    }
}
