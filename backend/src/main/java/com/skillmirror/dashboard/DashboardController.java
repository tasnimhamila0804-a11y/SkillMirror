package com.skillmirror.dashboard;

import com.skillmirror.dashboard.dto.DashboardDtos.DashboardDto;
import com.skillmirror.dashboard.dto.DashboardDtos.ProgressDto;
import com.skillmirror.security.AuthPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/dashboard")
    public DashboardDto dashboard(@AuthenticationPrincipal AuthPrincipal principal) {
        return dashboardService.dashboard(principal.id());
    }

    @GetMapping("/api/progress")
    public ProgressDto progress(@AuthenticationPrincipal AuthPrincipal principal) {
        return dashboardService.progress(principal.id());
    }
}
