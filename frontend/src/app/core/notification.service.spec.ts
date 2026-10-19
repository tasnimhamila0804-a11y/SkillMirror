import { TestBed } from '@angular/core/testing';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NotificationService);
  });

  it('pushes and dismisses toasts', () => {
    service.success('OK');
    expect(service.toasts().length).toBe(1);
    expect(service.toasts()[0].kind).toBe('success');

    service.error('KO');
    expect(service.toasts().length).toBe(2);

    service.dismiss(service.toasts()[0].id);
    expect(service.toasts().length).toBe(1);
    expect(service.toasts()[0].kind).toBe('error');
  });
});
