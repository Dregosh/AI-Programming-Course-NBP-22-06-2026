import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Location } from '@angular/common';
import { provideLocationMocks } from '@angular/common/testing';
import { RouterTestingHarness } from '@angular/router/testing';
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
