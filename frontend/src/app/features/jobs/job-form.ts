import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ExtractResponse, JobOffer, ReviewedSkill } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { IconComponent } from '../../shared/icon';

/**
 * One workspace to create an offer, run the AI extraction, review the extracted
 * skills (correct / add / remove) and launch the compatibility analysis.
 */
@Component({
  selector: 'sm-job-form',
  imports: [ReactiveFormsModule, RouterLink, IconComponent],
  templateUrl: './job-form.html',
  styleUrl: './job-form.css',
})
export class JobFormComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly notify = inject(NotificationService);

  protected readonly jobId = signal<number | null>(null);
  protected readonly job = signal<JobOffer | null>(null);
  protected readonly skills = signal<ReviewedSkill[]>([]);
  protected readonly simulated = signal(false);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly extracting = signal(false);
  protected readonly analyzing = signal(false);
  protected readonly formError = signal<string | null>(null);

  protected readonly levels = ['', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];
  protected readonly levelLabels: Record<string, string> = {
    '': 'Non précisé',
    BEGINNER: 'Débutant',
    INTERMEDIATE: 'Intermédiaire',
    ADVANCED: 'Avancé',
    EXPERT: 'Expert',
  };

  protected readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(190)]],
    company: ['', Validators.maxLength(190)],
    description: ['', [Validators.required, Validators.maxLength(30000)]],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) return;
    const id = Number(idParam);
    if (Number.isNaN(id)) return;
    this.jobId.set(id);
    this.loading.set(true);
    this.http.get<JobOffer>(`/api/jobs/${id}`).subscribe({
      next: (job) => {
        this.job.set(job);
        this.form.patchValue({
          title: job.title,
          company: job.company ?? '',
          description: job.description,
        });
        this.skills.set(
          job.skills.map((skill) => ({
            name: skill.name,
            category: skill.category,
            requirement: skill.requirement,
            weight: skill.weight,
            requiredLevel: skill.requiredLevel ?? undefined,
          })),
        );
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected get isEditing(): boolean {
    return this.jobId() !== null;
  }

  /** Saves the offer (create or update) without extraction. */
  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.formError.set('Renseignez l’intitulé et le texte de l’offre.');
      return;
    }
    this.formError.set(null);
    this.saving.set(true);
    const value = this.form.getRawValue();
    const request$ = this.isEditing
      ? this.http.put<JobOffer>(`/api/jobs/${this.jobId()}`, value)
      : this.http.post<JobOffer>('/api/jobs', value);
    request$.subscribe({
      next: (job) => {
        this.job.set(job);
        this.jobId.set(job.id);
        this.saving.set(false);
        this.notify.success('Offre enregistrée.');
      },
      error: () => this.saving.set(false),
    });
  }

  /** Saves, then runs the AI extraction (or re-extraction). */
  protected extract(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.formError.set('Renseignez l’intitulé et le texte de l’offre avant l’extraction.');
      return;
    }
    this.formError.set(null);
    this.extracting.set(true);
    const value = this.form.getRawValue();
    const save$ = this.isEditing
      ? this.http.put<JobOffer>(`/api/jobs/${this.jobId()}`, value)
      : this.http.post<JobOffer>('/api/jobs', value);

    save$.subscribe({
      next: (job) => {
        this.job.set(job);
        this.jobId.set(job.id);
        this.http.post<ExtractResponse>(`/api/jobs/${job.id}/extract`, {}).subscribe({
          next: (result) => {
            this.job.set(result.job);
            this.simulated.set(result.simulated);
            this.skills.set(
              result.job.skills.map((skill) => ({
                name: skill.name,
                category: skill.category,
                requirement: skill.requirement,
                weight: skill.weight,
                requiredLevel: skill.requiredLevel ?? undefined,
              })),
            );
            this.extracting.set(false);
            this.notify.success(
              result.simulated
                ? 'Extraction simulée (mode démonstration) : vérifiez les compétences.'
                : 'Compétences extraites : vérifiez-les avant de lancer l’analyse.',
            );
          },
          error: () => this.extracting.set(false),
        });
      },
      error: () => this.extracting.set(false),
    });
  }

  protected confirmSkills(): void {
    if (this.skills().length === 0) {
      this.notify.error('Ajoutez au moins une compétence avant de confirmer.');
      return;
    }
    this.saving.set(true);
    this.http
      .put<JobOffer>(`/api/jobs/${this.jobId()}/skills`, { skills: this.skills() })
      .subscribe({
        next: (job) => {
          this.job.set(job);
          this.saving.set(false);
          this.notify.success('Compétences validées. Vous pouvez lancer l’analyse.');
        },
        error: () => this.saving.set(false),
      });
  }

  protected analyze(): void {
    this.analyzing.set(true);
    this.http.post<{ analysis: { id: number } }>('/api/analyses', { jobOfferId: this.jobId() }).subscribe({
      next: (result) => {
        this.analyzing.set(false);
        this.router.navigate(['/analysis', result.analysis.id]);
      },
      error: () => this.analyzing.set(false),
    });
  }

  // --- skill review ---------------------------------------------------------

  protected addSkillRow(): void {
    this.skills.update((list) => [
      ...list,
      { name: '', category: 'OTHER', requirement: 'REQUIRED', weight: 3, requiredLevel: '' },
    ]);
  }

  protected removeSkillRow(index: number): void {
    this.skills.update((list) => list.filter((_, i) => i !== index));
  }

  protected updateSkill(index: number, patch: Partial<ReviewedSkill>): void {
    this.skills.update((list) =>
      list.map((skill, i) => {
        if (i !== index) return skill;
        const merged = { ...skill, ...patch };
        if (merged.requirement === 'DESIRED' && merged.weight === 3) merged.weight = 1;
        if (merged.requirement === 'REQUIRED' && merged.weight === 1) merged.weight = 3;
        return merged;
      }),
    );
  }

  protected get confirmedSkills(): number {
    return this.job()?.skills?.filter((skill) => skill.confirmed).length ?? 0;
  }

  protected get extractionStatus(): string {
    return this.job()?.extractionStatus ?? 'DRAFT';
  }
}
