# Contributing

Contributions are welcome, especially fixes that keep `readability-core` small, platform-neutral, and UI-free.

## Development Setup

Requirements:

- JDK 21
- Android SDK
- Xcode for iOS target builds on macOS

Useful commands:

```bash
./gradlew :readability-core:assemble
./gradlew :readability-core:testAndroidHostTest
./gradlew :androidApp:assembleDebug
```

## SDK Boundary

Keep reusable SDK code in `readability-core`.

Do not add Compose, Android `WebView`, SwiftUI, or `WKWebView` dependencies to `readability-core`. Platform-specific integrations should live in separate adapter modules or sample modules.

## Pull Requests

Before opening a pull request:

- Keep changes focused.
- Update README or changelog entries for user-facing changes.
- Add or update tests for behavior changes.
- Avoid committing generated build outputs or IDE state.

## Release Labels

Use these version phases:

- `alpha`: early API, breaking changes allowed.
- `beta`: feature-complete for the release line, API changes should be rare.
- `rc`: release candidate, only critical fixes expected.
- stable: no suffix, for example `0.1.0`.
