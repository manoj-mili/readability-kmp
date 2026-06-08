# readability-core

`readability-core` is the reusable KMP SDK module for reader mode. App modules should consume this module; extraction rules, public reader models, bundled script access, and future parser/injection contracts should live here.

## Current Assessment

What is correct:

- A separate `:readability-core` module already exists, which is the right boundary for an SDK/library.
- The module targets Android and iOS, so it can be consumed by browser apps on both platforms.
- Mozilla Readability is bundled as the extraction engine, which is a practical first implementation for article extraction.
- The public package is now `com.mili.readability.core`, independent from the sample app package.

What was not correct:

- The previous API and implementation used different packages, so the module was not SDK-shaped.
- `MozillaReadabilityRepository.getReadabilityScript()` was empty, so the core module could not provide the bundled extractor.
- The Android namespace used `com.mili.readability-core`, which is not a valid Java/Kotlin package name.
- Template `Platform.android.kt` and `Platform.ios.kt` files were left in the module, causing compilation failure because no common `expect` existed.
- The existing model mixed extracted article data with UI state. The SDK should expose both, but they are separate concepts.
- Browser-specific work such as WebView/WKWebView JavaScript injection should not be hidden inside common code unless it is represented through platform-specific adapters.

## Public Surface

Current minimal API:

```kotlin
val scriptProvider: ReadabilityScriptProvider = MozillaReadabilityScriptProvider()
val readabilityJs: String = scriptProvider.getReadabilityScript()
```

`ReaderArticle` represents extracted article data returned by Readability.js. `ReaderModeState` represents reader-mode workflow state for apps that want a shared state model.

## Recommended SDK Shape

Keep the SDK split into three layers:

- `readability-core`: common data models, script provider, extraction configuration, result/error types, and JSON contracts.
- `readability-webview-android`: Android adapter for `WebView.evaluateJavascript`, page readiness checks, and script injection.
- `readability-webview-ios`: iOS adapter for `WKWebView.evaluateJavaScript`, page readiness checks, and script injection.

This keeps the pure SDK portable while still giving browser apps convenient platform integrations.

## Next Implementation Steps

1. Add a `ReadabilityOptions` model matching the options passed to `new Readability(document, options)`.
2. Add a small JavaScript wrapper that injects Mozilla Readability and returns a stable JSON object:

```javascript
(() => {
  const article = new Readability(document.cloneNode(true)).parse();
  return JSON.stringify(article);
})();
```

3. Add `ReaderArticleJsonParser` in common code. Use `kotlinx.serialization` so Android and iOS decode the same payload.
4. Add platform adapter interfaces:

```kotlin
interface ReaderModeEngine {
    suspend fun extractCurrentPage(): Result<ReaderArticle>
    suspend fun canExtractCurrentPage(): Boolean
}
```

5. Implement Android and iOS adapters in platform modules or source sets, not in shared UI.
6. Make `sharedUI` and app modules consume the SDK through `ReaderModeEngine` or `ReadabilityScriptProvider`.
7. Add fixture-based tests with saved HTML pages and expected article fields before beta.

## Publishing Plan

For local reuse first:

```kotlin
implementation(project(":readability-core"))
```

For a generated local Maven build:

```shell
./gradlew :readability-core:publishToMavenLocal -PVERSION_NAME=0.1.0-alpha01
```

Then consume it from another KMP project:

```kotlin
repositories {
    google()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.mili.readability:readability-core:0.1.0-alpha01")
}
```

For a generated repository folder that can be zipped or copied:

```shell
./gradlew :readability-core:publishAllPublicationsToLocalBuildRepository -PVERSION_NAME=0.1.0-alpha01
```

The artifacts are written under `readability-core/build/repo`.

For use by other projects:

- Add Gradle Maven publishing metadata.
- Publish Android AAR plus KMP metadata to Maven Local or an internal Maven repository.
- Generate an XCFramework for iOS consumers that are not using Gradle directly.
- Keep package names and binary API stable before the first `0.1.0` release.

For JitPack:

```shell
git tag 0.1.0-alpha01
git push origin 0.1.0-alpha01
```

Consumers add the JitPack Maven repository:

```kotlin
repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.manoj-mili.readability-kmp:readability-core:0.1.0-alpha01")
}
```

## Verification

Current verified commands:

```shell
./gradlew :readability-core:embedReadabilityJs
./gradlew :readability-core:assemble
./gradlew :readability-core:testAndroidHostTest
```
