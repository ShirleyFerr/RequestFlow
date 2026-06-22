package com.requestflow;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class RequestResourceTest extends AbstractResourceTest {

    @Test
    void listRequestsRespectsUserVisibilityAndPagination() {
        String token = userToken();
        given()
                .auth().oauth2(token)
                .queryParam("requesterId", 999999)
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get("/requests")
                .then()
                .statusCode(200)
                .body("content.findAll { it.requester.email != 'user@requestflow.com' }.size()", equalTo(0))
                .body("page", equalTo(0))
                .body("size", equalTo(5))
                .body("totalElements", greaterThan(0))
                .body("totalPages", greaterThanOrEqualTo(1));
    }

    @Test
    void analystSeesOnlyUnassignedOrAssignedToSelf() {
        given()
                .auth().oauth2(analystToken())
                .when()
                .get("/requests")
                .then()
                .statusCode(200)
                .body("content.title", not(hasItem("RF-SEED-008 - Falha intermitente no VPN")));
    }

    @Test
    void managerSeesAllAndCanFilter() {
        String token = managerToken();
        given()
                .auth().oauth2(token)
                .queryParam("status", "CANCELLED")
                .when()
                .get("/requests")
                .then()
                .statusCode(200)
                .body("content.status", hasItem("CANCELLED"));

        Integer aliceId = userIdByEmail(token, "alice@requestflow.com");
        given()
                .auth().oauth2(token)
                .queryParam("assigneeId", aliceId)
                .when()
                .get("/requests")
                .then()
                .statusCode(200)
                .body("content.assignee.email", hasItem("alice@requestflow.com"));
    }

    @Test
    void userCanCreateRequestWithOpenStatusAndInitialHistory() {
        given()
                .auth().oauth2(userToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "Teste automatizado de criacao " + System.currentTimeMillis(),
                        "description", "Solicitacao criada por teste automatizado",
                        "category", "SUPPORT",
                        "priority", "MEDIUM",
                        "dueDate", LocalDateTime.now().plusDays(2).toString(),
                        "aiSummary", "Resumo do teste"
                ))
                .when()
                .post("/requests")
                .then()
                .statusCode(201)
                .body("status", equalTo("OPEN"))
                .body("requester.email", equalTo("user@requestflow.com"))
                .body("assignee", nullValue())
                .body("dueDate", org.hamcrest.Matchers.notNullValue())
                .body("statusHistory.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void analystAndManagerCannotCreateRequest() {
        Map<String, Object> body = Map.of(
                "title", "Criacao proibida",
                "description", "Tentativa de criacao por perfil sem permissao",
                "category", "SUPPORT",
                "priority", "MEDIUM",
                "dueDate", LocalDateTime.now().plusDays(1).toString()
        );

        given().auth().oauth2(analystToken()).contentType(ContentType.JSON).body(body)
                .when().post("/requests").then().statusCode(403);

        given().auth().oauth2(managerToken()).contentType(ContentType.JSON).body(body)
                .when().post("/requests").then().statusCode(403);
    }

    @Test
    void dueDateIsRequiredOnCreate() {
        given()
                .auth().oauth2(userToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "Sem vencimento",
                        "description", "Solicitacao invalida",
                        "category", "SUPPORT",
                        "priority", "MEDIUM"
                ))
                .when()
                .post("/requests")
                .then()
                .statusCode(400);
    }

    @Test
    void managerCanReassignToActiveAnalystAndHistoryIsRegistered() {
        String token = managerToken();
        Integer requestId = requestIdByTitle(token, "RF-SEED-001 - Acesso ao sistema financeiro");
        Integer aliceId = userIdByEmail(token, "alice@requestflow.com");

        given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of("assigneeId", aliceId, "note", "Teste de reatribuicao"))
                .when()
                .put("/requests/" + requestId + "/reassign")
                .then()
                .statusCode(200)
                .body("assignee.email", equalTo("alice@requestflow.com"))
                .body("statusHistory.note", hasItem(org.hamcrest.Matchers.containsString("Alice Moura")));
    }

    @Test
    void reassignValidatesRoleActiveAndCancelledRequest() {
        String token = managerToken();
        Integer requestId = requestIdByTitle(token, "RF-SEED-003 - Aguardando informacoes do solicitante");
        Integer userId = userIdByEmail(token, "user@requestflow.com");

        given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of("assigneeId", userId))
                .when()
                .put("/requests/" + requestId + "/reassign")
                .then()
                .statusCode(400);

        Integer inactiveAnalystId = createInactiveAnalyst(token);
        given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of("assigneeId", inactiveAnalystId))
                .when()
                .put("/requests/" + requestId + "/reassign")
                .then()
                .statusCode(400);

        Integer cancelledId = requestIdByTitle(token, "RF-SEED-006 - Solicitacao cancelada");
        Integer analystId = userIdByEmail(token, "analyst@requestflow.com");

        given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of("assigneeId", analystId))
                .when()
                .put("/requests/" + cancelledId + "/reassign")
                .then()
                .statusCode(400);
    }

    @Test
    void userAndAnalystCannotReassign() {
        String token = managerToken();
        Integer requestId = requestIdByTitle(token, "RF-SEED-003 - Aguardando informacoes do solicitante");
        Integer analystId = userIdByEmail(token, "analyst@requestflow.com");

        given().auth().oauth2(userToken()).contentType(ContentType.JSON).body(Map.of("assigneeId", analystId))
                .when().put("/requests/" + requestId + "/reassign").then().statusCode(403);

        given().auth().oauth2(analystToken()).contentType(ContentType.JSON).body(Map.of("assigneeId", analystId))
                .when().put("/requests/" + requestId + "/reassign").then().statusCode(403);
    }

    private Integer createInactiveAnalyst(String token) {
        String email = "inactive.analyst." + System.currentTimeMillis() + "@requestflow.com";
        return given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "Inactive Analyst",
                        "email", email,
                        "role", "ANALYST",
                        "birthDate", "1990-01-02",
                        "active", false
                ))
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }
}
