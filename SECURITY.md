# Security Policy

## Supported versions

This repository is an early-stage educational prototype. Security fixes are applied to the latest commit on `main`.

## Reporting a vulnerability

Do not open a public issue for a suspected vulnerability.

Use GitHub's private vulnerability reporting feature when enabled, or contact the repository maintainer privately. Include:

- A clear description of the issue
- Reproduction steps or proof of concept
- Affected files or versions
- Expected impact
- Suggested mitigation, if available

Please allow a reasonable period for investigation before public disclosure.

## Data and credential policy

FutureMe Financial currently uses mock data only.

- Never submit real bank credentials, Plaid tokens, account numbers, or customer financial data.
- Never commit signing keys, keystores, API keys, or `.env` files.
- Treat future financial-data connectors as privileged infrastructure.
- Keep AI providers downstream of deterministic calculator outputs.

This prototype has not undergone a formal security, privacy, regulatory, or compliance review and must not be used with real customer data.
