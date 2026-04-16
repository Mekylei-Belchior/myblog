# .ia/ — AI Development Context

This directory is the **single source of truth** for all AI-assisted development context
in this repository. Humans and AI agents read from and write to this directory.

## Structure

```
.ia/
├── context/             ← Source-of-truth documentation
│   ├── ai-context.md    ← Domain model, tech stack, API surface, known violations
│   └── architecture.md  ← System diagrams, deployment topology, improvement roadmap
├── prompts/             ← Reusable agent prompts (.prompt.md)
└── memory/              ← Persistent AI agent notes (session/repo memory)
```

## Workflow

| Who | Reads from | Writes to |
|-----|-----------|-----------|
| GitHub Copilot (chat) | `.github/docs/` (proxy) | — |
| GitHub Copilot (agent) | `.ia/context/` | `.ia/context/` |
| Human developer | `.ia/context/` | `.ia/context/` |

## Syncing proxies

After editing any file in `context/`, sync the corresponding proxy in `.github/docs/`:
- Copy content between `<!-- BEGIN SYNCED CONTENT -->` and `<!-- END SYNCED CONTENT -->` markers.
- Or run the sync prompt: `.github/prompts/sync-ai-docs.prompt.md`
