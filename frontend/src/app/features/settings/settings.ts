import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { NotificationService } from '../../core/notification.service';
import { IconComponent } from '../../shared/icon';

/** Account settings: name and password. */
@Component({
  selector: 'sm-settings',
  imports: [ReactiveFormsModule, IconComponent],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class SettingsComponent {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly notify = inject(NotificationService);

  protected readonly saving = signal(false);
  protected readonly changingPassword = signal(false);

  protected readonly accountForm = this.fb.nonNullable.group({
    name: [this.auth.user()?.name ?? '', [Validators.required, Validators.maxLength(120)]],
  });

  protected readonly passwordForm = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  protected saveAccount(): void {
    if (this.accountForm.invalid) {
      this.accountForm.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.http.put<{ name: string }>('/api/users/me', this.accountForm.getRawValue()).subscribe({
      next: (user) => {
        this.auth.user.update((current) => (current ? { ...current, name: user.name } : current));
        this.saving.set(false);
        this.notify.success('Informations mises à jour.');
      },
      error: () => this.saving.set(false),
    });
  }

  protected changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    this.changingPassword.set(true);
    this.http.put('/api/users/me/password', this.passwordForm.getRawValue()).subscribe({
      next: () => {
        this.changingPassword.set(false);
        this.passwordForm.reset();
        this.notify.success('Mot de passe modifié.');
      },
      error: () => this.changingPassword.set(false),
    });
  }
}
