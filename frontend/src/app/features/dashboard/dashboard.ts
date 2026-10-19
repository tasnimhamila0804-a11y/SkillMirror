import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { AnalysisSummary, Dashboard, PrioritySkill } from '../../core/models';
import { EmptyStateComponent } from '../../shared/empty-state';
import { IconComponent } from '../../shared/icon';
import { ScoreRingComponent } from '../../shared/score-ring';

const MATCH_LABELS: Record<string, string> = {
  DEMONSTRATED: 'Démontrée',
  DECLARED: 'Déclarée',
  PARTIAL: 'Partielle',
  MISSING: 'Manquante',
};

const MATCH_CLASSES: Record<string, string> = {
  DEMONSTRATED: 'badge-mint',
  DECLARED: 'badge-violet',
  PARTIAL: 'badge-amber',
  MISSING: 'badge-danger',
};

/** Personal dashboard — every value comes from the backend. */
@Component({
  selector: 'sm-dashboard',
  imports: [RouterLink, IconComponent, ScoreRingComponent, EmptyStateComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit {
  private readonly http = inject(HttpClient);
  protected readonly auth = inject(AuthService);

  protected readonly data = signal<Dashboard | null>(null);
  protected readonly loading = signal(true);

  protected readonly matchLabels = MATCH_LABELS;
  protected readonly matchClasses = MATCH_CLASSES;

  ngOnInit(): void {
    this.http.get<Dashboard>('/api/dashboard').subscribe({
      next: (dashboard) => {
        this.data.set(dashboard);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected shortDate(iso: string | null): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  protected prioritizedSkills(skills: PrioritySkill[]): PrioritySkill[] {
    return [...skills].sort((a, b) => a.priority - b.priority);
  }

  protected analysisScoreClass(analysis: AnalysisSummary): string {
    if (analysis.score >= 70) return 'mint';
    if (analysis.score >= 45) return 'violet';
    return '';
  }
}
