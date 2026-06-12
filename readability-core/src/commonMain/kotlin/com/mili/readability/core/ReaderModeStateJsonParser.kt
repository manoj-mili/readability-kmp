package com.mili.readability.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Parses raw JSON returned by Mozilla Readability into the shared reader-mode state model.
 *
 * Pass the result of `JSON.stringify(article)` here. Platform-specific wrappers, such as
 * Android WebView's quoted `evaluateJavascript` result, should be unwrapped before parsing.
 */
class ReaderModeStateJsonParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    },
) {
    fun parse(rawJson: String): ReaderModeState {
        val trimmedJson = rawJson.trim()
        if (trimmedJson.isEmpty() || trimmedJson == "null" || trimmedJson == "undefined") {
            return ReaderModeState.Unavailable(NO_READABLE_ARTICLE_REASON)
        }

        return runCatching {
            json.decodeFromString(ReadabilityArticlePayload.serializer(), trimmedJson)
        }.fold(
            onSuccess = { payload -> payload.toReaderModeState() },
            onFailure = { cause ->
                ReaderModeState.Failed(
                    message = PARSE_FAILURE_MESSAGE,
                    cause = cause,
                )
            },
        )
    }

    private fun ReadabilityArticlePayload.toReaderModeState(): ReaderModeState {
        val contentHtml = content?.takeIf { it.isNotBlank() } ?: contentHtml.orEmpty()
        if (contentHtml.isBlank()) {
            return ReaderModeState.Unavailable(NO_READABLE_ARTICLE_REASON)
        }

        val articleText = textContent.orEmpty()
        return ReaderModeState.Available(
            ReaderArticle(
                title = title.orEmpty(),
                contentHtml = contentHtml,
                textContent = articleText,
                length = length ?: articleText.length,
                excerpt = excerpt,
                byline = byline,
                dir = dir,
                siteName = siteName,
                lang = lang,
                publishedTime = publishedTime,
            ),
        )
    }
}

private const val NO_READABLE_ARTICLE_REASON = "No readable article found"
private const val PARSE_FAILURE_MESSAGE = "Failed to parse Readability JSON"

@Serializable
private data class ReadabilityArticlePayload(
    val title: String? = null,
    @SerialName("content")
    val content: String? = null,
    val contentHtml: String? = null,
    val textContent: String? = null,
    val length: Int? = null,
    val excerpt: String? = null,
    val byline: String? = null,
    val dir: String? = null,
    val siteName: String? = null,
    val lang: String? = null,
    val publishedTime: String? = null,
)
