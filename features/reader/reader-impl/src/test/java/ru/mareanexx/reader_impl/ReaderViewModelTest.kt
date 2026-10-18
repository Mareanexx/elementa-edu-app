package ru.mareanexx.reader_impl

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_impl.presentation.ReaderViewModel
import ru.mareanexx.reader_impl.presentation.model.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeReaderRepository
    @Before fun setup() { Dispatchers.setMain(dispatcher); repository = FakeReaderRepository() }
    @After fun cleanup() { Dispatchers.resetMain() }
    private fun selection(action: String) = ReaderSelection(ReaderAnchor("v1", "chapter_1", 0, 12, "Атомы и ионы", "", ""), action)
    @Test fun finishFlushesLatestPositionAndSettingsWithoutWaitingForDebounce() = runTest {
        val model = ReaderViewModel("book", repository)
        runCurrent()
        model.accept(ReaderIntent.Position(240, .4, "chapter_1", "Атомы", 4, 10))
        model.accept(ReaderIntent.Settings(ReaderSettings(theme = ReaderTheme.Night, fontSize = 22f)))
        model.finish()
        assertEquals(240, repository.positions.last().offset)
        assertEquals(64, repository.positions.last().sourcePagesRead)
        assertEquals(22f, repository.savedSettings.last().fontSize)
    }
    @Test fun failedSaveKeepsDraftAndDiscardNeedsConfirmation() = runTest {
        val model = ReaderViewModel("book", repository); runCurrent()
        model.accept(ReaderIntent.Selection(selection("note")))
        model.accept(ReaderIntent.NoteText("Вопрос для повторения"))
        repository.failSave = ReaderError.Storage
        model.accept(ReaderIntent.SaveNote); runCurrent()
        assertEquals("Вопрос для повторения", model.state.value.note!!.text)
        assertFalse(model.state.value.note!!.saving)
        model.accept(ReaderIntent.DismissNote)
        assertTrue(model.state.value.confirmDiscard)
        model.accept(ReaderIntent.KeepNote)
        assertNotNull(model.state.value.note)
        model.accept(ReaderIntent.DismissNote); model.accept(ReaderIntent.DiscardNote)
        assertNull(model.state.value.note)
    }
    @Test fun rejectsSelectionFromOtherPublicationVersion() = runTest {
        val model = ReaderViewModel("book", repository); runCurrent()
        model.accept(ReaderIntent.Selection(selection("highlight").copy(anchor = selection("highlight").anchor.copy(version = "old"))))
        assertEquals(ReaderPanel.None, model.state.value.panel)
        assertNull(model.state.value.selection)
    }
    @Test fun retryReopensAfterRecoverableFailure() = runTest {
        repository.openFailure = ReaderError.Offline
        val model = ReaderViewModel("book", repository); runCurrent()
        assertEquals(ReaderError.Offline, model.state.value.error)
        repository.openFailure = null
        model.accept(ReaderIntent.Retry); runCurrent()
        assertNotNull(model.state.value.document)
        assertNull(model.state.value.error)
    }
    @Test fun reachingEndNeedsConfirmationAndReviewingDoesNotLoseCompletedStatus() = runTest {
        val model = ReaderViewModel("book", repository); runCurrent()
        model.accept(ReaderIntent.Position(900, 1.0, "chapter_1", "Атомы", 10, 10, atEnd = true))
        advanceTimeBy(700); runCurrent()
        assertEquals(ReaderCompletion.Prompt, model.state.value.completion)
        assertFalse(model.state.value.completed)
        assertEquals(159, repository.positions.last().sourcePagesRead)
        model.accept(ReaderIntent.CompleteBook)
        model.finish()
        assertTrue(model.state.value.completed)
        assertEquals(ReaderCompletion.Finished, model.state.value.completion)
        assertEquals(160, repository.positions.last().sourcePagesRead)
        assertEquals(1.0, repository.positions.last().progression, 0.0)
        model.accept(ReaderIntent.DismissCompletion)
        model.accept(ReaderIntent.Position(400, .4, "chapter_1", "Атомы", 4, 10))
        advanceTimeBy(700); runCurrent()
        assertEquals(160, repository.positions.last().sourcePagesRead)
        assertEquals(.4, repository.positions.last().progression, 0.0)
        assertEquals(ReaderCompletion.Hidden, model.state.value.completion)
    }
    @Test fun completionFailureCanBeRetriedWithoutLeavingTheReader() = runTest {
        val model = ReaderViewModel("book", repository); runCurrent()
        model.accept(ReaderIntent.Position(900, 1.0, "chapter_1", "Атомы", 10, 10, atEnd = true))
        advanceTimeBy(700); runCurrent()
        repository.positionFailure = ReaderError.Storage
        model.accept(ReaderIntent.CompleteBook); runCurrent()
        assertFalse(model.state.value.completed)
        assertEquals(ReaderCompletion.Prompt, model.state.value.completion)
        assertEquals(ReaderError.Storage, model.state.value.completionError)
        repository.positionFailure = null
        model.accept(ReaderIntent.CompleteBook); runCurrent()
        assertTrue(model.state.value.completed)
        assertEquals(ReaderCompletion.Finished, model.state.value.completion)
    }
    @Test fun completionDoesNotInterruptPassingTheEndOrRepeatAfterDismissal() = runTest {
        val model = ReaderViewModel("book", repository); runCurrent()
        val end = ReaderIntent.Position(900, 1.0, "chapter_1", "Атомы", 10, 10, atEnd = true)
        val middle = ReaderIntent.Position(400, .4, "chapter_1", "Атомы", 4, 10)
        model.accept(end); advanceTimeBy(200)
        model.accept(middle); advanceTimeBy(700); runCurrent()
        assertEquals(ReaderCompletion.Hidden, model.state.value.completion)
        model.accept(end); advanceTimeBy(700); runCurrent()
        assertEquals(ReaderCompletion.Prompt, model.state.value.completion)
        model.accept(ReaderIntent.DismissCompletion)
        model.accept(middle); model.accept(end); advanceTimeBy(700); runCurrent()
        assertEquals(ReaderCompletion.Hidden, model.state.value.completion)
        assertFalse(model.state.value.completed)
    }    @Test fun latestPositionTakesPrecedenceOverDownloadedBookStatus() = runTest {
        repository.document = repository.document.copy(
            book = repository.book.copy(pagesRead = repository.book.pages),
            position = ReaderPosition("book", "v1", "chapter_1", 0, .01, 1, 100),
        )
        val model = ReaderViewModel("book", repository); runCurrent()
        assertFalse(model.state.value.completed)
        model.accept(ReaderIntent.Position(0, 0.0, "chapter_1", "Атомы", 1, 10))
        advanceTimeBy(700); runCurrent()
        assertEquals(1, repository.positions.last().sourcePagesRead)
    }

}
