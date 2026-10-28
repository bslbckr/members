package de.guc.entities;

import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EmailUpdateEntity
 * Uses H2 in-memory database for testing
 */
@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class EmailUpdateEntityTest {

    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up before each test
        Panache.deleteAll(EmailUpdateEntity.class);
    }

    @AfterEach
    @Transactional
    public void tearDown() {
        Panache.deleteAll(EmailUpdateEntity.class);
    }

    @Test
    @Transactional
    public void testCreateEmailUpdate_Insert() {
        EmailUpdateEntity emailUpdate = new EmailUpdateEntity();
        emailUpdate.emailAddress = "test@example.com";
        emailUpdate.change = EmailUpdateEntity.ChangeType.INSERT;
        
        // insertedAt should be set automatically
        LocalDateTime beforeInsert = LocalDateTime.now(ZoneOffset.UTC);
        emailUpdate.persist();
        LocalDateTime afterInsert = LocalDateTime.now(ZoneOffset.UTC);
        
        assertNotNull(emailUpdate.id);
        assertEquals("test@example.com", emailUpdate.emailAddress);
        assertEquals(EmailUpdateEntity.ChangeType.INSERT, emailUpdate.change);
        assertNotNull(emailUpdate.insertedAt);
        assertTrue(emailUpdate.insertedAt.isAfter(beforeInsert) || emailUpdate.insertedAt.isEqual(beforeInsert));
        assertTrue(emailUpdate.insertedAt.isBefore(afterInsert) || emailUpdate.insertedAt.isEqual(afterInsert));
    }

    @Test
    @Transactional
    public void testCreateEmailUpdate_Remove() {
        EmailUpdateEntity emailUpdate = new EmailUpdateEntity();
        emailUpdate.emailAddress = "old@example.com";
        emailUpdate.change = EmailUpdateEntity.ChangeType.REMOVE;
        
        emailUpdate.persist();
        
        assertNotNull(emailUpdate.id);
        assertEquals("old@example.com", emailUpdate.emailAddress);
        assertEquals(EmailUpdateEntity.ChangeType.REMOVE, emailUpdate.change);
        assertNotNull(emailUpdate.insertedAt);
    }

    @Test
    @Transactional
    public void testListAll() {
        // Create multiple email updates
        EmailUpdateEntity update1 = new EmailUpdateEntity();
        update1.emailAddress = "user1@example.com";
        update1.change = EmailUpdateEntity.ChangeType.INSERT;
        update1.persist();
        
        EmailUpdateEntity update2 = new EmailUpdateEntity();
        update2.emailAddress = "user2@example.com";
        update2.change = EmailUpdateEntity.ChangeType.REMOVE;
        update2.persist();
        
        EmailUpdateEntity update3 = new EmailUpdateEntity();
        update3.emailAddress = "user3@example.com";
        update3.change = EmailUpdateEntity.ChangeType.INSERT;
        update3.persist();

        List<EmailUpdateEntity> allUpdates = EmailUpdateEntity.listAll();
        
        assertNotNull(allUpdates);
        assertEquals(3, allUpdates.size());
    }

    @Test
    @Transactional
    public void testFindById() {
        EmailUpdateEntity emailUpdate = new EmailUpdateEntity();
        emailUpdate.emailAddress = "test@example.com";
        emailUpdate.change = EmailUpdateEntity.ChangeType.INSERT;
        emailUpdate.persist();
        
        long id = emailUpdate.id;
        
        EmailUpdateEntity retrieved = EmailUpdateEntity.findById(id);
        
        assertNotNull(retrieved);
        assertEquals(id, retrieved.id);
        assertEquals("test@example.com", retrieved.emailAddress);
        assertEquals(EmailUpdateEntity.ChangeType.INSERT, retrieved.change);
    }

    @Test
    @Transactional
    public void testUpdateEmailUpdate() {
        EmailUpdateEntity emailUpdate = new EmailUpdateEntity();
        emailUpdate.emailAddress = "original@example.com";
        emailUpdate.change = EmailUpdateEntity.ChangeType.INSERT;
        emailUpdate.persist();
        
        long id = emailUpdate.id;
        LocalDateTime originalInsertedAt = emailUpdate.insertedAt;
        
        // Update the email address
        emailUpdate.emailAddress = "updated@example.com";
        emailUpdate.change = EmailUpdateEntity.ChangeType.REMOVE;
        emailUpdate.persist();
        
        EmailUpdateEntity retrieved = EmailUpdateEntity.findById(id);
        
        assertNotNull(retrieved);
        assertEquals("updated@example.com", retrieved.emailAddress);
        assertEquals(EmailUpdateEntity.ChangeType.REMOVE, retrieved.change);
        // insertedAt should not change
        assertEquals(originalInsertedAt, retrieved.insertedAt);
    }

    @Test
    @Transactional
    public void testDeleteEmailUpdate() {
        EmailUpdateEntity emailUpdate = new EmailUpdateEntity();
        emailUpdate.emailAddress = "test@example.com";
        emailUpdate.change = EmailUpdateEntity.ChangeType.INSERT;
        emailUpdate.persist();
        
        long id = emailUpdate.id;
        
        // Verify it exists
        assertNotNull(EmailUpdateEntity.findById(id));
        
        // Delete
        Panache.deleteById(EmailUpdateEntity.class, id);
        
        // Verify it's gone
        assertNull(EmailUpdateEntity.findById(id));
    }

    @Test
    @Transactional
    public void testChangeTypeEnum() {
        // Verify the enum values
        assertEquals("INSERT", EmailUpdateEntity.ChangeType.INSERT.name());
        assertEquals("REMOVE", EmailUpdateEntity.ChangeType.REMOVE.name());
        
        // Verify there are exactly 2 enum values
        assertEquals(2, EmailUpdateEntity.ChangeType.values().length);
    }

    @Test
    @Transactional
    public void testMultipleUpdatesForSameEmail() {
        String email = "user@example.com";
        
        // Create multiple updates for the same email
        EmailUpdateEntity update1 = new EmailUpdateEntity();
        update1.emailAddress = email;
        update1.change = EmailUpdateEntity.ChangeType.INSERT;
        update1.persist();
        
        EmailUpdateEntity update2 = new EmailUpdateEntity();
        update2.emailAddress = email;
        update2.change = EmailUpdateEntity.ChangeType.REMOVE;
        update2.persist();
        
        EmailUpdateEntity update3 = new EmailUpdateEntity();
        update3.emailAddress = email;
        update3.change = EmailUpdateEntity.ChangeType.INSERT;
        update3.persist();

        List<EmailUpdateEntity> updates = EmailUpdateEntity.listAll();
        
        assertNotNull(updates);
        assertEquals(3, updates.size());
        
        // Verify all updates are for the same email
        long emailCount = updates.stream()
            .filter(u -> email.equals(u.emailAddress))
            .count();
        assertEquals(3, emailCount);
    }

    @Test
    @Transactional
    public void testInsertedAtIsAutomaticallySet() {
        EmailUpdateEntity emailUpdate = new EmailUpdateEntity();
        emailUpdate.emailAddress = "test@example.com";
        emailUpdate.change = EmailUpdateEntity.ChangeType.INSERT;
        
        // insertedAt should be null before persist
        assertNull(emailUpdate.insertedAt);
        
        emailUpdate.persist();
        
        // insertedAt should be set after persist
        assertNotNull(emailUpdate.insertedAt);
    }
}
