import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Location } from '@angular/common';
import { provideLocationMocks } from '@angular/common/testing';
import { RouterTestingHarness } from '@angular/router/testing';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { RequestForm } from './features/request-form/request-form';
import { Chat } from './features/chat/chat';

const testRoutes = [
  { path: '', component: RequestForm },
  { path: 'chat/:sessionId', component: Chat },
  { path: '**', redirectTo: '' },
];

describe('AppRoutingModule', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        ReactiveFormsModule,
        FormsModule,
        BrowserAnimationsModule,
        HttpClientTestingModule,
        MatFormFieldModule,
        MatSelectModule,
        MatInputModule,
        MatButtonModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatProgressBarModule,
        MatSnackBarModule,
        MatIconModule,
      ],
      declarations: [RequestForm, Chat],
      providers: [
        provideRouter(testRoutes),
        provideLocationMocks(),
      ],
    }).compileComponents();
  });

  it("trasa '' kieruje do RequestFormComponent", async () => {
    const harness = await RouterTestingHarness.create('/');
    expect(harness.routeDebugElement?.componentInstance).toBeInstanceOf(RequestForm);
  });

  it("trasa 'chat/session-123' kieruje do ChatComponent", async () => {
    const harness = await RouterTestingHarness.create('/chat/session-123');
    expect(harness.routeDebugElement?.componentInstance).toBeInstanceOf(Chat);
  });

  it("nieznana trasa przekierowuje do ''", async () => {
    await RouterTestingHarness.create('/unknown');
    const location = TestBed.inject(Location);
    expect(location.path()).toBe('/');
  });
});
