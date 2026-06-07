# ReadabilityKMP

ReadabilityKMP is a Kotlin Multiplatform reader-mode SDK built around Mozilla Readability.

The reusable SDK lives in [`readability-core`](./readability-core). It provides:

- A bundled Mozilla `Readability.js` payload.
- A `ReadabilityScriptProvider` abstraction for host apps that inject JavaScript into a browser view.
- Shared reader-mode models such as `ReaderArticle` and `ReaderModeState`.

The Compose and app modules in this repository are samples. They are not part of the SDK artifact consumers should import.

## Credits

This project bundles and exposes [Mozilla Readability](https://github.com/mozilla/readability), the article extraction library used by Firefox Reader View. Credit for the Readability extraction engine belongs to Mozilla and the Mozilla Readability contributors.

ReadabilityKMP provides Kotlin Multiplatform packaging, shared models, and host-app integration patterns around that engine.

## Project Structure

- [`readability-core`](./readability-core): reusable Kotlin Multiplatform SDK module.
- [`sharedLogic`](./sharedLogic): sample shared logic that consumes `readability-core`.
- [`sharedUI`](./sharedUI): sample Compose Multiplatform UI.
- [`androidApp`](./androidApp): Android sample app.
- [`iosApp`](./iosApp): iOS sample app that hosts the shared Compose UI.

Consumers should depend on `readability-core` only.

## Current SDK Scope

`readability-core` is intentionally UI-free. It does not own `WebView`, `WKWebView`, navigation, Compose UI, or SwiftUI code.

The host app is responsible for:

- Loading a web page.
- Injecting the bundled Readability script into the page.
- Running `new Readability(document.cloneNode(true)).parse()` in the page context.
- Mapping the returned article payload into `ReaderArticle`.
- Managing app-specific reader-mode UI.

This keeps the SDK reusable across Android, iOS, and future browser integrations.

## Install

### Local Project Dependency

If your app is in this same Gradle build:

```kotlin
dependencies {
    implementation(project(":readability-core"))
}
```

### Maven Local

Publish locally:

```bash
./gradlew :readability-core:publishToMavenLocal -PVERSION_NAME=0.1.0-local
```

Consume from another Kotlin Multiplatform project:

```kotlin
repositories {
    google()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.mili.readability:readability-core:0.1.0-local")
}
```

### GitHub Packages

Publish:

```bash
./gradlew :readability-core:publishAllPublicationsToGitHubPackagesRepository \
  -Pgithub.repository=OWNER/REPOSITORY \
  -Pgpr.user=GITHUB_USER \
  -Pgpr.key=GITHUB_TOKEN \
  -PVERSION_NAME=0.1.0
```

Consume:

```kotlin
repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/OWNER/REPOSITORY")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull
            password = providers.gradleProperty("gpr.key").orNull
        }
    }
}

dependencies {
    implementation("com.mili.readability:readability-core:0.1.0")
}
```

For private GitHub repositories or private packages, consumers need credentials with package read access.

## Public API

The SDK currently exposes a small, platform-neutral API from the package:

```kotlin
import com.mili.readability.core.MozillaReadabilityScriptProvider
import com.mili.readability.core.ReadabilityScriptProvider
import com.mili.readability.core.ReaderArticle
import com.mili.readability.core.ReaderModeState
```

### `ReadabilityScriptProvider`

Contract for getting the JavaScript payload that your app injects into a browser page.

```kotlin
interface ReadabilityScriptProvider {
    fun getReadabilityScript(): String
}
```

You normally use the default implementation:

```kotlin
val provider: ReadabilityScriptProvider = MozillaReadabilityScriptProvider()
val readabilityJs: String = provider.getReadabilityScript()
```

Output: a `String` containing the bundled Mozilla `Readability.js` source.

### `ReaderArticle`

Shared model for the article extracted by Readability.

```kotlin
data class ReaderArticle(
    val title: String,
    val contentHtml: String,
    val textContent: String,
    val length: Int,
    val excerpt: String? = null,
    val byline: String? = null,
    val dir: String? = null,
    val siteName: String? = null,
    val lang: String? = null,
    val publishedTime: String? = null,
)
```

Output: your app should map the JSON returned by the injected JavaScript into this model.

### `ReaderModeState`

Optional shared state model for your reader-mode workflow.

```kotlin
sealed interface ReaderModeState {
    data object Idle : ReaderModeState
    data object Loading : ReaderModeState
    data class Available(val article: ReaderArticle) : ReaderModeState
    data class Unavailable(val reason: String? = null) : ReaderModeState
    data class Failed(val message: String, val cause: Throwable? = null) : ReaderModeState
}
```

Output: your UI can render reader-mode availability using this state.

## What Your App Implements

`readability-core` does not directly control Android `WebView` or iOS `WKWebView`. Your app implements the platform bridge.

At minimum, your app needs to implement:

- A page-loaded hook, such as Android `WebViewClient.onPageFinished` or an equivalent `WKWebView` navigation callback.
- A JavaScript evaluator, such as `WebView.evaluateJavascript` or `WKWebView.evaluateJavaScript`.
- JSON decoding from the JavaScript result into `ReaderArticle`.
- Error handling that maps failures to `ReaderModeState.Failed` or `ReaderModeState.Unavailable`.

The integration flow is:

1. Create `MozillaReadabilityScriptProvider`.
2. Call `getReadabilityScript()`.
3. Inject the returned script into the loaded page.
4. Invoke the extraction wrapper in the page context.
5. Decode the returned JSON.
6. Return `ReaderModeState.Available(article)` when parsing succeeds.

The extraction wrapper your app invokes after injecting the SDK script is:

```javascript
(() => {
  const article = new Readability(document.cloneNode(true)).parse();
  return JSON.stringify(article);
})();
```

Expected JavaScript output is a JSON string. Readability may return `null` when the page is not readable.

Example successful output:

```json
{
  "title": "Example Article",
  "byline": "Author Name",
  "dir": "ltr",
  "lang": "en",
  "content": "<div><p>Readable article HTML.</p></div>",
  "textContent": "Readable article text.",
  "length": 1234,
  "excerpt": "Readable article text.",
  "siteName": "Example"
}
```

Map `content` from the Readability result to `ReaderArticle.contentHtml`.

## Basic Usage

Use `MozillaReadabilityScriptProvider` to access the bundled Mozilla Readability script:

```kotlin
import com.mili.readability.core.MozillaReadabilityScriptProvider

val scriptProvider = MozillaReadabilityScriptProvider()
val readabilityJs = scriptProvider.getReadabilityScript()
```

Inject that script into your platform browser context, then execute a wrapper that parses the page:

```javascript
(() => {
  const article = new Readability(document.cloneNode(true)).parse();
  return JSON.stringify(article);
})();
```

The returned JSON should be decoded by the host app and mapped to:

```kotlin
import com.mili.readability.core.ReaderArticle

val article = ReaderArticle(
    title = "Article title",
    contentHtml = "<article>...</article>",
    textContent = "Readable article text",
    length = 1234,
    excerpt = "Short preview",
    siteName = "Example"
)
```

Use `ReaderModeState` if your app wants a shared state model:

```kotlin
import com.mili.readability.core.ReaderModeState

val state: ReaderModeState = when {
    article != null -> ReaderModeState.Available(article)
    else -> ReaderModeState.Unavailable("No readable article found")
}
```

## Android Integration Sketch

In an Android app, inject the script into a `WebView` after the page is loaded:

```kotlin
val readabilityJs = MozillaReadabilityScriptProvider().getReadabilityScript()

webView.evaluateJavascript(readabilityJs) {
    webView.evaluateJavascript(
        """
        (() => {
          const article = new Readability(document.cloneNode(true)).parse();
          return JSON.stringify(article);
        })();
        """.trimIndent()
    ) { json ->
        // Decode json and map it to ReaderArticle.
    }
}
```

The exact WebView lifecycle, page-readiness checks, and JSON decoding should live in your app or a platform adapter module.

## iOS Integration Sketch

In an iOS app, expose the KMP framework to Swift and inject the script into `WKWebView`:

```swift
let script = MozillaReadabilityScriptProvider().getReadabilityScript()

webView.evaluateJavaScript(script) { _, error in
    guard error == nil else { return }

    let wrapper = """
    (() => {
      const article = new Readability(document.cloneNode(true)).parse();
      return JSON.stringify(article);
    })();
    """

    webView.evaluateJavaScript(wrapper) { result, error in
        // Decode result and map it to your reader article model.
    }
}
```

## Running Samples

Build the Android sample:

```bash
./gradlew :androidApp:assembleDebug
```

Build and test the SDK module:

```bash
./gradlew :readability-core:assemble
./gradlew :readability-core:testAndroidHostTest
```

Run the iOS sample from Xcode by opening [`iosApp`](./iosApp).

## Release Notes

Before publishing a public release:

- Update the POM metadata in [`readability-core/build.gradle.kts`](./readability-core/build.gradle.kts) from `OWNER/REPOSITORY` to the real repository URL.
- Choose a stable version, for example `0.1.0`.
- Publish only `readability-core`; keep `sharedLogic`, `sharedUI`, `androidApp`, and `iosApp` as samples.
