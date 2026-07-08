package com.fabsimple.app.components

import android.app.Activity
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fabsimple.app.AppContext

/**
 * Android implementation using a hidden WebView and system PrintManager.
 */
actual fun printHtml(html: String, jobName: String) {
    val context = AppContext.context ?: return
    if (context is Activity) {
        context.runOnUiThread {
            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = context.getSystemService(Activity.PRINT_SERVICE) as PrintManager
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    val builder = PrintAttributes.Builder()
                    printManager.print(jobName, printAdapter, builder.build())
                }
            }
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }
    }
}
