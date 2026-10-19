import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  AdminOverview,
  AdminSkill,
  AdminUser,
  AiUsageReport,
  AppSetting,
  ErrorLogEntry,
} from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { IconComponent } from '../../shared/icon';

type Tab = 'overview' | 'users' | 'skills' | 'ai' | 'errors' | 'settings';

/** Protected administration console (role ADMIN only). */
@Component({
  selector: 'sm-admin',
  imports: [FormsModule, IconComponent],
  templateUrl: './admin.html',
  styleUrl: './admin.css',
})
export class AdminComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly notify = inject(NotificationService);

  protected readonly tab = signal<Tab>('overview');
  protected readonly overview = signal<AdminOverview | null>(null);
  protected readonly users = signal<AdminUser[]>([]);
  protected readonly skills = signal<AdminSkill[]>([]);
  protected readonly usage = signal<AiUsageReport | null>(null);
  protected readonly errors = signal<ErrorLogEntry[]>([]);
  protected readonly settings = signal<AppSetting[]>([]);
  protected readonly loading = signal(true);
  protected readonly userQuery = signal('');
  protected readonly newSkill = signal({ name: '', category: 'LANGUAGES' });

  protected readonly tabs: { id: Tab; label: string; icon: string }[] = [
    { id: 'overview', label: 'Vue générale', icon: 'dashboard' },
    { id: 'users', label: 'Utilisateurs', icon: 'profile' },
    { id: 'skills', label: 'Référentiel', icon: 'spark' },
    { id: 'ai', label: 'Utilisation IA', icon: 'analysis' },
    { id: 'errors', label: 'Erreurs', icon: 'alert' },
    { id: 'settings', label: 'Paramètres', icon: 'settings' },
  ];

  ngOnInit(): void {
    this.http.get<AdminOverview>('/api/admin/overview').subscribe({
      next: (overview) => {
        this.overview.set(overview);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.loadUsers();
    this.http.get<AdminSkill[]>('/api/admin/skills').subscribe({ next: (s) => this.skills.set(s) });
  }

  protected selectTab(tab: Tab): void {
    this.tab.set(tab);
    if (tab === 'ai' && !this.usage()) {
      this.http.get<AiUsageReport>('/api/admin/ai-usage').subscribe({ next: (u) => this.usage.set(u) });
    }
    if (tab === 'errors' && this.errors().length === 0) {
      this.http.get<ErrorLogEntry[]>('/api/admin/errors').subscribe({ next: (e) => this.errors.set(e) });
    }
    if (tab === 'settings' && this.settings().length === 0) {
      this.http.get<AppSetting[]>('/api/admin/settings').subscribe({ next: (s) => this.settings.set(s) });
    }
  }

  protected loadUsers(): void {
    const query = this.userQuery().trim();
    this.http
      .get<AdminUser[]>(`/api/admin/users`, { params: query ? { q: query } : {} })
      .subscribe({ next: (users) => this.users.set(users) });
  }

  protected toggleUser(user: AdminUser): void {
    this.http.patch<AdminUser>(`/api/admin/users/${user.id}`, { enabled: !user.enabled }).subscribe({
      next: (updated) => {
        this.users.update((list) => list.map((entry) => (entry.id === updated.id ? updated : entry)));
        this.notify.success(updated.enabled ? 'Compte réactivé.' : 'Compte désactivé.');
      },
      error: () => undefined,
    });
  }

  protected promote(user: AdminUser): void {
    const nextRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
    this.http.patch<AdminUser>(`/api/admin/users/${user.id}`, { role: nextRole }).subscribe({
      next: (updated) => {
        this.users.update((list) => list.map((entry) => (entry.id === updated.id ? updated : entry)));
        this.notify.success(`Rôle mis à jour : ${nextRole}.`);
      },
      error: () => undefined,
    });
  }

  protected createSkill(): void {
    const draft = this.newSkill();
    if (!draft.name.trim()) return;
    this.http.post<AdminSkill>('/api/admin/skills', draft).subscribe({
      next: (created) => {
        this.skills.update((list) => [...list, created]);
        this.newSkill.set({ name: '', category: 'LANGUAGES' });
        this.notify.success('Compétence ajoutée au référentiel.');
      },
      error: () => undefined,
    });
  }

  protected deleteSkill(skill: AdminSkill): void {
    this.http.delete(`/api/admin/skills/${skill.id}`).subscribe({
      next: () => {
        this.skills.update((list) => list.filter((entry) => entry.id !== skill.id));
        this.notify.success('Compétence supprimée.');
      },
      error: () => undefined,
    });
  }

  protected saveSetting(setting: AppSetting, value: string): void {
    this.http.put<AppSetting[]>('/api/admin/settings', [{ key: setting.key, value }]).subscribe({
      next: (settings) => {
        this.settings.set(settings);
        this.notify.success(`Paramètre ${setting.key} enregistré.`);
      },
      error: () => undefined,
    });
  }

  protected shortDate(iso: string | null): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
