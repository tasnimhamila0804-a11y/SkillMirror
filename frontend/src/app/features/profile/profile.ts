import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, KeyValuePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  Certificate,
  Evidence,
  Experience,
  PortfolioProject,
  Profile,
  Skill,
  SkillLevel,
  SkillStatus,
  UserSkill,
} from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { EmptyStateComponent } from '../../shared/empty-state';
import { IconComponent } from '../../shared/icon';

type ModalKind = 'profile' | 'skill' | 'experience' | 'project' | 'certificate' | null;

const LEVEL_LABELS: Record<SkillLevel, string> = {
  BEGINNER: 'Débutant',
  INTERMEDIATE: 'Intermédiaire',
  ADVANCED: 'Avancé',
  EXPERT: 'Expert',
  UNKNOWN: 'Non évalué',
};

const LEVEL_RANK: Record<SkillLevel, number> = {
  UNKNOWN: 0,
  BEGINNER: 1,
  INTERMEDIATE: 2,
  ADVANCED: 3,
  EXPERT: 4,
};

const STATUS_LABELS: Record<SkillStatus, string> = {
  DECLARED: 'Déclarée',
  EVALUATED: 'Évaluée',
  DEMONSTRATED: 'Démontrée',
};

const STATUS_CLASSES: Record<SkillStatus, string> = {
  DECLARED: 'badge-slate',
  EVALUATED: 'badge-violet',
  DEMONSTRATED: 'badge-mint',
};

const SITUATIONS: Record<string, string> = {
  STUDENT: 'Étudiant',
  YOUNG_GRADUATE: 'Jeune diplômé',
  EMPLOYED: 'Salarié',
  CAREER_CHANGE: 'En reconversion',
  JOB_SEEKING: 'En recherche d’emploi',
};

/** Professional profile — the Skill Mirror / Skill DNA view. */
@Component({
  selector: 'sm-profile',
  imports: [FormsModule, RouterLink, IconComponent, EmptyStateComponent, DatePipe, KeyValuePipe],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class ProfileComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly notify = inject(NotificationService);

  protected readonly profile = signal<Profile | null>(null);
  protected readonly skills = signal<UserSkill[]>([]);
  protected readonly experiences = signal<Experience[]>([]);
  protected readonly certificates = signal<Certificate[]>([]);
  protected readonly projects = signal<PortfolioProject[]>([]);
  protected readonly evidences = signal<Evidence[]>([]);
  protected readonly referenceSkills = signal<Skill[]>([]);
  protected readonly loading = signal(true);

  protected readonly modal = signal<ModalKind>(null);
  protected readonly busy = signal(false);
  protected readonly skillSearch = signal('');
  protected readonly hoveredSkill = signal<string | null>(null);

  protected readonly levelLabels = LEVEL_LABELS;
  protected readonly statusLabels = STATUS_LABELS;
  protected readonly statusClasses = STATUS_CLASSES;
  protected readonly situations = SITUATIONS;

  // draft objects for the modals
  protected readonly draftProfile: {
    situation: string; domain: string; educationLevel: string; targetJob: string;
    careerGoal: string; positionType: string; hoursPerWeek: number | null;
    headline: string; bio: string; githubUrl: string; portfolioUrl: string;
  } = { situation: '', domain: '', educationLevel: '', targetJob: '', careerGoal: '', positionType: '', hoursPerWeek: null, headline: '', bio: '', githubUrl: '', portfolioUrl: '' };

  protected readonly draftExperience: { title: string; organization: string; type: string; description: string; startDate: string; endDate: string } =
    { title: '', organization: '', type: 'INTERNSHIP', description: '', startDate: '', endDate: '' };
  protected readonly draftProject: { name: string; description: string; repositoryUrl: string; demoUrl: string; technologies: string } =
    { name: '', description: '', repositoryUrl: '', demoUrl: '', technologies: '' };
  protected readonly draftCertificate: { name: string; issuer: string; issuedDate: string; url: string } =
    { name: '', issuer: '', issuedDate: '', url: '' };

  /** Up to 8 skills feed the radar; the full list stays available as text. */
  protected readonly radarSkills = computed(() => this.skills().slice(0, 8));
  protected readonly radarPointsString = computed(() => this.buildRadarPolygon());
  protected readonly center = 120;
  protected readonly maxRadius = 88;

  protected readonly demonstratedCount = computed(
    () => this.skills().filter((skill) => skill.status === 'DEMONSTRATED').length,
  );
  protected readonly verifiedProofs = computed(
    () => this.evidences().filter((evidence) => evidence.status === 'VERIFIED').length,
  );

  ngOnInit(): void {
    this.http.get<Profile>('/api/profiles/me').subscribe({ next: (p) => this.profile.set(p) });
    this.http.get<UserSkill[]>('/api/profiles/me/skills').subscribe({ next: (s) => this.skills.set(s) });
    this.http.get<Experience[]>('/api/profiles/me/experiences').subscribe({ next: (e) => this.experiences.set(e) });
    this.http.get<Certificate[]>('/api/profiles/me/certificates').subscribe({ next: (c) => this.certificates.set(c) });
    this.http.get<PortfolioProject[]>('/api/profiles/me/projects').subscribe({ next: (p) => this.projects.set(p) });
    this.http.get<Evidence[]>('/api/evidence').subscribe({ next: (e) => this.evidences.set(e) });
    this.http.get<Skill[]>('/api/skills').subscribe({
      next: (s) => this.referenceSkills.set(s),
      complete: () => this.loading.set(false),
      error: () => this.loading.set(false),
    });
  }

  // --- radar ---------------------------------------------------------------

  protected radarAxis(skill: UserSkill, index: number): { x: number; y: number; labelX: number; labelY: number } {
    const count = Math.max(1, this.radarSkills().length);
    const angle = (index / count) * 2 * Math.PI - Math.PI / 2;
    const rank = LEVEL_RANK[skill.level] / 4;
    const radius = 14 + rank * (this.maxRadius - 14);
    return {
      x: this.center + Math.cos(angle) * radius,
      y: this.center + Math.sin(angle) * radius,
      labelX: this.center + Math.cos(angle) * (this.maxRadius + 18),
      labelY: this.center + Math.sin(angle) * (this.maxRadius + 18),
    };
  }

  private buildRadarPolygon(): string {
    return this.radarSkills()
      .map((skill, index) => {
        const point = this.radarAxis(skill, index);
        return `${point.x.toFixed(1)},${point.y.toFixed(1)}`;
      })
      .join(' ');
  }

  protected ringPolygon(fraction: number): string {
    const count = Math.max(1, this.radarSkills().length);
    return Array.from({ length: count }, (_, index) => {
      const angle = (index / count) * 2 * Math.PI - Math.PI / 2;
      const radius = 14 + fraction * (this.maxRadius - 14);
      return `${(this.center + Math.cos(angle) * radius).toFixed(1)},${(this.center + Math.sin(angle) * radius).toFixed(1)}`;
    }).join(' ');
  }

  // --- skills --------------------------------------------------------------

  protected filteredReferenceSkills(): Skill[] {
    const query = this.skillSearch().trim().toLowerCase();
    const owned = new Set(this.skills().map((skill) => skill.name.toLowerCase()));
    return this.referenceSkills()
      .filter((skill) => !owned.has(skill.name.toLowerCase()))
      .filter((skill) => (query ? skill.name.toLowerCase().includes(query) : true))
      .slice(0, 40);
  }

  protected addSkill(skill: Skill): void {
    this.busy.set(true);
    this.http.post<UserSkill>('/api/profiles/me/skills', { skillId: skill.id, level: 'UNKNOWN' }).subscribe({
      next: (created) => {
        this.skills.update((list) => [...list, created]);
        this.busy.set(false);
        this.notify.success(`« ${skill.name} » ajoutée à votre profil.`);
      },
      error: () => this.busy.set(false),
    });
  }

  protected setLevel(skill: UserSkill, level: SkillLevel): void {
    this.http.put<UserSkill>(`/api/profiles/me/skills/${skill.id}`, { level }).subscribe({
      next: (updated) => {
        this.skills.update((list) =>
          list.map((entry) => (entry.id === updated.id ? updated : entry)),
        );
      },
      error: () => undefined,
    });
  }

  protected removeSkill(skill: UserSkill): void {
    this.http.delete(`/api/profiles/me/skills/${skill.id}`).subscribe({
      next: () => {
        this.skills.update((list) => list.filter((entry) => entry.id !== skill.id));
        this.notify.success(`« ${skill.name} » retirée.`);
      },
      error: () => undefined,
    });
  }

  // --- profile ---------------------------------------------------------------

  protected openProfileModal(): void {
    const profile = this.profile();
    if (!profile) return;
    this.draftProfile.situation = profile.situation ?? '';
    this.draftProfile.domain = profile.domain ?? '';
    this.draftProfile.educationLevel = profile.educationLevel ?? '';
    this.draftProfile.targetJob = profile.targetJob ?? '';
    this.draftProfile.careerGoal = profile.careerGoal ?? '';
    this.draftProfile.positionType = profile.positionType ?? '';
    this.draftProfile.hoursPerWeek = profile.hoursPerWeek;
    this.draftProfile.headline = profile.headline ?? '';
    this.draftProfile.bio = profile.bio ?? '';
    this.draftProfile.githubUrl = profile.githubUrl ?? '';
    this.draftProfile.portfolioUrl = profile.portfolioUrl ?? '';
    this.modal.set('profile');
  }

  protected saveProfile(): void {
    this.busy.set(true);
    this.http.put<Profile>('/api/profiles/me', this.draftProfile).subscribe({
      next: (updated) => {
        this.profile.set(updated);
        this.modal.set(null);
        this.busy.set(false);
        this.notify.success('Profil mis à jour.');
      },
      error: () => this.busy.set(false),
    });
  }

  // --- portfolio --------------------------------------------------------------

  protected saveExperience(): void {
    this.busy.set(true);
    this.http.post<Experience>('/api/profiles/me/experiences', this.draftExperience).subscribe({
      next: (created) => {
        this.experiences.update((list) => [created, ...list]);
        this.modal.set(null);
        this.busy.set(false);
        this.notify.success('Expérience ajoutée.');
      },
      error: () => this.busy.set(false),
    });
  }

  protected saveProject(): void {
    this.busy.set(true);
    this.http.post<PortfolioProject>('/api/profiles/me/projects', this.draftProject).subscribe({
      next: (created) => {
        this.projects.update((list) => [created, ...list]);
        this.modal.set(null);
        this.busy.set(false);
        this.notify.success('Projet ajouté.');
      },
      error: () => this.busy.set(false),
    });
  }

  protected saveCertificate(): void {
    this.busy.set(true);
    this.http.post<Certificate>('/api/profiles/me/certificates', this.draftCertificate).subscribe({
      next: (created) => {
        this.certificates.update((list) => [created, ...list]);
        this.modal.set(null);
        this.busy.set(false);
        this.notify.success('Certification ajoutée.');
      },
      error: () => this.busy.set(false),
    });
  }

  protected deleteExperience(experience: Experience): void {
    this.http.delete(`/api/profiles/me/experiences/${experience.id}`).subscribe({
      next: () => this.experiences.update((list) => list.filter((e) => e.id !== experience.id)),
    });
  }

  protected deleteProject(project: PortfolioProject): void {
    this.http.delete(`/api/profiles/me/projects/${project.id}`).subscribe({
      next: () => this.projects.update((list) => list.filter((p) => p.id !== project.id)),
    });
  }

  protected deleteCertificate(certificate: Certificate): void {
    this.http.delete(`/api/profiles/me/certificates/${certificate.id}`).subscribe({
      next: () => this.certificates.update((list) => list.filter((c) => c.id !== certificate.id)),
    });
  }

  protected closeModal(): void {
    this.modal.set(null);
  }

  /** Owner-only download (fetches a blob with the auth header). */
  protected downloadEvidence(evidence: Evidence): void {
    this.http.get(`/api/evidence/${evidence.id}/file`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = evidence.fileName ?? 'preuve';
        anchor.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.notify.error('Téléchargement impossible.'),
    });
  }

  protected readonly levelValues = Object.keys(LEVEL_LABELS) as SkillLevel[];
}
