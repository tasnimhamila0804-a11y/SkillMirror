package com.skillmirror.learning;

import com.skillmirror.common.ApiException;
import com.skillmirror.config.SkillMirrorProperties;
import com.skillmirror.learning.dto.LearningDtos.EvidenceDto;
import com.skillmirror.learning.dto.LearningDtos.SubmitEvidenceRequest;
import com.skillmirror.profile.UserProject;
import com.skillmirror.profile.UserProjectRepository;
import com.skillmirror.skill.Skill;
import com.skillmirror.skill.SkillRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Proof management: link/report evidence and validated file uploads.
 * Files are stored outside the web root in per-user folders with a random name;
 * downloads are restricted to the owner.
 */
@Service
public class EvidenceService {

    private final SkillEvidenceRepository evidenceRepository;
    private final LearningPathRepository pathRepository;
    private final UserProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final SkillMirrorProperties properties;

    public EvidenceService(SkillEvidenceRepository evidenceRepository, LearningPathRepository pathRepository,
                           UserProjectRepository projectRepository, SkillRepository skillRepository,
                           SkillMirrorProperties properties) {
        this.evidenceRepository = evidenceRepository;
        this.pathRepository = pathRepository;
        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
        this.properties = properties;
    }

    public List<EvidenceDto> list(Long userId) {
        return evidenceRepository.findByUserIdOrderByIdDesc(userId).stream().map(EvidenceService::toDto).toList();
    }

    @Transactional
    public EvidenceDto submit(Long userId, Long learningPathId, SubmitEvidenceRequest request) {
        String type = request.type() == null ? "LINK" : request.type().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("LINK", "REPORT").contains(type)) {
            throw ApiException.badRequest("INVALID_EVIDENCE_TYPE", "Type de preuve invalide (LINK ou REPORT).");
        }
        if ("LINK".equals(type) && (request.url() == null || !request.url().startsWith("http"))) {
            throw ApiException.badRequest("URL_REQUIRED", "Fournissez un lien commençant par http(s)://.");
        }
        if ("REPORT".equals(type) && (request.description() == null || request.description().trim().length() < 20)) {
            throw ApiException.badRequest("REPORT_TOO_SHORT",
                    "Le rapport doit contenir au moins 20 caractères.");
        }

        SkillEvidence evidence = new SkillEvidence();
        evidence.setUserId(userId);
        evidence.setType(type);
        evidence.setUrl(type.equals("LINK") ? trim(request.url(), 600) : null);
        evidence.setDescription(type.equals("REPORT") ? trim(request.description(), 4000) : trim(request.description(), 4000));
        evidence.setStatus("SUBMITTED");

        if (learningPathId != null) {
            LearningPath path = pathRepository.findByIdAndUserId(learningPathId, userId)
                    .orElseThrow(() -> ApiException.notFound("Parcours introuvable."));
            evidence.setLearningPathId(path.getId());
            evidence.setSkillName(path.getSkillName());
            if (path.getSkillId() != null) evidence.setSkillId(path.getSkillId());
        } else if (request.projectId() != null) {
            UserProject project = projectRepository.findByIdAndUserId(request.projectId(), userId)
                    .orElseThrow(() -> ApiException.notFound("Projet introuvable."));
            evidence.setProjectId(project.getId());
            evidence.setSkillName(trim(project.getName(), 120));
        } else if (request.url() != null && !request.url().isBlank()) {
            evidence.setSkillName("Preuve liée");
        } else {
            throw ApiException.badRequest("TARGET_REQUIRED",
                    "Associez la preuve à un parcours ou à un projet.");
        }
        return toDto(evidenceRepository.save(evidence));
    }

    @Transactional
    public EvidenceDto upload(Long userId, Long learningPathId, String description, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("FILE_REQUIRED", "Sélectionnez un fichier.");
        }
        long maxBytes = properties.getUpload().getMaxBytes();
        if (file.getSize() > maxBytes) {
            throw ApiException.badRequest("FILE_TOO_LARGE",
                    "Fichier trop volumineux (maximum " + (maxBytes / (1024 * 1024)) + " Mo).");
        }
        String original = file.getOriginalFilename() == null ? "fichier" : file.getOriginalFilename();
        String extension = extensionOf(original);
        Set<String> allowed = Arrays.stream(properties.getUpload().getAllowedExtensions().split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toSet());
        if (!allowed.contains(extension)) {
            throw ApiException.badRequest("FILE_TYPE_NOT_ALLOWED",
                    "Type de fichier refusé. Autorisés : " + String.join(", ", allowed) + ".");
        }

        LearningPath path = null;
        if (learningPathId != null) {
            path = pathRepository.findByIdAndUserId(learningPathId, userId)
                    .orElseThrow(() -> ApiException.notFound("Parcours introuvable."));
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path directory = Paths.get(properties.getUpload().getDirectory(), "user-" + userId);
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(storedName);
            try (var input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw ApiException.badRequest("UPLOAD_FAILED", "Impossible d'enregistrer le fichier.");
        }

        SkillEvidence evidence = new SkillEvidence();
        evidence.setUserId(userId);
        evidence.setType("FILE");
        evidence.setFileName(trim(original, 255));
        evidence.setFilePath(directory.resolve(storedName).toAbsolutePath().toString());
        evidence.setDescription(trim(description, 4000));
        evidence.setStatus("SUBMITTED");
        if (path != null) {
            evidence.setLearningPathId(path.getId());
            evidence.setSkillName(path.getSkillName());
            if (path.getSkillId() != null) evidence.setSkillId(path.getSkillId());
        } else {
            evidence.setSkillName("Fichier joint");
        }
        return toDto(evidenceRepository.save(evidence));
    }

    /** Owner-only download of an uploaded proof. */
    public DownloadedFile download(Long userId, Long evidenceId) {
        SkillEvidence evidence = evidenceRepository.findByIdAndUserId(evidenceId, userId)
                .orElseThrow(() -> ApiException.notFound("Preuve introuvable."));
        if (evidence.getFilePath() == null || evidence.getFileName() == null) {
            throw ApiException.badRequest("NOT_A_FILE", "Cette preuve n'est pas un fichier téléversé.");
        }
        try {
            Path path = Paths.get(evidence.getFilePath());
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw ApiException.notFound("Fichier introuvable sur le serveur.");
            }
            String contentType = Files.probeContentType(path);
            return new DownloadedFile(resource, contentType == null ? "application/octet-stream" : contentType,
                    evidence.getFileName());
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ApiException.notFound("Fichier introuvable sur le serveur.");
        }
    }

    @Transactional
    public void delete(Long userId, Long evidenceId) {
        SkillEvidence evidence = evidenceRepository.findByIdAndUserId(evidenceId, userId)
                .orElseThrow(() -> ApiException.notFound("Preuve introuvable."));
        if (evidence.getFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(evidence.getFilePath()));
            } catch (IOException ignored) {
                // orphan files are harmless
            }
        }
        evidenceRepository.delete(evidence);
    }

    public record DownloadedFile(Resource resource, String contentType, String fileName) {}

    public static EvidenceDto toDto(SkillEvidence e) {
        return new EvidenceDto(e.getId(), e.getSkillId(), e.getSkillName(), e.getLearningPathId(),
                e.getProjectId(), e.getType(), e.getUrl(), e.getFileName(), e.getDescription(),
                e.getStatus(), e.getReviewNote(), e.getCreatedAt());
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String trim(String value, int max) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }
}
