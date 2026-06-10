# Provider Services

This boundary will host authenticated adapters for financial data, persistence, alerts, and enterprise systems.

Rules:

- Providers return shared-compatible contracts.
- Providers do not implement financial formulas.
- Bank access is opt-in and backend-only.
- Credentials belong in managed secret storage.
- Every imported datum requires provenance, consent, and retention policy.

Planned adapters include Plaid sandbox, manual-entry sync, banker workflows, and core banking/CRM integrations.
