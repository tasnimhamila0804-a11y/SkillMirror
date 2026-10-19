package com.skillmirror.learning;

import com.skillmirror.learning.dto.LearningDtos.CompletionCheckDto;
import com.skillmirror.learning.dto.LearningDtos.EvidenceDto;
import com.skillmirror.learning.dto.LearningDtos.GenerateRequest;
import com.skillmirror.learning.dto.LearningDtos.LearningPathDto;
import com.skillmirror.learning.dto.LearningDtos.LearningPathSummaryDto;
import com.skillmirror.learning.dto.LearningDtos.SubmitEvidenceRequest;
import com.skillmirror.learning.dto.LearningDtos.TaskUpdateRequest;
import com.skillmirror.security.AuthPrincipal;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class LearningController {

    private final LearningService learningService;
    private final EvidenceService evidenceService;

    public LearningController(LearningService learningService, EvidenceService evidenceService) {
        this.learningService = learningService;
        this.evidenceService = evidenceService;
    }

    // --- paths ---------------------------------------------------------------

    @PostMapping("/learning-paths/generate")
    public ResponseEntity<List<LearningPathSummaryDto>> generate(@AuthenticationPrincipal AuthPrincipal principal,
                                                                 @RequestBody GenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(learningService.generate(principal.id(), request));
    }

    @GetMapping("/learning-paths")
    public List<LearningPathSummaryDto> list(@AuthenticationPrincipal AuthPrincipal principal,
                                             @RequestParam(required = false) String status) {
        return learningService.list(principal.id(), status);
    }

    @GetMapping("/learning-paths/{id}")
    public LearningPathDto get(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        return learningService.get(principal.id(), id);
    }

    @PatchMapping("/learning-paths/{id}/status")
    public LearningPathDto updateStatus(@AuthenticationPrincipal AuthPrincipal principal,
                                        @PathVariable Long id,
                                        @RequestBody java.util.Map<String, String> body) {
        return learningService.updateStatus(principal.id(), id, body.get("status"));
    }

    @GetMapping("/learning-paths/{id}/completion-check")
    public CompletionCheckDto check(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        return learningService.checkCompletion(principal.id(), id);
    }

    /** Validates the path against its explicit criteria before touching any skill status. */
    @PostMapping("/learning-paths/{id}/complete")
    public LearningPathDto complete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        return learningService.complete(principal.id(), id);
    }

    // --- tasks ---------------------------------------------------------------

    @PatchMapping("/learning-tasks/{id}")
    public LearningPathDto updateTask(@AuthenticationPrincipal AuthPrincipal principal,
                                      @PathVariable Long id,
                                      @RequestBody TaskUpdateRequest request) {
        return learningService.updateTaskById(principal.id(), id, request);
    }

    // --- evidence --------------------------------------------------------------

    @PostMapping("/learning-paths/{id}/evidence")
    public ResponseEntity<EvidenceDto> submitForPath(@AuthenticationPrincipal AuthPrincipal principal,
                                                     @PathVariable Long id,
                                                     @RequestBody SubmitEvidenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evidenceService.submit(principal.id(), id, request));
    }

    @PostMapping("/learning-paths/{id}/evidence/upload")
    public ResponseEntity<EvidenceDto> uploadForPath(@AuthenticationPrincipal AuthPrincipal principal,
                                                     @PathVariable Long id,
                                                     @RequestParam("file") MultipartFile file,
                                                     @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evidenceService.upload(principal.id(), id, description, file));
    }

    @PostMapping("/evidence")
    public ResponseEntity<EvidenceDto> submit(@AuthenticationPrincipal AuthPrincipal principal,
                                              @RequestBody SubmitEvidenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evidenceService.submit(principal.id(), null, request));
    }

    @PostMapping("/evidence/upload")
    public ResponseEntity<EvidenceDto> upload(@AuthenticationPrincipal AuthPrincipal principal,
                                              @RequestParam("file") MultipartFile file,
                                              @RequestParam(required = false) Long projectId,
                                              @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evidenceService.upload(principal.id(), projectId, description, file));
    }

    @GetMapping("/evidence")
    public List<EvidenceDto> listEvidence(@AuthenticationPrincipal AuthPrincipal principal) {
        return evidenceService.list(principal.id());
    }

    @GetMapping("/evidence/{id}/file")
    public ResponseEntity<org.springframework.core.io.Resource> download(
            @AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        EvidenceService.DownloadedFile file = evidenceService.download(principal.id(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.fileName().replace("\"", "") + "\"")
                .body(file.resource());
    }

    @DeleteMapping("/evidence/{id}")
    public ResponseEntity<Void> deleteEvidence(@AuthenticationPrincipal AuthPrincipal principal,
                                               @PathVariable Long id) {
        evidenceService.delete(principal.id(), id);
        return ResponseEntity.noContent().build();
    }
}
