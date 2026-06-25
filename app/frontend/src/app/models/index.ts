export type RequestType = 'COMPLAINT' | 'RETURN';

export type EquipmentCategory = 'SMARTPHONE' | 'LAPTOP' | 'TABLET' | 'HEADPHONES' | 'SMARTWATCH' | 'OTHER';

export interface DecisionDto {
  outcome: 'APPROVE' | 'REJECT' | 'ESCALATE';
  binding: boolean;
  justification: string;
  nextSteps: string[];
  ruleReferences: string[];
}

export interface CreateSessionResponse {
  sessionId: string;
  decision: DecisionDto;
  firstMessage: string;
  createdAt: string;
}

export interface ChatMessage {
  role: 'SYSTEM' | 'USER' | 'ASSISTANT';
  content: string;
  pending?: boolean;
}

export type SseEvent =
  | { type: 'delta'; token: string }
  | { type: 'done'; finishReason?: string }
  | { type: 'error'; code: string; message: string };

export interface ApiError {
  code: string;
  message: string;
  fields?: Record<string, string>;
}

export interface SessionSnapshot {
  sessionId: string;
  requestType: RequestType;
  category: EquipmentCategory;
  decision: DecisionDto;
  messages: { role: string; content: string; createdAt: string }[];
}
