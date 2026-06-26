package com.requestflow;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class ProfileResourceTest extends AbstractResourceTest {

    @Test
    void authenticatedUserCanAccessOwnProfileWithoutPasswordHash() {
        given()
                .auth().oauth2(userToken())
                .when()
                .get("/profile")
                .then()
                .statusCode(200)
                .body("email", equalTo("user@requestflow.com"))
                .body("birthDate", notNullValue())
                .body("password", nullValue())
                .body("passwordHash", nullValue());
    }

    @Test
    void profileRequiresAuthentication() {
        given()
                .when()
                .get("/profile")
                .then()
                .statusCode(401);
    }

    @Test
    void changePasswordRejectsDifferentConfirmation() {
        given()
                .auth().oauth2(userToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "newPassword", "novaSenha123",
                        "confirmPassword", "senhaDiferente123"
                ))
                .when()
                .put("/profile/password")
                .then()
                .statusCode(400);
    }

    @Test
    void changePasswordRejectsShortPassword() {
        given()
                .auth().oauth2(userToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "newPassword", "123",
                        "confirmPassword", "123"
                ))
                .when()
                .put("/profile/password")
                .then()
                .statusCode(400);
    }

    @Test
    void userCanChangeOwnPasswordAndLoginWithNewPassword() {
        String email = "profile.user." + System.currentTimeMillis() + "@requestflow.com";
        String initialPassword = "02011990";
        String newPassword = "novaSenha456";

        given()
                .auth().oauth2(managerToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Usuario Perfil",
                        "email", email,
                        "role", "USER",
                        "birthDate", "1990-01-02",
                        "active", true
                ))
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("passwordHash", nullValue());

        String token = login(email, initialPassword);

        given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "newPassword", newPassword,
                        "confirmPassword", newPassword
                ))
                .when()
                .put("/profile/password")
                .then()
                .statusCode(200)
                .body("email", equalTo(email))
                .body("passwordHash", nullValue());

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", initialPassword))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", newPassword))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("user.passwordHash", nullValue());
    }
}
