package com.mili.readabilitykmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformReaderWebView(
    url: String,
    readabilityMode: Boolean,
    modifier: Modifier = Modifier,
    onStatusChanged: (String) -> Unit,
)

fun readabilityExtractionScript(readabilityScript: String): String {
    return """
        (function() {
            function escapeHtml(value) {
                return String(value || '')
                    .replace(/&/g, '&amp;')
                    .replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;')
                    .replace(/"/g, '&quot;')
                    .replace(/'/g, '&#039;');
            }

            $readabilityScript

            var article = new Readability(document.cloneNode(true)).parse();
            if (!article || !article.content) {
                return 'UNAVAILABLE';
            }

            var content = String(article.content || '').trim();
            if (!content) {
                return 'UNAVAILABLE';
            }

            var readerHtml = '<!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1" />' +
                '<style>' +
                'body{margin:0;background:#f8f5ed;color:#1f1d19;font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif;line-height:1.65;}' +
                'main{max-width:760px;margin:0 auto;padding:28px 20px 56px;}' +
                'h1{font-size:2rem;line-height:1.15;margin:0 0 12px;}' +
                '.byline{color:#6e675c;margin:0 0 24px;font-size:.95rem;}' +
                'p,li{font-size:1.05rem;}' +
                'img,video,iframe{max-width:100%;height:auto;}' +
                'table{display:block;max-width:100%;overflow-x:auto;border-collapse:collapse;}' +
                'th,td{border:1px solid #d5cdbf;padding:6px;}' +
                'pre{white-space:pre-wrap;overflow-wrap:anywhere;}' +
                '[style*="display:none"],[hidden]{display:initial!important;}' +
                'a{color:#0b5cad;}' +
                '</style></head><body><main>' +
                '<h1>' + escapeHtml(article.title || document.title || 'Readable article') + '</h1>' +
                '<p class="byline">' + escapeHtml(article.byline || article.siteName || location.hostname) + '</p>' +
                content +
                '</main></body></html>';

            return 'HTML:' + readerHtml;
        })();
    """.trimIndent()
}
