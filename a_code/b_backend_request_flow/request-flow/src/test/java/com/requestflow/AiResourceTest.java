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
class AiResourceTest extends AbstractResourceTest {

    @Test
    void aiSuggestionRequiresAuthentication() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("description", "Preciso de acesso ao sistema"))
                .when()
                .post("/requests/ai-suggestion")
                .then()
                .statusCode(401);
    }

    @Test
    void fallbackSuggestionReturnsCategoryPrioritySummaryAndSource() {
        given()
                .auth().oauth2(userToken())
                .contentType(ContentType.JSON)
                .body(Map.of("description", "Tenho uma falha grave no login do sistema"))
                .when()
                .post("/requests/ai-suggestion")
                .then()
                .statusCode(200)
                .body("category", equalTo("ACCESS"))
                .body("priority", equalTo("HIGH"))
                .body("summary", notNullValue())
                .body("source", equalTo("FALLBACK"))
                .body("password", nullValue())
                .body("token", nullValue());
    }

    @Test
    void incidentTermsReturnCriticalIncidentFallback() {
        given()
                .auth().oauth2(userToken())
                .contentType(ContentType.JSON)
                .body(Map.of("description", "Ambiente de producao indisponivel e operacao parada"))
                .when()
                .post("/requests/ai-suggestion")
                .then()
                .statusCode(200)
                .body("category", equalTo("INCIDENT"))
                .body("priority", equalTo("CRITICAL"))
                .body("source", equalTo("FALLBACK"));
    }
}
