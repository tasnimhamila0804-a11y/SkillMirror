import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { IconComponent } from './shared/icon';
import { NotificationService } from './core/notification.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, IconComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly notify = inject(NotificationService);
}
