import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AnalysisDetail, AnalysisResult, AnalysisSummary, MatchCategory } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { EmptyStateComponent } from '../../shared/empty-state';
import { IconComponent } from '../../shared/icon';
import { ScoreRingComponent } from '../../shared/score-ring';

const MATCH_LABELS: Record<MatchCategory, string> = {
  DEMONSTRATED: 'Démontrée',
  DECLARED: 'Déclarée',
  PARTIAL: 'Partielle',
  MISSING: 'Manquante',
};

/** Career Gap Engine results page. */
@Component({
  selector: 'sm-analysis',
  imports: [RouterLink, IconComponent, ScoreRingComponent, EmptyStateComponent],
  templateUrl: './analysis.html',
  styleUrl: './analysis.css',
})
export class AnalysisComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly notify = inject(NotificationService);

  protected readonly result = signal<AnalysisResult | null>(null);
  protected readonly history = signal<AnalysisSummary[]>([]);
  protected readonly loading = signal(true);
  protected readonly generating = signal(false);

  protected readonly matchLabels = MATCH_LABELS;
  protected readonly Math = Math;
  protected readonly Number = Number;

  protected readonly strengths = signal<AnalysisDetail[]>([]);
  protected readonly toImprove = signal<AnalysisDetail[]>([]);
  protected readonly missing = signal<AnalysisDetail[]>([]);

  ngOnInit(): void {
    this.http.get<AnalysisSummary[]>('/api/analyses').subscribe({
      next: (history) => {
        this.history.set(history);
        const routeId = this.route.snapshot.paramMap.get('id');
        const id = routeId ? Number(routeId) : history[0]?.id;
        if (!id) {
          this.loading.set(false);
          return;
        }
        this.loadAnalysis(id);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadAnalysis(id: number): void {
    this.loading.set(true);
    this.http.get<AnalysisResult>(`/api/analyses/${id}`).subscribe({
      next: (result) => {
        this.result.set(result);
        this.strengths.set(
          result.details.filter((d) => d.matchCategory === 'DEMONSTRATED' || d.matchCategory === 'DECLARED'),
        );
        this.toImprove.set(result.details.filter((d) => d.matchCategory === 'PARTIAL'));
        this.missing.set(
          result.details.filter((d) => d.matchCategory === 'MISSING').sort((a, b) => b.priority - a.priority),
        );
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected select(analysisId: number): void {
    this.router.navigate(['/analysis', analysisId]);
    this.loadAnalysis(analysisId);
  }

  protected generatePaths(): void {
    const result = this.result();
    if (!result) return;
    this.generating.set(true);
    this.http
      .post<{ id: number }[]>('/api/learning-paths/generate', {
        analysisId: result.analysis.id,
        limit: 3,
      })
      .subscribe({
        next: (paths) => {
          this.generating.set(false);
          this.notify.success(`${paths.length} parcours généré(s). Bonne progression !`);
          this.router.navigate(['/learning']);
        },
        error: () => this.generating.set(false),
      });
  }

  protected shortDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    });
  }

  protected get masteryPercent(): number {
    return Math.round((this.result()?.details.reduce((sum, d) => sum + Number(d.masteryScore), 0) ?? 0)
      / Math.max(1, this.result()?.details.length ?? 1) * 100);
  }
}
