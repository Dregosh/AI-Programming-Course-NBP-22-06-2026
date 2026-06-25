import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-request-form',
  standalone: false,
  templateUrl: './request-form.html',
  styleUrl: './request-form.scss',
})
export class RequestForm {
  form: FormGroup;
  isLoading = false;
  imageFile: File | null = null;
  imageError: string | null = null;
  imageSrc: string | null = null;

  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  constructor() {
    this.form = this.fb.group({
      requestType: [null, Validators.required],
      category: [null, Validators.required],
      modelName: ['', Validators.required],
      purchaseDate: [null, Validators.required],
      reason: [''],
    });

    this.form.get('requestType')!.valueChanges.subscribe(type => {
      const reasonCtrl = this.form.get('reason')!;
      if (type === 'COMPLAINT') {
        reasonCtrl.setValidators(Validators.required);
      } else {
        reasonCtrl.clearValidators();
      }
      reasonCtrl.updateValueAndValidity();
    });
  }

  get maxDate(): Date { return new Date(); }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.imageFile = null;
    this.imageError = null;
    this.imageSrc = null;
    if (!file) return;
    const allowed = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowed.includes(file.type)) {
      this.imageError = 'Dozwolone formaty: JPEG, PNG, WebP';
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.imageError = 'Maksymalny rozmiar pliku to 10 MB';
      return;
    }
    this.imageFile = file;
    const reader = new FileReader();
    reader.onload = e => (this.imageSrc = e.target?.result as string);
    reader.readAsDataURL(file);
  }

  removeImage(): void {
    this.imageFile = null;
    this.imageError = null;
    this.imageSrc = null;
  }

  get canSubmit(): boolean {
    return this.form.valid && !!this.imageFile && !this.imageError;
  }

  onSubmit(): void {
    if (!this.canSubmit) return;
    const fd = new FormData();
    Object.entries(this.form.value).forEach(([k, v]) => {
      if (v !== null && v !== undefined) {
        fd.append(k, v instanceof Date ? v.toISOString().split('T')[0] : String(v));
      }
    });
    fd.append('image', this.imageFile!);
    this.isLoading = true;
    this.form.disable();
    this.api.createSession(fd).subscribe({
      next: res =>
        this.router.navigate(['/chat', res.sessionId], {
          state: { firstMessage: res.firstMessage, decision: res.decision },
        }),
      error: err => {
        this.isLoading = false;
        this.form.enable();
        const status = err.status;
        if (status === 413) {
          this.imageError = 'Plik jest za duży (max 10 MB)';
        } else if (status === 415) {
          this.imageError = 'Nieobsługiwany format pliku';
        } else if (status === 400 && err.error?.fields?.reason) {
          this.form.get('reason')!.setErrors({ serverError: err.error.fields.reason });
        } else {
          this.snackBar.open('Błąd usługi. Spróbuj ponownie.', 'OK', { duration: 5000 });
        }
      },
    });
  }
}
