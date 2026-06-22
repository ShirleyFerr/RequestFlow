package com.requestflow;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class AuthResourceTest extends AbstractResourceTest {

    @Test
    void loginValidReturnsJwtWithoutPasswordHash() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "manager@requestflow.com", "password", "123456"))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("tokenType", notNullValue())
                .body("user.email", notNullValue())
                .body("user.passwordHash", nullValue());
    }

    @Test
    void loginInvalidPasswordReturns401() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "manager@requestflow.com", "password", "wrong-password"))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void loginUnknownUserReturns401() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "missing@requestflow.com", "password", "123456"))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void inactiveUserCannotLogin() {
        String email = "inactive." + System.currentTimeMillis() + "@requestflow.com";
        given()
                .auth().oauth2(managerToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Inactive User",
                        "email", email,
                        "role", "USER",
                        "birthDate", "1990-01-02",
                        "active", false
                ))
                .when()
                .post("/users")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", "02011990"))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(403);
    }

    @Test
    void meReturnsAuthenticatedUserWithoutPasswordHash() {
        given()
                .auth().oauth2(managerToken())
                .when()
                .get("/auth/me")
                .then()
                .statusCode(200)
                .body("email", notNullValue())
                .body("passwordHash", nullValue());
    }
}
