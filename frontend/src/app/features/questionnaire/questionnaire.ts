import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import {
  QuestionnairePayload,
  QuestionnaireResponse,
  QuestionnaireSkill,
  Skill,
  SkillLevel,
} from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { IconComponent } from '../../shared/icon';

interface Option {
  value: string;
  label: string;
  hint?: string;
}

/** Multi-step profiling assistant: save, resume, back/forward navigation. */
@Component({
  selector: 'sm-questionnaire',
  imports: [ReactiveFormsModule, FormsModule, IconComponent],
  templateUrl: './questionnaire.html',
  styleUrl: './questionnaire.css',
})
export class QuestionnaireComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly notify = inject(NotificationService);

  protected readonly step = signal(1);
  protected readonly maxStep = signal(1);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly completing = signal(false);
  protected readonly stepError = signal<string | null>(null);
  protected readonly referenceSkills = signal<Skill[]>([]);
  protected readonly skills = signal<QuestionnaireSkill[]>([]);
  protected readonly search = signal('');
  protected readonly activeCategory = signal('LANGUAGES');
  protected readonly customSkill = signal('');
  protected readonly submitAttempted = signal(false);

  protected readonly stepTitles = [
    'Situation actuelle',
    'Compétences',
    'Autoévaluation',
    'Expériences & preuves',
    'Objectif professionnel',
    "Préférences d'apprentissage",
  ];

  protected readonly situations: Option[] = [
    { value: 'STUDENT', label: 'Étudiant', hint: 'Formation en cours' },
    { value: 'YOUNG_GRADUATE', label: 'Jeune diplômé', hint: 'Diplôme récent' },
    { value: 'EMPLOYED', label: 'Salarié', hint: 'En poste' },
    { value: 'CAREER_CHANGE', label: 'En reconversion', hint: 'Changement de domaine' },
    { value: 'JOB_SEEKING', label: 'En recherche d’emploi', hint: 'Candidatures actives' },
  ];

  protected readonly skillCategories: Option[] = [
    { value: 'LANGUAGES', label: 'Langages' },
    { value: 'WEB', label: 'Développement web' },
    { value: 'DATABASES', label: 'Bases de données' },
    { value: 'DATA', label: 'Données & analyse' },
    { value: 'AI', label: 'IA' },
    { value: 'TOOLS', label: 'Outils' },
    { value: 'SOFT_SKILLS', label: 'Soft skills' },
    { value: 'OTHER', label: 'Autres' },
  ];

  protected readonly levels: Option[] = [
    { value: 'BEGINNER', label: 'Débutant', hint: 'Je connais les notions fondamentales.' },
    { value: 'INTERMEDIATE', label: 'Intermédiaire', hint: 'Je réalise des exercices et projets simples.' },
    { value: 'ADVANCED', label: 'Avancé', hint: 'Je réalise des projets complexes en autonomie.' },
    { value: 'EXPERT', label: 'Expert', hint: 'Expérience approfondie, problèmes complexes.' },
    { value: 'UNKNOWN', label: 'Je ne sais pas', hint: 'Mon niveau n’est pas évalué pour l’instant.' },
  ];

  protected readonly careerGoals: Option[] = [
    { value: 'FIRST_JOB', label: 'Obtenir un premier emploi' },
    { value: 'PROGRESSION', label: 'Progresser dans mon poste' },
    { value: 'CAREER_CHANGE', label: 'Changer de métier' },
    { value: 'UNDEFINED', label: 'Je ne sais pas encore' },
  ];

  protected readonly positionTypes: Option[] = [
    { value: 'INTERNSHIP', label: 'Stage' },
    { value: 'JUNIOR', label: 'Emploi junior' },
    { value: 'EXPERIENCED', label: 'Poste expérimenté' },
  ];

  protected readonly targetJobs = [
    'Développeur Full Stack',
    'Data Analyst',
    'Ingénieur IA junior',
    'Spécialiste cybersécurité',
    'Développeur Front-end',
    'Développeur Back-end',
    'DevOps',
    'Designer UX/UI',
    'Analyste métier',
  ];

  protected readonly learningPreferences: Option[] = [
    { value: 'PROJECTS', label: 'Projets pratiques' },
    { value: 'EXERCISES', label: 'Exercices ciblés' },
    { value: 'COURSES', label: 'Cours théoriques' },
  ];

  protected readonly difficulties: Option[] = [
    { value: 'BEGINNER', label: 'Progressif' },
    { value: 'INTERMEDIATE', label: 'Équilibré' },
    { value: 'ADVANCED', label: 'Exigeant' },
  ];

  protected payload: QuestionnairePayload = {
    situation: {},
    skills: [],
    experience: { projects: [], experiences: [], certificates: [] },
    objective: {},
    preferences: { hoursPerWeek: 8, learningPreference: 'PROJECTS', preferredDifficulty: 'INTERMEDIATE' },
  };

  protected readonly situationForm = this.fb.nonNullable.group({
    situation: ['', Validators.required],
    domain: ['', [Validators.required, Validators.maxLength(160)]],
    educationLevel: ['', Validators.maxLength(120)],
    experienceSummary: ['', Validators.maxLength(2000)],
  });

  protected readonly objectiveForm = this.fb.nonNullable.group({
    targetJob: ['', [Validators.required, Validators.maxLength(160)]],
    careerGoal: ['', Validators.required],
    positionType: ['', Validators.required],
    targetCompany: ['', Validators.maxLength(160)],
  });

  protected readonly preferencesForm = this.fb.nonNullable.group({
    hoursPerWeek: [8, [Validators.required, Validators.min(1), Validators.max(80)]],
    deadline: ['', Validators.maxLength(60)],
  });

  ngOnInit(): void {
    this.http.get<Skill[]>('/api/skills').subscribe({
      next: (skills) => this.referenceSkills.set(skills),
      error: () => undefined,
    });
    this.http.get<QuestionnaireResponse>('/api/questionnaire/responses').subscribe({
      next: (response) => {
        if (response.payload) {
          this.payload = {
            situation: response.payload.situation ?? {},
            skills: response.payload.skills ?? [],
            experience: {
              projects: response.payload.experience?.projects ?? [],
              experiences: response.payload.experience?.experiences ?? [],
              certificates: response.payload.experience?.certificates ?? [],
              githubUrl: response.payload.experience?.githubUrl,
              portfolioUrl: response.payload.experience?.portfolioUrl,
            },
            objective: response.payload.objective ?? {},
            preferences: {
              hoursPerWeek: response.payload.preferences?.hoursPerWeek ?? 8,
              learningPreference:
                response.payload.preferences?.learningPreference ?? 'PROJECTS',
              preferredDifficulty:
                response.payload.preferences?.preferredDifficulty ?? 'INTERMEDIATE',
              deadline: response.payload.preferences?.deadline,
            },
          };
          this.situationForm.patchValue({
            situation: this.payload.situation?.situation ?? '',
            domain: this.payload.situation?.domain ?? '',
            educationLevel: this.payload.situation?.educationLevel ?? '',
            experienceSummary: this.payload.situation?.experienceSummary ?? '',
          });
          this.objectiveForm.patchValue({
            targetJob: this.payload.objective?.targetJob ?? '',
            careerGoal: this.payload.objective?.careerGoal ?? '',
            positionType: this.payload.objective?.positionType ?? '',
            targetCompany: this.payload.objective?.targetCompany ?? '',
          });
          this.preferencesForm.patchValue({
            hoursPerWeek: this.payload.preferences?.hoursPerWeek ?? 8,
            deadline: this.payload.preferences?.deadline ?? '',
          });
          const restoredStep = Math.max(1, Math.min(6, response.currentStep || 1));
          this.step.set(restoredStep);
          this.maxStep.set(restoredStep);
          if (response.completed) {
            this.maxStep.set(6);
          }
        }
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.notify.error('Impossible de charger vos réponses enregistrées.');
      },
    });
  }

  // --- step navigation ----------------------------------------------------

  protected goNext(): void {
    const error = this.validateStep(this.step());
    if (error) {
      this.stepError.set(error);
      this.submitAttempted.set(true);
      return;
    }
    this.stepError.set(null);
    this.submitAttempted.set(false);
    this.persistDraft();
    if (this.step() < 6) {
      this.step.update((value) => value + 1);
      this.maxStep.update((value) => Math.max(value, this.step()));
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  protected goPrev(): void {
    if (this.step() > 1) {
      this.stepError.set(null);
      this.step.update((value) => value - 1);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  protected goTo(index: number): void {
    if (index <= this.maxStep()) {
      this.step.set(index);
      this.stepError.set(null);
    }
  }

  private validateStep(step: number): string | null {
    switch (step) {
      case 1: {
        if (this.situationForm.invalid) {
          this.situationForm.markAllAsTouched();
          return 'Renseignez votre situation actuelle et votre domaine pour continuer.';
        }
        return null;
      }
      case 2:
        return this.skills().length === 0
          ? 'Sélectionnez au moins une compétence pour continuer.'
          : null;
      case 3: {
        const unevaluated = this.skills().filter((skill) => !skill.level);
        return unevaluated.length > 0
          ? `Indiquez un niveau pour : ${unevaluated.map((s) => s.name).join(', ')}.`
          : null;
      }
      case 4:
        return null;
      case 5: {
        if (this.objectiveForm.invalid) {
          this.objectiveForm.markAllAsTouched();
          return 'Indiquez le métier visé, votre objectif et le type de poste.';
        }
        return null;
      }
      case 6: {
        if (this.preferencesForm.invalid) {
          this.preferencesForm.markAllAsTouched();
          return 'Indiquez un nombre d’heures par semaine (1 à 80).';
        }
        return null;
      }
      default:
        return null;
    }
  }

  // --- draft persistence --------------------------------------------------

  private buildPayload(): QuestionnairePayload {
    if (this.step() >= 1 && this.situationForm.valid) {
      this.payload.situation = this.situationForm.getRawValue();
    }
    this.payload.skills = this.skills();
    this.payload.objective = this.objectiveForm.getRawValue();
    this.payload.preferences = {
      ...this.payload.preferences,
      ...this.preferencesForm.getRawValue(),
    };
    return this.payload;
  }

  private persistDraft(): void {
    this.saving.set(true);
    this.http
      .put<QuestionnaireResponse>('/api/questionnaire/responses', {
        currentStep: this.step(),
        payload: this.buildPayload(),
      })
      .subscribe({
        next: () => this.saving.set(false),
        error: () => {
          this.saving.set(false);
          this.notify.error('Impossible d’enregistrer le brouillon.');
        },
      });
  }

  saveDraft(): void {
    this.persistDraft();
    this.notify.success('Réponses enregistrées. Vous pourrez reprendre plus tard.');
  }

  // --- skills --------------------------------------------------------------

  protected get selectedNames(): Set<string> {
    return new Set(this.skills().map((skill) => skill.name.toLowerCase()));
  }

  protected filteredSkills(): Skill[] {
    const query = this.search().trim().toLowerCase();
    return this.referenceSkills().filter((skill) => {
      const inCategory = query ? true : skill.category === this.activeCategory();
      const matches = query ? skill.name.toLowerCase().includes(query) : true;
      return inCategory && matches;
    });
  }

  protected toggleSkill(skill: Skill): void {
    const exists = this.skills().find(
      (entry) => entry.name.toLowerCase() === skill.name.toLowerCase(),
    );
    if (exists) {
      this.skills.update((list) => list.filter((entry) => entry !== exists));
    } else {
      this.skills.update((list) => [
        ...list,
        { name: skill.name, skillId: skill.id, category: skill.category, level: undefined },
      ]);
    }
  }

  protected addCustomSkill(): void {
    const name = this.customSkill().trim();
    if (!name) return;
    const exists = this.skills().find(
      (entry) => entry.name.toLowerCase() === name.toLowerCase(),
    );
    if (!exists) {
      this.skills.update((list) => [...list, { name, category: 'OTHER', level: undefined }]);
    }
    this.customSkill.set('');
  }

  protected removeSkill(name: string): void {
    this.skills.update((list) => list.filter((entry) => entry.name !== name));
  }

  protected setLevel(name: string, level: SkillLevel): void {
    this.skills.update((list) =>
      list.map((entry) => (entry.name === name ? { ...entry, level } : entry)),
    );
  }

  protected levelOf(name: string): SkillLevel | undefined {
    return this.skills().find((entry) => entry.name === name)?.level;
  }

  // --- experience arrays ----------------------------------------------------

  protected addProject(): void {
    this.payload.experience = this.payload.experience ?? {};
    const projects = this.payload.experience.projects ?? [];
    projects.push({ name: '', description: '', repositoryUrl: '', technologies: '' });
    this.payload.experience.projects = projects;
  }

  protected addExperience(): void {
    this.payload.experience = this.payload.experience ?? {};
    const experiences = this.payload.experience.experiences ?? [];
    experiences.push({ title: '', organization: '', type: 'INTERNSHIP', description: '' });
    this.payload.experience.experiences = experiences;
  }

  protected addCertificate(): void {
    this.payload.experience = this.payload.experience ?? {};
    const certificates = this.payload.experience.certificates ?? [];
    certificates.push({ name: '', issuer: '' });
    this.payload.experience.certificates = certificates;
  }

  protected removeProject(index: number): void {
    this.payload.experience?.projects?.splice(index, 1);
  }

  protected removeExperience(index: number): void {
    this.payload.experience?.experiences?.splice(index, 1);
  }

  protected removeCertificate(index: number): void {
    this.payload.experience?.certificates?.splice(index, 1);
  }

  // --- completion -----------------------------------------------------------

  protected complete(): void {
    const error = this.validateStep(6);
    if (error) {
      this.stepError.set(error);
      return;
    }
    this.completing.set(true);
    const payload = this.buildPayload();
    this.http
      .post<{ profileCompleted: boolean }>('/api/questionnaire/complete', { payload })
      .subscribe({
        next: () => {
          this.completing.set(false);
          this.auth.setProfileCompleted(true);
          this.notify.success('Profil créé ! Votre dashboard est prêt.');
          this.router.navigate(['/dashboard']);
        },
        error: () => {
          this.completing.set(false);
          this.notify.error('La finalisation du profil a échoué. Réessayez.');
        },
      });
  }
}
