package com.skillmirror.admin;

import com.skillmirror.admin.SettingsService.SettingDto;
import com.skillmirror.admin.dto.AdminDtos.AdminSkillDto;
import com.skillmirror.admin.dto.AdminDtos.AdminUserDto;
import com.skillmirror.admin.dto.AdminDtos.AiUsageReportDto;
import com.skillmirror.admin.dto.AdminDtos.ErrorDto;
import com.skillmirror.admin.dto.AdminDtos.OverviewDto;
import com.skillmirror.admin.dto.AdminDtos.SettingInput;
import com.skillmirror.admin.dto.AdminDtos.SkillRequest;
import com.skillmirror.admin.dto.AdminDtos.UserUpdateRequest;
import com.skillmirror.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin space: accounts, skill reference, AI usage, technical errors, settings. */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final SettingsService settingsService;

    public AdminController(AdminService adminService, SettingsService settingsService) {
        this.adminService = adminService;
        this.settingsService = settingsService;
    }

    @GetMapping("/overview")
    public OverviewDto overview() {
        return adminService.overview();
    }

    @GetMapping("/users")
    public List<AdminUserDto> users(@RequestParam(required = false) String q,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return adminService.users(q, page, size);
    }

    @PatchMapping("/users/{id}")
    public AdminUserDto updateUser(@AuthenticationPrincipal AuthPrincipal principal,
                                   @PathVariable Long id,
                                   @RequestBody UserUpdateRequest request) {
        return adminService.updateUser(principal.id(), id, request);
    }

    @GetMapping("/skills")
    public List<AdminSkillDto> skills() {
        return adminService.skills();
    }

    @PostMapping("/skills")
    public ResponseEntity<AdminSkillDto> createSkill(@AuthenticationPrincipal AuthPrincipal principal,
                                                     @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createSkill(principal.id(), request));
    }

    @PutMapping("/skills/{id}")
    public AdminSkillDto updateSkill(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        return adminService.updateSkill(id, request);
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        adminService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ai-usage")
    public AiUsageReportDto aiUsage() {
        return adminService.aiUsage();
    }

    @GetMapping("/errors")
    public List<ErrorDto> errors() {
        return adminService.errors();
    }

    @GetMapping("/settings")
    public List<SettingDto> settings() {
        return settingsService.all();
    }

    @PutMapping("/settings")
    public List<SettingDto> updateSettings(@RequestBody List<SettingInput> inputs) {
        List<SettingDto> mapped = inputs.stream()
                .map(i -> new SettingDto(i.key(), i.value(), null))
                .toList();
        return settingsService.update(mapped);
    }
}
