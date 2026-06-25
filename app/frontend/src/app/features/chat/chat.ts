import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ApiService } from '../../services/api.service';
import type { ChatMessage, DecisionDto } from '../../models';

@Component({
  selector: 'app-chat',
  standalone: false,
  templateUrl: './chat.html',
  styleUrl: './chat.scss',
})
export class Chat implements OnInit {
  sessionId!: string;
  messages: ChatMessage[] = [];
  decision: DecisionDto | null = null;
  isStreaming = false;
  streamError: string | null = null;
  inputContent = '';
  private subscription?: Subscription;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private api = inject(ApiService);

  ngOnInit(): void {
    this.sessionId = this.route.snapshot.paramMap.get('sessionId')!;
    const navState = history.state;
    if (navState?.firstMessage) {
      this.decision = navState.decision ?? null;
      this.messages = [{ role: 'SYSTEM', content: navState.firstMessage }];
    } else {
      this.api.getSession(this.sessionId).subscribe({
        next: snapshot => {
          this.decision = snapshot.decision;
          this.messages = snapshot.messages.map(m => ({
            role: m.role as ChatMessage['role'],
            content: m.content,
          }));
        },
        error: err => {
          if (err.status === 404) this.router.navigate(['/']);
        },
      });
    }
  }

  sendMessage(): void {
    if (!this.inputContent.trim() || this.isStreaming) return;
    const content = this.inputContent.trim();
    this.inputContent = '';
    this.messages.push({ role: 'USER', content });
    const assistantMsg: ChatMessage = { role: 'ASSISTANT', content: '', pending: true };
    this.messages.push(assistantMsg);
    this.isStreaming = true;
    this.streamError = null;

    this.subscription = this.api.streamMessage(this.sessionId, content).subscribe({
      next: event => {
        if (event.type === 'delta') {
          assistantMsg.content += event.token;
        } else if (event.type === 'done') {
          assistantMsg.pending = false;
          this.isStreaming = false;
        } else if (event.type === 'error') {
          assistantMsg.pending = false;
          this.isStreaming = false;
          this.streamError = 'Błąd podczas generowania odpowiedzi. Spróbuj ponownie.';
          this.messages = this.messages.filter(m => m !== assistantMsg);
        }
      },
      error: () => {
        assistantMsg.pending = false;
        this.isStreaming = false;
        this.streamError = 'Błąd połączenia. Spróbuj ponownie.';
        this.messages = this.messages.filter(m => m !== assistantMsg);
      },
    });
  }

  retryLastMessage(): void {
    this.streamError = null;
    const lastUser = [...this.messages].reverse().find(m => m.role === 'USER');
    if (lastUser) { this.inputContent = lastUser.content; }
  }

  startNewRequest(): void {
    this.router.navigate(['/']);
  }

  getOutcomeLabel(outcome: string): string {
    return ({ APPROVE: 'ZATWIERDZONE', REJECT: 'ODRZUCONE', ESCALATE: 'DO ESKALACJI' } as Record<string, string>)[outcome] ?? outcome;
  }

  getOutcomeClass(outcome: string): string {
    return ({ APPROVE: 'outcome-approve', REJECT: 'outcome-reject', ESCALATE: 'outcome-escalate' } as Record<string, string>)[outcome] ?? '';
  }

  isPreliminary(outcome: string): boolean {
    return outcome === 'REJECT' || outcome === 'ESCALATE';
  }

  safeHtml(content: string): string {
    return content
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br>');
  }
}
