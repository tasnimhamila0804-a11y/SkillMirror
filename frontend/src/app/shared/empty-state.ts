import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from './icon';

/** Smart empty state with a clear call to action. */
@Component({
  selector: 'sm-empty-state',
  imports: [RouterLink, IconComponent],
  template: `
    <div class="empty-state fade-in">
      <div class="icon-bubble" aria-hidden="true">
        <sm-icon [name]="icon()" [size]="26" />
      </div>
      <h3>{{ title() }}</h3>
      <p>{{ description() }}</p>
      @if (actionLabel()) {
        <a class="btn btn-primary" [routerLink]="actionUrl()">{{ actionLabel() }}</a>
      }
      <ng-content />
    </div>
  `,
  styles: [
    `
      .icon-bubble {
        width: 58px;
        height: 58px;
        margin: 0 auto 14px;
        border-radius: 18px;
        background: var(--lavender);
        color: var(--violet-deep);
        display: flex;
        align-items: center;
        justify-content: center;
      }
    `,
  ],
})
export class EmptyStateComponent {
  readonly title = input.required<string>();
  readonly description = input.required<string>();
  readonly icon = input('mirror');
  readonly actionLabel = input<string | null>(null);
  readonly actionUrl = input<string>('/');
}
