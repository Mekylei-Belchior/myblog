# 🧠 AI Prompt Base Template

## 📌 Context (MANDATORY)

Before performing any task:

1. Read and understand:
   - `.github/docs/ai-context.md` → domain, business rules, API behavior
   - `.github/docs/architecture.md` → system design, constraints, patterns

2. Follow strictly:
   - `.github/copilot-instructions.md`

---

## 🎯 Core Principles

- Prefer consistency over creativity
- Avoid duplication of logic
- Keep solutions simple and maintainable
- Respect existing architecture and patterns
- Make incremental improvements (no big rewrites unless required)
- Ensure production readiness

---

## 🧱 Architectural Constraints

### Backend (Spring Boot)

- Follow layered architecture:
  - Controller → Service → Repository
- Controllers must be thin (no business logic)
- Business logic must live in services
- Use DTO pattern:
  - NEVER expose entities directly in API
- Use proper validation (request validation layer)

---

### Frontend (Angular)

- Use component-based architecture
- Use service layer for API communication
- Avoid tight coupling with backend models
- Keep components focused and reusable

---

### Security

- Respect JWT authentication flow
- Do NOT expose sensitive data
- Protect endpoints appropriately
- Never hardcode secrets or credentials

---

## 🔄 Expected Behavior

- Analyze BEFORE implementing
- If something is unclear:
  - infer from `ai-context.md` and `architecture.md`
- Prefer small, safe, incremental changes
- Ensure compatibility with:
  - Docker
  - Kubernetes (k3s)
  - Jenkins pipelines

---

## ⚠️ Anti-Patterns to Avoid

- Business logic inside controllers
- Direct entity exposure in API
- Hardcoded configuration
- Tight coupling between layers
- Breaking API contracts
- Overengineering solutions

---

## 🧪 Quality Expectations

- Code must be:
  - Readable
  - Maintainable
  - Testable

- Follow:
  - SOLID principles
  - Clean Code
  - Clean Architecture

---

## 📤 Output Guidelines

- Be clear and structured
- Prefer bullet points over long paragraphs
- Provide production-ready solutions
- Avoid unnecessary explanations unless requested