# Contributing

Thanks for improving FutureMe Financial.

## Engineering rules

- Keep all financial formulas in `shared/calculators`.
- Keep scenario policy in `shared/scenario-engine`.
- AI providers may explain shared results but may not generate financial figures.
- Platform modules own UI and platform integrations only.
- Use mock data. Never commit credentials or real financial records.
- Preserve accessibility, dark mode, loading, empty, and error states.
- Document assumption changes as product changes.

## Local checks

```bash
./gradlew :shared:testDebugUnitTest :apps:android:assembleDebug
cd apps/web
npm install
npm test
npm run build
```

Open `apps/ios/FutureMeFinancial.xcodeproj` and build an iPhone simulator target for iOS changes.

## Pull requests

Include:

- Problem and user impact
- Architectural choice
- Tests performed
- Screenshots for UI changes
- Calculation assumptions changed, if any

## Checklist

- [ ] Shared tests pass
- [ ] Android debug APK builds
- [ ] Web tests and production build pass
- [ ] iOS simulator build was checked when applicable
- [ ] No duplicate formulas were added to a client
- [ ] No secrets or real financial data were added
- [ ] Accessibility and light/dark themes were checked
- [ ] The educational disclaimer remains visible
