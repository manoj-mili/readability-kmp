package com.mili.readabilitykmp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mili.readability.core.defaultReadabilityScriptProvider
import org.json.JSONArray

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformReaderWebView(
    url: String,
    readabilityMode: Boolean,
    modifier: Modifier,
    onStatusChanged: (String) -> Unit,
) {
    val currentReadabilityMode by rememberUpdatedState(readabilityMode)
    val currentOnStatusChanged by rememberUpdatedState(onStatusChanged)
    val extractionScript = remember {
        readabilityExtractionScript(defaultReadabilityScriptProvider().getReadabilityScript())
    }
    var lastLoadedUrl by remember { mutableStateOf<String?>(null) }
    var lastReadabilityMode by remember { mutableStateOf(false) }
    var isShowingReaderHtml by remember { mutableStateOf(false) }

    fun applyReadability(webView: WebView) {
        currentOnStatusChanged("Applying readability mode")
        webView.evaluateJavascript(extractionScript) { result ->
            val extracted = result.decodeJavaScriptString()
            when {
                extracted == "UNAVAILABLE" -> {
                    currentOnStatusChanged("Readability unavailable for this page")
                }
                extracted?.startsWith("HTML:") == true -> {
                    isShowingReaderHtml = true
                    webView.loadDataWithBaseURL(
                        url,
                        extracted.removePrefix("HTML:"),
                        "text/html",
                        "UTF-8",
                        url,
                    )
                    currentOnStatusChanged("Readability mode applied")
                }
                else -> {
                    currentOnStatusChanged("Readability failed")
                }
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        currentOnStatusChanged("Loading page")
                    }

                    override fun onPageFinished(view: WebView, url: String?) {
                        if (isShowingReaderHtml) {
                            currentOnStatusChanged("Readability mode applied")
                        } else {
                            currentOnStatusChanged("Page loaded")
                        }
                        if (currentReadabilityMode && !isShowingReaderHtml) {
                            applyReadability(view)
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            currentOnStatusChanged("Failed to load page")
                        }
                    }
                }
            }
        },
        update = { webView ->
            val needsReload = lastLoadedUrl != url || (lastReadabilityMode && !readabilityMode)
            when {
                needsReload -> {
                    lastLoadedUrl = url
                    isShowingReaderHtml = false
                    currentOnStatusChanged("Loading page")
                    webView.loadUrl(url)
                }
                !lastReadabilityMode && readabilityMode && !isShowingReaderHtml -> applyReadability(webView)
            }
            lastReadabilityMode = readabilityMode
        },
    )
}

private fun String?.decodeJavaScriptString(): String? {
    if (this == null || this == "null") return null
    return runCatching { JSONArray("[$this]").getString(0) }.getOrNull()
}
