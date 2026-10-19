import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { ApiError } from './models';
import { NotificationService } from './notification.service';

/** Attaches the Bearer token to every API request. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;
  if (token && !req.headers.has('Authorization')) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};

const STATUS_MESSAGES: Record<number, string> = {
  0: 'Service injoignable. Vérifiez que le backend est démarré.',
  400: 'Certains champs sont invalides.',
  403: "Vous n'avez pas les droits nécessaires pour cette action.",
  404: 'Ressource introuvable.',
  409: 'Conflit : cette opération n’est pas possible en l’état.',
  413: 'Fichier trop volumineux.',
  429: 'Trop de requêtes. Réessayez dans un instant.',
  500: 'Une erreur interne est survenue. Réessayez plus tard.',
};

/** Centralized API error handling: toasts + session expiry. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notify = inject(NotificationService);
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 0) {
          notify.error(STATUS_MESSAGES[0]);
        } else if (error.status === 401) {
          const onAuthPage = router.url.startsWith('/auth');
          auth.clearSession();
          notify.error('Session expirée ou invalide. Connectez-vous à nouveau.');
          if (!onAuthPage) {
            router.navigate(['/auth/login']);
          }
        } else {
          const apiError = error.error as ApiError | null;
          const message =
            apiError && typeof apiError.message === 'string'
              ? apiError.message
              : STATUS_MESSAGES[error.status] ?? `Erreur ${error.status}.`;
          notify.error(message);
        }
      }
      return throwError(() => error);
    }),
  );
};
