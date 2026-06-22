package com.requestflow.controller;

import com.requestflow.dto.dashboard.AnalystDashboardDTO;
import com.requestflow.dto.dashboard.ManagerDashboardDTO;
import com.requestflow.service.DashboardService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class DashboardController {

    @Inject
    DashboardService dashboardService;

    @GET
    public Object dashboard() {
        return dashboardService.getDashboardForCurrentUser();
    }

    @GET
    @Path("/analyst")
    @RolesAllowed("ANALYST")
    public AnalystDashboardDTO analystDashboard() {
        return dashboardService.getAnalystDashboard();
    }

    @GET
    @Path("/manager")
    @RolesAllowed("MANAGER")
    public ManagerDashboardDTO managerDashboard() {
        return dashboardService.getManagerDashboard();
    }
}
