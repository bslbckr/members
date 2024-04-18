package de.guc;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

import org.hamcrest.Matchers;
import org.jboss.resteasy.reactive.RestResponse.StatusCode;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(ConfigurationResource.class)
public class ConfigurationResourceTest {
    
    @Test
    public void testGetConfiguration_unauthorized() {
        given().when().get("")
            .then()
            .statusCode(StatusCode.UNAUTHORIZED);
    }

    @Test
    @TestSecurity(user="alice", roles="guc-members")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value="aaaaaaa-bbbb-ccccc-ddddd-eee")
        })
    public void testGetConfiguration() {
        given().when().get("")
            .then()
            .statusCode(StatusCode.OK)
            .and().contentType(ContentType.JSON)
            .and().body(Matchers.arrayWithSize(0));
    }
}
