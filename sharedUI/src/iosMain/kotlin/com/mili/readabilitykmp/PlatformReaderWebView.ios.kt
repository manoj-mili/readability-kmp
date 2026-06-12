package com.mili.readabilitykmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.mili.readability.core.defaultReadabilityScriptProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
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

    fun applyReadability(webView: WKWebView) {
        currentOnStatusChanged("Applying readability mode")
        webView.evaluateJavaScript(extractionScript) { result, error ->
            val extracted = result as? String
            when {
                error != null -> {
                    currentOnStatusChanged("Readability failed")
                }
                extracted == "UNAVAILABLE" -> {
                    currentOnStatusChanged("Readability unavailable for this page")
                }
                extracted?.startsWith("HTML:") == true -> {
                    isShowingReaderHtml = true
                    webView.loadHTMLString(
                        string = extracted.removePrefix("HTML:"),
                        baseURL = NSURL.URLWithString(url),
                    )
                    currentOnStatusChanged("Readability mode applied")
                }
                else -> {
                    currentOnStatusChanged("Readability failed")
                }
            }
        }
    }

    val navigationDelegate = remember {
        ReaderNavigationDelegate(
            isReadabilityMode = { currentReadabilityMode },
            isShowingReaderHtml = { isShowingReaderHtml },
            onStatusChanged = { currentOnStatusChanged(it) },
            applyReadability = { applyReadability(it) },
        )
    }

    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(
                frame = CGRectZero.readValue(),
                configuration = WKWebViewConfiguration(),
            ).apply {
                setNavigationDelegate(navigationDelegate)
            }
        },
        update = { webView ->
            val needsReload = lastLoadedUrl != url || (lastReadabilityMode && !readabilityMode)
            when {
                needsReload -> {
                    lastLoadedUrl = url
                    isShowingReaderHtml = false
                    currentOnStatusChanged("Loading page")
                    NSURL.URLWithString(url)?.let { nsUrl ->
                        webView.loadRequest(
                            NSURLRequest.requestWithURL(
                                URL = nsUrl,
                                cachePolicy = NSURLRequestReloadIgnoringLocalCacheData,
                                timeoutInterval = 30.0,
                            ),
                        )
                    }
                }
                !lastReadabilityMode && readabilityMode && !isShowingReaderHtml -> applyReadability(webView)
            }
            lastReadabilityMode = readabilityMode
        },
    )
}

private class ReaderNavigationDelegate(
    private val isReadabilityMode: () -> Boolean,
    private val isShowingReaderHtml: () -> Boolean,
    private val onStatusChanged: (String) -> Unit,
    private val applyReadability: (WKWebView) -> Unit,
) : NSObject(), WKNavigationDelegateProtocol {
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
        onStatusChanged("Loading page")
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        if (isShowingReaderHtml()) {
            onStatusChanged("Readability mode applied")
        } else {
            onStatusChanged("Page loaded")
        }
        if (isReadabilityMode() && !isShowingReaderHtml()) {
            applyReadability(webView)
        }
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailNavigation: WKNavigation?,
        withError: platform.Foundation.NSError,
    ) {
        onStatusChanged("Failed to load page")
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: platform.Foundation.NSError,
    ) {
        onStatusChanged("Failed to load page")
    }
}
