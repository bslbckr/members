package de.guc.resource;

import de.guc.AdminResource;
import de.guc.Constants;
import de.guc.dto.CancelMembershipDto;
import de.guc.dto.SetDFVNumberDto;
import de.guc.entities.MemberEntity;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for AdminResource
 * Tests administrative endpoints with proper authentication
 */
@QuarkusTest
@TestTransaction
@TestHTTPEndpoint(AdminResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class AdminResourceTest {

    @Inject
    AdminResource adminResource;

    private UUID testMemberId1;
    private UUID testMemberId2;

    @BeforeEach
    public void setUp() {
        // Create test members
        MemberEntity member1 = new MemberEntity();
        member1.name = "Doe";
        member1.givenName = "John";
        member1.entryDate = LocalDate.of(2020, 1, 15);
        member1.dayOfBirth = LocalDate.of(1990, 5, 20);
        member1.gender = "male";
        member1.state = "aktiv";
        member1.stateEffective = LocalDate.of(2020, 1, 15);
        member1.dfvNumber = 12345;
        member1.dse = true;
        member1.rabatt = false;
        member1.street = "Main Street";
        member1.zipCode = "12345";
        member1.city = "Berlin";
        member1.mobile = "+123456789";
        member1.email = "john.doe@example.com";
        member1.emailList = true;
        member1.persist();
        testMemberId1 = member1.id;
        
        MemberEntity member2 = new MemberEntity();
        member2.name = "Smith";
        member2.givenName = "Jane";
        member2.entryDate = LocalDate.of(2020, 2, 20);
        member2.dayOfBirth = LocalDate.of(1992, 3, 10);
        member2.gender = "female";
        member2.state = "aktiv";
        member2.stateEffective = LocalDate.of(2020, 2, 20);
        member2.dfvNumber = 67890;
        member2.dse = false;
        member2.rabatt = true;
        member2.street = "Ocean Road";
        member2.zipCode = "54321";
        member2.city = "Hamburg";
        member2.mobile = "+987654321";
        member2.email = "jane.smith@example.com";
        member2.emailList = false;
        member2.persist();
        testMemberId2 = member2.id;
    }

    // ========== POST /v1/admin/updateDfv Tests ==========

    @Test
    public void testUpdateDfvNumbers_Unauthorized() {
        SetDFVNumberDto dto = new SetDFVNumberDto();
        dto.setUuid(testMemberId1);
        dto.setDfvNumber(99999);

        given()
            .contentType(ContentType.JSON)
            .body(List.of(dto))
            .when()
            .post("/updateDfv")
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "adminUser", roles = "onboarding")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "admin-user-id")
    })
    public void testUpdateDfvNumbers_Success() {
        SetDFVNumberDto dto1 = new SetDFVNumberDto();
        dto1.setUuid(testMemberId1);
        dto1.setDfvNumber(99999);

        SetDFVNumberDto dto2 = new SetDFVNumberDto();
        dto2.setUuid(testMemberId2);
        dto2.setDfvNumber(88888);

        given()
            .contentType(ContentType.JSON)
            .body(List.of(dto1, dto2))
            .when()
            .post("/updateDfv")
            .then()
            .statusCode(StatusCode.OK);
    }

    @Test
    @TestSecurity(user = "adminUser", roles = "onboarding")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "admin-user-id")
    })
    public void testUpdateDfvNumbers_VerifyUpdate() {
        int newDfvNumber = 99999;
        
        SetDFVNumberDto dto = new SetDFVNumberDto();
        dto.setUuid(testMemberId1);
        dto.setDfvNumber(newDfvNumber);

        given()
            .contentType(ContentType.JSON)
            .body(List.of(dto))
            .when()
            .post("/updateDfv")
            .then()
            .statusCode(StatusCode.OK);

        // Verify the DFV number was updated
        MemberEntity updatedMember = MemberEntity.findById(testMemberId1);
        assertThat(updatedMember.dfvNumber, equalTo(newDfvNumber));
    }

    @Test
    @TestSecurity(user = "adminUser", roles = "onboarding")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "admin-user-id")
    })
    public void testUpdateDfvNumbers_NonExistentMember() {
        UUID nonExistentId = UUID.randomUUID();
        
        SetDFVNumberDto dto = new SetDFVNumberDto();
        dto.setUuid(nonExistentId);
        dto.setDfvNumber(99999);

        given()
            .contentType(ContentType.JSON)
            .body(List.of(dto))
            .when()
            .post("/updateDfv")
            .then()
            .statusCode(StatusCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    @TestSecurity(user = "adminUser", roles = "onboarding")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "admin-user-id")
    })
    public void testUpdateDfvNumbers_EmptyList() {
        given()
            .contentType(ContentType.JSON)
            .body(List.of())  // Empty list
            .when()
            .post("/updateDfv")
            .then()
            .statusCode(StatusCode.OK);  // Empty list should be OK
    }

    @Test
    @TestSecurity(user = "adminUser", roles = "onboarding")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "admin-user-id")
    })
    public void testUpdateDfvNumbers_MultipleUpdates() {
        SetDFVNumberDto dto1 = new SetDFVNumberDto();
        dto1.setUuid(testMemberId1);
        dto1.setDfvNumber(11111);

        SetDFVNumberDto dto2 = new SetDFVNumberDto();
        dto2.setUuid(testMemberId2);
        dto2.setDfvNumber(22222);

        given()
            .contentType(ContentType.JSON)
            .body(List.of(dto1, dto2))
            .when()
            .post("/updateDfv")
            .then()
            .statusCode(StatusCode.OK);

        // Verify both DFV numbers were updated
        MemberEntity member1 = MemberEntity.findById(testMemberId1);
        MemberEntity member2 = MemberEntity.findById(testMemberId2);
        
        assertThat(member1.dfvNumber, equalTo(11111));
        assertThat(member2.dfvNumber, equalTo(22222));
    }

    // ========== POST /v1/admin/cancelMembership Tests ==========

    @Test
    public void testCancelMemberships_Unauthorized() {
        LocalDate cancellationDate = LocalDate.of(2024, 12, 31);
        CancelMembershipDto dto = new CancelMembershipDto(cancellationDate, List.of(testMemberId1));

        given()
            .contentType(ContentType.JSON)
            .body(dto)
            .when()
            .post("/cancelMembership")
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user = "adminUser", roles = "onboarding")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "admin-user-id")
    })
    public void testCancelMemberships_Success() {
        LocalDate cancellationDate = LocalDate.of(2024, 12, 31);
        CancelMembershipDto dto = new CancelMembershipDto(cancellationDate, List.of(testMemberId1));

        given()
            .contentType(ContentType.JSON)
            .body(dto)
            .when()
            .post("/cancelMembership")
            .then()
            .statusCode(StatusCode.NO_CONTENT);
    }

    @Test
    @TestSecurity(user = "adminUser", roles = "onboarding")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "admin-user-id")
    })
    public void testCancelMemberships_VerifyCancellation() {
        LocalDate cancellationDate = LocalDate.of(2024, 12, 31);
        CancelMembershipDto dto = new CancelMembershipDto(cancellationDate, List.of(testMemberId1));

        given()
            .contentType(ContentType.JSON)
            .body(dto)
            .when()
            .post("/cancelMembership")
            .then()
            .statusCode(StatusCode.NO_CONTENT);

        // Verify the exit date was set
        MemberEntity member = MemberEntity.findById(testMemberId1);
        assertThat(member.exitDate, equalTo(cancellationDate));
    }

    @Test
    @TestSecurity(user = "adminUser", roles = "onboarding")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "admin-user-id")
    })
    public void testCancelMemberships_MultipleMembers() {
        LocalDate cancellationDate = LocalDate.of(2024, 12, 31);
        CancelMembershipDto dto = new CancelMembershipDto(
            cancellationDate, 
            List.of(testMemberId1, testMemberId2)
        );

        given()
            .contentType(ContentType.JSON)
            .body(dto)
            .when()
            .post("/cancelMembership")
            .then()
            .statusCode(StatusCode.NO_CONTENT);

        // Verify both members have exit dates set
        MemberEntity member1 = MemberEntity.findById(testMemberId1);
        MemberEntity member2 = MemberEntity.findById(testMemberId2);
        
        assertThat(member1.exitDate, equalTo(cancellationDate));
        assertThat(member2.exitDate, equalTo(cancellationDate));
    }

    @Test
    @TestSecurity(user = "adminUser", roles = "onboarding")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "admin-user-id")
    })
    public void testCancelMemberships_NonExistentMember() {
        LocalDate cancellationDate = LocalDate.of(2024, 12, 31);
        UUID nonExistentId = UUID.randomUUID();
        
        CancelMembershipDto dto = new CancelMembershipDto(
            cancellationDate, 
            List.of(nonExistentId)
        );

        // This should still succeed (non-existent members are silently ignored)
        given()
            .contentType(ContentType.JSON)
            .body(dto)
            .when()
            .post("/cancelMembership")
            .then()
            .statusCode(StatusCode.NO_CONTENT);
    }

    @Test
    @TestSecurity(user = "adminUser", roles = "onboarding")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "admin-user-id")
    })
    public void testCancelMemberships_MixedMembers() {
        LocalDate cancellationDate = LocalDate.of(2024, 12, 31);
        UUID nonExistentId = UUID.randomUUID();
        
        CancelMembershipDto dto = new CancelMembershipDto(
            cancellationDate, 
            List.of(testMemberId1, nonExistentId, testMemberId2)
        );

        given()
            .contentType(ContentType.JSON)
            .body(dto)
            .when()
            .post("/cancelMembership")
            .then()
            .statusCode(StatusCode.NO_CONTENT);

        // Verify existing members were cancelled
        MemberEntity member1 = MemberEntity.findById(testMemberId1);
        MemberEntity member2 = MemberEntity.findById(testMemberId2);
        
        assertThat(member1.exitDate, equalTo(cancellationDate));
        assertThat(member2.exitDate, equalTo(cancellationDate));
    }

    // ========== Wrong Role Tests ==========

    @Test
    @TestSecurity(user = "regularUser", roles = "member")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "regular-user-id")
    })
    public void testUpdateDfvNumbers_WrongRole() {
        SetDFVNumberDto dto = new SetDFVNumberDto();
        dto.setUuid(testMemberId1);
        dto.setDfvNumber(99999);

        given()
            .contentType(ContentType.JSON)
            .body(List.of(dto))
            .when()
            .post("/updateDfv")
            .then()
            .statusCode(StatusCode.FORBIDDEN);
    }

    @Test
    @TestSecurity(user = "regularUser", roles = "member")
    @OidcSecurity(claims = {
        @Claim(key = "sub", value = "regular-user-id")
    })
    public void testCancelMemberships_WrongRole() {
        LocalDate cancellationDate = LocalDate.of(2024, 12, 31);
        CancelMembershipDto dto = new CancelMembershipDto(cancellationDate, List.of(testMemberId1));

        given()
            .contentType(ContentType.JSON)
            .body(dto)
            .when()
            .post("/cancelMembership")
            .then()
            .statusCode(StatusCode.FORBIDDEN);
    }
}
