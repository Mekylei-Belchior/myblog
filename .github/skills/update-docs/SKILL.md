# SKILL: DOCUMENTATION SYNC & ARCHITECTURE ALIGNMENT

## PURPOSE

Maintain and continuously improve the project documentation to ensure it is:

- Non-redundant
- Consistent
- Architecturally correct
- Easy for AI to consume
- Easy for humans to maintain

This includes:

- Root README.md (PRIMARY human-facing document Always in Brazilian Portuguese)
- Client README (if relevant)
- .ia/context/* (source of truth)
- .github/docs/* (derived documentation)
- .github/copilot-instructions.md

---

## SOURCE OF TRUTH

The following files are authoritative:

- .ia/context/ai-context.md
- .ia/context/architecture.md

RULE:
- NEVER modify source-of-truth files automatically
- All other documentation must align with them AND the actual code

---

## CONTEXT GOVERNANCE (CRITICAL)

- `.ia/context/*` is the source of truth but NOT immutable

- The agent MUST NOT modify `.ia/context/*` automatically

- The agent MUST:
  - Detect inconsistencies between:
    - Code
    - Documentation
    - `.ia/context/*`
  - Propose updates when inconsistencies are found

- Every proposal MUST include:
  - What should change
  - Why it should change
  - Impact of the change

- Updates to `.ia/context/*` require explicit human approval

- NEVER ignore context drift

---

## DOCUMENT RESPONSIBILITIES (STRICT)

Enforce single responsibility:

- ai-context.md  
  → domain, business rules, API contracts, entities

- architecture.md  
  → system design, layers, infrastructure, patterns

- copilot-instructions.md  
  → AI behavior rules, constraints, execution guidance

- README.md (root)  
  → high-level project overview for humans:
    - purpose
    - architecture summary
    - tech stack
    - how to run
    - project structure

- client/README.md  
  → frontend-specific instructions (only if relevant)

- .github/docs/*  
  → synchronized mirrors of `.ia/context/*`

---

## EXECUTION STRATEGY (MULTI-MODEL)

---

### STEP 1 — ANALYSIS (ASK | Opus)

Perform a deep analysis of:

- Entire project structure
- Backend (api/)
- Frontend (client/)
- Kubernetes (k8s/)
- Root README.md
- client/README.md (if exists)
- .ia/context/*
- .github/docs/*
- .github/copilot-instructions.md

#### REQUIRED ANALYSIS

##### 1. DUPLICATION DETECTION
- Detect duplicated content across:
  - ai-context.md vs architecture.md
  - context vs README
  - context vs copilot-instructions.md
- Identify unnecessary repetition

##### 2. RESPONSIBILITY VIOLATIONS
- Detect mixed concerns across files
- Flag incorrect content placement

##### 3. DOCUMENTATION vs CODE GAP
- Identify:
  - Missing features in docs
  - Outdated descriptions
  - Incorrect architecture representation

##### 4. CONSISTENCY CHECK
Ensure consistency in:
- Terminology
- API naming
- Security rules
- Architectural patterns

##### 5. PROXY VALIDATION
Check `.github/docs/*`:
- Must contain:
  <!-- BEGIN SYNCED CONTENT -->
  <!-- END SYNCED CONTENT -->
- Must match `.ia/context/*`
- Must NOT contain manual edits inside sync block

##### 6. README QUALITY
Evaluate root README.md:
- Is it aligned with current architecture?
- Does it reflect:
  - api + client + k8s?
  - docker usage?
- Is it too verbose or too generic?

---

### STEP 2 — PLANNING (PLAN | Sonnet)

Create a precise and structured plan.

For each file, define:

- File path
- Action (update / refactor / remove / keep)
- Reason

Include:

#### REQUIRED ACTIONS

- Fix duplication
- Enforce single responsibility
- Improve AI readability:
  - structured sections
  - clear headings
  - explicit rules
- Improve README:
  - clear overview
  - architecture summary
  - updated structure
- Align `.github/docs/*` with `.ia/context/*`

---

### STEP 3 — EXECUTION (AGENT | Haiku)

Execute ONLY what was planned.

#### FILES TO UPDATE

- README.md (ROOT)
- client/README.md (if needed)
- .github/docs/ai-context.md
- .github/docs/architecture.md
- .github/docs/README.md
- .github/copilot-instructions.md

#### RULES

- DO NOT invent information
- DO NOT assume architecture
- DO NOT modify `.ia/context/*` automatically
- DO NOT duplicate content across files
- DO NOT add unnecessary verbosity

#### README REQUIREMENTS (ROOT)

Ensure it contains:

- Project overview
- Architecture summary (api + client + k8s)
- Tech stack
- How to run (docker / local)
- Project structure (based on real tree)

---

### STEP 4 — VALIDATION (ASK | Sonnet)

Validate final result:

#### CHECKLIST

- [ ] No duplicated content across documents
- [ ] Responsibilities correctly separated
- [ ] README reflects real project structure
- [ ] `.github/docs/*` matches `.ia/context/*`
- [ ] No hallucinated information
- [ ] Terminology is consistent
- [ ] Architecture description is accurate
- [ ] Documentation is concise and structured

If issues are found:
- Suggest minimal corrections

---

### STEP 5 — CONTEXT EVOLUTION (PLAN | Sonnet)

Objective:
Ensure `.ia/context/*` remains accurate over time

#### ACTIONS

- Detect drift between:
  - Code
  - Architecture
  - Current context files

- If drift exists:
  - Propose a minimal patch for:
    - ai-context.md
    - architecture.md

#### OUTPUT FORMAT

For each proposed change:

- File:
- Section:
- Change:
- Reason:
- Impact:

#### RULES

- DO NOT apply changes automatically
- DO NOT rewrite entire files
- Keep changes minimal and precise
- Focus only on real inconsistencies

---

## GLOBAL RULES

- Prefer precision over verbosity
- Prefer structure over narrative text
- Prefer explicit rules over descriptions
- Always trust:
  1. Code
  2. .ia/context
- Never generate generic documentation

---

## SUCCESS CRITERIA

The skill is successful if:

- README is accurate and useful for humans
- Documentation is clean and non-redundant
- AI can reliably use documentation for development
- Architecture is correctly represented everywhere
- Context drift is detected and properly proposed (not ignored)

---

## EXECUTE NOW

Follow strictly:

1. ASK (Opus)
2. PLAN (Sonnet)
3. AGENT (Haiku)
4. VALIDATE (Sonnet)
5. CONTEXT EVOLUTION (Sonnet)

Do not skip steps.