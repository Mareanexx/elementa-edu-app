package ru.mareanexx.reader_impl.presentation.engine

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.google.gson.Gson
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_impl.presentation.model.ReaderIntent

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReaderWebContent(document: ReaderDocument, settings: ReaderSettings, annotations: List<ReaderAnnotation>, controller: ReaderController, position: ReaderPosition?, ready: Boolean, accept: (ReaderIntent) -> Unit, modifier: Modifier = Modifier) {
    val callback by rememberUpdatedState(accept)
    val liveSettings by rememberUpdatedState(settings)
    val liveAnnotations by rememberUpdatedState(annotations)
    val gson = remember { Gson() }
    val initialPosition = remember(document.book.id, document.version) { position ?: document.position }
    AndroidView(modifier = modifier, factory = { context ->
        ReaderSelectionWebView(android.view.ContextThemeWrapper(context, ru.mareanexx.reader_impl.R.style.ReaderSelectionTheme)).apply {
            setBackgroundColor(Color.TRANSPARENT)
            this.settings.apply {
                javaScriptEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
                domStorageEnabled = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                cacheMode = WebSettings.LOAD_NO_CACHE
                textZoom = 100
                displayZoomControls = false
            }
            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
            overScrollMode = android.view.View.OVER_SCROLL_NEVER
            val loader = WebViewAssetLoader.Builder().addPathHandler("/", ReaderAssetHandler(context.applicationContext, document)).build()
            addJavascriptInterface(ReaderBridge(document.version) { callback(it) }, "ReaderBridge")
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                    return if (request.url.scheme == "https" && request.url.host == WebViewAssetLoader.DEFAULT_DOMAIN && request.method == "GET")
                        loader.shouldInterceptRequest(request.url) ?: denied()
                    else denied()
                }
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = true
                override fun onPageFinished(view: WebView, url: String) {
                    if (url != "https://appassets.androidplatform.net/engine/index.html") return
                    val config = mapOf("settings" to liveSettings, "annotations" to liveAnnotations.filter { it.anchor.version == document.version }, "noteLabel" to context.getString(ru.mareanexx.reader_impl.R.string.reader_note_marker), "position" to initialPosition?.takeIf { it.version == document.version && it.progression < 1.0 }, "progression" to if (initialPosition?.progression == 1.0 || document.book.pagesRead == document.book.pages) 0.0 else document.book.pagesRead.toDouble() / document.book.pages)
                    view.evaluateJavascript("window.ReaderStart(${gson.toJson(config)})", null)
                }
                override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean { callback(ReaderIntent.RenderFailed); view.destroy(); return true }
            }
            controller.view = this
            loadUrl("https://appassets.androidplatform.net/engine/index.html")
        }
    }, update = { view ->
        if (ready) {
            // Один WebView живёт всю сессию: настройки не пересоздают документ.
            val settingsJson = gson.toJson(settings)
            if (view.appliedSettings != settingsJson) { view.appliedSettings = settingsJson; view.evaluateJavascript("window.Reader?.settings($settingsJson)", null) }
            val annotationJson = gson.toJson(annotations.filter { it.anchor.version == document.version })
            if (view.appliedAnnotations != annotationJson) { view.appliedAnnotations = annotationJson; view.evaluateJavascript("window.Reader?.annotations($annotationJson)", null) }
        }
        view.keepScreenOn = settings.keepScreenOn
    }, onRelease = { view ->
        controller.view = null
        view.removeJavascriptInterface("ReaderBridge")
        view.stopLoading()
        view.destroy()
    })
}
private fun denied() = WebResourceResponse("text/plain", "UTF-8", 403, "Forbidden", emptyMap(), java.io.ByteArrayInputStream(ByteArray(0)))
