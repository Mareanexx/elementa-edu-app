package ru.mareanexx.reader_impl.presentation.engine

import android.webkit.WebView
import com.google.gson.Gson

class ReaderController {
    internal var view: WebView? = null
    private val gson = Gson()
    fun step(delta: Int) { view?.evaluateJavascript("window.Reader?.step(${delta.coerceIn(-1, 1)})", null) }
    fun jump(offset: Int) { view?.evaluateJavascript("window.Reader?.jump(${offset.coerceAtLeast(0)})", null) }
    fun chapter(id: String) { view?.evaluateJavascript("window.Reader?.chapter(${gson.toJson(id)})", null) }
    suspend fun flushPosition() = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main.immediate) {
        val current = view ?: return@withContext
        kotlinx.coroutines.suspendCancellableCoroutine<Unit> { continuation ->
            current.evaluateJavascript("window.Reader?.position()") { if (continuation.isActive) continuation.resumeWith(Result.success(Unit)) }
        }
    }
    fun position() { view?.evaluateJavascript("window.Reader?.position()", null) }
}
