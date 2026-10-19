import { Component, input } from '@angular/core';

/** Consistent line-icon set (no emoji, stroke-based, 24px grid). */
@Component({
  selector: 'sm-icon',
  standalone: true,
  template: `
    <svg [attr.width]="size()" [attr.height]="size()" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"
         aria-hidden="true">
      @switch (name()) {
        @case ('dashboard') {
          <rect x="3" y="3" width="7" height="9" rx="1.5" /><rect x="14" y="3" width="7" height="5" rx="1.5" />
          <rect x="14" y="12" width="7" height="9" rx="1.5" /><rect x="3" y="16" width="7" height="5" rx="1.5" />
        }
        @case ('mirror') {
          <rect x="5" y="3" width="14" height="14" rx="7" /><path d="M12 17v4" /><path d="M9 10.5a3 3 0 0 1 6 0" />
        }
        @case ('jobs') {
          <rect x="3" y="7" width="18" height="13" rx="2" /><path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2" />
          <path d="M3 12h18" />
        }
        @case ('analysis') {
          <path d="M4 20V10" /><path d="M10 20V4" /><path d="M16 20v-7" /><path d="M21 20H3" />
        }
        @case ('learning') {
          <path d="M12 4l9 5-9 5-9-5 9-5z" /><path d="M5 11v5c0 1.5 3.1 3 7 3s7-1.5 7-3v-5" />
        }
        @case ('progress') {
          <path d="M3 17l6-6 4 4 8-8" /><path d="M15 7h6v6" />
        }
        @case ('profile') {
          <circle cx="12" cy="8" r="4" /><path d="M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6" />
        }
        @case ('admin') {
          <path d="M12 3l8 4v5c0 5-3.4 8.4-8 9.5C7.4 20.4 4 17 4 12V7l8-4z" /><path d="M9.5 12l1.8 1.8 3.7-3.7" />
        }
        @case ('settings') {
          <circle cx="12" cy="12" r="3" />
          <path d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.9.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.9 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.9l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.9.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.9-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.9V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z" />
        }
        @case ('plus') { <path d="M12 5v14" /><path d="M5 12h14" /> }
        @case ('check') { <path d="M4 12.5l5 5L20 6.5" /> }
        @case ('arrow-right') { <path d="M5 12h14" /><path d="M13 6l6 6-6 6" /> }
        @case ('spark') {
          <path d="M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8L12 3z" />
          <path d="M19 16l.9 2.1L22 19l-2.1.9L19 22l-.9-2.1L16 19l2.1-.9L19 16z" />
        }
        @case ('logout') { <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><path d="M16 17l5-5-5-5" /><path d="M21 12H9" /> }
        @case ('menu') { <path d="M4 6h16" /><path d="M4 12h16" /><path d="M4 18h16" /> }
        @case ('close') { <path d="M6 6l12 12" /><path d="M18 6L6 18" /> }
        @case ('trash') { <path d="M4 7h16" /><path d="M10 11v6" /><path d="M14 11v6" /><path d="M6 7l1 13a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1l1-13" /><path d="M9 7V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" /> }
        @case ('link') { <path d="M10 13a5 5 0 0 0 7.5.5l2-2a5 5 0 0 0-7-7l-1.2 1.2" /><path d="M14 11a5 5 0 0 0-7.5-.5l-2 2a5 5 0 0 0 7 7l1.2-1.2" /> }
        @case ('file') { <path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8l-5-5z" /><path d="M14 3v5h5" /> }
        @case ('clock') { <circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /> }
        @case ('alert') { <path d="M12 9v4" /><path d="M12 17h.01" /><path d="M10.3 3.9L2.4 17a2 2 0 0 0 1.7 3h15.8a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z" /> }
        @case ('info') { <circle cx="12" cy="12" r="9" /><path d="M12 11v5" /><path d="M12 8h.01" /> }
        @case ('pause') { <path d="M9 5v14" /><path d="M15 5v14" /> }
        @case ('play') { <path d="M7 5l12 7-12 7V5z" /> }
        @case ('search') { <circle cx="11" cy="11" r="7" /><path d="M20 20l-3.5-3.5" /> }
        @case ('target') { <circle cx="12" cy="12" r="9" /><circle cx="12" cy="12" r="5" /><circle cx="12" cy="12" r="1.2" /> }
        @default { <circle cx="12" cy="12" r="9" /> }
      }
    </svg>
  `,
})
export class IconComponent {
  readonly name = input.required<string>();
  readonly size = input<number>(18);
}
