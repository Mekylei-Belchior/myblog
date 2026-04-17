Check if the skill "update-docs" exists in `.github/skills/`.

IF the skill exists:
- Use the "update-docs" skill for the current project state

- Focus on:
  - detecting documentation drift
  - improving README
  - eliminating duplication
  - ensuring context consistency

- Execute full pipeline:

  1. ASK (Opus)
  2. PLAN (Sonnet)
  3. AGENT (Haiku)
  4. VALIDATE (Sonnet)
  5. CONTEXT EVOLUTION (Sonnet)

ELSE:
- Do nothing