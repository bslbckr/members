package de.guc.entities;

import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MemberEntity
 * Uses H2 in-memory database for testing
 */
@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class MemberEntityTest {

    @Inject
    EntityManager entityManager;

    private UUID testMemberId;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up before each test
        Panache.deleteAll(MemberEntity.class);
        
        // Create a test member
        MemberEntity member = new MemberEntity();
        member.name = "TestMember";
        member.givenName = "Test";
        member.entryDate = LocalDate.of(2020, 1, 15);
        member.dayOfBirth = LocalDate.of(1990, 5, 20);
        member.gender = "male";
        member.state = "aktiv";
        member.stateEffective = LocalDate.of(2020, 1, 15);
        member.dfvNumber = 12345;
        member.dse = true;
        member.rabatt = false;
        member.street = "Test Street";
        member.zipCode = "12345";
        member.city = "Test City";
        member.mobile = "+123456789";
        member.email = "test@example.com";
        member.emailList = true;
        member.exitDate = null;
        
        member.persist();
        testMemberId = member.id;
    }

    @AfterEach
    @Transactional
    public void tearDown() {
        Panache.deleteAll(MemberEntity.class);
    }

    @Test
    public void testById_ExistingMember() {
        Optional<MemberEntity> result = MemberEntity.byId(testMemberId);
        
        assertTrue(result.isPresent());
        assertEquals("TestMember", result.get().name);
        assertEquals("Test", result.get().givenName);
    }

    @Test
    public void testById_NonExistingMember() {
        UUID nonExistingId = UUID.randomUUID();
        Optional<MemberEntity> result = MemberEntity.byId(nonExistingId);
        
        assertFalse(result.isPresent());
    }

    @Test
    public void testById_WithLocked() {
        Optional<MemberEntity> result = MemberEntity.byId(testMemberId, false);
        assertTrue(result.isPresent());
        
        // Test with locked = true
        Optional<MemberEntity> lockedResult = MemberEntity.byId(testMemberId, true);
        assertTrue(lockedResult.isPresent());
    }

    @Test
    @Transactional
    public void testListAll() {
        // Create additional members
        MemberEntity member2 = new MemberEntity();
        member2.name = "Member2";
        member2.givenName = "Test2";
        member2.persist();
        
        MemberEntity member3 = new MemberEntity();
        member3.name = "Member3";
        member3.givenName = "Test3";
        member3.persist();
        
        List<MemberEntity> allMembers = MemberEntity.listAll();
        
        assertNotNull(allMembers);
        assertTrue(allMembers.size() >= 3);
    }

    @Test
    @Transactional
    public void testActiveMembers() {
        // Create an inactive member (with exitDate)
        MemberEntity inactiveMember = new MemberEntity();
        inactiveMember.name = "Inactive";
        inactiveMember.givenName = "Test";
        inactiveMember.entryDate = LocalDate.now().minusMonths(6);
        inactiveMember.exitDate = LocalDate.now().minusMonths(1);
        inactiveMember.persist();
        
        // Create an active member
        MemberEntity activeMember = new MemberEntity();
        activeMember.name = "Active";
        activeMember.givenName = "Test";
        activeMember.entryDate = LocalDate.now().minusMonths(6);
        activeMember.exitDate = null;
        activeMember.persist();

        List<MemberOverview> activeMembers = MemberEntity.activeMembers();
        
        assertNotNull(activeMembers);
        // Should include the original test member and the new active member
        assertTrue(activeMembers.size() >= 2);
        
        // Check that inactive member is not in the list
        boolean hasInactive = activeMembers.stream()
            .anyMatch(m -> "Inactive".equals(m.name()));
        assertFalse(hasInactive);
    }

    @Test
    @Transactional
    public void testActiveMemberEmails() {
        // Create additional members
        MemberEntity member2 = new MemberEntity();
        member2.name = "Member2";
        member2.email = "member2@example.com";
        member2.persist();
        
        MemberEntity member3 = new MemberEntity();
        member3.name = "Member3";
        member3.email = "member3@example.com";
        member3.exitDate = LocalDate.now().minusMonths(1); // Inactive
        member3.persist();

        List<String> emails = MemberEntity.activeMemberEmails();
        
        assertNotNull(emails);
        assertTrue(emails.size() >= 2);
        assertTrue(emails.contains("test@example.com"));
        assertTrue(emails.contains("member2@example.com"));
        // Inactive member should not be included
        assertFalse(emails.contains("member3@example.com"));
    }

    @Test
    @Transactional
    public void testEntriesSinceDate() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(2);
        
        // Create a member with recent entry date
        MemberEntity recentMember = new MemberEntity();
        recentMember.name = "Recent";
        recentMember.givenName = "Test";
        recentMember.entryDate = LocalDate.now().minusDays(1);
        recentMember.persist();
        
        // Create a member with old entry date
        MemberEntity oldMember = new MemberEntity();
        oldMember.name = "Old";
        oldMember.givenName = "Test";
        oldMember.entryDate = LocalDate.now().minusMonths(3);
        oldMember.persist();

        List<EntryOrCancellation> entries = MemberEntity.entriesSinceDate(cutoffDate);
        
        assertNotNull(entries);
        // Should include recent member and original test member
        assertTrue(entries.size() >= 2);
        
        // Check that old member is not included
        boolean hasOld = entries.stream()
            .anyMatch(e -> "Old".equals(e.name()));
        assertFalse(hasOld);
    }

    @Test
    @Transactional
    public void testPersistAndRetrieve() {
        MemberEntity newMember = new MemberEntity();
        newMember.name = "NewMember";
        newMember.givenName = "New";
        newMember.email = "new@example.com";
        
        newMember.persist();
        
        // Clear the persistence context to force a database read
        entityManager.clear();
        
        Optional<MemberEntity> retrieved = MemberEntity.byId(newMember.id);
        assertTrue(retrieved.isPresent());
        assertEquals("NewMember", retrieved.get().name);
        assertEquals("New", retrieved.get().givenName);
        assertEquals("new@example.com", retrieved.get().email);
    }

    @Test
    @Transactional
    public void testUpdateMember() {
        Optional<MemberEntity> memberOpt = MemberEntity.byId(testMemberId);
        assertTrue(memberOpt.isPresent());
        
        MemberEntity member = memberOpt.get();
        String originalName = member.name;
        
        // Update the member
        member.name = "UpdatedName";
        member.persist();
        
        // Clear the persistence context
        entityManager.clear();
        
        // Retrieve again and verify update
        Optional<MemberEntity> updatedMemberOpt = MemberEntity.byId(testMemberId);
        assertTrue(updatedMemberOpt.isPresent());
        assertEquals("UpdatedName", updatedMemberOpt.get().name);
    }

    @Test
    @Transactional
    public void testDeleteMember() {
        MemberEntity memberToDelete = new MemberEntity();
        memberToDelete.name = "ToDelete";
        memberToDelete.givenName = "Test";
        memberToDelete.persist();
        UUID idToDelete = memberToDelete.id;
        
        // Verify it exists
        assertTrue(MemberEntity.byId(idToDelete).isPresent());
        
        // Delete
        Panache.deleteById(MemberEntity.class, idToDelete);
        
        // Verify it's gone
        assertFalse(MemberEntity.byId(idToDelete).isPresent());
    }
}
