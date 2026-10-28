package de.guc.resource;

import de.guc.Constants;
import de.guc.ConfigurationResource;
import de.guc.dto.ResourceDto;
import de.guc.entities.MemberEntity;
import de.guc.entities.ResourcesEntity;
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
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for ConfigurationResource
 * Tests resource access configuration endpoints with proper authentication
 */
@QuarkusTest
@TestHTTPEndpoint(ConfigurationResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class ConfigurationResourceTest {

    @Inject
    ConfigurationResource configurationResource;

    private UUID testMemberId;
    private UUID testIdentityId;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up before each test
        ResourcesEntity.deleteAll();
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
        
        // Create a resource access entry
        ResourcesEntity access = new ResourcesEntity();
        access.identity = testIdentityId;
        access.member = member;
        access.role = "SELF";
        access.name = "John";
        access.rType = ResourcesEntity.ResourceType.MEMBER;
        access.persist();
    }

    @AfterEach
    @Transactional
    public void tearDown() {
        ResourcesEntity.deleteAll();
        MemberEntity.deleteAll();
    }

    // ========== GET /v1/configuration Tests ==========

    @Test
    public void testGetAccessibleResource_Unauthorized() {
        given()
            .when()
            .get("/v1/configuration")
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testGetAccessibleResource_Success() {
        given()
            .when()
            .get("/v1/configuration")
            .then()
            .statusCode(StatusCode.OK)
            .contentType(ContentType.JSON);
    }

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testGetAccessibleResource_ReturnsList() {
        given()
            .when()
            .get("/v1/configuration")
            .then()
            .statusCode(StatusCode.OK)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testGetAccessibleResource_ContainsMemberInfo() {
        given()
            .when()
            .get("/v1/configuration")
            .then()
            .statusCode(StatusCode.OK)
            .body(containsString("Doe"))
            .body(containsString("John"));
    }
    /*
    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testGetAccessibleResource_ReturnsResourceDto() {
        given()
            .when()
            .get("/v1/configuration")
            .then()
            .statusCode(StatusCode.OK)
            .body(everyItem(
                allOf(
                    containsPath("name"),
                    containsPath("id"),
                    containsPath("role")
                )
            ));
    }*/

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "non-existent-identity-id")
    })
    public void testGetAccessibleResource_NoResources() {
        given()
            .when()
            .get("/v1/configuration")
            .then()
            .statusCode(StatusCode.OK)
            .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "testuser", roles = Constants.ROLE_BOARD_MEMBER)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testGetAccessibleResource_MultipleResources() {
        // Create another member and resource
        MemberEntity member2 = new MemberEntity();
        member2.name = "Smith";
        member2.givenName = "Jane";
        member2.persist();
        
        ResourcesEntity access2 = new ResourcesEntity();
        access2.identity = testIdentityId;
        access2.member = member2;
        access2.role = "PARENT";
        access2.name = "Jane";
        access2.rType = ResourcesEntity.ResourceType.MEMBER;
        access2.persist();

        given()
            .when()
            .get("/v1/configuration")
            .then()
            .statusCode(StatusCode.OK)
            .body("size()", greaterThanOrEqualTo(2));
    }

    // ========== Wrong Role Tests ==========

    @Test
    @TestSecurity(user = "regularUser", roles = "member")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "df8ea850-52a3-4856-aea0-79361d48ff8c")
    })
    public void testGetAccessibleResource_WrongRole() {
        given()
            .when()
            .get("/v1/configuration")
            .then()
            .statusCode(StatusCode.FORBIDDEN);
    }

    // ========== Direct ResourceDto Tests ==========

    @Test
    public void testResourceDto_Creation() {
        ResourceDto dto = new ResourceDto("John Doe", "123", "SELF");
        
        assertThat(dto, notNullValue());
        assertThat("John Doe", equalTo(dto.getName()));
        assertThat(123, equalTo(dto.getMemberId()));
        assertThat("SELF", equalTo(dto.getOwner()));
    }

    @Test
    public void testResourceDto_Equality() {
        ResourceDto dto1 = new ResourceDto("John Doe", "123", "SELF");
        ResourceDto dto2 = new ResourceDto("John Doe", "123", "SELF");
        
        assertThat(dto1, equalTo(dto2));
        assertThat(dto1.hashCode(), equalTo(dto2.hashCode()));
    }

    @Test
    public void testResourceDto_Inequality() {
        ResourceDto dto1 = new ResourceDto("John Doe", "123", "SELF");
        ResourceDto dto2 = new ResourceDto("Jane Smith", "456", "PARENT");
        
        assertThat(dto1, not(equalTo(dto2)));
    }

    @Test
    public void testResourceDto_ToString() {
        ResourceDto dto = new ResourceDto("John Doe", "123", "SELF");
        
        String result = dto.toString();
        assertThat(result, notNullValue());
        assertThat(result.contains("ResourceDto"), is(true));
    }
}
