import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';
import { vi } from 'vitest';
import type { CreateSessionResponse, SessionSnapshot } from '../models';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService],
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  describe('createSession', () => {
    it('should POST FormData to /api/sessions and return CreateSessionResponse', () => {
      const mockResponse: CreateSessionResponse = {
        sessionId: 'abc-123',
        decision: { outcome: 'APPROVE', binding: true, justification: 'ok', nextSteps: [], ruleReferences: [] },
        firstMessage: 'Witaj',
        createdAt: '2026-06-25T10:00:00Z',
      };
      const fd = new FormData();
      fd.append('requestType', 'RETURN');

      let result: CreateSessionResponse | undefined;
      service.createSession(fd).subscribe(r => (result = r));

      const req = httpMock.expectOne('/api/sessions');
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse, { status: 201, statusText: 'Created' });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getSession', () => {
    it('should GET /api/sessions/{id} and return SessionSnapshot', () => {
      const mockSnapshot: SessionSnapshot = {
        sessionId: 'abc-123',
        requestType: 'RETURN',
        category: 'LAPTOP',
        decision: { outcome: 'APPROVE', binding: true, justification: 'ok', nextSteps: [], ruleReferences: [] },
        messages: [],
      };

      let result: SessionSnapshot | undefined;
      service.getSession('abc-123').subscribe(r => (result = r));

      const req = httpMock.expectOne('/api/sessions/abc-123');
      expect(req.request.method).toBe('GET');
      req.flush(mockSnapshot);
      expect(result).toEqual(mockSnapshot);
    });
  });

  describe('streamMessage', () => {
    it('should stream delta then done events', async () => {
      // Build a ReadableStream with valid SSE frames
      const encoder = new TextEncoder();
      const sseData = 'event: delta\ndata: {"token":"hello"}\n\nevent: done\ndata: {"finishReason":"stop"}\n\n';
      const stream = new ReadableStream({
        start(controller) {
          controller.enqueue(encoder.encode(sseData));
          controller.close();
        },
      });

      vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: true,
        body: stream,
      } as Response);

      const events: unknown[] = [];
      await new Promise<void>((resolve, reject) => {
        service.streamMessage('abc-123', 'hello').subscribe({
          next: e => events.push(e),
          error: reject,
          complete: () => { resolve(); },
        });
      });

      expect(events).toEqual([
        { type: 'delta', token: 'hello' },
        { type: 'done', finishReason: 'stop' },
      ]);
    });
  });
});
