package com.skillmirror.skill;

import com.skillmirror.security.AuthPrincipal;
import com.skillmirror.skill.dto.SkillDtos.AddSkillRequest;
import com.skillmirror.skill.dto.SkillDtos.SkillDto;
import com.skillmirror.skill.dto.SkillDtos.UpdateSkillRequest;
import com.skillmirror.skill.dto.SkillDtos.UserSkillDto;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping("/skills")
    public List<SkillDto> reference(@RequestParam(required = false) String category,
                                    @RequestParam(required = false) String q) {
        return skillService.reference(category, q);
    }

    @GetMapping("/skills/{id}")
    public SkillDto get(@PathVariable Long id) {
        return skillService.get(id);
    }

    @GetMapping("/profiles/me/skills")
    public List<UserSkillDto> mySkills(@AuthenticationPrincipal AuthPrincipal principal) {
        return skillService.mySkills(principal.id());
    }

    @PostMapping("/profiles/me/skills")
    public ResponseEntity<UserSkillDto> add(@AuthenticationPrincipal AuthPrincipal principal,
                                            @Valid @RequestBody AddSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.addSkill(principal.id(), request));
    }

    @PutMapping("/profiles/me/skills/{id}")
    public UserSkillDto update(@AuthenticationPrincipal AuthPrincipal principal,
                               @PathVariable Long id,
                               @Valid @RequestBody UpdateSkillRequest request) {
        return skillService.updateSkill(principal.id(), id, request);
    }

    @DeleteMapping("/profiles/me/skills/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        skillService.removeSkill(principal.id(), id);
        return ResponseEntity.noContent().build();
    }
}
