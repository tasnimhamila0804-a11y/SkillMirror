import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProgressReport, ScorePoint, SkillStatus } from '../../core/models';
import { EmptyStateComponent } from '../../shared/empty-state';
import { IconComponent } from '../../shared/icon';

/** Progress page: paths, score history, skill evolution, proofs and timeline. */
@Component({
  selector: 'sm-progress',
  imports: [RouterLink, IconComponent, EmptyStateComponent],
  templateUrl: './progress.html',
  styleUrl: './progress.css',
})
export class ProgressComponent implements OnInit {
  private readonly http = inject(HttpClient);

  protected readonly report = signal<ProgressReport | null>(null);
  protected readonly loading = signal(true);
  protected readonly Math = Math;

  protected readonly statusLabels: Record<SkillStatus, string> = {
    DECLARED: 'Déclarée',
    EVALUATED: 'Évaluée',
    DEMONSTRATED: 'Démontrée',
  };
  protected readonly statusClasses: Record<SkillStatus, string> = {
    DECLARED: 'badge-slate',
    EVALUATED: 'badge-violet',
    DEMONSTRATED: 'badge-mint',
  };
  protected readonly levelLabels: Record<string, string> = {
    BEGINNER: 'Débutant',
    INTERMEDIATE: 'Intermédiaire',
    ADVANCED: 'Avancé',
    EXPERT: 'Expert',
    UNKNOWN: 'Non évalué',
  };
  protected readonly pathStatusLabels: Record<string, string> = {
    NOT_STARTED: 'Non commencé',
    IN_PROGRESS: 'En cours',
    PAUSED: 'En pause',
    COMPLETED: 'Terminé',
    VALIDATED: 'Validé',
  };
  protected readonly eventTypeLabels: Record<string, string> = {
    ANALYSIS: 'Analyse',
    PATH: 'Parcours',
    EVIDENCE: 'Preuve',
    PROJECT: 'Projet',
  };

  /** Score history as SVG points, oldest first. */
  protected readonly chartPoints = computed(() => {
    const history = [...(this.report()?.scoreHistory ?? [])].sort(
      (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime(),
    );
    const width = 520;
    const height = 170;
    const padding = 28;
    const count = history.length;
    return history.map((point: ScorePoint, index) => {
      const x = count === 1
        ? width / 2
        : padding + (index / (count - 1)) * (width - padding * 2);
      const y = height - padding - (point.score / 100) * (height - padding * 2);
      return { x, y, score: point.score, label: point.jobTitle, date: point.date };
    });
  });

  protected readonly polyline = computed(() =>
    this.chartPoints().map((point) => `${point.x.toFixed(1)},${point.y.toFixed(1)}`).join(' '),
  );

  ngOnInit(): void {
    this.http.get<ProgressReport>('/api/progress').subscribe({
      next: (report) => {
        this.report.set(report);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected shortDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
