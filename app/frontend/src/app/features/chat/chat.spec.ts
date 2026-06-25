import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { of, throwError, Subject } from 'rxjs';
import { vi } from 'vitest';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Chat } from './chat';
import { ApiService } from '../../services/api.service';
import type { SessionSnapshot, SseEvent } from '../../models';

const mockSnapshot: SessionSnapshot = {
  sessionId: 'sess-001',
  requestType: 'COMPLAINT',
  category: 'LAPTOP',
  decision: { outcome: 'REJECT', binding: false, justification: 'brak danych', nextSteps: [], ruleReferences: [] },
  messages: [{ role: 'SYSTEM', content: 'Cześć', createdAt: '2026-06-25T10:00:00Z' }],
};

describe('Chat', () => {
  let component: Chat;
  let fixture: ComponentFixture<Chat>;
  let apiSpy: {
    getSession: ReturnType<typeof vi.fn>;
    streamMessage: ReturnType<typeof vi.fn>;
  };
  let routerSpy: { navigate: ReturnType<typeof vi.fn> };

  function createComponent(historyState: Record<string, unknown> = {}): void {
    Object.defineProperty(window, 'history', {
      value: { state: historyState },
      writable: true,
    });
  }

  beforeEach(async () => {
    apiSpy = {
      getSession: vi.fn().mockReturnValue(of(mockSnapshot)),
      streamMessage: vi.fn().mockReturnValue(of({ type: 'done', finishReason: 'stop' } as SseEvent)),
    };
    routerSpy = { navigate: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        RouterModule.forRoot([]),
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatProgressBarModule,
        BrowserAnimationsModule,
      ],
      declarations: [Chat],
      providers: [
        { provide: ApiService, useValue: apiSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => 'sess-001' } },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Chat);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    createComponent({});
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('ngOnInit with router state (firstMessage)', () => {
    it('should load messages from router state instead of API', () => {
      createComponent({ firstMessage: 'Witaj', decision: mockSnapshot.decision });
      fixture.detectChanges();
      expect(apiSpy.getSession).not.toHaveBeenCalled();
      expect(component.messages.length).toBe(1);
      expect(component.messages[0].content).toBe('Witaj');
      expect(component.decision).toEqual(mockSnapshot.decision);
    });
  });

  describe('ngOnInit without router state (deep-link fallback)', () => {
    it('should call getSession and load messages', () => {
      createComponent({});
      fixture.detectChanges();
      expect(apiSpy.getSession).toHaveBeenCalledWith('sess-001');
      expect(component.messages.length).toBe(1);
    });

    it('should navigate to / on 404', () => {
      apiSpy.getSession.mockReturnValue(throwError(() => ({ status: 404 })));
      createComponent({});
      fixture.detectChanges();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    });
  });

  describe('decision helpers', () => {
    it('isPreliminary returns true for REJECT', () => {
      expect(component.isPreliminary('REJECT')).toBe(true);
    });

    it('isPreliminary returns true for ESCALATE', () => {
      expect(component.isPreliminary('ESCALATE')).toBe(true);
    });

    it('isPreliminary returns false for APPROVE', () => {
      expect(component.isPreliminary('APPROVE')).toBe(false);
    });
  });

  describe('sendMessage', () => {
    beforeEach(() => {
      createComponent({ firstMessage: 'Cześć', decision: mockSnapshot.decision });
      fixture.detectChanges();
    });

    it('should add user message and call streamMessage', () => {
      const streamSubject = new Subject<SseEvent>();
      apiSpy.streamMessage.mockReturnValue(streamSubject.asObservable());
      component.inputContent = 'Pytanie';
      component.sendMessage();
      expect(component.messages.some(m => m.role === 'USER' && m.content === 'Pytanie')).toBe(true);
      expect(apiSpy.streamMessage).toHaveBeenCalledWith('sess-001', 'Pytanie');
    });

    it('should append delta tokens to assistant message', () => {
      const streamSubject = new Subject<SseEvent>();
      apiSpy.streamMessage.mockReturnValue(streamSubject.asObservable());
      component.inputContent = 'Hello';
      component.sendMessage();
      streamSubject.next({ type: 'delta', token: 'Tak' });
      streamSubject.next({ type: 'delta', token: ', zgadza' });
      const assistant = component.messages.find(m => m.role === 'ASSISTANT');
      expect(assistant?.content).toBe('Tak, zgadza');
    });

    it('should set isStreaming=false on done event', () => {
      const streamSubject = new Subject<SseEvent>();
      apiSpy.streamMessage.mockReturnValue(streamSubject.asObservable());
      component.inputContent = 'Hello';
      component.sendMessage();
      expect(component.isStreaming).toBe(true);
      streamSubject.next({ type: 'done', finishReason: 'stop' });
      streamSubject.complete();
      expect(component.isStreaming).toBe(false);
    });

    it('should set streamError and remove assistant message on error event', () => {
      const streamSubject = new Subject<SseEvent>();
      apiSpy.streamMessage.mockReturnValue(streamSubject.asObservable());
      component.inputContent = 'Hello';
      component.sendMessage();
      const initialMsgCount = component.messages.length;
      streamSubject.next({ type: 'error', code: 'ERR', message: 'fail' });
      streamSubject.complete();
      expect(component.streamError).toBeTruthy();
      // empty assistant msg removed
      expect(component.messages.length).toBeLessThan(initialMsgCount);
    });

    it('should not send when input is empty', () => {
      component.inputContent = '  ';
      component.sendMessage();
      expect(apiSpy.streamMessage).not.toHaveBeenCalled();
    });
  });
});
