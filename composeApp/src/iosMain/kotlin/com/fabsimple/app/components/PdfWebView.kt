package com.fabsimple.app.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PdfWebView(
    url: String,
    modifier: Modifier
) {
    val embeddedUrl = if (url.startsWith("http")) url else "https://pdfobject.com/pdf/sample.pdf"

    UIKitView(
        factory = {
            val webView = WKWebView()
            val nsUrl = NSURL.URLWithString(embeddedUrl)
            if (nsUrl != null) {
                val request = NSURLRequest.requestWithURL(nsUrl)
                webView.loadRequest(request)
            }
            webView
        },
        modifier = modifier,
        update = { webView ->
            val nsUrl = NSURL.URLWithString(embeddedUrl)
            if (nsUrl != null) {
                val request = NSURLRequest.requestWithURL(nsUrl)
                webView.loadRequest(request)
            }
        }
    )
}
