import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { RequestForm } from './request-form';
import { ApiService } from '../../services/api.service';
import type { CreateSessionResponse } from '../../models';

const mockCreateSessionResponse: CreateSessionResponse = {
  sessionId: 'sess-001',
  decision: { outcome: 'APPROVE', binding: true, justification: 'ok', nextSteps: [], ruleReferences: [] },
  firstMessage: 'Cześć',
  createdAt: '2026-06-25T10:00:00Z',
};

describe('RequestForm', () => {
  let component: RequestForm;
  let fixture: ComponentFixture<RequestForm>;
  let apiSpy: { createSession: ReturnType<typeof vi.fn> };
  let routerSpy: { navigate: ReturnType<typeof vi.fn> };
  let snackBarSpy: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    apiSpy = { createSession: vi.fn().mockReturnValue(of(mockCreateSessionResponse)) };
    routerSpy = { navigate: vi.fn() };
    snackBarSpy = { open: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        RouterModule.forRoot([]),
        MatFormFieldModule,
        MatSelectModule,
        MatInputModule,
        MatButtonModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatProgressBarModule,
        BrowserAnimationsModule,
      ],
      declarations: [RequestForm],
      providers: [
        { provide: ApiService, useValue: apiSpy },
        { provide: Router, useValue: routerSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RequestForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('reason field validation', () => {
    it('should require reason when COMPLAINT selected', () => {
      component.form.get('requestType')!.setValue('COMPLAINT');
      fixture.detectChanges();
      component.form.get('reason')!.setValue('');
      component.form.get('reason')!.markAsTouched();
      expect(component.form.get('reason')!.valid).toBe(false);
    });

    it('should not require reason when RETURN selected', () => {
      component.form.get('requestType')!.setValue('RETURN');
      fixture.detectChanges();
      component.form.get('reason')!.setValue('');
      expect(component.form.get('reason')!.valid).toBe(true);
    });
  });

  describe('file validation', () => {
    it('should set imageError for disallowed file type (GIF)', () => {
      const gifFile = new File(['gif'], 'test.gif', { type: 'image/gif' });
      const event = { target: { files: [gifFile] } } as unknown as Event;
      component.onFileChange(event);
      expect(component.imageError).toBeTruthy();
      expect(component.canSubmit).toBe(false);
    });

    it('should set imageError for file over 10 MB', () => {
      const bigBuffer = new ArrayBuffer(11 * 1024 * 1024);
      const bigFile = new File([bigBuffer], 'big.jpg', { type: 'image/jpeg' });
      const event = { target: { files: [bigFile] } } as unknown as Event;
      component.onFileChange(event);
      expect(component.imageError).toBeTruthy();
      expect(component.canSubmit).toBe(false);
    });

    it('should accept valid JPEG file', () => {
      const validFile = new File(['img'], 'photo.jpg', { type: 'image/jpeg' });
      const event = { target: { files: [validFile] } } as unknown as Event;
      component.onFileChange(event);
      expect(component.imageError).toBeNull();
      expect(component.imageFile).toBe(validFile);
    });
  });

  describe('canSubmit', () => {
    it('should be false when form is invalid or no image', () => {
      expect(component.canSubmit).toBe(false);
    });
  });

  describe('onSubmit', () => {
    function fillForm(component: RequestForm): void {
      component.form.get('requestType')!.setValue('RETURN');
      component.form.get('category')!.setValue('LAPTOP');
      component.form.get('modelName')!.setValue('ThinkPad X1');
      component.form.get('purchaseDate')!.setValue(new Date('2025-01-15'));
      component.imageFile = new File(['img'], 'photo.jpg', { type: 'image/jpeg' });
    }

    it('should call createSession and navigate on success (201)', async () => {
      fillForm(component);
      expect(component.canSubmit).toBe(true);
      component.onSubmit();
      await Promise.resolve();
      expect(apiSpy.createSession).toHaveBeenCalled();
      expect(routerSpy.navigate).toHaveBeenCalledWith(
        ['/chat', 'sess-001'],
        expect.objectContaining({ state: expect.objectContaining({ firstMessage: 'Cześć' }) })
      );
    });

    it('should show snackBar and re-enable form on 503 error', async () => {
      apiSpy.createSession.mockReturnValue(throwError(() => ({ status: 503, error: {} })));
      fillForm(component);
      component.onSubmit();
      await Promise.resolve();
      expect(snackBarSpy.open).toHaveBeenCalled();
      expect(component.form.disabled).toBe(false);
      expect(routerSpy.navigate).not.toHaveBeenCalled();
    });

    it('should set imageError on 413 error', async () => {
      apiSpy.createSession.mockReturnValue(throwError(() => ({ status: 413, error: {} })));
      fillForm(component);
      component.onSubmit();
      await Promise.resolve();
      expect(component.imageError).toBeTruthy();
    });
  });
});
