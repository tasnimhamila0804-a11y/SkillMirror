import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CompletionCheck, Evidence, LearningPath } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { IconComponent } from '../../shared/icon';

/** Project workspace: tasks, notes, proofs and explicit validation criteria. */
@Component({
  selector: 'sm-learning-detail',
  imports: [FormsModule, RouterLink, IconComponent],
  templateUrl: './learning-detail.html',
  styleUrl: './learning-detail.css',
})
export class LearningDetailComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly notify = inject(NotificationService);

  protected readonly path = signal<LearningPath | null>(null);
  protected readonly check = signal<CompletionCheck | null>(null);
  protected readonly loading = signal(true);
  protected readonly completing = signal(false);
  protected readonly evidenceType = signal<'LINK' | 'REPORT'>('LINK');
  protected readonly evidenceUrl = signal('');
  protected readonly evidenceDescription = signal('');
  protected readonly submittingEvidence = signal(false);
  protected readonly noteDrafts = signal<Record<number, string>>({});

  protected readonly difficultyLabels: Record<string, string> = {
    BEGINNER: 'Progressif',
    INTERMEDIATE: 'Équilibré',
    ADVANCED: 'Exigeant',
  };

  private pathId = 0;

  ngOnInit(): void {
    this.pathId = Number(this.route.snapshot.paramMap.get('id'));
    this.reload();
  }

  protected reload(): void {
    this.http.get<LearningPath>(`/api/learning-paths/${this.pathId}`).subscribe({
      next: (path) => {
        this.path.set(path);
        const drafts: Record<number, string> = {};
        path.tasks.forEach((task) => (drafts[task.id] = task.note ?? ''));
        this.noteDrafts.set(drafts);
        this.loading.set(false);
        this.loadCheck();
      },
      error: () => this.loading.set(false),
    });
  }

  private loadCheck(): void {
    this.http
      .get<CompletionCheck>(`/api/learning-paths/${this.pathId}/completion-check`)
      .subscribe({ next: (check) => this.check.set(check) });
  }

  protected toggleTask(taskId: number, done: boolean): void {
    this.http.patch<LearningPath>(`/api/learning-tasks/${taskId}`, { done }).subscribe({
      next: (updated) => {
        this.path.set(updated);
        this.loadCheck();
      },
      error: () => undefined,
    });
  }

  protected updateNoteDraft(taskId: number, value: string): void {
    this.noteDrafts.update((drafts) => ({ ...drafts, [taskId]: value }));
  }

  protected saveNote(taskId: number): void {
    const note = this.noteDrafts()[taskId] ?? '';
    this.http.patch<LearningPath>(`/api/learning-tasks/${taskId}`, { note }).subscribe({
      next: (updated) => {
        this.path.set(updated);
        this.notify.success('Note enregistrée.');
      },
      error: () => undefined,
    });
  }

  protected setStatus(status: 'IN_PROGRESS' | 'PAUSED'): void {
    this.http.patch<LearningPath>(`/api/learning-paths/${this.pathId}/status`, { status }).subscribe({
      next: (updated) => this.path.set(updated),
      error: () => undefined,
    });
  }

  protected submitEvidence(): void {
    const type = this.evidenceType();
    if (type === 'LINK' && !this.evidenceUrl().startsWith('http')) {
      this.notify.error('Fournissez un lien commençant par http(s)://.');
      return;
    }
    if (type === 'REPORT' && this.evidenceDescription().trim().length < 20) {
      this.notify.error('Le rapport doit contenir au moins 20 caractères.');
      return;
    }
    this.submittingEvidence.set(true);
    this.http
      .post<Evidence>(`/api/learning-paths/${this.pathId}/evidence`, {
        type,
        url: type === 'LINK' ? this.evidenceUrl() : null,
        description: this.evidenceDescription(),
      })
      .subscribe({
        next: () => {
          this.submittingEvidence.set(false);
          this.evidenceUrl.set('');
          this.evidenceDescription.set('');
          this.notify.success('Preuve soumise. Elle sera prise en compte à la validation.');
          this.reload();
        },
        error: () => this.submittingEvidence.set(false),
      });
  }

  protected uploadFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    formData.append('description', 'Fichier joint au parcours');
    this.submittingEvidence.set(true);
    this.http
      .post<Evidence>(`/api/learning-paths/${this.pathId}/evidence/upload`, formData)
      .subscribe({
        next: () => {
          this.submittingEvidence.set(false);
          this.notify.success(`Fichier « ${file.name} » joint au parcours.`);
          input.value = '';
          this.reload();
        },
        error: () => {
          this.submittingEvidence.set(false);
          input.value = '';
        },
      });
  }

  protected removeEvidence(evidence: Evidence): void {
    this.http.delete(`/api/evidence/${evidence.id}`).subscribe({
      next: () => {
        this.notify.success('Preuve retirée.');
        this.reload();
      },
    });
  }

  protected complete(): void {
    this.completing.set(true);
    this.http.post<LearningPath>(`/api/learning-paths/${this.pathId}/complete`, {}).subscribe({
      next: (updated) => {
        this.path.set(updated);
        this.completing.set(false);
        this.notify.success('Parcours validé ! Votre Skill DNA a été mis à jour.');
        this.loadCheck();
      },
      error: () => this.completing.set(false),
    });
  }

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

  protected get isActive(): boolean {
    const status = this.path()?.status;
    return status === 'NOT_STARTED' || status === 'IN_PROGRESS' || status === 'PAUSED';
  }

  protected get isDone(): boolean {
    const status = this.path()?.status;
    return status === 'COMPLETED' || status === 'VALIDATED';
  }
}
