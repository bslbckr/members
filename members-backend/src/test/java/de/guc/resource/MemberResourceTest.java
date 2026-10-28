package de.guc.resource;

import de.guc.Constants;
import de.guc.MemberResource;
import de.guc.dto.MemberDto;
import de.guc.entities.MemberEntity;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.jboss.resteasy.reactive.RestResponse.StatusCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for MemberResource
 * Tests all endpoints with proper authentication and authorization
 */
@QuarkusTest
@TestHTTPEndpoint(MemberResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class MemberResourceTest {

    @Inject
    MemberResource memberResource;

    private UUID testMemberId;
    private UUID testIdentityId;

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
        testIdentityId = UUID.fromString("df8ea850-52a3-4856-aea0-79361d48ff8c");
    }

    @AfterEach
    @Transactional
    public void tearDown() {
        MemberEntity.deleteAll();
    }

    // ========== GET /v1/member/{id} Tests ==========

    @Test
    public void testGetMemberById_Unauthorized() {
        given()
            .when()
            .get("/v1/member/{id}", testMemberId.toString())
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testGetMemberById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        
        given()
            .when()
            .get("/v1/member/{id}", nonExistentId.toString())
            .then()
            .statusCode(StatusCode.NOT_FOUND);
    }

    // ========== GET /v1/member/ Tests ==========

    @Test
    public void testGetAllActiveMembers_Unauthorized() {
        given()
            .when()
            .get("/v1/member/")
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "boarduser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testGetAllActiveMembers_Success() {
        given()
            .when()
            .get("/v1/member/")
            .then()
            .statusCode(StatusCode.OK)
            .contentType(ContentType.JSON)
            .body(Matchers.notNullValue());
    }

    @Test
    @TestSecurity(user = "boarduser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testGetAllActiveMembers_ReturnsList() {
        given()
            .when()
            .get("/v1/member/")
            .then()
            .statusCode(StatusCode.OK)
            .body("size()", greaterThanOrEqualTo(1));
    }

    // ========== PUT /v1/member/{id} Tests ==========

    @Test
    public void testUpdateMember_Unauthorized() {
        MemberDto updateDto = new MemberDto();
        updateDto.setId(testMemberId);
        updateDto.setName("UpdatedName");
        updateDto.setGivenName("UpdatedGivenName");
        
        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .when()
            .put("/v1/member/{id}", testMemberId.toString())
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testUpdateMember_IdMismatch() {
        MemberDto updateDto = new MemberDto();
        updateDto.setId(UUID.randomUUID()); // Different ID
        
        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .when()
            .put("/v1/member/{id}", testMemberId.toString())
            .then()
            .statusCode(StatusCode.BAD_REQUEST);
    }

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testUpdateMember_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        MemberDto updateDto = new MemberDto();
        updateDto.setId(nonExistentId);
        
        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .when()
            .put("/v1/member/{id}", nonExistentId.toString())
            .then()
            .statusCode(StatusCode.NOT_FOUND);
    }

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testUpdateMember_Success() {
        MemberDto updateDto = new MemberDto();
        updateDto.setId(testMemberId);
        updateDto.setName("UpdatedName");
        updateDto.setGivenName("UpdatedGivenName");
        updateDto.setCity("UpdatedCity");
        updateDto.setStreet("UpdatedStreet");
        updateDto.setZipCode("99999");
        updateDto.setEmail("updated@example.com");
        updateDto.setMobile("+987654321");
        updateDto.setState("passiv");
        updateDto.setStateEffective(LocalDate.now());
        updateDto.setDayOfBirth(LocalDate.of(1990, 5, 20));
        updateDto.setGender("male");
        updateDto.setDse(true);
        updateDto.setRabatt(false);
        updateDto.setEmailList(true);
        updateDto.setEntryDate(LocalDate.of(2020, 1, 15));
        updateDto.setDfvNumber(12345);

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .when()
            .put("/v1/member/{id}", testMemberId.toString())
            .then()
            .statusCode(StatusCode.OK)
            .contentType(ContentType.JSON)
            .body("name", equalTo("UpdatedName"))
            .body("givenName", equalTo("UpdatedGivenName"))
            .body("city", equalTo("UpdatedCity"));
    }

    // ========== Validation Tests ==========

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testUpdateMember_InvalidEmail() {
        MemberDto updateDto = new MemberDto();
        updateDto.setId(testMemberId);
        updateDto.setName("Test");
        updateDto.setGivenName("Test");
        updateDto.setEmail("invalid-email"); // Invalid email format
        
        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .when()
            .put("/v1/member/{id}", testMemberId.toString())
            .then()
            .statusCode(StatusCode.BAD_REQUEST);
    }

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testUpdateMember_EmptyName() {
        MemberDto updateDto = new MemberDto();
        updateDto.setId(testMemberId);
        updateDto.setName(""); // Empty name
        updateDto.setGivenName("Test");
        
        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .when()
            .put("/v1/member/{id}", testMemberId.toString())
            .then()
            .statusCode(StatusCode.BAD_REQUEST);
    }

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testUpdateMember_InvalidState() {
        MemberDto updateDto = new MemberDto();
        updateDto.setId(testMemberId);
        updateDto.setName("Test");
        updateDto.setGivenName("Test");
        updateDto.setState("invalid-state"); // Not in the allowed pattern
        
        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .when()
            .put("/v1/member/{id}", testMemberId.toString())
            .then()
            .statusCode(StatusCode.BAD_REQUEST);
    }

    // ========== State Change Tests ==========

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testUpdateMember_StateChange() {
        MemberDto updateDto = new MemberDto();
        updateDto.setId(testMemberId);
        updateDto.setName("Test");
        updateDto.setGivenName("Test");
        updateDto.setState("passiv");
        updateDto.setStateEffective(LocalDate.now());
        updateDto.setEmail("test@example.com");
        updateDto.setCity("Berlin");
        updateDto.setStreet("Street");
        updateDto.setZipCode("12345");
        updateDto.setGender("male");
        updateDto.setDayOfBirth(LocalDate.of(1990, 1, 1));
        updateDto.setMobile("+123456789");
        updateDto.setDse(true);
        updateDto.setRabatt(false);
        updateDto.setEmailList(true);
        updateDto.setEntryDate(LocalDate.of(2020, 1, 1));
        updateDto.setDfvNumber(12345);

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .when()
            .put("/v1/member/{id}", testMemberId.toString())
            .then()
            .statusCode(StatusCode.OK);
    }

    // ========== Content-Type Tests ==========

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testGetAllActiveMembers_ContentType() {
        given()
            .when()
            .get("/v1/member/")
            .then()
            .contentType(ContentType.JSON);
    }

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testUpdateMember_ContentType() {
        MemberDto updateDto = new MemberDto();
        updateDto.setId(testMemberId);
        updateDto.setName("Test");
        updateDto.setGivenName("Test");
        updateDto.setEmail("test@example.com");
        updateDto.setCity("Berlin");
        updateDto.setStreet("Street");
        updateDto.setZipCode("12345");
        updateDto.setGender("male");
        updateDto.setDayOfBirth(LocalDate.of(1990, 1, 1));
        updateDto.setMobile("+123456789");
        updateDto.setState("aktiv");
        updateDto.setStateEffective(LocalDate.of(2020, 1, 1));
        updateDto.setDse(true);
        updateDto.setRabatt(false);
        updateDto.setEmailList(true);
        updateDto.setEntryDate(LocalDate.of(2020, 1, 1));
        updateDto.setDfvNumber(12345);

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .when()
            .put("/v1/member/{id}", testMemberId.toString())
            .then()
            .contentType(ContentType.JSON);
    }
}
