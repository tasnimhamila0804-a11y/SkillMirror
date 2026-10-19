import { Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { IconComponent } from '../icon';

interface NavLink {
  label: string;
  path: string;
  icon: string;
}

/** Sidebar + topbar layout used by every authenticated page. */
@Component({
  selector: 'sm-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, IconComponent],
  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class ShellComponent {
  protected readonly auth = inject(AuthService);
  protected readonly sidebarOpen = signal(false);
  protected readonly pageTitle = signal('Tableau de bord');

  protected readonly mainNav: NavLink[] = [
    { label: 'Tableau de bord', path: '/dashboard', icon: 'dashboard' },
    { label: 'Mon profil', path: '/profile', icon: 'profile' },
    { label: 'Offres & analyses', path: '/jobs', icon: 'jobs' },
    { label: 'Mes parcours', path: '/learning', icon: 'learning' },
    { label: 'Progression', path: '/progress', icon: 'progress' },
  ];

  protected readonly accountNav: NavLink[] = [
    { label: 'Paramètres', path: '/settings', icon: 'settings' },
  ];

  private readonly router = inject(Router);

  constructor() {
    this.auth.ensureLoaded();
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.sidebarOpen.set(false);
        this.pageTitle.set(this.titleFor(event.urlAfterRedirects));
      });
    this.pageTitle.set(this.titleFor(this.router.url));
  }

  private titleFor(url: string): string {
    if (url.startsWith('/questionnaire')) return 'Profilage initial';
    if (url.startsWith('/dashboard')) return 'Tableau de bord';
    if (url.startsWith('/profile')) return 'Mon profil — Skill DNA';
    if (url.startsWith('/jobs/new')) return "Nouvelle offre d'emploi";
    if (url.startsWith('/jobs')) return "Offres d'emploi";
    if (url.startsWith('/analysis')) return 'Analyse des écarts';
    if (url.startsWith('/learning')) return 'Parcours d\u2019apprentissage';
    if (url.startsWith('/progress')) return 'Ma progression';
    if (url.startsWith('/settings')) return 'Paramètres du compte';
    if (url.startsWith('/admin')) return 'Administration';
    return 'SkillMirror';
  }

  toggleSidebar(): void {
    this.sidebarOpen.update((open) => !open);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }
}
