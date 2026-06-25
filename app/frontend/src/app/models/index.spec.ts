import { DecisionDto } from './index';

describe('Models', () => {
  it('DecisionDto can be constructed', () => {
    const dto: DecisionDto = {
      outcome: 'APPROVE',
      binding: true,
      justification: 'Uzasadnienie',
      nextSteps: ['Krok 1'],
      ruleReferences: ['Reg-001'],
    };
    expect(dto).toBeTruthy();
  });
});
