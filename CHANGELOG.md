# Changelog

All notable changes to this project will be documented in this file.

This project uses semantic versioning with Android-style pre-release labels:
`alpha`, `beta`, and `rc`.

## Unreleased

### Breaking Changes

- `MozillaReadabilityScriptProvider` is now `internal`. Use `defaultReadabilityScriptProvider()` factory function instead.

## 0.1.0-alpha01 - 2026-06-08

Initial public alpha for the Kotlin Multiplatform SDK.

- Added `readability-core` as the SDK module.
- Bundled Mozilla Readability JavaScript.
- Added `ReadabilityScriptProvider` and `MozillaReadabilityScriptProvider`.
- Added shared reader-mode models: `ReaderArticle` and `ReaderModeState`.
- Added sample Android, iOS, shared logic, and shared UI modules.
- Added publishing metadata for JitPack-style consumption.

This is an alpha release. Public APIs may change before `0.1.0`.
