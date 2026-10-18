package ru.mareanexx.reader_impl.presentation.engine

import android.content.Context
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import ru.mareanexx.reader_api.domain.model.ReaderDocument
import ru.mareanexx.reader_impl.data.epub.EpubPaths
import java.io.ByteArrayInputStream
import java.io.File
import ru.mareanexx.common_ui.R as CommonR

internal class ReaderAssetHandler(
    private val context: Context,
    private val document: ReaderDocument,
) : WebViewAssetLoader.PathHandler {
    override fun handle(path: String): WebResourceResponse = runCatching {
        when (path) {
            "engine/index.html" -> {
                val body = File(document.directory, "content.html").readText()
                val html = """<!doctype html><html lang="ru"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1"><meta http-equiv="Content-Security-Policy" content="default-src 'none'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self'; font-src 'self'; connect-src 'none'; form-action 'none'; base-uri 'none';"><link rel="stylesheet" href="/engine/engine.css"></head><body><div id="viewport"><main id="content">$body</main></div><script src="/engine/engine.js"></script></body></html>"""
                response("text/html", html.byteInputStream())
            }
            "engine/font-italic.ttf" -> response("font/ttf", font(CommonR.font.nunito_italic_variable_font))
            "engine/font.ttf" -> response("font/ttf", font(CommonR.font.nunito_variable_font))
            "engine/engine.js" -> response("text/javascript", context.assets.open("reader/engine.js"))
            "engine/engine.css" -> response("text/css", context.assets.open("reader/engine.css"))
            "engine/note-marker.svg" -> response("image/svg+xml", context.assets.open("reader/note-marker.svg"))
            else -> {
                require(path.startsWith("publication/"))
                val relative = path.removePrefix("publication/")
                val mime = when (relative.substringAfterLast('.').lowercase()) { "png" -> "image/png"; "jpg", "jpeg" -> "image/jpeg"; "gif" -> "image/gif"; "webp" -> "image/webp"; else -> error("Недопустимый ресурс") }
                response(mime, EpubPaths.file(File(document.directory, "resources"), relative).inputStream())
            }
        }
    }.getOrElse { WebResourceResponse("text/plain", "UTF-8", 404, "Not found", emptyMap(), ByteArrayInputStream(ByteArray(0))) }
    // openRawResource читает бинарный файл шрифта; аннотация Android SDK ограничена типом raw.
    @android.annotation.SuppressLint("ResourceType")
    private fun font(@androidx.annotation.FontRes id: Int) = context.resources.openRawResource(id)
    private fun response(mime: String, input: java.io.InputStream) = WebResourceResponse(mime, "UTF-8", 200, "OK", mapOf("Cache-Control" to "no-store", "X-Content-Type-Options" to "nosniff"), input)
}
