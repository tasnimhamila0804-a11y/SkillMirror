import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/** Blocks private routes when there is no session. */
export const authGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated) {
    return router.createUrlTree(['/auth/login']);
  }
  const user = await auth.ensureLoaded();
  if (!user) {
    return router.createUrlTree(['/auth/login']);
  }
  return true;
};

/** Restricts a route to administrators. */
export const adminGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated) {
    return router.createUrlTree(['/auth/login']);
  }
  const user = await auth.ensureLoaded();
  if (!user) return router.createUrlTree(['/auth/login']);
  if (user.role !== 'ADMIN') {
    return router.createUrlTree(['/dashboard']);
  }
  return true;
};

/**
 * After login, a user who has not finished the initial profiling must go through
 * the questionnaire (except on the questionnaire itself and account settings).
 */
export const onboardingGuard: CanActivateFn = async (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated) return true;
  const user = await auth.ensureLoaded();
  if (!user) return true;
  if (user.profileCompleted) return true;

  const path = route.routeConfig?.path ?? '';
  if (path.startsWith('questionnaire') || path.startsWith('settings')) {
    return true;
  }
  return router.createUrlTree(['/questionnaire']);
};

/** Already-authenticated users skip the login/register pages. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) {
    return router.createUrlTree(['/dashboard']);
  }
  return true;
};
