package com.skillmirror.profile;

import com.skillmirror.profile.dto.ProfileDtos.CertificateDto;
import com.skillmirror.profile.dto.ProfileDtos.CertificateRequest;
import com.skillmirror.profile.dto.ProfileDtos.ExperienceDto;
import com.skillmirror.profile.dto.ProfileDtos.ExperienceRequest;
import com.skillmirror.profile.dto.ProfileDtos.ProfileDto;
import com.skillmirror.profile.dto.ProfileDtos.ProjectDto;
import com.skillmirror.profile.dto.ProfileDtos.ProjectRequest;
import com.skillmirror.profile.dto.ProfileDtos.UpdateProfileRequest;
import com.skillmirror.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles/me")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileDto get(@AuthenticationPrincipal AuthPrincipal principal) {
        return profileService.get(principal.id());
    }

    @PutMapping
    public ProfileDto update(@AuthenticationPrincipal AuthPrincipal principal,
                             @RequestBody UpdateProfileRequest request) {
        return profileService.update(principal.id(), request);
    }

    @GetMapping("/experiences")
    public List<ExperienceDto> experiences(@AuthenticationPrincipal AuthPrincipal principal) {
        return profileService.experiences(principal.id());
    }

    @PostMapping("/experiences")
    public ResponseEntity<ExperienceDto> addExperience(@AuthenticationPrincipal AuthPrincipal principal,
                                                       @Valid @RequestBody ExperienceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.addExperience(principal.id(), request));
    }

    @PutMapping("/experiences/{id}")
    public ExperienceDto updateExperience(@AuthenticationPrincipal AuthPrincipal principal,
                                          @PathVariable Long id,
                                          @Valid @RequestBody ExperienceRequest request) {
        return profileService.updateExperience(principal.id(), id, request);
    }

    @DeleteMapping("/experiences/{id}")
    public ResponseEntity<Void> deleteExperience(@AuthenticationPrincipal AuthPrincipal principal,
                                                 @PathVariable Long id) {
        profileService.deleteExperience(principal.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/certificates")
    public List<CertificateDto> certificates(@AuthenticationPrincipal AuthPrincipal principal) {
        return profileService.certificates(principal.id());
    }

    @PostMapping("/certificates")
    public ResponseEntity<CertificateDto> addCertificate(@AuthenticationPrincipal AuthPrincipal principal,
                                                         @Valid @RequestBody CertificateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.addCertificate(principal.id(), request));
    }

    @PutMapping("/certificates/{id}")
    public CertificateDto updateCertificate(@AuthenticationPrincipal AuthPrincipal principal,
                                            @PathVariable Long id,
                                            @Valid @RequestBody CertificateRequest request) {
        return profileService.updateCertificate(principal.id(), id, request);
    }

    @DeleteMapping("/certificates/{id}")
    public ResponseEntity<Void> deleteCertificate(@AuthenticationPrincipal AuthPrincipal principal,
                                                  @PathVariable Long id) {
        profileService.deleteCertificate(principal.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects")
    public List<ProjectDto> projects(@AuthenticationPrincipal AuthPrincipal principal) {
        return profileService.projects(principal.id());
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectDto> addProject(@AuthenticationPrincipal AuthPrincipal principal,
                                                 @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.addProject(principal.id(), request));
    }

    @PutMapping("/projects/{id}")
    public ProjectDto updateProject(@AuthenticationPrincipal AuthPrincipal principal,
                                    @PathVariable Long id,
                                    @Valid @RequestBody ProjectRequest request) {
        return profileService.updateProject(principal.id(), id, request);
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@AuthenticationPrincipal AuthPrincipal principal,
                                              @PathVariable Long id) {
        profileService.deleteProject(principal.id(), id);
        return ResponseEntity.noContent().build();
    }
}
