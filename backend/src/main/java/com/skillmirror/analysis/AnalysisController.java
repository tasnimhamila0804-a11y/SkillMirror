package com.skillmirror.analysis;

import com.skillmirror.analysis.dto.AnalysisDtos.AnalysisResultDto;
import com.skillmirror.analysis.dto.AnalysisDtos.AnalysisSummaryDto;
import com.skillmirror.analysis.dto.AnalysisDtos.GapDto;
import com.skillmirror.security.AuthPrincipal;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    public record AnalyzeRequest(@NotNull Long jobOfferId) {}

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyses")
    public ResponseEntity<AnalysisResultDto> analyze(@AuthenticationPrincipal AuthPrincipal principal,
                                                     @RequestBody AnalyzeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(analysisService.analyze(principal.id(), request.jobOfferId()));
    }

    /** Endpoint named in the cahier des charges API table. */
    @PostMapping("/jobs/{jobId}/analyze")
    public ResponseEntity<AnalysisResultDto> analyzeFromJob(@AuthenticationPrincipal AuthPrincipal principal,
                                                            @PathVariable Long jobId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analysisService.analyze(principal.id(), jobId));
    }

    @GetMapping("/analyses")
    public List<AnalysisSummaryDto> history(@AuthenticationPrincipal AuthPrincipal principal) {
        return analysisService.history(principal.id());
    }

    @GetMapping("/analyses/{id}")
    public AnalysisResultDto get(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        return analysisService.get(principal.id(), id);
    }

    @GetMapping("/career/gaps")
    public List<GapDto> gaps(@AuthenticationPrincipal AuthPrincipal principal,
                             @RequestParam Long analysisId) {
        return analysisService.gaps(principal.id(), analysisId);
    }
}
