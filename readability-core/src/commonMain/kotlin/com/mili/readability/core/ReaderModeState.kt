package com.mili.readability.core

/**
 * Article payload returned by Mozilla Readability after host-side extraction.
 *
 * The host app owns WebView/WKWebView injection and JSON decoding. This model
 * keeps the reusable SDK independent from a specific browser UI implementation.
 */
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

sealed interface ReaderModeState {
    data object Idle : ReaderModeState
    data object Loading : ReaderModeState
    data class Available(val article: ReaderArticle) : ReaderModeState
    data class Unavailable(val reason: String? = null) : ReaderModeState
    data class Failed(val message: String, val cause: Throwable? = null) : ReaderModeState
}
