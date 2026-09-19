package com.fabsimple.app.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URLEncoder

@Composable
actual fun PdfWebView(
    url: String,
    modifier: Modifier
) {
    val embeddedUrl = if (url.endsWith(".pdf", ignoreCase = true) || url.contains(".pdf?") || !url.startsWith("http")) {
        "https://docs.google.com/viewer?url=${URLEncoder.encode(url, "UTF-8")}&embedded=true"
    } else {
        url
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()
                loadUrl(embeddedUrl)
            }
        },
        update = { webView ->
            webView.loadUrl(embeddedUrl)
        }
    )
}
