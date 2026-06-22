package com.requestflow;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class DashboardResourceTest extends AbstractResourceTest {

    @Test
    void userCannotAccessDashboard() {
        given()
                .auth().oauth2(userToken())
                .when()
                .get("/dashboard")
                .then()
                .statusCode(403);
    }

    @Test
    void analystReceivesOwnOperationalDashboard() {
        given()
                .auth().oauth2(analystToken())
                .when()
                .get("/dashboard/analyst")
                .then()
                .statusCode(200)
                .body("summary.totalRequests", greaterThanOrEqualTo(1))
                .body("assignedToMe", greaterThanOrEqualTo(1))
                .body("workQueue", notNullValue())
                .body("slaAlerts", notNullValue());
    }

    @Test
    void managerReceivesGlobalDashboard() {
        given()
                .auth().oauth2(managerToken())
                .when()
                .get("/dashboard/manager")
                .then()
                .statusCode(200)
                .body("summary.totalRequests", greaterThanOrEqualTo(8))
                .body("byStatus.OPEN", greaterThanOrEqualTo(1))
                .body("byCategory.ACCESS", greaterThanOrEqualTo(1))
                .body("byPriority.CRITICAL", greaterThanOrEqualTo(1))
                .body("teamPerformance.size()", greaterThanOrEqualTo(3));
    }

    @Test
    void dashboardSlaIgnoresCancelledAndResolvedAsOverdue() {
        given()
                .auth().oauth2(managerToken())
                .when()
                .get("/dashboard/manager")
                .then()
                .statusCode(200)
                .body("slaAlerts.title", hasItem("RF-SEED-005 - Incidente em producao"))
                .body("slaAlerts.status", not(hasItem("CANCELLED")))
                .body("slaAlerts.status", not(hasItem("RESOLVED")));
    }

    @Test
    void analystCannotAccessManagerDashboard() {
        given()
                .auth().oauth2(analystToken())
                .when()
                .get("/dashboard/manager")
                .then()
                .statusCode(403);
    }
}
