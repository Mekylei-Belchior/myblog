# 🤖 AI Agent Task Template — Execution

## 📌 Context (MANDATORY)

Before implementing:

1. Read:
   - `.github/docs/ai-context.md`
   - `.github/docs/architecture.md`

2. Follow:
   - `.github/copilot-instructions.md`

---

## 🎯 Task

[Describe the task clearly]

---

## 📋 Based on Plan

If a plan exists:

- Follow the defined steps strictly
- Do NOT skip steps
- Do NOT introduce unrelated changes

---

## ⚙️ Execution Rules

- Keep changes minimal and focused
- Do NOT refactor unrelated code
- Preserve existing behavior unless explicitly required
- Respect all architectural constraints

---

## 🧱 Implementation Guidelines

### Backend

- Use DTOs
- Keep controllers thin
- Put logic in services
- Use repository only for persistence

### Frontend

- Use service layer for API
- Keep components focused
- Avoid tight coupling

---

## 🔐 Security

- Do NOT expose sensitive data
- Respect authentication and authorization rules

---

## ⚠️ DO NOT

- Introduce breaking changes
- Duplicate logic
- Hardcode configuration
- Bypass architecture

---

## 📤 Output Expectations

- Modify only necessary files
- Keep code clean and readable
- Ensure production readiness

---

## ✅ After Implementation

- Ensure code aligns with:
  - ai-context.md
  - architecture.md
- Ready for checklist validation