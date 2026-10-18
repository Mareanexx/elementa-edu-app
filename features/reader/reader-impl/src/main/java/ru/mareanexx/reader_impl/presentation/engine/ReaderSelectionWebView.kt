package ru.mareanexx.reader_impl.presentation.engine

import android.content.*
import android.graphics.Rect
import android.view.*
import android.webkit.WebView
import com.google.gson.Gson
import ru.mareanexx.reader_impl.R

/** Сохраняет системные маркеры выделения, заменяя только действия в компактном меню. */
internal class ReaderSelectionWebView(
    context: Context,
) : WebView(context) {
    var appliedSettings: String? = null
    var appliedAnnotations: String? = null
    override fun startActionMode(callback: ActionMode.Callback): ActionMode? = super.startActionMode(actions(callback))
    override fun startActionMode(callback: ActionMode.Callback, type: Int): ActionMode? = super.startActionMode(actions(callback), type)
    private fun actions(original: ActionMode.Callback) = object : ActionMode.Callback2() {
        private fun fill(menu: Menu) {
            menu.clear()
            listOf(R.string.reader_highlight, R.string.reader_note_add, R.string.reader_copy).forEachIndexed { index, text ->
                menu.add(Menu.NONE, index + 1, index, text).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        }
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean { original.onCreateActionMode(mode, menu); fill(menu); return true }
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean { fill(menu); return true }
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                1, 2 -> evaluateJavascript("window.Reader.selection('${if (item.itemId == 1) "highlight" else "note"}')") { mode.finish() }
                3 -> evaluateJavascript("window.getSelection().toString()") { value ->
                    val selected = runCatching { Gson().fromJson(value, String::class.java) }.getOrNull().orEmpty()
                    if (selected.isNotBlank()) (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(context.getString(R.string.reader_quote), selected))
                    mode.finish()
                }
                else -> return false
            }
            return true
        }
        override fun onDestroyActionMode(mode: ActionMode) { original.onDestroyActionMode(mode) }
        override fun onGetContentRect(mode: ActionMode, view: View, outRect: Rect) {
            if (original is ActionMode.Callback2) original.onGetContentRect(mode, view, outRect) else super.onGetContentRect(mode, view, outRect)
        }
    }
}
