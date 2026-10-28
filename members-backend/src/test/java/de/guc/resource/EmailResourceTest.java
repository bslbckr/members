package de.guc.resource;

import de.guc.Constants;
import de.guc.EmailResource;
import de.guc.dto.EmailDto;
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
import org.jboss.resteasy.reactive.RestResponse.StatusCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.transaction.Transactional;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for EmailResource
 * Tests email sending endpoints with proper authentication
 */
@QuarkusTest
@TestHTTPEndpoint(EmailResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class EmailResourceTest {

    @Inject
    EmailResource emailResource;

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

    // ========== POST /v1/email Tests ==========

    @Test
    public void testSendEmail_Unauthorized() {
        EmailDto emailDto = new EmailDto();
        emailDto.setSubject("Test Subject");
        emailDto.setBody("Test Body");

        given()
            .contentType(ContentType.JSON)
            .body(emailDto)
            .when()
            .post("/v1/email")
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testSendEmail_Success() {
        EmailDto emailDto = new EmailDto();
        emailDto.setSubject("Test Subject");
        emailDto.setBody("Test Body Content");

        given()
            .contentType(ContentType.JSON)
            .body(emailDto)
            .when()
            .post("/v1/email")
            .then()
            .statusCode(StatusCode.NO_CONTENT);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testSendEmail_WithEmptySubject() {
        EmailDto emailDto = new EmailDto();
        emailDto.setSubject("");
        emailDto.setBody("Test Body");

        given()
            .contentType(ContentType.JSON)
            .body(emailDto)
            .when()
            .post("/v1/email")
            .then()
            .statusCode(StatusCode.NO_CONTENT);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testSendEmail_WithEmptyBody() {
        EmailDto emailDto = new EmailDto();
        emailDto.setSubject("Test Subject");
        emailDto.setBody("");

        given()
            .contentType(ContentType.JSON)
            .body(emailDto)
            .when()
            .post("/v1/email")
            .then()
            .statusCode(StatusCode.NO_CONTENT);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testSendEmail_WithSpecialCharacters() {
        EmailDto emailDto = new EmailDto();
        emailDto.setSubject("Test with \u00e4 umlaut");
        emailDto.setBody("Body with special chars: \n\t\"quotes\"");

        given()
            .contentType(ContentType.JSON)
            .body(emailDto)
            .when()
            .post("/v1/email")
            .then()
            .statusCode(StatusCode.NO_CONTENT);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testSendEmail_WithLongContent() {
        EmailDto emailDto = new EmailDto();
        
        StringBuilder longSubject = new StringBuilder();
        StringBuilder longBody = new StringBuilder();
        
        for (int i = 0; i < 500; i++) {
            longSubject.append("a");
            longBody.append("b");
        }
        
        emailDto.setSubject(longSubject.toString());
        emailDto.setBody(longBody.toString());

        given()
            .contentType(ContentType.JSON)
            .body(emailDto)
            .when()
            .post("/v1/email")
            .then()
            .statusCode(StatusCode.NO_CONTENT);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testSendEmail_WithNullSubject() {
        EmailDto emailDto = new EmailDto();
        emailDto.setSubject(null);
        emailDto.setBody("Test Body");

        given()
            .contentType(ContentType.JSON)
            .body(emailDto)
            .when()
            .post("/v1/email")
            .then()
            .statusCode(StatusCode.NO_CONTENT);
    }

    @Test
    @TestSecurity(user = "boardUser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "board-user-id")
    })
    public void testSendEmail_WithNullBody() {
        EmailDto emailDto = new EmailDto();
        emailDto.setSubject("Test Subject");
        emailDto.setBody(null);

        given()
            .contentType(ContentType.JSON)
            .body(emailDto)
            .when()
            .post("/v1/email")
            .then()
            .statusCode(StatusCode.NO_CONTENT);
    }

    // ========== Wrong Role Tests ==========

    @Test
    @TestSecurity(user = "regularUser", roles = "member")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "regular-user-id")
    })
    public void testSendEmail_WrongRole() {
        EmailDto emailDto = new EmailDto();
        emailDto.setSubject("Test Subject");
        emailDto.setBody("Test Body");

        given()
            .contentType(ContentType.JSON)
            .body(emailDto)
            .when()
            .post("/v1/email")
            .then()
            .statusCode(StatusCode.FORBIDDEN);
    }

    // ========== Missing Role Tests ==========

    @Test
    @TestSecurity(user = "userNoRoles", roles = {})
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "no-roles-user-id")
    })
    public void testSendEmail_MissingRole() {
        EmailDto emailDto = new EmailDto();
        emailDto.setSubject("Test Subject");
        emailDto.setBody("Test Body");

        given()
            .contentType(ContentType.JSON)
            .body(emailDto)
            .when()
            .post("/v1/email")
            .then()
            .statusCode(StatusCode.FORBIDDEN);
    }
}
