package com.skillmirror.skill;

/** Verification status of a skill: declared, evaluated or demonstrated by a proof. */
public enum SkillStatus {
    /** Self-declared during the questionnaire, not verified. */
    DECLARED,
    /** Practised and validated through a project's evaluation criteria. */
    EVALUATED,
    /** Backed by an accepted proof (repository, report, certification). */
    DEMONSTRATED
}
