package com.requestflow;

import io.restassured.http.ContentType;

import java.util.Map;

import static io.restassured.RestAssured.given;

public abstract class AbstractResourceTest {

    protected String login(String email, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    protected String userToken() {
        return login("user@requestflow.com", "123456");
    }

    protected String analystToken() {
        return login("analyst@requestflow.com", "123456");
    }

    protected String managerToken() {
        return login("manager@requestflow.com", "123456");
    }

    protected Integer userIdByEmail(String token, String email) {
        return given()
                .auth().oauth2(token)
                .queryParam("size", 100)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getInt("find { it.email == '" + email + "' }.id");
    }

    protected Integer requestIdByTitle(String token, String title) {
        return given()
                .auth().oauth2(token)
                .queryParam("size", 100)
                .when()
                .get("/requests")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getInt("content.find { it.title == '" + title + "' }.id");
    }
}
