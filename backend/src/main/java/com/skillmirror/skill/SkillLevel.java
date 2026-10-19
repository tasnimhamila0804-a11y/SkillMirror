package com.skillmirror.skill;

/** Explicit mastery scale. UNKNOWN is never treated as an acquired skill. */
public enum SkillLevel {
    BEGINNER(1),
    INTERMEDIATE(2),
    ADVANCED(3),
    EXPERT(4),
    UNKNOWN(0);

    private final int rank;

    SkillLevel(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public static SkillLevel from(Object raw) {
        if (raw == null) return UNKNOWN;
        try {
            return SkillLevel.valueOf(String.valueOf(raw).trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}
