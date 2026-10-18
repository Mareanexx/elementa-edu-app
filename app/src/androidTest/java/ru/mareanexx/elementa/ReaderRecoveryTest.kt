package ru.mareanexx.elementa

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.*
import ru.mareanexx.auth_api.domain.model.AuthResult
import ru.mareanexx.reader_api.domain.model.*

/** Две отдельные команды instrumentation с остановкой процесса между ними. */
@org.junit.FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
class ReaderRecoveryTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val container get() = (context.applicationContext as ElementaApplication).container
    private val controls get() = context.getSharedPreferences("reader_mock_controls", Context.MODE_PRIVATE)
    @Test fun enqueueBeforeProcessRestart() = runBlocking<Unit> {
        container.authRepository.clearSession()
        rule.waitUntil(15000) { rule.onAllNodesWithText("Зарегистрироваться").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(AuthResult.Success, container.authRepository.register("Проверка загрузки", "qa_recovery_${System.nanoTime()}", "Elementa123"))
        val user = container.authRepository.session.first()!!.user
        val repository = container.readerRepository(user)
        repository.saveSettings(ReaderSettings(theme = ReaderTheme.Paper, fontSize = 21f))
        controls.edit().putLong("download_delay_ms", 10000).commit()
        repository.requestDownload("book_6")
        withTimeout(15000) { repository.transfers.first { values -> values.any { it.bookId == "book_6" && it.phase == TransferPhase.Downloading } } }
        // Сессия и очередь намеренно остаются для следующего запуска процесса.
    }
    @Test fun verifiesDownloadAfterProcessRestart() = runBlocking<Unit> {
        val user = requireNotNull(container.authRepository.session.first()).user
        assertTrue(user.username.startsWith("qa_recovery_"))
        val repository = container.readerRepository(user)
        try {
            withTimeout(35000) { repository.transfers.first { values -> values.any { it.bookId == "book_6" && it.phase == TransferPhase.Ready } } }
            assertEquals(21f, repository.settings.first().fontSize)
            assertEquals(ReaderTheme.Paper, repository.settings.first().theme)
            val document = repository.openBook("book_6") as ReaderResult.Success
            assertEquals(4, document.value.chapters.size)
            assertTrue((repository.downloadedBooks() as ReaderResult.Success).value.any { it.book.id == "book_6" })
            repository.releaseBook("book_6")
        } finally {
            controls.edit().clear().commit()
            container.authRepository.clearSession()
        }
    }
    @Test fun zzCleanupDemoBackgroundWork() {
        val preferences = context.getSharedPreferences("auth_demo_accounts", Context.MODE_PRIVATE)
        val records = preferences.all.filterKeys { it.matches(Regex("qa_(reader|recovery)_[0-9]+")) }
        records.forEach { (name, payload) ->
            val id = runCatching { com.google.gson.JsonParser.parseString(payload as String).asJsonObject["id"].asString }.getOrNull()
            if (id != null) {
                ru.mareanexx.reader_impl.data.ReaderBackgroundTasks.cancelForUser(context, id)
                androidx.work.WorkManager.getInstance(context).cancelAllWorkByTag("reader_user_$id").result.get(5, java.util.concurrent.TimeUnit.SECONDS)
            }
            preferences.edit().remove(name).commit()
        }
    }
}
