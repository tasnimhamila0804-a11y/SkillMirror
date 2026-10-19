import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { JobSummary } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { EmptyStateComponent } from '../../shared/empty-state';
import { IconComponent } from '../../shared/icon';

/** Saved job offers with their extraction state. */
@Component({
  selector: 'sm-jobs-list',
  imports: [RouterLink, IconComponent, EmptyStateComponent],
  templateUrl: './jobs-list.html',
  styleUrl: './jobs-list.css',
})
export class JobsListComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly notify = inject(NotificationService);

  protected readonly jobs = signal<JobSummary[]>([]);
  protected readonly loading = signal(true);

  protected readonly statusLabels: Record<string, string> = {
    DRAFT: 'Brouillon',
    EXTRACTED: 'Compétences extraites',
    CONFIRMED: 'Prête pour analyse',
  };

  ngOnInit(): void {
    this.reload();
  }

  protected reload(): void {
    this.http.get<JobSummary[]>('/api/jobs').subscribe({
      next: (jobs) => {
        this.jobs.set(jobs);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected analyze(job: JobSummary): void {
    this.http.post<{ analysis: { id: number } }>('/api/analyses', { jobOfferId: job.id }).subscribe({
      next: (result) => {
        this.notify.success('Analyse calculée.');
        this.router.navigate(['/analysis', result.analysis.id]);
      },
      error: () => undefined,
    });
  }

  protected delete(job: JobSummary): void {
    this.http.delete(`/api/jobs/${job.id}`).subscribe({
      next: () => {
        this.jobs.update((list) => list.filter((entry) => entry.id !== job.id));
        this.notify.success('Offre supprimée.');
      },
      error: () => undefined,
    });
  }

  protected shortDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
