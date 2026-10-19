import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { IconComponent } from '../../shared/icon';

function passwordMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirm')?.value;
  return password && confirm && password !== confirm ? { mismatch: true } : null;
}

/** Registration page. */
@Component({
  selector: 'sm-register',
  imports: [ReactiveFormsModule, RouterLink, IconComponent],
  templateUrl: './register.html',
  styleUrl: './auth.css',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly pending = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group(
    {
      name: ['', [Validators.required, Validators.maxLength(120)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirm: ['', [Validators.required]],
    },
    { validators: passwordMatch },
  );

  protected fieldError(name: 'name' | 'email' | 'password' | 'confirm'): string | null {
    const control = this.form.controls[name];
    if (!control.touched) return null;
    if (control.hasError('required')) return 'Ce champ est requis.';
    if (control.hasError('email')) return "Format d'e-mail invalide.";
    if (control.hasError('minlength')) return '8 caractères minimum.';
    if (control.hasError('maxlength')) return '120 caractères maximum.';
    if (name === 'confirm' && this.form.hasError('mismatch')) return 'Les mots de passe ne correspondent pas.';
    return null;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.pending.set(true);
    this.serverError.set(null);
    const { name, email, password } = this.form.getRawValue();
    this.auth.register({ name, email, password }).subscribe({
      next: () => {
        this.pending.set(false);
        this.router.navigate(['/questionnaire']);
      },
      error: () => {
        this.pending.set(false);
        this.serverError.set("La création du compte a échoué. L'e-mail est peut-être déjà utilisé.");
      },
    });
  }
}
