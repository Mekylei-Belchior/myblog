# ✅ AI Checklist — Validation Before Completion

## 📌 Context (MANDATORY)

Before validating:

- Read `.github/docs/ai-context.md`
- Read `.github/docs/architecture.md`
- Follow `.github/copilot-instructions.md`

---

## 🎯 Goal

Validate that the implementation or change is:

- Correct
- Consistent
- Maintainable
- Aligned with architecture

---

## 🔍 Validation Checklist

### 🧠 Domain & Business

- [ ] Uses correct domain concepts (Post, User, Comment, etc.)
- [ ] Does NOT introduce conflicting terminology
- [ ] Respects business rules defined in ai-context.md

---

### 🏗️ Architecture

- [ ] Follows defined architecture (layered / modular)
- [ ] No violation of separation of concerns
- [ ] No tight coupling introduced

---

### 🧩 Backend (if applicable)

- [ ] Controller → Service → Repository respected
- [ ] DTO pattern used correctly
- [ ] No entity exposure in API
- [ ] Validation handled properly
- [ ] Security rules respected (JWT, protected endpoints)

---

### 🎨 Frontend (if applicable)

- [ ] Components are well structured
- [ ] API calls via service layer
- [ ] No direct coupling with backend entities
- [ ] State handling is clear

---

### 🔐 Security

- [ ] No hardcoded secrets
- [ ] Sensitive endpoints are protected
- [ ] JWT flow respected

---

### ⚙️ DevOps / Infra

- [ ] Works with Docker setup
- [ ] Compatible with k3s deployment
- [ ] Environment variables used correctly

---

### 🧼 Code Quality

- [ ] No duplication
- [ ] Clear naming
- [ ] Small, focused methods/functions
- [ ] Readable and maintainable

---

## ⚠️ Common Issues to Flag

- Business logic in controllers
- Mixed responsibilities
- Hidden coupling
- Overengineering
- Breaking existing API contracts

---

## 📤 Output Format

### ✅ Valid
- short justification

### ⚠️ Issues Found
- bullet points

### 🔧 Suggested Fixes
- actionable improvements