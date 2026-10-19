import { TestBed } from '@angular/core/testing';
import { ScoreRingComponent } from './score-ring';

describe('ScoreRingComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ScoreRingComponent] }).compileComponents();
  });

  it('binds the visible arc length to the score value', () => {
    const fixture = TestBed.createComponent(ScoreRingComponent);
    fixture.componentRef.setInput('score', 50);
    fixture.componentRef.setInput('animated', false);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    const progressCircle = element.querySelector('circle.ring-fg') as SVGCircleElement;
    const radius = Number(progressCircle.getAttribute('r'));
    const circumference = 2 * Math.PI * radius;
    const offset = Number(progressCircle.getAttribute('stroke-dashoffset'));
    expect(offset).toBeCloseTo(circumference * 0.5, 1);
  });

  it('clamps values above 100 to a full arc', () => {
    const fixture = TestBed.createComponent(ScoreRingComponent);
    fixture.componentRef.setInput('score', 140);
    fixture.componentRef.setInput('animated', false);
    fixture.detectChanges();

    const progressCircle = (fixture.nativeElement as HTMLElement)
      .querySelector('circle.ring-fg') as SVGCircleElement;
    expect(Number(progressCircle.getAttribute('stroke-dashoffset'))).toBe(0);
  });
});
