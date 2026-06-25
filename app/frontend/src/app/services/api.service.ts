import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateSessionResponse, SessionSnapshot, SseEvent } from '../models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);

  createSession(formData: FormData): Observable<CreateSessionResponse> {
    return this.http.post<CreateSessionResponse>('/api/sessions', formData);
  }

  streamMessage(sessionId: string, content: string): Observable<SseEvent> {
    return new Observable(observer => {
      const controller = new AbortController();
      fetch(`/api/sessions/${sessionId}/messages`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content }),
        signal: controller.signal,
      })
        .then(response => {
          if (!response.ok) {
            observer.error({ code: 'HTTP_ERROR', message: `HTTP ${response.status}` });
            return;
          }
          const reader = response.body!.getReader();
          const decoder = new TextDecoder();
          let buffer = '';

          function pump(): void {
            reader.read().then(({ done, value }) => {
              if (done) { observer.complete(); return; }
              buffer += decoder.decode(value, { stream: true });
              const frames = buffer.split('\n\n');
              buffer = frames.pop() ?? '';
              for (const frame of frames) {
                if (!frame.trim()) continue;
                const lines = frame.split('\n');
                let eventName = 'message';
                let data = '';
                for (const line of lines) {
                  if (line.startsWith('event:')) eventName = line.slice(6).trim();
                  else if (line.startsWith('data:')) data = line.slice(5).trim();
                }
                try {
                  const payload = JSON.parse(data);
                  if (eventName === 'delta') observer.next({ type: 'delta', token: payload.token });
                  else if (eventName === 'done') {
                    observer.next({ type: 'done', finishReason: payload.finishReason });
                    observer.complete();
                    return;
                  } else if (eventName === 'error') {
                    observer.next({ type: 'error', code: payload.code, message: payload.message });
                    observer.complete();
                    return;
                  }
                } catch { /* skip malformed frame */ }
              }
              pump();
            }).catch(err => observer.error(err));
          }
          pump();
        })
        .catch(err => {
          if (err.name !== 'AbortError') observer.error(err);
        });

      return () => controller.abort();
    });
  }

  getSession(sessionId: string): Observable<SessionSnapshot> {
    return this.http.get<SessionSnapshot>(`/api/sessions/${sessionId}`);
  }
}
