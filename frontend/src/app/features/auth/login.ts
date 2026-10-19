import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { IconComponent } from '../../shared/icon';

/** Login page. */
@Component({
  selector: 'sm-login',
  imports: [ReactiveFormsModule, RouterLink, IconComponent],
  templateUrl: './login.html',
  styleUrl: './auth.css',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly pending = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  protected fieldError(name: 'email' | 'password'): string | null {
    const control = this.form.controls[name];
    if (control.touched && control.hasError('required')) return 'Ce champ est requis.';
    if (control.touched && control.hasError('email')) return "Format d'e-mail invalide.";
    return null;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.pending.set(true);
    this.serverError.set(null);
    const { email, password } = this.form.getRawValue();
    this.auth.login({ email, password }).subscribe({
      next: (response) => {
        this.pending.set(false);
        this.auth.setProfileCompleted(response.user.profileCompleted);
        this.router.navigate(
          response.user.profileCompleted ? ['/dashboard'] : ['/questionnaire'],
        );
      },
      error: () => {
        this.pending.set(false);
        this.serverError.set("Échec de la connexion. Vérifiez vos identifiants.");
      },
    });
  }
}
