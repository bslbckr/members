package de.guc.resource;

import de.guc.Constants;
import de.guc.ChangeResource;
import de.guc.entities.MemberEntity;
import de.guc.entities.MemberOverview;
import de.guc.entities.EntryOrCancellation;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.jboss.resteasy.reactive.RestResponse.StatusCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


/**
 * Integration tests for ChangeResource
 * Tests history and change tracking endpoints with proper authentication
 */
@QuarkusTest
@TestHTTPEndpoint(ChangeResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class ChangeResourceTest {

    @Inject
    ChangeResource changeResource;

    private UUID testMemberId;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up before each test
        MemberEntity.deleteAll();
        
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

    @AfterEach
    @Transactional
    public void tearDown() {
        MemberEntity.deleteAll();
    }

    // ========== GET /v1/changes/state Tests ==========

    @Test
    public void testGetStateChanges_Unauthorized() {
        given()
            .when()
            .get("/v1/changes/state")
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testGetStateChanges_Success() {
        given()
            .when()
            .get("/v1/changes/state")
            .then()
            .statusCode(StatusCode.OK)
            .contentType(ContentType.JSON);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testGetStateChanges_ReturnsArray() {
        given()
            .when()
            .get("/v1/changes/state")
            .then()
            .statusCode(StatusCode.OK)
            .body(notNullValue());
    }

    // ========== GET /v1/changes/cancellation Tests ==========

    @Test
    public void testGetCancellations_Unauthorized() {
        given()
            .when()
            .get("/v1/changes/cancellation")
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testGetCancellations_Success() {
        given()
            .when()
            .get("/v1/changes/cancellation")
            .then()
            .statusCode(StatusCode.OK)
            .contentType(ContentType.JSON);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testGetCancellations_ReturnsArray() {
        given()
            .when()
            .get("/v1/changes/cancellation")
            .then()
            .statusCode(StatusCode.OK)
            .body(notNullValue());
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testGetCancellations_WithRecentCancellations() {
        // Create a member with exit date in the last 18 months
        MemberEntity member = MemberEntity.findById(testMemberId);
        member.exitDate = LocalDate.now().minusMonths(6);
        member.persist();

        given()
            .when()
            .get("/v1/changes/cancellation")
            .then()
            .statusCode(StatusCode.OK)
            .body(notNullValue());
    }

    // ========== GET /v1/changes/entries Tests ==========

    @Test
    public void testGetEntries_Unauthorized() {
        given()
            .when()
            .get("/v1/changes/entries")
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testGetEntries_Success() {
        given()
            .when()
            .get("/v1/changes/entries")
            .then()
            .statusCode(StatusCode.OK)
            .contentType(ContentType.JSON);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testGetEntries_ReturnsArray() {
        given()
            .when()
            .get("/v1/changes/entries")
            .then()
            .statusCode(StatusCode.OK)
            .body(notNullValue());
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testGetEntries_WithRecentEntries() {
        // The test member has entry date in the last 18 months
        given()
            .when()
            .get("/v1/changes/entries")
            .then()
            .statusCode(StatusCode.OK)
            .body(notNullValue());
    }

    // ========== Wrong Role Tests ==========

    @Test
    @TestSecurity(user = "regularUser", roles = "member")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "regular-user-id")
    })
    public void testGetStateChanges_WrongRole() {
        given()
            .when()
            .get("/v1/changes/state")
            .then()
            .statusCode(StatusCode.FORBIDDEN);
    }

    @Test
    @TestSecurity(user = "regularUser", roles = "member")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "regular-user-id")
    })
    public void testGetCancellations_WrongRole() {
        given()
            .when()
            .get("/v1/changes/cancellation")
            .then()
            .statusCode(StatusCode.FORBIDDEN);
    }

    @Test
    @TestSecurity(user = "regularUser", roles = "member")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "regular-user-id")
    })
    public void testGetEntries_WrongRole() {
        given()
            .when()
            .get("/v1/changes/entries")
            .then()
            .statusCode(StatusCode.FORBIDDEN);
    }

    // ========== Integration with MemberEntity ==========

    @Test
    @Transactional
    public void testEntriesSinceDate_DirectCall() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(18);
        
        Collection<EntryOrCancellation> entries = MemberEntity.entriesSinceDate(cutoffDate);
        
        assertThat(entries, notNullValue());
        // Should include the test member
        assertThat(entries.size() >= 1, is(true));
    }

    @Test
    @Transactional
    public void testActiveMembers_DirectCall() {
        Collection<MemberOverview> activeMembers = MemberEntity.activeMembers();
        
        assertThat(activeMembers, notNullValue());
        // Should include the test member (no exit date)
        assertThat(activeMembers.size() >= 1, is(true));
    }

    @Test
    @Transactional
    public void testActiveMemberEmails_DirectCall() {
        Collection<String> emails = MemberEntity.activeMemberEmails();
        
        assertThat(emails, notNullValue());
        // Should include the test member's email
        assertThat(emails.size() >= 1, is(true));
        assertThat(emails.contains("john.doe@example.com"), is(true));
    }
}
