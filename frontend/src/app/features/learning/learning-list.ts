import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LearningPathSummary } from '../../core/models';
import { EmptyStateComponent } from '../../shared/empty-state';
import { IconComponent } from '../../shared/icon';

/** Generated learning paths (Project-to-Proof). */
@Component({
  selector: 'sm-learning-list',
  imports: [RouterLink, IconComponent, EmptyStateComponent],
  templateUrl: './learning-list.html',
  styleUrl: './learning-list.css',
})
export class LearningListComponent implements OnInit {
  private readonly http = inject(HttpClient);

  protected readonly paths = signal<LearningPathSummary[]>([]);
  protected readonly loading = signal(true);
  protected readonly filter = signal<'ALL' | 'ACTIVE' | 'DONE'>('ALL');

  protected readonly difficultyLabels: Record<string, string> = {
    BEGINNER: 'Progressif',
    INTERMEDIATE: 'Équilibré',
    ADVANCED: 'Exigeant',
  };

  protected readonly statusLabels: Record<string, string> = {
    NOT_STARTED: 'Non commencé',
    IN_PROGRESS: 'En cours',
    PAUSED: 'En pause',
    COMPLETED: 'Terminé',
    VALIDATED: 'Validé',
  };

  ngOnInit(): void {
    this.http.get<LearningPathSummary[]>('/api/learning-paths').subscribe({
      next: (paths) => {
        this.paths.set(paths);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected get filtered(): LearningPathSummary[] {
    const all = this.paths();
    switch (this.filter()) {
      case 'ACTIVE':
        return all.filter((path) =>
          ['NOT_STARTED', 'IN_PROGRESS', 'PAUSED'].includes(path.status));
      case 'DONE':
        return all.filter((path) => ['COMPLETED', 'VALIDATED'].includes(path.status));
      default:
        return all;
    }
  }

  protected statusClass(status: string): string {
    if (status === 'VALIDATED') return 'badge-mint';
    if (status === 'IN_PROGRESS') return 'badge-violet';
    if (status === 'PAUSED') return 'badge-amber';
    return 'badge-slate';
  }
}
