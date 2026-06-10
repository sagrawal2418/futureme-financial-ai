# Contributing to FutureMe Financial

Thanks for helping improve this educational fintech prototype.

## Development principles

- Keep financial formulas deterministic and covered by unit tests.
- Keep Compose UI free of business calculations.
- Depend on repository and explanation contracts rather than concrete providers.
- Use mock data only. Do not commit credentials, account numbers, tokens, or private financial records.
- Preserve accessibility, dark mode, loading, empty, and error behavior.
- Avoid describing simulation output as financial advice.

## Local workflow

1. Create a focused branch from `main`.
2. Make the smallest coherent change.
3. Add or update tests for financial behavior.
4. Run:

```bash
./gradlew testDebugUnitTest assembleDebug
```

5. Open a pull request with the problem, implementation, screenshots for UI changes, and verification performed.

## Code style

- Follow Kotlin coding conventions.
- Prefer immutable models and pure functions.
- Add comments only when they clarify a non-obvious decision or assumption.
- Keep package boundaries aligned with `docs/architecture.md`.
- Treat calculation-policy changes as product changes and document their assumptions.

## Pull request checklist

- [ ] Unit tests pass
- [ ] Debug APK builds
- [ ] No real financial data or secrets were added
- [ ] Accessibility labels were considered
- [ ] Light and dark themes were checked
- [ ] Documentation reflects user-visible or architectural changes
- [ ] Educational disclaimer remains visible where recommendations appear

## Reporting issues

Use GitHub Issues for reproducible defects and feature proposals. For security concerns, follow [SECURITY.md](SECURITY.md).
