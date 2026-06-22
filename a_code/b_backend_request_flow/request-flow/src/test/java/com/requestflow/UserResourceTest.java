package com.requestflow;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class UserResourceTest extends AbstractResourceTest {

    @Test
    void managerCanListUsersWithoutPasswordHash() {
        given()
                .auth().oauth2(managerToken())
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(5))
                .body("[0].passwordHash", nullValue());
    }

    @Test
    void userAndAnalystCannotAccessUsers() {
        given().auth().oauth2(userToken()).when().get("/users").then().statusCode(403);
        given().auth().oauth2(analystToken()).when().get("/users").then().statusCode(403);
    }

    @Test
    void managerCanCreateUserWithInitialPasswordHashFromBirthDate() {
        String email = "new.user." + System.currentTimeMillis() + "@requestflow.com";
        LocalDate birthDate = LocalDate.of(1990, 1, 2);

        given()
                .auth().oauth2(managerToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Novo Usuario",
                        "email", email,
                        "role", "USER",
                        "birthDate", birthDate.toString(),
                        "active", true
                ))
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("email", org.hamcrest.Matchers.equalTo(email))
                .body("passwordHash", nullValue());

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", "02011990"))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", org.hamcrest.Matchers.notNullValue())
                .body("user.passwordHash", nullValue());
    }

    @Test
    void duplicateEmailReturns409() {
        given()
                .auth().oauth2(managerToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Duplicado",
                        "email", "user@requestflow.com",
                        "role", "USER",
                        "birthDate", "1990-01-02",
                        "active", true
                ))
                .when()
                .post("/users")
                .then()
                .statusCode(409);
    }
}
