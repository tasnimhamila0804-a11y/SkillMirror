package com.skillmirror.ai;

import com.skillmirror.ai.dto.AiDtos.ExtractionResult;
import com.skillmirror.ai.dto.AiDtos.ExtractedSkill;
import com.skillmirror.ai.dto.AiDtos.GeneratedProject;
import com.skillmirror.skill.Skill;
import com.skillmirror.skill.SkillCategory;
import com.skillmirror.skill.SkillRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Deterministic, explainable "demo intelligence" used when no AI API key is configured
 * (or when the live provider fails). Every result is flagged as simulated so the UI can
 * clearly distinguish simulated from live AI output.
 */
@Component
public class DemoIntelligence {

    private static final List<String> DESIRED_MARKERS = List.of(
            "nice to have", "would be a plus", "plus appreciated", "optional", "preferred", "desirable",
            "asset", "bonus", "not required", "souhait", "appréci", "de préférence", "un plus", "optionnel");

    private static final Map<String, String> ALIASES = new LinkedHashMap<>();

    static {
        ALIASES.put("js", "javascript");
        ALIASES.put("ts", "typescript");
        ALIASES.put("reactjs", "react");
        ALIASES.put("react.js", "react");
        ALIASES.put("vue", "vue.js");
        ALIASES.put("vuejs", "vue.js");
        ALIASES.put("angularjs", "angular");
        ALIASES.put("node", "node.js");
        ALIASES.put("nodejs", "node.js");
        ALIASES.put("spring", "spring boot");
        ALIASES.put("springframework", "spring boot");
        ALIASES.put("postgres", "postgresql");
        ALIASES.put("mongo", "mongodb");
        ALIASES.put("k8s", "kubernetes");
        ALIASES.put("ci cd", "ci/cd");
        ALIASES.put("continuous integration", "ci/cd");
        ALIASES.put("excel", "microsoft excel");
        ALIASES.put("powerbi", "power bi");
        ALIASES.put("nlp", "natural language processing");
        ALIASES.put("sklearn", "scikit-learn");
        ALIASES.put("scikit learn", "scikit-learn");
        ALIASES.put("llm", "generative ai");
        ALIASES.put("genai", "generative ai");
        ALIASES.put("team player", "teamwork");
        ALIASES.put("team spirit", "teamwork");
        ALIASES.put("travail d'équipe", "teamwork");
        ALIASES.put("esprit d'équipe", "teamwork");
        ALIASES.put("problem solving", "problem solving");
        ALIASES.put("résolution de problèmes", "problem solving");
        ALIASES.put("critical thinking", "critical thinking");
        ALIASES.put("esprit critique", "critical thinking");
        ALIASES.put("communication skills", "communication");
        ALIASES.put("gestion du temps", "time management");
        ALIASES.put("rigueur", "attention to detail");
        ALIASES.put("sense of initiative", "adaptability");
        ALIASES.put("adaptabilité", "adaptability");
    }

    private final SkillRepository skillRepository;

    public DemoIntelligence(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    // --- skill extraction ---------------------------------------------------

    /** Resolves a common alias to its canonical reference name ("js" → "javascript"). */
    public String canonical(String normalizedName) {
        if (normalizedName == null) return "";
        String target = ALIASES.get(normalizedName);
        return target != null ? target : normalizedName;
    }

    public ExtractionResult extractJobSkills(String jobText) {
        String text = jobText == null ? "" : jobText;
        List<Skill> reference = skillRepository.findAllByOrderByName();

        Map<String, ExtractedSkill> technical = new LinkedHashMap<>();
        Map<String, ExtractedSkill> soft = new LinkedHashMap<>();

        for (Skill skill : reference) {
            Integer sentenceIndex = findSentence(text, skill.getName());
            if (sentenceIndex == null) continue;
            String sentence = sentence(text, sentenceIndex);
            boolean desired = isDesired(sentence);
            ExtractedSkill extracted = new ExtractedSkill(
                    skill.getName(),
                    skill.getCategory().name(),
                    desired ? "DESIRED" : "REQUIRED",
                    desired ? 1 : 3,
                    null);
            if (skill.getCategory() == SkillCategory.SOFT_SKILLS) {
                soft.putIfAbsent(skill.getNormalizedName(), extracted);
            } else {
                technical.putIfAbsent(skill.getNormalizedName(), extracted);
            }
        }

        // Alias pass: "JS" → JavaScript, "travail d'équipe" → Teamwork, ...
        for (Map.Entry<String, String> alias : ALIASES.entrySet()) {
            if (alias.getKey().equals(alias.getValue())) continue;
            int idx = indexOfIgnoreCase(text, alias.getKey(), true);
            if (idx < 0) continue;
            Skill target = skillRepository.findByNormalizedName(alias.getValue()).orElse(null);
            if (target == null) continue;
            if (technical.containsKey(target.getNormalizedName())
                    || soft.containsKey(target.getNormalizedName())) continue;
            String sentence = sentenceOfIndex(text, idx);
            boolean desired = isDesired(sentence);
            ExtractedSkill extracted = new ExtractedSkill(target.getName(), target.getCategory().name(),
                    desired ? "DESIRED" : "REQUIRED", desired ? 1 : 3, null);
            if (target.getCategory() == SkillCategory.SOFT_SKILLS) {
                soft.put(target.getNormalizedName(), extracted);
            } else {
                technical.put(target.getNormalizedName(), extracted);
            }
        }

        List<ExtractedSkill> techList = new ArrayList<>(technical.values().stream().limit(30).toList());
        List<ExtractedSkill> softList = new ArrayList<>(soft.values().stream().limit(12).toList());
        return new ExtractionResult(techList, softList, detectExperienceLevel(text),
                detectEducation(text), true);
    }

    private Integer findSentence(String text, String skillName) {
        String needle = skillName.trim();
        int flags = caseFlagsFor(needle);
        Pattern pattern = Pattern.compile("(?<![A-Za-z0-9])" + Pattern.quote(needle) + "(?![A-Za-z0-9])",
                flags | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) return null;
        return sentenceIndexAt(text, matcher.start());
    }

    /**
     * Single/double letter skills (C, R, Go) are matched case-sensitively: a lowercase "c" in
     * French prose must never be read as the C language.
     */
    private int caseFlagsFor(String needle) {
        return needle.length() <= 2 ? 0 : Pattern.CASE_INSENSITIVE;
    }

    private int sentenceIndexAt(String text, int offset) {
        int index = 0;
        for (int i = 0; i < offset && i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '.' || c == '!' || c == '?' || c == '\n' || c == ';' || c == '•') {
                // skip separators that are not sentence ends (e.g. "3.5", "node.js")
                boolean isDecimal = c == '.' && i + 1 < text.length() && Character.isDigit(text.charAt(i + 1));
                boolean precededByDigit = i > 0 && Character.isDigit(text.charAt(i - 1));
                boolean isTechDot = c == '.' && i + 1 < text.length()
                        && (Character.isLetter(text.charAt(i + 1)));
                if (isDecimal || (c == '.' && precededByDigit) || (c == '.' && isTechDot)) continue;
                index = i + 1;
            }
        }
        return index;
    }

    private String sentence(String text, int start) {
        int end = text.length();
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n' || c == '•' || c == '!' || c == '?') {
                end = i;
                break;
            }
            if (c == '.' && i + 1 < text.length() && (text.charAt(i + 1) == ' ' || text.charAt(i + 1) == '\n')) {
                end = i;
                break;
            }
        }
        return text.substring(start, Math.min(end, text.length()));
    }

    private String sentenceOfIndex(String text, int offset) {
        return sentence(text, sentenceIndexAt(text, offset));
    }

    private boolean isDesired(String sentence) {
        String lower = sentence.toLowerCase(Locale.ROOT);
        return DESIRED_MARKERS.stream().anyMatch(lower::contains);
    }

    private int indexOfIgnoreCase(String text, String needle, boolean wordish) {
        Pattern pattern = wordish
                ? Pattern.compile("(?<![A-Za-z0-9])" + Pattern.quote(needle) + "(?![A-Za-z0-9])",
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                : Pattern.compile(Pattern.quote(needle), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.start() : -1;
    }

    public String detectExperienceLevel(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (Pattern.compile("(senior|lead|expert|5\\s*[+]?\\s*ans|confirmé|advanced level)").matcher(lower).find()) {
            return "SENIOR";
        }
        if (Pattern.compile("(junior|débutant|entry[- ]level|0\\s*[-–]\\s*2\\s*ans|1\\s*[-–]\\s*3\\s*ans|stagiaire|internship|fresh graduate)").matcher(lower).find()) {
            return "JUNIOR";
        }
        if (Pattern.compile("(mid[- ]level|3\\s*[-–]\\s*5\s*ans|intermédiaire|2\\s*[-–]\\s*4\s*ans)").matcher(lower).find()) {
            return "MIDDLE";
        }
        return null;
    }

    public String detectEducation(String text) {
        Pattern pattern = Pattern.compile(
                "(baccalauréat|bac\\s*[+\\-]\\s*\\d|bachelor|licence|master|ingénieur|diplôme[^.\\n]{0,80}|degree in[^.\\n]{0,80})",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            int start = Math.max(0, matcher.start() - 40);
            int end = Math.min(text.length(), matcher.end() + 40);
            String excerpt = text.substring(start, end).replaceAll("\\s+", " ").trim();
            if (start > 0) {
                int space = excerpt.indexOf(' ');
                if (space > 0 && space < 20) excerpt = excerpt.substring(space + 1);
            }
            return excerpt.length() > 190 ? excerpt.substring(0, 190) : excerpt;
        }
        return null;
    }

    // --- project generation --------------------------------------------------

    public GeneratedProject generateProject(String skillName, SkillCategory category, String difficulty,
                                            String targetJob, int hoursPerWeek) {
        boolean soft = category == SkillCategory.SOFT_SKILLS;
        String difficultyEnum = switch (difficulty == null ? "" : difficulty.toUpperCase(Locale.ROOT)) {
            case "BEGINNER" -> "BEGINNER";
            case "ADVANCED" -> "ADVANCED";
            default -> "INTERMEDIATE";
        };
        String difficultyLabel = switch (difficultyEnum) {
            case "BEGINNER" -> "débutant";
            case "ADVANCED" -> "avancé";
            default -> "intermédiaire";
        };
        String jobContext = (targetJob == null || targetJob.isBlank()) ? "votre objectif professionnel" : targetJob;

        if (soft) {
            return new GeneratedProject(
                    "Atelier " + skillName + " — mise en situation " + jobContext,
                    "Un exercice pratique centré sur " + skillName + ", conçu pour être réalisé en environ "
                            + hoursPerWeek + " h/semaine et démontré dans un contexte réel de " + jobContext + ".",
                    difficultyEnum,
                    List.of(
                            "Identifier les situations où " + skillName + " est déterminante.",
                            "Appliquer une méthode structurée et la documenter.",
                            "Recevoir un retour d'expérience et mesurer l'amélioration."),
                    List.of("Carnet de bord", "Grille d'auto-évaluation", "Enregistrement ou compte-rendu"),
                    List.of(
                            "Décrire 3 situations réelles où " + skillName + " a été mise en œuvre.",
                            "Choisir une méthode et la formaliser (structure, critères, rituel).",
                            "Réaliser l'activité et journaliser les décisions prises.",
                            "Collecter un retour d'un pair ou d'un tuteur.",
                            "Rédiger une synthèse d'une page avec 3 points d'amélioration."),
                    List.of(
                            "Rédiger le brief de la situation (contexte, objectif, contraintes).",
                            "Structurer la méthode retenue en 5 étapes.",
                            "Réaliser l'activité et noter les décisions.",
                            "Demander un retour et l'intégrer.",
                            "Produire la synthèse finale et l'auto-évaluation."),
                    List.of(
                            "La situation décrite est concrète et contextualisée.",
                            "La méthode est structurée et justifiée.",
                            "Le retour d'expérience est réellement intégré.",
                            "La synthèse est claire et actionnable."),
                    "Un compte-rendu d'une page + une auto-évaluation honnête de " + skillName + ".",
                    "Lien vers le compte-rendu ou capture de la présentation du retour d'expérience.",
                    true);
        }

        if ("DATABASES".equals(category.name()) || "SQL".equalsIgnoreCase(skillName)) {
            return new GeneratedProject(
                    "Base de données " + skillName + " : gestion et analyse de données",
                    "Concevez une base de données " + skillName + " complète, alimentez-la, interrogez-la et "
                            + "produisez un rapport d'analyse. Projet calibré pour " + difficultyLabel
                            + " à raison d'environ " + hoursPerWeek + " h/semaine, en lien avec " + jobContext + ".",
                    difficultyEnum,
                    List.of(
                            "Modéliser un schéma relationnel cohérent (entités, clés, contraintes).",
                            "Écrire des requêtes de sélection, jointure et agrégation.",
                            "Analyser les résultats et présenter 3 indicateurs clés.",
                            "Documenter les choix de conception."),
                    List.of(skillName, "Un client SQL (DBeaver, MySQL Workbench...)", "Git"),
                    List.of(
                            "Définir le besoin métier et les entités (au moins 5).",
                            "Réaliser le script de création du schéma avec contraintes et index.",
                            "Insérer un jeu de données représentatif (au moins 200 lignes).",
                            "Écrire 10 requêtes utiles dont 3 avec agrégations.",
                            "Construire un rapport final avec 3 indicateurs et leur interprétation.",
                            "Publier le dépôt et rédiger le README."),
                    List.of(
                            "Écrire le script de schéma (tables, clés, contraintes).",
                            "Générer et insérer les données de test.",
                            "Réaliser les 10 requêtes dans un fichier commenté.",
                            "Créer les 3 indicateurs du rapport.",
                            "Rédiger le README (installation, requêtes, résultats).",
                            "Publier le dépôt Git."),
                    List.of(
                            "Le schéma est normalisé et les contraintes sont posées.",
                            "Les requêtes sont justes et commentées.",
                            "Le rapport présente des indicateurs interprétés, pas seulement bruts.",
                            "Le dépôt est réutilisable par un tiers sans aide."),
                    "Une base fonctionnelle + un rapport d'analyse avec 3 indicateurs.",
                    "Lien vers le dépôt Git public contenant le schéma, les requêtes et le rapport.",
                    true);
        }

        return new GeneratedProject(
                "Projet " + skillName + " pour " + jobContext,
                "Mettez en pratique " + skillName + " à travers un livrable concret et observable, "
                        + "calibré pour un niveau " + difficultyLabel + " à raison d'environ "
                        + hoursPerWeek + " h/semaine.",
                difficultyEnum,
                List.of(
                        "Acquérir les notions essentielles de " + skillName + " par la pratique.",
                        "Réaliser un livrable fonctionnel et le documenter.",
                        "Structurer son travail avec Git et une démarche itérative.",
                        "Présenter le résultat et justifier les choix techniques."),
                List.of(skillName, "Git / GitHub", "Éditeur de code ou outil dédié"),
                List.of(
                        "Cadrer le besoin : objectif, utilisateur, contraintes.",
                        "Monter un squelette fonctionnel versionné sur Git.",
                        "Implémenter le cœur fonctionnel en 3 incréments.",
                        "Tester avec des cas réels et corriger.",
                        "Rédiger le README (installation, utilisation, décisions).",
                        "Publier la preuve et faire une auto-évaluation critique."),
                List.of(
                        "Initialiser le dépôt Git et le squelette du projet.",
                        "Implémenter l'incrément 1 (fonctionnalité de base).",
                        "Implémenter l'incrément 2 (cas d'erreur et limites).",
                        "Implémenter l'incrément 3 (fiabilité et finitions).",
                        "Écrire les tests ou le protocole de vérification.",
                        "Rédiger le README et publier le dépôt."),
                List.of(
                        "Le livrable répond au besoin défini au départ.",
                        "Le code est lisible, versionné et documenté.",
                        "Les cas d'erreur sont traités.",
                        "La preuve est publiquement consultable."),
                "Un dépôt public fonctionnel + une démonstration courte du résultat.",
                "Lien vers le dépôt Git public et, si possible, une démo en ligne.",
                true);
    }

    // --- analysis summary ----------------------------------------------------

    public List<String> analysisSummary(int score, int demonstrated, int declared, int partial, int missing) {
        String level;
        if (score >= 80) level = "très bonne adéquation";
        else if (score >= 60) level = "bonne adéquation";
        else if (score >= 40) level = "adéquation partielle";
        else level = "écart important";
        return List.of(
                "Votre adéquation à ce poste est " + level + " (" + score + "/100).",
                demonstrated + " compétence(s) démontrée(s) par une preuve, " + declared
                        + " déclarée(s), " + partial + " partielle(s), " + missing + " manquante(s).",
                "Concentrez vos prochains projets sur les compétences manquantes de plus fort poids.");
    }
}
