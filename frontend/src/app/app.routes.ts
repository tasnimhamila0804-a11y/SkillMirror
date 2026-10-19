import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard, onboardingGuard } from './core/auth.guard';
import { ShellComponent } from './shared/shell/shell';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/landing/landing').then((m) => m.LandingComponent),
    title: 'SkillMirror — See your skills. Unlock your potential.',
  },
  {
    path: 'auth/login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login').then((m) => m.LoginComponent),
    title: 'Connexion — SkillMirror',
  },
  {
    path: 'auth/register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/register').then((m) => m.RegisterComponent),
    title: 'Créer un compte — SkillMirror',
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard, onboardingGuard],
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.DashboardComponent) },
      { path: 'questionnaire', loadComponent: () => import('./features/questionnaire/questionnaire').then((m) => m.QuestionnaireComponent) },
      { path: 'profile', loadComponent: () => import('./features/profile/profile').then((m) => m.ProfileComponent) },
      { path: 'jobs', loadComponent: () => import('./features/jobs/jobs-list').then((m) => m.JobsListComponent) },
      { path: 'jobs/new', loadComponent: () => import('./features/jobs/job-form').then((m) => m.JobFormComponent) },
      { path: 'jobs/:id', loadComponent: () => import('./features/jobs/job-form').then((m) => m.JobFormComponent) },
      { path: 'analysis', loadComponent: () => import('./features/analysis/analysis').then((m) => m.AnalysisComponent) },
      { path: 'analysis/:id', loadComponent: () => import('./features/analysis/analysis').then((m) => m.AnalysisComponent) },
      { path: 'learning', loadComponent: () => import('./features/learning/learning-list').then((m) => m.LearningListComponent) },
      { path: 'learning/:id', loadComponent: () => import('./features/learning/learning-detail').then((m) => m.LearningDetailComponent) },
      { path: 'progress', loadComponent: () => import('./features/progress/progress').then((m) => m.ProgressComponent) },
      { path: 'settings', loadComponent: () => import('./features/settings/settings').then((m) => m.SettingsComponent) },
      { path: 'admin', canActivate: [adminGuard], loadComponent: () => import('./features/admin/admin').then((m) => m.AdminComponent) },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '' },
];
