package com.skillmirror.job;

import com.skillmirror.job.dto.JobDtos.ConfirmSkillsRequest;
import com.skillmirror.job.dto.JobDtos.ExtractResponse;
import com.skillmirror.job.dto.JobDtos.JobOfferDto;
import com.skillmirror.job.dto.JobDtos.JobSummaryDto;
import com.skillmirror.job.dto.JobDtos.SaveJobRequest;
import com.skillmirror.job.dto.JobDtos.UpdateJobRequest;
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
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobSummaryDto> list(@AuthenticationPrincipal AuthPrincipal principal) {
        return jobService.list(principal.id());
    }

    @PostMapping
    public ResponseEntity<JobOfferDto> create(@AuthenticationPrincipal AuthPrincipal principal,
                                              @Valid @RequestBody SaveJobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.create(principal.id(), request));
    }

    @GetMapping("/{id}")
    public JobOfferDto get(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        return jobService.get(principal.id(), id);
    }

    @PutMapping("/{id}")
    public JobOfferDto update(@AuthenticationPrincipal AuthPrincipal principal,
                              @PathVariable Long id,
                              @Valid @RequestBody UpdateJobRequest request) {
        return jobService.update(principal.id(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        jobService.delete(principal.id(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/extract")
    public ExtractResponse extract(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        return jobService.extract(principal.id(), id);
    }

    @PutMapping("/{id}/skills")
    public JobOfferDto confirmSkills(@AuthenticationPrincipal AuthPrincipal principal,
                                     @PathVariable Long id,
                                     @RequestBody ConfirmSkillsRequest request) {
        return jobService.confirmSkills(principal.id(), id, request);
    }
}
