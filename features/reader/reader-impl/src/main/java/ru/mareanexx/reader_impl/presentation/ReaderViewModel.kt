package ru.mareanexx.reader_impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_api.domain.repository.ReaderRepository
import ru.mareanexx.reader_api.domain.validation.ReaderRules
import ru.mareanexx.reader_impl.presentation.model.*
import java.util.UUID

class ReaderViewModel(
    private val bookId: String,
    private val repository: ReaderRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(ReaderState())
    val state = mutable.asStateFlow()
    private var opening: Job? = null
    private var presented = false
    private var savingPosition: Job? = null
    private var savingSettings: Job? = null
    private var completing: Job? = null
    private var completionPrompt: Job? = null
    private var completionShown = false
    init {
        viewModelScope.launch { repository.settings.collect { settings -> if (savingSettings?.isActive != true) mutable.update { it.copy(settings = settings) } } }
        viewModelScope.launch { repository.transfers.collect { values -> mutable.update { it.copy(transfer = values.firstOrNull { value -> value.bookId == bookId }) } } }
        viewModelScope.launch { repository.annotations(bookId).collect { annotations -> mutable.update { it.copy(annotations = annotations) } } }
        viewModelScope.launch { repository.syncState(bookId).collect { sync -> mutable.update { it.copy(sync = sync) } } }
        open()
    }
    fun present() {
        if (presented) open() else presented = true
    }
    private fun open() {
        opening?.cancel()
        opening = viewModelScope.launch {
            completing?.join()
            completionPrompt?.cancel()
            completionShown = false
            mutable.update { it.copy(attempt = it.attempt + 1, loading = true, rendering = true, document = null, error = null, atEnd = false, completion = ReaderCompletion.Hidden, completionError = null) }
            when (val result = repository.openBook(bookId)) {
                is ReaderResult.Success -> mutable.update { it.copy(document = result.value, position = result.value.position, chapter = result.value.chapters.firstOrNull()?.title.orEmpty(), loading = false,
                    completed = result.value.position?.let { position -> position.sourcePagesRead == result.value.book.pages } ?: (result.value.book.pagesRead == result.value.book.pages)) }
                is ReaderResult.Failure -> mutable.update { it.copy(loading = false, rendering = false, error = result.error) }
            }
        }
    }
    fun accept(intent: ReaderIntent) {
        when (intent) {
            ReaderIntent.Retry -> open()
            ReaderIntent.RenderFailed -> mutable.update { it.copy(rendering = false, error = ReaderError.CorruptBook) }
            ReaderIntent.Ready -> mutable.update { it.copy(rendering = false) }
            ReaderIntent.ToggleChrome -> mutable.update { it.copy(chrome = !it.chrome, panel = ReaderPanel.None) }
            is ReaderIntent.Panel -> mutable.update { it.copy(panel = intent.value, chrome = true, operationError = null) }
            is ReaderIntent.Settings -> {
                mutable.update { it.copy(settings = intent.value) }
                savingSettings?.cancel()
                savingSettings = viewModelScope.launch { delay(200); handle(repository.saveSettings(intent.value)) }
            }
            is ReaderIntent.Position -> {
                val document = mutable.value.document ?: return
                if (!intent.progression.isFinite() || intent.progression !in 0.0..1.0 || intent.offset < 0 || intent.pages !in 1..100000 || intent.page !in 1..intent.pages || document.chapters.none { it.id == intent.chapterId }) return
                val current = mutable.value
                val progression = if (current.completed) intent.progression else intent.progression.coerceAtMost(.999)
                val pagesRead = if (current.completed) document.book.pages else if (document.book.pages > 1) (progression * document.book.pages).toInt().coerceIn(1, document.book.pages - 1) else 0
                val position = ReaderPosition(bookId, document.version, intent.chapterId, intent.offset, progression, pagesRead, System.currentTimeMillis())
                mutable.update { it.copy(position = position, page = intent.page, pages = intent.pages, chapter = intent.chapter, atEnd = intent.atEnd,
                    completion = if (!intent.atEnd && it.completion == ReaderCompletion.Prompt) ReaderCompletion.Hidden else it.completion) }
                if (current.completion != ReaderCompletion.Saving) {
                    savingPosition?.cancel()
                    savingPosition = viewModelScope.launch { delay(500); handle(repository.savePosition(position)) }
                }
                if (intent.atEnd && !current.completed && !completionShown && completionPrompt?.isActive != true) {
                    completionPrompt = viewModelScope.launch {
                        delay(650)
                        if (mutable.value.atEnd && !mutable.value.completed) {
                            completionShown = true
                            mutable.update { it.copy(completion = ReaderCompletion.Prompt, chrome = true) }
                        }
                    }
                } else if (!intent.atEnd) completionPrompt?.cancel()
            }
            is ReaderIntent.Selection -> {
                val selection = intent.value
                if (!ReaderRules.valid(selection.anchor) || selection.anchor.version != mutable.value.document?.version) return
                val existing = mutable.value.annotations.firstOrNull { it.anchor.version == selection.anchor.version && it.anchor.start == selection.anchor.start && it.anchor.end == selection.anchor.end }
                mutable.update { it.copy(selection = selection, selectedAnnotation = existing, panel = if (selection.action == "highlight") ReaderPanel.Colors else ReaderPanel.None) }
                if (selection.action == "note") editNote()
            }
            is ReaderIntent.Annotation -> mutable.value.annotations.firstOrNull { it.id == intent.id }?.let { value ->
                mutable.update { it.copy(selectedAnnotation = value, selection = null, panel = ReaderPanel.Annotation) }
            }
            is ReaderIntent.Color -> color(intent.value)
            ReaderIntent.EditNote -> editNote()
            is ReaderIntent.NoteText -> if (intent.value.length <= ReaderRules.MAX_NOTE) mutable.update { it.copy(note = it.note?.copy(text = intent.value), operationError = null) }
            ReaderIntent.SaveNote -> saveNote()
            ReaderIntent.DismissNote -> mutable.update { if (it.note?.text != it.note?.original) it.copy(confirmDiscard = true) else it.copy(note = null) }
            ReaderIntent.DiscardNote -> mutable.update { it.copy(note = null, confirmDiscard = false) }
            ReaderIntent.KeepNote -> mutable.update { it.copy(confirmDiscard = false) }
            ReaderIntent.DeleteAnnotation -> mutable.value.selectedAnnotation?.let { value -> viewModelScope.launch { handle(repository.deleteAnnotation(value.id)); mutable.update { it.copy(panel = ReaderPanel.None, selectedAnnotation = null) } } }
            ReaderIntent.DismissError -> mutable.update { it.copy(operationError = null) }
            ReaderIntent.Flush -> flush()
            ReaderIntent.CompleteBook -> completeBook()
            ReaderIntent.DismissCompletion -> if (mutable.value.completion != ReaderCompletion.Saving) mutable.update { it.copy(completion = ReaderCompletion.Hidden, completionError = null) }
        }
    }
    private fun color(color: HighlightColor) {
        val current = mutable.value
        val annotation = current.selectedAnnotation
        val anchor = annotation?.anchor ?: current.selection?.anchor ?: return
        val now = System.currentTimeMillis()
        val value = annotation?.copy(color = color) ?: ReaderAnnotation(UUID.randomUUID().toString(), bookId, anchor, color, null, now, now)
        viewModelScope.launch { if (handle(repository.saveAnnotation(value))) mutable.update { it.copy(panel = ReaderPanel.None, selection = null, selectedAnnotation = null) } }
    }
    private fun editNote() {
        val current = mutable.value
        val value = current.selectedAnnotation
        val anchor = value?.anchor ?: current.selection?.anchor ?: return
        mutable.update { it.copy(panel = ReaderPanel.None, note = ReaderNoteDraft(anchor, value?.id, value?.color ?: HighlightColor.Amber, value?.note.orEmpty(), value?.note.orEmpty())) }
    }
    private fun saveNote() {
        val draft = mutable.value.note?.takeUnless { it.saving } ?: return
        if (draft.text.isBlank()) return
        val now = System.currentTimeMillis()
        val old = mutable.value.annotations.firstOrNull { it.id == draft.annotationId }
        val value = old?.copy(note = draft.text.trim()) ?: ReaderAnnotation(UUID.randomUUID().toString(), bookId, draft.anchor, draft.color, draft.text.trim(), now, now)
        mutable.update { it.copy(note = draft.copy(saving = true), operationError = null) }
        viewModelScope.launch {
            if (handle(repository.saveAnnotation(value))) mutable.update { it.copy(note = null, selection = null, selectedAnnotation = null) }
            else mutable.update { it.copy(note = it.note?.copy(saving = false)) }
        }
    }
    private fun completeBook() {
        val current = mutable.value
        val document = current.document ?: return
        val position = current.position ?: return
        if (!current.atEnd || current.completed || current.completion != ReaderCompletion.Prompt) return
        mutable.update { it.copy(completion = ReaderCompletion.Saving, completionError = null) }
        completing = viewModelScope.launch {
            savingPosition?.cancelAndJoin()
            val completed = position.copy(progression = 1.0, sourcePagesRead = document.book.pages, updatedAt = System.currentTimeMillis())
            when (val result = repository.savePosition(completed)) {
                is ReaderResult.Success -> {
                    mutable.update {
                        val latest = it.position ?: completed
                        it.copy(completed = true, completion = ReaderCompletion.Finished,
                            position = latest.copy(progression = if (it.atEnd) 1.0 else latest.progression, sourcePagesRead = document.book.pages, updatedAt = completed.updatedAt))
                    }
                    mutable.value.position?.takeUnless { it == completed }?.let { handle(repository.savePosition(it)) }
                    repository.synchronize(bookId)
                }
                is ReaderResult.Failure -> mutable.update { it.copy(completion = if (it.atEnd) ReaderCompletion.Prompt else ReaderCompletion.Hidden, completionError = result.error) }
            }
        }
    }
    private fun handle(result: ReaderResult<Unit>): Boolean {
        if (result is ReaderResult.Failure) mutable.update { it.copy(operationError = result.error) }
        return result is ReaderResult.Success
    }
    suspend fun finish() {
        completing?.join()
        savingPosition?.cancelAndJoin()
        savingSettings?.cancelAndJoin()
        mutable.value.position?.let { handle(repository.savePosition(it)) }
        handle(repository.saveSettings(mutable.value.settings))
        repository.synchronize(bookId)
    }
    fun flush() {
        savingPosition?.cancel()
        viewModelScope.launch {
            completing?.join()
            mutable.value.position?.let { handle(repository.savePosition(it)) }
            repository.synchronize(bookId)
        }
    }
}
