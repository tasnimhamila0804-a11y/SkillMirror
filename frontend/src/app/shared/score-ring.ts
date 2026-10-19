import { Component, effect, input, signal } from '@angular/core';

/**
 * Animated circular score. The arc is bound to the real score value —
 * it never displays a decorative fake percentage.
 */
@Component({
  selector: 'sm-score-ring',
  standalone: true,
  template: `
    <div class="score-ring" [style.width.px]="size()" [style.height.px]="size()">
      <svg [attr.width]="size()" [attr.height]="size()" role="img"
           [attr.aria-label]="'Score de compatibilité ' + score() + ' sur 100'">
        <defs>
          <linearGradient id="scoreGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="#7C5CFC" />
            <stop offset="100%" stop-color="#13B88A" />
          </linearGradient>
        </defs>
        <circle class="ring-bg" [attr.cx]="center()" [attr.cy]="center()" [attr.r]="radius()"
                fill="none" [attr.stroke-width]="stroke" />
        <circle class="ring-fg" [attr.cx]="center()" [attr.cy]="center()" [attr.r]="radius()"
                fill="none" [attr.stroke-width]="stroke" [attr.stroke-dasharray]="circumference()"
                [attr.stroke-dashoffset]="offset()" />
      </svg>
      <div class="ring-content">
        <div class="ring-value" [style.font-size.px]="size() * 0.26">{{ displayed() }}<span style="font-size:0.5em;opacity:0.6">/100</span></div>
        <div class="ring-label">{{ label() }}</div>
      </div>
    </div>
  `,
})
export class ScoreRingComponent {
  readonly score = input.required<number>();
  readonly size = input(180);
  readonly label = input('Compatibilité');
  readonly animated = input(true);

  protected readonly stroke = 12;
  protected readonly displayed = signal(0);

  protected readonly center = () => this.size() / 2;
  protected readonly radius = () => (this.size() - this.stroke) / 2;
  protected readonly circumference = () => 2 * Math.PI * this.radius();
  protected readonly offset = () => this.circumference() * (1 - Math.max(0, Math.min(100, this.score())) / 100);

  constructor() {
    effect(() => {
      const target = Math.max(0, Math.min(100, this.score()));
      if (!this.animated()) {
        this.displayed.set(target);
        return;
      }
      const steps = 26;
      const increment = target / steps;
      let current = 0;
      let index = 0;
      const timer = setInterval(() => {
        index++;
        current = index >= steps ? target : increment * index;
        this.displayed.set(Math.round(current));
        if (index >= steps) clearInterval(timer);
      }, 26);
    });
  }
}
