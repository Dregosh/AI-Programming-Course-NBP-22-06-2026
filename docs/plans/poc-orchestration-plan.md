# PoC Orchestration Plan — Hardware Service Decision Copilot

> **Role of this document:** the **orchestrator's playbook**. I (the orchestrator) do not write code.
> Each step below is a single delegation to **one** specialized agent, with the exact context that
> agent needs and nothing more. Every step is a full TDD cycle ending in one focused commit.

---

## Context

`app/` is an **empty scaffold** — no Spring Boot, no Angular, no build tooling. The PRD, all four
ADRs, the two Polish policy documents (`docs/policies/`), the NBP design tokens, and `.env.example`
are all present and accepted. The task is to build a **fully working PoC** of the Hardware Service
Decision Copilot: an Angular SPA + Spring Boot backend that takes a complaint/return form + one
photo, runs a two-step LLM pipeline (vision analysis → policy decision) via OpenRouter's Responses
API, and hands the decision into a streamed chat — all with TDD, all UI text in Polish.

**Why this shape:** backend (`app/backend/`) and frontend (`app/frontend/`) are independent build
artifacts in non-overlapping directories. The API contract between them is already fully fixed by
**ADR-001 §4–5** (DTOs, endpoints, SSE event shapes) and **ADR-003 §4** (TS mirrors). That lets the
two tracks run **in parallel against a frozen contract**, with QA's real-stack E2E run last.

### Decisions locked with the user

| # | Decision |
|---|---|
| 1 | **Plan only** this session — deliver this playbook; execution triggered later. |
| 2 | **Parallel BE + FE** on branch `mcproject`; separate dirs ⇒ no file conflict. QA after both. |
| 3 | **Live LLM everywhere** incl. Playwright E2E (real key available). Unit/integration still fake only the OpenRouter HTTP endpoint (TAC-02). |
| 4 | **Try JDK 25 first**; pin to JDK 21 only if Spring Boot 3.5 fails to build/run. |
| 5 | **Commit per step** (`Area: summary`), **no push**, on `mcproject`. |
| 6 | Agents **author the Polish prompts + first-message template freely** from the policy docs (constrained by AC-22/AC-28). |

### The frozen contract (single source of truth for both tracks)

Any change here must be coordinated by the orchestrator across **both** tracks before either proceeds.

- **Endpoints** (ADR-001 §5): `POST /api/sessions` (multipart → `201 CreateSessionResponse`),
  `POST /api/sessions/{id}/messages` (JSON → `text/event-stream`), `GET /api/sessions/{id}`,
  `GET /api/health`.
- **DTOs** (ADR-001 §4 / ADR-003 §4): `CreateSessionRequest`, `DecisionDto`
  (`outcome ∈ {APPROVE,REJECT,ESCALATE}`, `binding`, `justification`, `nextSteps[]`,
  `ruleReferences[]`), `CreateSessionResponse` (`sessionId`, `decision`, `firstMessage`, `createdAt`),
  `PostMessageRequest` (`content`), `SessionSnapshot`, `ApiError` (`code`, `message`, `fields?`).
- **SSE events** (ADR-001 §4): named events `delta {token}`, terminal `done {finishReason}`,
  `error {code,message}`. **Both B9 and F2 must use this exact framing** (not the Vercel AI-SDK
  data-stream format seen in the course examples). ⚠ Highest-risk contract point.
- **Error→status map:** `400` validation, `404` unknown/expired session, `413` >10 MB image,
  `415` unsupported image type, `502`/`503` LLM upstream failure/timeout.

---

## Cross-cutting context (give to *every* agent, once, at track start)

Keep this brief; do not paste full ADRs into every step.

- **Product:** end-customer self-service copilot; outcomes **Approve (binding)** / **Reject
  (preliminary)** / **Escalate (preliminary)**. Reject & Escalate must carry the human-review
  disclosure. **All user-facing text in Polish** (AC-28).
- **Workflow (AGENTS.md):** strict TDD — write/extend the test, confirm it fails for the right
  reason, implement the minimum, run the changed-scope verification green, refactor green.
- **Git:** branch `mcproject`; one focused commit per step; format `Backend:` / `Frontend:` / `QA:`;
  **never push**.
- **Verification before each commit:** BE `./mvnw test` (+ `./mvnw verify` for integration scope);
  FE `ng test --watch=false` + `ng lint` + `ng build`. Start the app before the track's final commit.
- **Context7 handles** (fetch docs, don't re-search): Spring Boot `/spring-projects/spring-boot`,
  openai-java `/openai/openai-java`, Angular `/angular/angular`, Angular Material
  `/websites/material_angular_dev`.
- **Scope discipline:** BE edits only under `app/backend/`; FE only under `app/frontend/`; QA only
  under `app/e2e/` (+ may run both apps). No agent edits another track's files.

---

## Phase plan (overview)

| Phase | Tracks | Parallelism | Gate to exit |
|---|---|---|---|
| **0 — Scaffold** | B0 (be-dev) ∥ F0 (fe-dev) | Parallel | Both apps build & start; `/api/health` 200; `ng serve` boots |
| **1 — Build** | B1–B10 (be-dev) ∥ F1–F5 (fe-dev) | Parallel tracks; steps sequential within a track | All unit/integration green; both build |
| **2 — Integration** | I1 (be-dev) | Single | Live form→decision→chat works through the dev proxy |
| **3 — QA / E2E** | Q1–Q3 (qa-eng) | Single | Playwright E2E green on real stack; full regression passes |

Within a track, steps run **in listed order** (each depends on the prior). Across tracks, B and F
are independent until Phase 2.

---

## Phase 0 — Scaffold (parallel)

### B0 — be-developer — Backend scaffold + health
- **Context to provide:** ADR-000 §3 (repo structure `app/backend/...`, stack table, Decision 8.7
  JDK note), ADR-001 §1 + §5 health contract. Cross-cutting block.
- **Task:** Create the Spring Boot 3.5.x (Maven) project under `app/backend/` **with the Maven
  Wrapper** (`mvnw`, `mvnw.cmd`, `.mvn/`) — `mvn` is not installed globally. `pom.xml`:
  `<java.version>21</java.version>`, deps `spring-boot-starter-web`, `spring-boot-starter-validation`,
  `spring-boot-starter-test`, `com.openai:openai-java:4.41.x`. Add `HealthController` →
  `GET /api/health` 200.
  **JDK contingency (Decision 4):** attempt build/run on the installed JDK 25 first; if Spring Boot
  3.5 fails (toolchain/bytecode), install Temurin 21 and pin the build to JDK 21. Report which path was taken.
- **TDD:** integration test asserts `GET /api/health` → 200 **before** the controller exists.
- **Done when:** `./mvnw test` green; `./mvnw spring-boot:run` starts; health returns 200.
- **Commit:** `Backend: scaffold Spring Boot 3.5 (Java 21) with health endpoint`.

### F0 — fe-developer — Frontend scaffold + NBP theme base + proxy
- **Context to provide:** ADR-003 §3 (routes), Decision 8.6 (dev proxy `/api/*`),
  `assets/design-tokens.json` (full), design-guidelines.md §2–3 + §5 (colors, fonts, small radii),
  `assets/logo.svg` + `assets/favicon.ico` paths. Cross-cutting block.
- **Task:** `ng new` Angular (latest) app under `app/frontend/` with Angular Material; add routing
  (`''`→form, `'chat/:sessionId'`→chat, `'**'`→form — placeholder components OK for now);
  `src/proxy.conf.json` mapping `/api/*` → `http://localhost:3000`; wire `@font-face` for Brygada 1918
  (headings) + Libre Franklin (body) with `font-display: swap` and fallbacks; define SCSS variables /
  Material theme from the design tokens (navy `#152E52`, accent `#BDAD7D`, link `#4A74B0`, small
  radii); set favicon + page title (Polish).
- **TDD:** default `AppComponent` spec passes; add a spec asserting the three routes resolve.
- **Done when:** `ng build` + `ng lint` clean; `ng serve` boots; theme tokens applied at `:root`.
- **Commit:** `Frontend: scaffold Angular + Material with NBP theme and dev proxy`.

> **Gate G0:** both B0 and F0 committed and green before their tracks continue.

---

## Phase 1 — Build (two parallel tracks)

### Backend track (be-developer) — bottom-up, dependency-respecting

Dependency direction (ADR-000 §4): `web → decision/chat → llm/image/policy/session`. Build leaves
first, then orchestration, then web.

| Step | Module | What to build (TDD) | Context to provide (only this) | TAC |
|---|---|---|---|---|
| **B1** | domain | Enums `RequestType`,`EquipmentCategory`; `ServiceRequest` (validation: non-future `purchaseDate`, `reason` required iff `COMPLAINT`); `VisualAssessment` (RETURN + COMPLAINT field sets + `analyzable`); `Decision` (`binding` derived = outcome==APPROVE); `ChatMessage`; `Session`. | ADR-000 §5 (data models verbatim); ADR-002 §4 (assessment field sets). | TAC-04 |
| **B2** | policy | `PolicyProvider`: load `docs/policies/return-policy.md` / `complaint-policy.md` by `RequestType`, cache in memory, expose text. | ADR-001 §3 (PolicyProvider bullet); PRD §8 doc-reference table; **both policy file paths**. | TAC-002-04 (feeds B5) |
| **B3** | image | `ImageService`: accept only jpeg/png/webp (magic-number sniff vs declared type), reject >10 MB, downscale longest edge ≤1568 px (no upscale), re-encode JPEG q≈0.8, base64 `data:image/jpeg;base64,…`. | ADR-001 §3 "Image validation & compression" verbatim; PRD AC-07/08/10/11. `javax.imageio` is JDK built-in. | TAC-001-03 |
| **B4** | session | `SessionStore` interface + `InMemorySessionStore` (concurrent map, TTL default 2 h, max 500 entries, oldest-evicted-first). | ADR-001 §3 (SessionStore bullet) + Decision 6.3. | TAC-001-07 |
| **B5** | llm port | `LlmClient` interface (3 ops, no openai-java types in signatures); `LlmUpstreamException`; `PromptFactory` (**author Polish** analysis + decision system prompts per scenario, inject policy text from B2); `LlmConfig` (env key-resolution: `OPENAI_API_KEY` else `OPENROUTER_API_KEY`; base URL; split text/vision models + fallback). | ADR-002 §3 (port + routing table), §5 (port semantics), ADR-000 §7 (env vars). PRD §11 (allowed/not-allowed, disclosures, Polish tone). Both policy texts (for prompt authoring). | TAC-002-04, 002-07 |
| **B6** | llm adapter | `OpenRouterResponsesAdapter` (the **only** openai-java-coupled class): `analyzeImage` (vision model, `input_text`+`input_image`, structured `VisualAssessment`), `decide` (text model, policy-injected instructions, structured `Decision`, enum-constrained outcome), `streamChat` (`createStreaming`, forward `OutputTextDelta`, accumulate). Normalize failures → `LlmUpstreamException`; timeout + ≤1 retry; one re-ask on malformed structured output then fail. | ADR-002 §3 (adapter bullets), §6.1/6.2/6.4, §7 sequence diagrams. Context7 `/openai/openai-java`. OpenRouter Responses URLs in ADR-002 §2. | TAC-002-01/02/03/05/06 |
| **B7** | decision | `DecisionOrchestrator`: validate → `ImageService.compress` → `LlmClient.analyzeImage` → `LlmClient.decide` → **compose first chat message** (greeting → decision → justification → binding/preliminary note → next steps, in order, Polish) → `SessionStore.save`. Escalate (not guess) when any input missing or `analyzable=false`. | ADR-001 §3 (DecisionOrchestrator), Decision 8.5; PRD AC-17/22, §11.4–11.5. Mock `LlmClient`/`ImageService` in tests. | TAC-05, 002-03 |
| **B8** | chat | `ChatService`: load session, append USER msg, `LlmClient.streamChat(history, onDelta)`, forward deltas, append completed ASSISTANT msg only on success. History includes form data + assessment + first decision message (context retention). | ADR-001 §3 (ChatService), PRD AC-23/24/25. Mock `LlmClient`. | TAC-001-04 (chat half) |
| **B9** | web | DTOs (frozen contract); `SessionController.create` (multipart binding + server validation), `MessageController.stream` (SSE `delta`/`done`/`error`), `SessionController.get`, `GlobalExceptionHandler` (ApiError + status map). **Integration tests fake only the OpenRouter endpoint via MockWebServer** (TAC-02). | ADR-001 §4 (DTOs), §5 (contracts), §6.1/6.2, §8 scenario table; the frozen-contract block above. | TAC-001-01/02/04/05/06, 05, 06, 07, 08 |
| **B10** | wire/run | `application.yml` (env placeholders, multipart max 10 MB, server.port from `PORT`); start app; `GET /api/health` 200; **live smoke** one real `POST /api/sessions` against OpenRouter with a sample image (Decision 3). | ADR-000 §7, ADR-001 §6.2. `.env` setup from `.env.example`. | TAC-10 |

Each B-step: write/extend tests first (fail), implement, `./mvnw test` (+`verify` for B6/B9 integration), commit `Backend: <step>`.

### Frontend track (fe-developer) — against the frozen contract, ApiService mocked in unit tests

| Step | Area | What to build (TDD) | Context to provide (only this) | TAC |
|---|---|---|---|---|
| **F1** | models | TS interfaces mirroring DTOs: `RequestType`, `EquipmentCategory`, `DecisionDto`, `CreateSessionResponse`, `ChatMessage`, `SseEvent` (delta/done/error union), `ApiError`. | ADR-003 §4 verbatim; frozen-contract block. | — |
| **F2** | ApiService | `createSession(FormData)` → POST multipart; `streamMessage(id,content)` → `fetch` + `ReadableStream` reader parsing `text/event-stream` frames into typed `delta`/`done`/`error` events (Observable/AsyncIterable); `getSession(id)`. | ADR-003 §3 (Services), §6.2, §5 (consumed contracts); SSE event shapes from frozen contract. Context7 `/angular/angular`. | TAC-003-06/07 |
| **F3** | form | `RequestFormComponent`: reactive form + Material controls (selects, datepicker max=today, textarea, file input + thumbnail + remove/replace); client validation AC-01..09 (reason required iff Complaint; image required; jpeg/png/webp only; >10 MB inline error; submit disabled until valid); on submit → FormData → `createSession`, loading state; error mapping 400/413/415 inline + 502/503 snackbar (preserve values, no nav). | ADR-003 §3 (RequestFormComponent), PRD §9.1 + AC-01..09/26/27; design tokens for styling. Context7 Angular Material. | TAC-003-01/02/03/05 |
| **F4** | chat | `ChatComponent`: message list (distinct system/assistant/user bubbles); first message rendered formatted (safe markdown) with **outcome badge/heading** + visible **preliminary/human-review disclosure** for Reject/Escalate; composer disabled while streaming + typing indicator; append deltas → finalize on `done`; mid-stream `error` → inline retry keeping history; "Start new request"; deep-link reload → `getSession` fallback (404 → form). | ADR-003 §3 (ChatComponent), §6.3, PRD §9.2 + AC-19/20/21/22/24; design tokens. | TAC-003-04/06/07 |
| **F5** | theme polish | Apply NBP design across form + chat: navy header with `logo.svg` (linked to `/`), Libre Franklin body / Brygada 1918 headings, accent/link colors, small radii, generous whitespace, responsive (desktop + mobile). Confirm all visible text is Polish. | design-guidelines.md §2–8; `assets/logo.svg`; design-tokens.json. | TAC-003-08 |

Each F-step: write/extend `*.spec.ts` first (fail), implement, `ng test --watch=false` + `ng lint`, commit `Frontend: <step>`. `ng build` clean at F5.

> **Gate G1:** all of B1–B10 and F1–F5 committed and green; both apps build. Only then Phase 2.

---

## Phase 2 — Integration (be-developer, single)

### I1 — Live end-to-end wiring through the dev proxy
- **Context to provide:** Decision 8.6 (proxy), frozen-contract block, PRD §4 main flows.
- **Task:** Run backend (`./mvnw spring-boot:run`) + frontend (`ng serve`) together. Manually walk
  one happy path (Return → Approve) live against OpenRouter through the proxy. Fix any
  contract/serialization mismatch (DTO field names, SSE framing, multipart field names). If a fix
  touches the frozen contract, orchestrator coordinates the mirror change in the other track.
- **Done when:** form submit → decision → first message → one streamed follow-up all work live.
- **Commit (only if fixes were needed):** `Backend: align <contract item> for end-to-end wiring`.

> **Gate G2:** live happy path verified before QA.

---

## Phase 3 — QA / E2E (qa-engineer, single, real stack)

### Q1 — Manual smoke + screenshots
- **Context:** PRD §4 flows + §6 ACs; Decision 3 (live). Playwright MCP available.
- **Task:** With both apps running, smoke-test each scenario and capture screenshots: Return→Approve,
  Complaint→Approve, Reject (worn item / out-of-policy damage), Escalate (blurry/wrong item),
  validation blocks (missing reason on complaint, wrong file type, >10 MB), LLM-failure retry
  (force an upstream error). Report defects to the orchestrator for routing back to BE/FE.

### Q2 — Playwright E2E (no mocking — TAC-09)
- **Context:** ADR-000 §10 (E2E layer), ADR-003 §8 (Full-flow row), TAC-003-09. Real key.
- **Task:** Author Playwright specs under `app/e2e/` driving the full form→decision→chat flow against
  the **real stack**; assert outcome emphasis and the preliminary/human-review disclosure for
  Reject/Escalate; assert streamed reply renders incrementally. Run green.
- **Commit:** `QA: Playwright E2E for form→decision→chat on real stack`.

### Q3 — Regression sign-off
- **Task:** Run full BE suite (`./mvnw verify`), full FE suite (`ng test --watch=false`, `ng lint`,
  `ng build`), start both apps, confirm `/api/health` 200 (TAC-10). Produce a short pass/fail report
  mapped to the TAC list across all three ADRs.
- **Commit (if test-only fixes):** `QA: regression fixes for PoC sign-off`.

> **Gate G3 (PoC complete):** E2E green on real stack + full regression green + app starts.

---

## Dependency matrix

### Step → blocking predecessors

| Step | Agent | Must finish first |
|---|---|---|
| B0 | be-dev | — |
| F0 | fe-dev | — |
| B1 | be-dev | B0 |
| B2 | be-dev | B1 |
| B3 | be-dev | B1 |
| B4 | be-dev | B1 |
| B5 | be-dev | B1, B2 |
| B6 | be-dev | B5 |
| B7 | be-dev | B3, B4, B6 |
| B8 | be-dev | B4, B6 |
| B9 | be-dev | B7, B8 |
| B10 | be-dev | B9 |
| F1 | fe-dev | F0 |
| F2 | fe-dev | F1 |
| F3 | fe-dev | F2 |
| F4 | fe-dev | F2 |
| F5 | fe-dev | F3, F4 |
| I1 | be-dev | **B10 + F5** (Gate G1) |
| Q1 | qa-eng | I1 (Gate G2) |
| Q2 | qa-eng | Q1 |
| Q3 | qa-eng | Q2 |

### Agent ownership & conflict avoidance

| Agent | Owns (writes only) | Runs in parallel with | Never touches |
|---|---|---|---|
| be-developer | `app/backend/**` | fe-developer (Phase 0–1) | `app/frontend/**` |
| fe-developer | `app/frontend/**` | be-developer (Phase 0–1) | `app/backend/**` |
| qa-engineer | `app/e2e/**` | — (runs solo after G2) | `app/backend/**`, `app/frontend/**` |

No two concurrently-running agents share a directory, so same-branch parallel commits cannot
conflict. The only true synchronization point is the **frozen contract** (B9 ↔ F1/F2): orchestrator
verifies both sides match before Gate G1, and gates I1 on it.

### Critical path

`B0 → B1 → B5 → B6 → B7 → B9 → B10 → I1 → Q1 → Q2 → Q3`
(the FE track F0→…→F5 runs alongside and must also be done by G1, but the backend chain is longer).

---

## Verification (how to confirm the PoC works end-to-end)

1. **Toolchain:** `./mvnw -v` (Java 21 or, per Decision 4, JDK 25 if it built clean); `node -v`,
   `ng version`.
2. **Backend alone:** `cd app/backend && ./mvnw verify` (unit + MockWebServer integration green),
   `./mvnw spring-boot:run`, then `GET http://localhost:3000/api/health` → 200; one live
   `POST /api/sessions` with a sample JPEG returns `201` with a decision + first message.
3. **Frontend alone:** `cd app/frontend && ng test --watch=false && ng lint && ng build` all clean;
   `ng serve` boots.
4. **Integrated (live):** both running; in the browser complete a Return and a Complaint, observe a
   decision and at least one streamed follow-up reply; trigger a validation error and an upstream
   error to see inline/snackbar handling.
5. **E2E:** Playwright suite under `app/e2e/` green against the real stack (TAC-09); assert outcome
   emphasis + disclosure for Reject/Escalate.
6. **TAC sweep:** Q3 maps results to TAC-01..10 (ADR-000), TAC-001-01..07, TAC-002-01..07,
   TAC-003-01..08.

---

## Orchestrator notes (for the execution session)

- Spawn **B0 and F0 together** (one message, two `Agent` calls) — parallel, non-overlapping dirs.
- Drive each track as a **sequential chain**, continuing the same agent via its session id so module
  context carries forward; advance a track only after its step commit is green.
- Hold the **frozen contract** as the integration guard; if either side proposes a contract change,
  pause both tracks, reconcile, then resume.
- Give each step **only its "Context to provide" cell** — not the whole ADR set — to keep agent
  context lean, exactly as required.
- Route QA-found defects back to the owning track's agent as a new TDD step (failing test first).
