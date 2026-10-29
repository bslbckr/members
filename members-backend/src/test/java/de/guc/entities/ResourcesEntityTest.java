package de.guc.entities;

import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.TestTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ResourcesEntity
 * Uses H2 in-memory database for testing
 */
@QuarkusTest
@TestTransaction
@QuarkusTestResource(H2DatabaseTestResource.class)
public class ResourcesEntityTest {

    @Inject
    EntityManager entityManager;

    private UUID testMemberId;
    private UUID testIdentityId;

    @BeforeEach
    @Transactional
    public void setUp() {
        
        // Create a test member
        MemberEntity member = new MemberEntity();
        member.name = "TestMember";
        member.givenName = "Test";
        member.persist();
        testMemberId = member.id;
        
        testIdentityId = UUID.fromString("df8ea850-52a3-4856-aea0-79361d48ff8c");
    }

    @Test
    @Transactional
    public void testCreateAccessResource() {
        ResourcesEntity access = new ResourcesEntity();
        access.identity = testIdentityId;
        access.member = MemberEntity.findById(testMemberId);
        access.role = "SELF";
        access.name = "Test";
        access.rType = ResourcesEntity.ResourceType.MEMBER;
        
        access.persist();
        
        assertNotNull(access.id);
        assertEquals(testIdentityId, access.identity);
        assertEquals(testMemberId, access.member.id);
        assertEquals("SELF", access.role);
        assertEquals("Test", access.name);
        assertEquals(ResourcesEntity.ResourceType.MEMBER, access.rType);
    }

    @Test
    @Transactional
    public void testResourcesForIdentity() {
        // Create multiple resources for the same identity
        ResourcesEntity access1 = new ResourcesEntity();
        access1.identity = testIdentityId;
        access1.member = MemberEntity.findById(testMemberId);
        access1.role = "SELF";
        access1.name = "Test1";
        access1.rType = ResourcesEntity.ResourceType.MEMBER;
        access1.persist();
        
        ResourcesEntity access2 = new ResourcesEntity();
        access2.identity = testIdentityId;
        access2.member = MemberEntity.findById(testMemberId);
        access2.role = "PARENT";
        access2.name = "Test2";
        access2.rType = ResourcesEntity.ResourceType.MEMBER;
        access2.persist();
        
        // Create a resource for a different identity
        UUID otherIdentity = UUID.randomUUID();
        ResourcesEntity access3 = new ResourcesEntity();
        access3.identity = otherIdentity;
        access3.member = MemberEntity.findById(testMemberId);
        access3.role = "SELF";
        access3.name = "Test3";
        access3.rType = ResourcesEntity.ResourceType.MEMBER;
        access3.persist();

        List<ResourcesEntity> resources = ResourcesEntity.resourcesForIdentity(testIdentityId.toString());
        
        assertNotNull(resources);
        assertEquals(2, resources.size());
        
        // Verify the resources belong to the correct identity
        for (ResourcesEntity res : resources) {
            assertEquals(testIdentityId, res.identity);
        }
    }

    @Test
    @Transactional
    public void testHasAccess_Existing() {
        ResourcesEntity access = new ResourcesEntity();
        access.identity = testIdentityId;
        access.member = MemberEntity.findById(testMemberId);
        access.role = "SELF";
        access.name = "Test";
        access.rType = ResourcesEntity.ResourceType.MEMBER;
        access.persist();

        Optional<ResourcesEntity> result = ResourcesEntity.hasAccess(testIdentityId, testMemberId);
        
        assertTrue(result.isPresent());
        assertEquals(testIdentityId, result.get().identity);
        assertEquals(testMemberId, result.get().member.id);
    }

    @Test
    @Transactional
    public void testHasAccess_NonExisting() {
        UUID nonExistingIdentity = UUID.randomUUID();
        UUID nonExistingMember = UUID.randomUUID();
        
        Optional<ResourcesEntity> result = ResourcesEntity.hasAccess(nonExistingIdentity, nonExistingMember);
        
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    public void testResourceTypes() {
        ResourcesEntity memberResource = new ResourcesEntity();
        memberResource.identity = testIdentityId;
        memberResource.member = MemberEntity.findById(testMemberId);
        memberResource.role = "SELF";
        memberResource.rType = ResourcesEntity.ResourceType.MEMBER;
        memberResource.persist();
        
        ResourcesEntity urlResource = new ResourcesEntity();
        urlResource.identity = UUID.randomUUID();
        urlResource.member = MemberEntity.findById(testMemberId);
        urlResource.role = "VIEWER";
        urlResource.rType = ResourcesEntity.ResourceType.URL;
        urlResource.persist();

        List<ResourcesEntity> allResources = ResourcesEntity.listAll();
        
        assertNotNull(allResources);
        assertEquals(2, allResources.size());
        
        // Verify resource types
        boolean hasMemberType = allResources.stream()
            .anyMatch(r -> r.rType == ResourcesEntity.ResourceType.MEMBER);
        boolean hasUrlType = allResources.stream()
            .anyMatch(r -> r.rType == ResourcesEntity.ResourceType.URL);
        
        assertTrue(hasMemberType);
        assertTrue(hasUrlType);
    }

    @Test
    @Transactional
    public void testListByMember() {
        ResourcesEntity access1 = new ResourcesEntity();
        access1.identity = testIdentityId;
        access1.member = MemberEntity.findById(testMemberId);
        access1.role = "SELF";
        access1.persist();
        
        ResourcesEntity access2 = new ResourcesEntity();
        access2.identity = UUID.randomUUID();
        access2.member = MemberEntity.findById(testMemberId);
        access2.role = "PARENT";
        access2.persist();

/*        List<ResourcesEntity> resources = ResourcesEntity.list("member", MemberEntity.findById(testMemberId));
        
        assertNotNull(resources);
        assertEquals(2, resources.size());
        
        // Verify all resources are for the same member
        for (ResourcesEntity res : resources) {
            assertEquals(testMemberId, res.member.id);
        }*/
    }

    @Test
    @Transactional
    public void testDeleteResource() {
        ResourcesEntity access = new ResourcesEntity();
        access.identity = testIdentityId;
        access.member = MemberEntity.findById(testMemberId);
        access.role = "SELF";
        access.persist();
        
        long id = access.id;
        
        // Verify it exists
        assertTrue(ResourcesEntity.findByIdOptional(id).isPresent());
        
        // Delete
        ResourcesEntity.deleteById(id);
        
        // Verify it's gone
        assertFalse(ResourcesEntity.findByIdOptional(id).isPresent());
    }

    @Test
    @Transactional
    public void testUpdateResource() {
        ResourcesEntity access = new ResourcesEntity();
        access.identity = testIdentityId;
        access.member = MemberEntity.findById(testMemberId);
        access.role = "SELF";
        access.name = "Original";
        access.persist();
        
        long id = access.id;
        
        // Update the resource
        access.name = "Updated";
        access.role = "ADMIN";
        access.persist();
        
        // Clear the persistence context
        entityManager.clear();
        
        // Retrieve again and verify update
        Optional<ResourcesEntity> updated = ResourcesEntity.findByIdOptional(id);
        assertTrue(updated.isPresent());
        assertEquals("Updated", updated.get().name);
        assertEquals("ADMIN", updated.get().role);
    }
}
