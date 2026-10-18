package ru.mareanexx.elementa

import android.content.*
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.*
import android.webkit.WebView
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.*
import ru.mareanexx.auth_api.domain.model.*
import ru.mareanexx.reader_api.domain.model.*
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Реальные WorkManager, EPUB, WebView и локальные заметки на отдельном демо-аккаунте. */
class ReaderFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val container get() = (context.applicationContext as ElementaApplication).container
    private val controls get() = context.getSharedPreferences("reader_mock_controls", Context.MODE_PRIVATE)
    private lateinit var user: AuthUser
    private lateinit var account: String
    private val repository get() = container.readerRepository(user)
    private fun waitTag(tag: String) { rule.waitUntil(30000) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() } }
    private fun button(text: String) = rule.onNode(hasText(text) and hasClickAction())
    private fun open(id: String = "book_1") {
        waitTag("home_continue_pager")
        if (id == "book_2") rule.onNodeWithTag("home_continue_pager").performTouchInput { swipeLeft() }
        waitTag("home_read_$id")
        rule.onNodeWithTag("home_read_$id").performClick()
        waitTag("reader_ready")
        instrumentation.uiAutomation.rootInActiveWindow?.findAccessibilityNodeInfosByText("Got it")?.firstOrNull()?.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
        rule.waitUntil(30000) { js("typeof Reader==='object' && !!document.querySelector('section')").asBoolean }
        SystemClock.sleep(500)
    }
    private fun capture(name: String) {
        rule.waitForIdle(); SystemClock.sleep(300)
        val directory = File(context.filesDir, "qa").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
        File(directory, "$name.txt").writeText(rule.onAllNodes(isRoot()).onLast().printToString())
    }
    private fun web(): WebView {
        fun descend(view: View): WebView? = if (view is WebView) view else (view as? ViewGroup)?.let { group -> (0 until group.childCount).firstNotNullOfOrNull { descend(group.getChildAt(it)) } }
        var found: WebView? = null
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) found = descend(rule.activity.window.decorView)
        else instrumentation.runOnMainSync { found = descend(rule.activity.window.decorView) }
        return requireNotNull(found)
    }
    private fun js(script: String): JsonElement {
        val latch = CountDownLatch(1)
        var result = "null"
        instrumentation.runOnMainSync { web().evaluateJavascript(script) { result = it; latch.countDown() } }
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        return JsonParser.parseString(result)
    }
    private fun select(action: String) {
        js("""(()=>{const n=document.querySelectorAll('p')[1].firstChild;const r=document.createRange();r.setStart(n,0);r.setEnd(n,Math.min(n.length,80));const s=getSelection();s.removeAllRanges();s.addRange(r);Reader.selection('$action');return true;})()""")
    }
    @Before fun setup() {
        controls.edit().clear().commit()
        runBlocking { container.authRepository.clearSession() }
        rule.waitUntil(15000) { rule.onAllNodesWithText("Зарегистрироваться").fetchSemanticsNodes().isNotEmpty() }
        account = "qa_reader_${System.nanoTime()}"
        assertEquals(AuthResult.Success, runBlocking { container.authRepository.register("Исследователь", account, "Elementa123") })
        user = runBlocking { container.authRepository.session.first()!!.user }
        waitTag("home_screen")
    }
    @After fun cleanup() {
        runCatching { capture("reader-cleanup") }
        controls.edit().clear().commit()
        if (::user.isInitialized) runBlocking { container.authRepository.clearSession() }
    }
    @Test fun themesPaginationSelectionNotesAndProgressSurviveReopening() {
        open()
        assertTrue(js("document.querySelectorAll('math').length").asInt >= 2)
        assertTrue(js("document.body.innerText.length").asInt > 6000)
        capture("reader-light")
        instrumentation.uiAutomation.performGlobalAction(1)
        SystemClock.sleep(300)
        rule.waitForIdle()
        rule.onNodeWithTag("reader_screen").assertIsDisplayed()
        rule.onNodeWithTag("reader_settings").performClick()
        waitTag("reader_menu_Settings")
        rule.onNodeWithTag("reader_menu_Settings").performClick()
        waitTag("reader_settings_panel")
        rule.onNodeWithTag("reader_theme_Night").performClick()
        rule.onNodeWithTag("reader_settings_panel").assertIsDisplayed()
        rule.onNodeWithTag("reader_font_larger").performClick()
        rule.onNode(hasContentDescription("Межстрочный интервал") and SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))
            .performScrollTo().performSemanticsAction(SemanticsActions.SetProgress) { it(1.7f) }
        rule.waitUntil(5000) { js("parseFloat(getComputedStyle(document.getElementById('content')).lineHeight)>30").asBoolean }
        rule.onNodeWithTag("reader_mode_Pages").performScrollTo().performClick()
        capture("reader-settings-night")
        rule.onNodeWithContentDescription("Закрыть настройки").performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithTag("reader_settings_panel").fetchSemanticsNodes().isEmpty() }
        assertEquals("rgb(29, 29, 32)", js("getComputedStyle(document.documentElement).backgroundColor").asString)
        rule.onNodeWithTag("reader_page_next").performClick()
        capture("reader-night-pages")
        rule.onNodeWithTag("reader_settings").performClick()
        waitTag("reader_menu_Settings")
        rule.onNodeWithTag("reader_menu_Settings").performClick()
        waitTag("reader_settings_panel")
        rule.onNodeWithTag("reader_mode_Scroll").performClick()
        rule.onNodeWithContentDescription("Закрыть настройки").performClick()
        js("Reader.jump(0);true")
        select("highlight")
        waitTag("reader_annotation_menu")
        rule.onNodeWithTag("reader_color_Mint").performClick()
        rule.waitUntil(10000) { runBlocking { repository.annotations("book_1").first().isNotEmpty() } }
        assertEquals(HighlightColor.Mint, runBlocking { repository.annotations("book_1").first().single().color })
        select("note")
        waitTag("reader_note_editor")
        rule.onNodeWithTag("reader_note_input").performTextInput("Проверить единицы и повторить определение атома.")
        capture("reader-note")
        rule.onNodeWithTag("reader_note_save").performClick()
        rule.waitUntil(10000) { runBlocking { repository.annotations("book_1").first().any { it.note != null } } }
        rule.onNodeWithTag("reader_settings").performClick()
        waitTag("reader_menu_Notes")
        rule.onNodeWithTag("reader_menu_Notes").performClick()
        waitTag("reader_inventory")
        rule.onNodeWithText("Проверить единицы и повторить определение атома.").assertIsDisplayed()
        capture("reader-notes")
        rule.onNodeWithContentDescription("Назад к чтению").performClick()
        js("Reader.chapter('chapter_3');true")
        SystemClock.sleep(700)
        rule.onNodeWithTag("reader_close").performClick()
        waitTag("home_screen")
        val saved = runBlocking { repository.openBook("book_1") } as ReaderResult.Success
        assertEquals("chapter_3", saved.value.position!!.chapterId)
        repository.releaseBook("book_1")
        open()
        assertEquals("18px", js("getComputedStyle(document.getElementById('content')).fontSize").asString)
        assertEquals("rgb(225, 223, 232)", js("getComputedStyle(document.body).color").asString)
        assertTrue(js("CSS.highlights.get('Mint').size").asInt > 0)
        capture("reader-restored")
    }
    @Test fun compactChromeStaysCenteredAndMenuOpensEachDestination() {
        open()
        val screen = rule.onNodeWithTag("reader_screen").getUnclippedBoundsInRoot()
        val heading = rule.onNodeWithTag("reader_heading").getUnclippedBoundsInRoot()
        val close = rule.onNodeWithTag("reader_close").getUnclippedBoundsInRoot()
        val page = rule.onNodeWithTag("reader_page_count").getUnclippedBoundsInRoot()
        assertEquals((close.bottom - close.top).value, (heading.bottom - heading.top).value, .5f)
        assertEquals((screen.left.value + screen.right.value) / 2f, (heading.left.value + heading.right.value) / 2f, .5f)
        assertEquals((screen.left.value + screen.right.value) / 2f, (page.left.value + page.right.value) / 2f, .5f)
        assertEquals(30f, (page.bottom - page.top).value, .5f)
        rule.onNodeWithTag("reader_text").performTouchInput { click(center) }
        rule.waitUntil(5000) { rule.onAllNodesWithTag("reader_heading").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithTag("reader_text").performTouchInput { click(center) }
        waitTag("reader_heading")
        rule.onNodeWithTag("reader_settings").performClick()
        waitTag("reader_menu_Chapters")
        capture("reader-bubble-menu")
        rule.onNodeWithTag("reader_menu_Chapters").performClick()
        waitTag("reader_inventory")
        rule.onNodeWithText("Оглавление").assertIsDisplayed()
        rule.onNodeWithContentDescription("Назад к чтению").performClick()
        rule.onNodeWithTag("reader_settings").performClick()
        waitTag("reader_menu_Notes")
        rule.onNodeWithTag("reader_menu_Notes").performClick()
        waitTag("reader_inventory")
        rule.onNodeWithText("Заметки и выделения").assertIsDisplayed()
        rule.onNodeWithContentDescription("Назад к чтению").performClick()
        rule.onNodeWithTag("reader_settings").performClick()
        waitTag("reader_menu_Settings")
        rule.onNodeWithTag("reader_menu_Settings").performClick()
        waitTag("reader_settings_panel")
        rule.onNodeWithTag("reader_mode_Pages").performClick()
        rule.onNodeWithContentDescription("Закрыть настройки").performClick()
        SystemClock.sleep(500)
        assertTrue(js("document.getElementById('viewport').clientHeight > 400").asBoolean)
        assertTrue(js("document.getElementById('content').clientHeight > 350").asBoolean)
        assertTrue(js("document.getElementById('viewport').scrollWidth / innerWidth < 30").asBoolean)
        assertTrue(js("Array.from(document.querySelectorAll('p')).some(p=>Array.from(p.getClientRects()).some(r=>r.width>0&&r.height>0&&r.right>20&&r.left<innerWidth-20&&r.bottom>24&&r.top<innerHeight-24))").asBoolean)
        val paged = rule.onNodeWithTag("reader_page_count").getUnclippedBoundsInRoot()
        assertEquals((screen.left.value + screen.right.value) / 2f, (paged.left.value + paged.right.value) / 2f, .5f)
        val next = rule.onNodeWithTag("reader_page_next").getUnclippedBoundsInRoot()
        val menu = rule.onNodeWithTag("reader_settings").getUnclippedBoundsInRoot()
        assertTrue(next.right < menu.left)
        capture("reader-compact-paged")
    }
    @Test fun noteMarkerAndCompletionKeepTheBookOpen() {
        open()
        js("Reader.jump(0);true")
        select("note")
        waitTag("reader_note_editor")
        rule.onNodeWithTag("reader_note_input").performTextInput("Вернуться к этому определению.")
        rule.onNodeWithTag("reader_note_save").performClick()
        rule.waitUntil(10000) { js("document.querySelectorAll('.reader-note-marker').length===1").asBoolean }
        assertFalse(js("CSS.highlights.has('notes')").asBoolean)
        assertTrue(js("CSS.highlights.get('Amber').size>0").asBoolean)
        assertTrue(js("document.getElementById('content').textContent.length>6000").asBoolean)
        capture("reader-note-marker")
        val point = js("""(()=>{const b=document.querySelector('.reader-note-marker').getBoundingClientRect();return {x:b.left+b.width/2,y:b.top+b.height/2,w:innerWidth}})()""").asJsonObject
        val native = web()
        val location = IntArray(2)
        instrumentation.runOnMainSync { native.getLocationOnScreen(location) }
        val scale = native.width / point["w"].asFloat
        val x = location[0] + point["x"].asFloat * scale
        val y = location[1] + point["y"].asFloat * scale
        val at = SystemClock.uptimeMillis()
        instrumentation.uiAutomation.injectInputEvent(MotionEvent.obtain(at, at, MotionEvent.ACTION_DOWN, x, y, 0), true)
        instrumentation.uiAutomation.injectInputEvent(MotionEvent.obtain(at, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0), true)
        waitTag("reader_annotation_menu")
        button("Редактировать заметку").assertIsDisplayed()
        instrumentation.uiAutomation.performGlobalAction(1)
        rule.waitUntil(5000) { rule.onAllNodesWithTag("reader_annotation_menu").fetchSemanticsNodes().isEmpty() }
        js("scrollTo(0,document.documentElement.scrollHeight);true")
        waitTag("reader_completion")
        rule.onNodeWithText("Последняя страница").assertIsDisplayed()
        capture("reader-completion-prompt")
        val pending = (runBlocking { repository.openBook("book_1") } as ReaderResult.Success).value
        assertTrue(pending.position!!.sourcePagesRead < pending.book.pages)
        repository.releaseBook("book_1")
        rule.onNodeWithTag("reader_complete_book").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("Книга прочитана").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("reader_ready").assertExists()
        capture("reader-completion-saved")
        val complete = (runBlocking { repository.openBook("book_1") } as ReaderResult.Success).value
        assertEquals(complete.book.pages, complete.position!!.sourcePagesRead)
        repository.releaseBook("book_1")
        rule.onNodeWithTag("reader_complete_book").performClick()
        js("Reader.jump(0);true")
        SystemClock.sleep(800)
        val reviewing = (runBlocking { repository.openBook("book_1") } as ReaderResult.Success).value
        assertEquals(reviewing.book.pages, reviewing.position!!.sourcePagesRead)
        repository.releaseBook("book_1")
        rule.onNodeWithTag("reader_ready").assertExists()
    }
    @Test fun detailsDownloadMenuAndReaderFillTheWholeWindow() {
        runBlocking {
            repository.requestDownload("book_1")
            withTimeout(30000) { repository.transfers.first { rows -> rows.any { it.bookId == "book_1" && it.phase == TransferPhase.Ready } } }
        }
        rule.onNode(hasText("Органическая химия") and hasClickAction() and hasAnyAncestor(hasTestTag("home_continue_book_1"))).performClick()
        waitTag("book_screen")
        waitTag("book_download")
        val download = rule.onNodeWithTag("book_download").getUnclippedBoundsInRoot()
        val favorite = rule.onNodeWithTag("book_favorite").getUnclippedBoundsInRoot()
        assertTrue((favorite.left - download.right).value >= 7.5f)
        rule.onNodeWithTag("book_download").performClick()
        waitTag("book_download_menu")
        rule.onNodeWithText("Удалить скачанный файл").assertIsDisplayed()
        rule.onNodeWithText("Прогресс чтения и заметки сохранятся.").assertIsDisplayed()
        capture("reader-download-menu")
        instrumentation.uiAutomation.performGlobalAction(1)
        rule.waitUntil(5000) { rule.onAllNodesWithTag("book_download_menu").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithTag("book_content").performScrollToNode(hasTestTag("book_read"))
        rule.onNodeWithTag("book_read").performClick()
        waitTag("reader_ready")
        val bounds = rule.onNodeWithTag("reader_screen").getUnclippedBoundsInRoot()
        assertEquals(0f, bounds.top.value, .5f)
        capture("reader-from-details-fullscreen")
        rule.onNodeWithTag("reader_close").performClick()
        waitTag("book_screen")
    }
    @Test fun downloadMakesOfflineLibraryAndNoManifestIsNeededToOpenAgain() {
        assertEquals(ReaderResult.Success(Unit), runBlocking { repository.requestDownload("book_2") })
        runBlocking { withTimeout(30000) { repository.transfers.first { rows -> rows.any { it.bookId == "book_2" && it.phase == TransferPhase.Ready } } } }
        assertEquals(listOf("book_2"), (runBlocking { repository.downloadedBooks() } as ReaderResult.Success).value.map { it.book.id })
        val books = container.bookRepository(user)
        assertTrue(runBlocking { books.setFavorite("book_2", true) } is ru.mareanexx.book_api.domain.model.BookResult.Success)
        assertTrue((runBlocking { repository.downloadedBooks() } as ReaderResult.Success).value.single().book.shelves.favorite)
        controls.edit().putInt("next_status", 503).putLong("download_delay_ms", 10000).commit()
        val beforeOpen = SystemClock.elapsedRealtime()
        open("book_2")
        assertTrue(SystemClock.elapsedRealtime() - beforeOpen < 5000)
        assertTrue(js("document.querySelector('section')!==null").asBoolean)
        rule.onNodeWithTag("reader_close").performClick()
        waitTag("home_screen")
        button("Библиотека").performClick()
        waitTag("library_screen")
        rule.onNodeWithTag("library_tabs").performScrollToNode(hasTestTag("library_tab_Downloaded"))
        rule.onNodeWithTag("library_tab_Downloaded").performClick()
        waitTag("library_tile_book_2")
        capture("reader-downloaded-library")
        val library = container.libraryRepository(user)
        assertTrue(runBlocking { library.setReadingStatus("book_2", ru.mareanexx.user_library_api.domain.model.ReadingStatus.Completed) } is ru.mareanexx.user_library_api.domain.model.LibraryResult.Success)
        val completed = (runBlocking { repository.downloadedBooks() } as ReaderResult.Success).value.single()
        assertEquals(completed.book.pages, completed.pagesRead)
        rule.onNodeWithTag("library_read_book_2").performClick()
        waitTag("reader_ready")
        SystemClock.sleep(400)
        assertTrue(js("scrollY<5").asBoolean)
        rule.onNodeWithTag("reader_close").performClick()
        waitTag("library_screen")
        assertEquals(ReaderResult.Success(Unit), runBlocking { repository.removeDownload("book_2") })
        assertTrue((runBlocking { repository.downloadedBooks() } as ReaderResult.Success).value.isEmpty())
    }
    @Test fun cancellationDoesNotOverwriteImmediateRetry() {
        controls.edit().putLong("download_delay_ms", 3000).commit()
        runBlocking {
            repository.requestDownload("book_3")
            withTimeout(15000) { repository.transfers.first { values -> values.any { it.bookId == "book_3" && it.phase == TransferPhase.Downloading } } }
            repository.cancelDownload("book_3")
            repository.requestDownload("book_3")
            withTimeout(30000) { repository.transfers.first { values -> values.any { it.bookId == "book_3" && it.phase == TransferPhase.Ready } } }
            assertTrue((repository.downloadedBooks() as ReaderResult.Success).value.any { it.book.id == "book_3" })
        }
    }
    @Test fun nativeSelectionHasCompactCustomActions() {
        open()
        js("Reader.jump(0);true")
        // Прячем панели, чтобы они не перекрывали выделяемый абзац.
        rule.onNodeWithTag("reader_text").performTouchInput { click(center) }
        SystemClock.sleep(500)
        val position = js("""(()=>{const n=document.querySelectorAll('p')[1].firstChild;const r=document.createRange();r.setStart(n,2);r.setEnd(n,5);const b=r.getBoundingClientRect();return {x:b.left+b.width/2,y:b.top+b.height/2,w:innerWidth};})()""").asJsonObject
        val native = web()
        val location = IntArray(2)
        instrumentation.runOnMainSync { native.getLocationOnScreen(location) }
        val scale = native.width / position["w"].asFloat
        val x = location[0] + position["x"].asFloat * scale
        val y = location[1] + position["y"].asFloat * scale
        val at = SystemClock.uptimeMillis()
        instrumentation.uiAutomation.injectInputEvent(MotionEvent.obtain(at, at, MotionEvent.ACTION_DOWN, x, y, 0), true)
        SystemClock.sleep(900)
        instrumentation.uiAutomation.injectInputEvent(MotionEvent.obtain(at, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0), true)
        SystemClock.sleep(700)
        capture("reader-native-selection")
        assertFalse(js("getSelection().isCollapsed").asBoolean)
        val automation = instrumentation.uiAutomation
        automation.serviceInfo = automation.serviceInfo.apply { flags = flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
        var actions = emptyList<android.view.accessibility.AccessibilityNodeInfo>()
        rule.waitUntil(5000) {
            actions = automation.windows.flatMap { window -> window.root?.findAccessibilityNodeInfosByText("Выделить").orEmpty() }
            actions.isNotEmpty()
        }
        assertTrue(actions.first().performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK))
        waitTag("reader_annotation_menu")
        rule.onNodeWithTag("reader_color_Lavender").performClick()
        rule.waitUntil(5000) { runBlocking { repository.annotations("book_1").first().isNotEmpty() } }
    }
}
