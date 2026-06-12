package com.mili.readabilitykmp

import com.mili.readability.core.defaultReadabilityScriptProvider
import com.mili.readability.core.ReaderArticle
import com.mili.readability.core.ReaderModeState

class ReaderSdkSample {
    fun summary(): String {
        val state = ReadabilityUiSample().state()
        return "Readability SDK loaded: ${state.scriptLoaded} (${state.scriptLength} chars)"
    }
}

data class ReadabilityUiSampleState(
    val title: String,
    val platformName: String,
    val scriptLoaded: Boolean,
    val scriptLength: Int,
    val validatedStates: List<String>,
    val sampleArticleTitle: String,
    val sampleArticleExcerpt: String,
) {
    val statusLabel: String
        get() = if (scriptLoaded) "Ready" else "Not ready"
}

class ReadabilityUiSample {
    fun state(): ReadabilityUiSampleState {
        val script = defaultReadabilityScriptProvider().getReadabilityScript()
        val article = ReaderArticle(
            title = "Readable Sample Article",
            contentHtml = "<article><h1>Readable Sample Article</h1><p>Reader mode content preview.</p></article>",
            textContent = "Reader mode content preview.",
            length = 28,
            excerpt = "Reader mode content preview.",
            siteName = "ReadabilityKMP",
        )
        val states = listOf(
            ReaderModeState.Idle,
            ReaderModeState.Loading,
            ReaderModeState.Available(article),
            ReaderModeState.Unavailable("No article detected"),
            ReaderModeState.Failed("Extraction failed"),
        )

        return ReadabilityUiSampleState(
            title = "Readability UI Validation",
            platformName = getPlatform().name,
            scriptLoaded = script.contains("function Readability"),
            scriptLength = script.length,
            validatedStates = states.map { state ->
                when (state) {
                    ReaderModeState.Idle -> "Idle"
                    ReaderModeState.Loading -> "Loading"
                    is ReaderModeState.Available -> "Available"
                    is ReaderModeState.Unavailable -> "Unavailable"
                    is ReaderModeState.Failed -> "Failed"
                }
            },
            sampleArticleTitle = article.title,
            sampleArticleExcerpt = article.excerpt ?: article.textContent,
        )
    }
}
