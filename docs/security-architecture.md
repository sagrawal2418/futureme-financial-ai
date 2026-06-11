# Security Architecture

## Current Posture

Version 2 uses mock household data and mock providers. It is not approved for real customer data.

## Credential Boundaries

- No banking or LLM credential is stored in Android, iOS, or web.
- Claude is called only through a backend `LlmProvider`.
- Plaid tokens are created, exchanged, and stored only on the backend.
- Clients receive opaque item references, never Plaid access tokens.
- `.env`, signing keys, and secret files are excluded from Git.

## PII Minimization

`UserIdentity` is separate from `FinancialProfile`. Provider payloads should be normalized into the minimum fields required by a calculation. Raw provider responses should not be retained by default.

## Future Encryption

- TLS for every network boundary
- Managed KMS encryption for backend records
- Token vaulting for Plaid credentials
- Android Keystore and iOS Keychain for local opaque references
- Field-level encryption for sensitive profile attributes

## Future Audit Logging

Audit events should record consent, provider access, normalization version, assumption version, scenario execution, LLM model/prompt version, and administrative access. Logs must exclude secrets and minimize PII.

## Required Reviews Before Production

- Threat model
- Privacy impact assessment
- Authentication and authorization review
- Data retention and deletion design
- Incident response plan
- Vendor and model-risk review
- Regulatory and accessibility assessment
