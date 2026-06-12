package com.mili.readability.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class ReaderModeStateJsonParserTest {
    private val parser = ReaderModeStateJsonParser()

    @Test
    fun parseReturnsAvailableForReadableArticleJson() {
        val state = parser.parse(
            """
            {
              "title": "Example Article",
              "byline": "Author Name",
              "dir": "ltr",
              "lang": "en",
              "content": "<div><p>Readable article HTML.</p></div>",
              "textContent": "Readable article text.",
              "length": 1234,
              "excerpt": "Readable article text.",
              "siteName": "Example",
              "publishedTime": "2026-06-12T00:00:00Z",
              "unknownField": "ignored"
            }
            """.trimIndent(),
        )

        val available = assertIs<ReaderModeState.Available>(state)
        assertEquals("Example Article", available.article.title)
        assertEquals("<div><p>Readable article HTML.</p></div>", available.article.contentHtml)
        assertEquals("Readable article text.", available.article.textContent)
        assertEquals(1234, available.article.length)
        assertEquals("Readable article text.", available.article.excerpt)
        assertEquals("Author Name", available.article.byline)
        assertEquals("ltr", available.article.dir)
        assertEquals("Example", available.article.siteName)
        assertEquals("en", available.article.lang)
        assertEquals("2026-06-12T00:00:00Z", available.article.publishedTime)
    }

    @Test
    fun parseReturnsUnavailableForNullBlankOrMissingContent() {
        assertEquals(
            ReaderModeState.Unavailable("No readable article found"),
            parser.parse("null"),
        )
        assertEquals(
            ReaderModeState.Unavailable("No readable article found"),
            parser.parse("undefined"),
        )
        assertEquals(
            ReaderModeState.Unavailable("No readable article found"),
            parser.parse("   "),
        )
        assertEquals(
            ReaderModeState.Unavailable("No readable article found"),
            parser.parse("""{"title":"No body"}"""),
        )
        assertEquals(
            ReaderModeState.Unavailable("No readable article found"),
            parser.parse("""{"content":"   "}"""),
        )
    }

    @Test
    fun parseAcceptsContentHtmlAlias() {
        val state = parser.parse(
            """
            {
              "title": "SDK Article",
              "contentHtml": "<article>SDK HTML</article>",
              "textContent": "SDK text"
            }
            """.trimIndent(),
        )

        val available = assertIs<ReaderModeState.Available>(state)
        assertEquals("<article>SDK HTML</article>", available.article.contentHtml)
        assertEquals("SDK text", available.article.textContent)
    }

    @Test
    fun parsePrefersReadabilityContentOverContentHtmlAlias() {
        val state = parser.parse(
            """
            {
              "content": "<article>Readability HTML</article>",
              "contentHtml": "<article>SDK HTML</article>"
            }
            """.trimIndent(),
        )

        val available = assertIs<ReaderModeState.Available>(state)
        assertEquals("<article>Readability HTML</article>", available.article.contentHtml)
    }

    @Test
    fun parseReturnsFailedForMalformedJson() {
        val state = parser.parse("""{"title":""")

        val failed = assertIs<ReaderModeState.Failed>(state)
        assertEquals("Failed to parse Readability JSON", failed.message)
        assertNotNull(failed.cause)
    }

    @Test
    fun parseReturnsFailedForInvalidTopLevelShape() {
        val state = parser.parse("[]")

        val failed = assertIs<ReaderModeState.Failed>(state)
        assertEquals("Failed to parse Readability JSON", failed.message)
        assertNotNull(failed.cause)
    }

    @Test
    fun parseFallsBackWhenOptionalFieldsAreMissing() {
        val state = ReaderModeStateJsonParser().parse(
            """
            {
              "content": "<p>Only content</p>",
              "textContent": "Only content"
            }
            """.trimIndent(),
        )

        val available = assertIs<ReaderModeState.Available>(state)
        assertEquals("", available.article.title)
        assertEquals("<p>Only content</p>", available.article.contentHtml)
        assertEquals("Only content", available.article.textContent)
        assertEquals("Only content".length, available.article.length)
        assertEquals(null, available.article.excerpt)
        assertEquals(null, available.article.byline)
    }
}
