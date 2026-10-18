package ru.mareanexx.reader_impl.presentation.engine

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.google.gson.JsonParser
import ru.mareanexx.reader_api.domain.model.ReaderAnchor
import ru.mareanexx.reader_impl.presentation.model.*

internal class ReaderBridge(
    private val version: String,
    private val accept: (ReaderIntent) -> Unit,
) {
    private val main = Handler(Looper.getMainLooper())
    @JavascriptInterface fun message(json: String) {
        if (json.length > 80000) return
        val intent = runCatching {
            val message = JsonParser.parseString(json).asJsonObject
            val value = message.getAsJsonObject("value")
            when (message["type"].asString) {
                "error" -> ReaderIntent.RenderFailed
                "ready" -> ReaderIntent.Ready
                "tap" -> ReaderIntent.ToggleChrome
                "annotation" -> ReaderIntent.Annotation(value["id"].asString.take(100))
                "position" -> ReaderIntent.Position(value["offset"].asInt, value["progression"].asDouble, value["chapterId"].asString.take(200), value["chapter"].asString.take(200), value["page"].asInt, value["pages"].asInt, value["atEnd"]?.asBoolean ?: false)
                "selection" -> ReaderIntent.Selection(ReaderSelection(ReaderAnchor(version, value["chapterId"].asString.take(200), value["start"].asInt, value["end"].asInt, value["quote"].asString, value["prefix"].asString.take(64), value["suffix"].asString.take(64)), value["action"].asString))
                else -> null
            }
        }.getOrNull() ?: return
        main.post { accept(intent) }
    }
}
