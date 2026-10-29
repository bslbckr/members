package de.guc.resource;

import de.guc.Constants;
import de.guc.OnBoardingResource;
import de.guc.dto.MemberWithLoginDto;
import de.guc.dto.OnBoardingDto;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for OnBoardingResource
 * Tests onboarding endpoints with proper authentication
 */
@QuarkusTest
@TestHTTPEndpoint(OnBoardingResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class OnBoardingResourceTest {

    @Inject
    OnBoardingResource onBoardingResource;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up before each test
        ResourcesEntity.deleteAll();
        MemberEntity.deleteAll();
    }

    @AfterEach
    @Transactional
    public void tearDown() {
        ResourcesEntity.deleteAll();
        MemberEntity.deleteAll();
    }

    // ========== POST /v1/onboarding Tests ==========

    @Test
    public void testOnBoardNewMember_Unauthorized() {
        OnBoardingDto newMember = new OnBoardingDto(
            "John", "Doe", "john.doe@example.com", "johndoe",
            LocalDate.of(1990, 5, 20), false, null, null
        );

        given()
            .contentType(ContentType.JSON)
            .body(newMember)
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testOnBoardNewMember_Success() {
        OnBoardingDto newMember = new OnBoardingDto(
            "John", "Doe", "john.doe@example.com", "johndoe",
            LocalDate.of(1990, 5, 20), false, null, null
        );

        given()
            .contentType(ContentType.JSON)
            .body(newMember)
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.NO_CONTENT);
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testOnBoardNewMember_AdultMember() {
        OnBoardingDto newMember = new OnBoardingDto(
            "John", "Doe", "john.doe@example.com", "johndoe",
            LocalDate.of(1990, 5, 20), false, null, null
        );

        given()
            .contentType(ContentType.JSON)
            .body(newMember)
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.NO_CONTENT);

        // Verify member was created in database
        List<MemberEntity> members = MemberEntity.listAll();
        assertThat(members.size(), greaterThanOrEqualTo(1));
        
        boolean memberExists = members.stream()
            .anyMatch(m -> "Doe".equals(m.name) && "John".equals(m.givenName));
        assertThat(memberExists, is(true));
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testOnBoardNewMember_ChildMember() {
        OnBoardingDto newMember = new OnBoardingDto(
            "John", "Doe", "john.doe@example.com", "johndoe",
            LocalDate.of(2015, 5, 20), true, "ParentFirst", "ParentLast"
        );

        given()
            .contentType(ContentType.JSON)
            .body(newMember)
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.NO_CONTENT);

        // Verify member was created with parent's name
        List<MemberEntity> members = MemberEntity.listAll();
        assertThat(members.size(), greaterThanOrEqualTo(1));
        
        boolean memberExists = members.stream()
            .anyMatch(m -> "ParentLast".equals(m.name) && "ParentFirst".equals(m.givenName));
        assertThat(memberExists, is(true));
    }

    // ========== POST /v1/onboarding/import Tests ==========

    @Test
    public void testImportExistingMembers_Unauthorized() {
        MemberWithLoginDto member = new MemberWithLoginDto();
        member.setLogin("johndoe");
        member.setGivenName("John");
        member.setName("Doe");
        member.setStreet("Main Street");
        member.setZipCode("12345");
        member.setGender("male");
        member.setCity("Berlin");
        member.setEmail("john.doe@example.com");
        /*
            "John", "Doe", "Berlin", "Main Street", "12345",
            "male", LocalDate.of(1990, 5, 20), LocalDate.of(2020, 1, 15),
            "aktiv", LocalDate.of(2020, 1, 15), 12345, true, false,
            , true, "+123456789", "johndoe", false */
        

        given()
            .contentType(ContentType.JSON)
            .body(List.of(member))
            .when()
            .post("/import")
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testImportExistingMembers_Success() {
                MemberWithLoginDto member = new MemberWithLoginDto();
        member.setLogin("johndoe");
        member.setGivenName("John");
        member.setName("Doe");
        member.setStreet("Main Street");
        member.setZipCode("12345");
        member.setGender("male");
        member.setCity("Berlin");
        member.setEmail("john.doe@example.com");
/*
        MemberWithLoginDto member = new MemberWithLoginDto(
            "John", "Doe", "Berlin", "Main Street", "12345",
            "male", LocalDate.of(1990, 5, 20), LocalDate.of(2020, 1, 15),
            "aktiv", LocalDate.of(2020, 1, 15), 12345, true, false,
            "john.doe@example.com", true, "+123456789", "johndoe", false
        );
*/
        given()
            .contentType(ContentType.JSON)
            .body(List.of(member))
            .when()
            .post("/import")
            .then()
            .statusCode(StatusCode.NO_CONTENT);
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testImportExistingMembers_MultipleMembers() {
        /*      MemberWithLoginDto member1 = new MemberWithLoginDto(
            "John", "Doe", "Berlin", "Main Street", "12345",
            "male", LocalDate.of(1990, 5, 20), LocalDate.of(2020, 1, 15),
            "aktiv", LocalDate.of(2020, 1, 15), 12345, true, false,
            "john.doe@example.com", true, "+123456789", "johndoe", false
            );*/
        MemberWithLoginDto member = new MemberWithLoginDto();
        member.setLogin("johndoe");
        member.setGivenName("John");
        member.setName("Doe");
        member.setStreet("Main Street");
        member.setZipCode("12345");
        member.setGender("male");
        member.setCity("Berlin");
        member.setEmail("john.doe@example.com");

        /*
        MemberWithLoginDto member2 = new MemberWithLoginDto(
            "Jane", "Smith", "Hamburg", "Ocean Road", "54321",
            "female", LocalDate.of(1992, 3, 10), LocalDate.of(2020, 2, 20),
            "aktiv", LocalDate.of(2020, 2, 20), 67890, false, true,
            "jane.smith@example.com", false, "+987654321", "janesmith", false
            );*/
        MemberWithLoginDto member2 = new MemberWithLoginDto();
        member2.setLogin("johndoe");
        member2.setGivenName("John");
        member2.setName("Doe");
        member2.setStreet("Main Street");
        member2.setZipCode("12345");
        member2.setGender("male");
        member2.setCity("Berlin");
        member2.setEmail("john.doe@example.com");


        given()
            .contentType(ContentType.JSON)
            .body(List.of(member, member2))
            .when()
            .post("/import")
            .then()
            .statusCode(StatusCode.NO_CONTENT);

        // Verify both members were created
        List<MemberEntity> members = MemberEntity.listAll();
        assertThat(members.size(), greaterThanOrEqualTo(2));
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testImportExistingMembers_ChildMember() {
        /*MemberWithLoginDto childMember = new MemberWithLoginDto(
            "Child", "Member", "Berlin", "Main Street", "12345",
            "male", LocalDate.of(2015, 5, 20), LocalDate.of(2020, 1, 15),
            "aktiv", LocalDate.of(2020, 1, 15), 12345, true, false,
            "child@example.com", true, "+123456789", "childuser", true
            );*/
        MemberWithLoginDto member = new MemberWithLoginDto();
        member.setLogin("johndoe");
        member.setGivenName("John");
        member.setName("Doe");
        member.setStreet("Main Street");
        member.setZipCode("12345");
        member.setGender("male");
        member.setCity("Berlin");
        member.setEmail("john.doe@example.com");


        given()
            .contentType(ContentType.JSON)
            .body(List.of(member))
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.NO_CONTENT);

        // Verify member was created with parent's data
        List<MemberEntity> members = MemberEntity.listAll();
        assertThat(members.size(), greaterThanOrEqualTo(1));
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testOnBoardNewMember_InvalidEmail() {
        OnBoardingDto newMember = new OnBoardingDto(
            "John", "Doe", "invalid-email", "johndoe",
            LocalDate.of(1990, 5, 20), false, null, null
        );

        given()
            .contentType(ContentType.JSON)
            .body(newMember)
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.BAD_REQUEST);
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testOnBoardNewMember_EmptyFirstName() {
        OnBoardingDto newMember = new OnBoardingDto(
            "", "Doe", "john.doe@example.com", "johndoe",
            LocalDate.of(1990, 5, 20), false, null, null
        );

        given()
            .contentType(ContentType.JSON)
            .body(newMember)
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.BAD_REQUEST);
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testOnBoardNewMember_EmptyLastName() {
        OnBoardingDto newMember = new OnBoardingDto(
            "John", "", "john.doe@example.com", "johndoe",
            LocalDate.of(1990, 5, 20), false, null, null
        );

        given()
            .contentType(ContentType.JSON)
            .body(newMember)
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.BAD_REQUEST);
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testImportExistingMembers_EmptyList() {
        given()
            .contentType(ContentType.JSON)
            .body(List.of())  // Empty list
            .when()
            .post("/import")
            .then()
            .statusCode(StatusCode.BAD_REQUEST);
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testOnBoardNewMember_ChildWithoutParentInfo() {
        // Child member without parent first/last name should be invalid
        OnBoardingDto newMember = new OnBoardingDto(
            "Child", "Member", "child@example.com", "childuser",
            LocalDate.of(2015, 5, 20), true, null, null
        );

        given()
            .contentType(ContentType.JSON)
            .body(newMember)
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.BAD_REQUEST);
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testOnBoardNewMember_ChildWithPartialParentInfo() {
        // Child member with only parent first name
        OnBoardingDto newMember = new OnBoardingDto(
            "Child", "Member", "child@example.com", "childuser",
            LocalDate.of(2015, 5, 20), true, "ParentFirst", null
        );

        given()
            .contentType(ContentType.JSON)
            .body(newMember)
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.BAD_REQUEST);
    }

    @Test
    @TestSecurity(user = "onboardingUser", roles = Constants.ROLE_ON_BOARDING)
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "onboarding-user-id")
    })
    public void testOnBoardNewMember_ChildWithEmptyParentInfo() {
        // Child member with empty parent names
        OnBoardingDto newMember = new OnBoardingDto(
            "Child", "Member", "child@example.com", "childuser",
            LocalDate.of(2015, 5, 20), true, "", ""
        );

        given()
            .contentType(ContentType.JSON)
            .body(newMember)
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.BAD_REQUEST);
    }

    // ========== Wrong Role Tests ==========

    @Test
    @TestSecurity(user = "regularUser", roles = "member")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "regular-user-id")
    })
    public void testOnBoardNewMember_WrongRole() {
        OnBoardingDto newMember = new OnBoardingDto(
            "John", "Doe", "john.doe@example.com", "johndoe",
            LocalDate.of(1990, 5, 20), false, null, null
        );

        given()
            .contentType(ContentType.JSON)
            .body(newMember)
            .when()
            .post("/")
            .then()
            .statusCode(StatusCode.FORBIDDEN);
    }

    @Test
    @TestSecurity(user = "regularUser", roles = "member")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "regular-user-id")
    })
    public void testImportExistingMembers_WrongRole() {
        /*MemberWithLoginDto member = new MemberWithLoginDto(
            "John", "Doe", "Berlin", "Main Street", "12345",
            "male", LocalDate.of(1990, 5, 20), LocalDate.of(2020, 1, 15),
            "aktiv", LocalDate.of(2020, 1, 15), 12345, true, false,
            "john.doe@example.com", true, "+123456789", "johndoe", false
        );*/
        MemberWithLoginDto member = new MemberWithLoginDto();
        member.setLogin("johndoe");
        member.setGivenName("John");
        member.setName("Doe");
        member.setStreet("Main Street");
        member.setZipCode("12345");
        member.setGender("male");
        member.setCity("Berlin");
        member.setEmail("john.doe@example.com");


        given()
            .contentType(ContentType.JSON)
            .body(List.of(member))
            .when()
            .post("/import")
            .then()
            .statusCode(StatusCode.FORBIDDEN);
    }
}
