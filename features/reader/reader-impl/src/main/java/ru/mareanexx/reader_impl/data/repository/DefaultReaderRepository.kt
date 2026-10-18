package ru.mareanexx.reader_impl.data.repository

import android.content.Context
import ru.mareanexx.book_api.domain.model.BookShelves
import androidx.room.withTransaction
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_api.domain.repository.ReaderRepository
import ru.mareanexx.reader_api.domain.validation.ReaderRules
import ru.mareanexx.reader_impl.data.epub.*
import ru.mareanexx.reader_impl.data.local.*
import ru.mareanexx.reader_impl.data.local.entity.*
import ru.mareanexx.reader_impl.data.remote.model.ReadingManifest
import ru.mareanexx.reader_impl.data.remote.source.*
import ru.mareanexx.reader_impl.data.work.ReaderWorkScheduler
import java.io.*
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

internal class DefaultReaderRepository(
    private val context: Context,
    private val local: ReaderLocalStore,
    private val remote: ReaderRemoteDataSource,
    private val downloader: PublicationDownloader,
    private val scheduler: ReaderWorkScheduler,
) : ReaderRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val active = ConcurrentHashMap<String, MutableSet<String>>()
    private val lastSync = ConcurrentHashMap<String, Long>()
    private val syncing = MutableStateFlow(emptyMap<String, Boolean>())
    private val errors = MutableStateFlow(emptyMap<String, ReaderError>())
    private val mutableChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    private val syncMutex = Mutex()
    private val processor = EpubProcessor()
    override val changes = mutableChanges.asSharedFlow()
    override val settings = local.settings
    override val transfers = local.transfers.map { rows -> rows.map(::transfer) }
    init { scope.launch {
        scheduler.maintenance()
        local.dao.allTransfers(local.userId).filter { it.phase in listOf("Queued", "Downloading", "Preparing") }.forEach { row ->
            val legacy = row.requestId.isBlank()
            val requestId = if (legacy) UUID.randomUUID().toString() else row.requestId
            if (legacy) local.mutateTransfer(row.bookId) { it.copy(requestId = requestId) }
            scheduler.download(row.bookId, requestId, replace = legacy)
        }
        val dirty = local.dao.dirtyPositions(local.userId).map { it.bookId } + local.dao.dirtyAnnotations(local.userId).map { it.bookId }
        dirty.distinct().forEach(scheduler::sync)
    } }
    override fun annotations(bookId: String) = local.annotations(bookId)
    override fun syncState(bookId: String): Flow<ReaderSyncState> = combine(local.dao.annotations(local.userId, bookId), local.dao.observePosition(local.userId, bookId), syncing, errors) { notes, position, running, failures ->
        ReaderSyncState(notes.count { it.revision > it.syncedRevision } + if (position != null && position.revision > position.syncedRevision) 1 else 0, running[bookId] == true, failures[bookId])
    }
    override suspend fun requestDownload(bookId: String, pinned: Boolean): ReaderResult<Unit> = storage {
        require(bookId.isNotBlank())
        val current = local.dao.transfer(local.userId, bookId)
        if (current?.phase == "Ready" && current.directory?.let { File(it, "publication.json").isFile } == true) {
            local.mutateTransfer(bookId) { it.copy(pinned = it.pinned || pinned, lastAccess = System.currentTimeMillis()) }
            if (pinned && !current.pinned) mutableChanges.emit(Unit)
        } else {
            val next = local.mutateTransfer(bookId) {
                val keep = it.phase in listOf("Queued", "Downloading", "Preparing") && it.requestId.isNotBlank()
                it.copy(pinned = it.pinned || pinned, requestId = if (keep) it.requestId else UUID.randomUUID().toString(), phase = if (keep) it.phase else "Queued", lastAccess = System.currentTimeMillis(), error = null)
            }
            scheduler.download(bookId, next.requestId, replace = current?.requestId != next.requestId)
        }
    }
    override suspend fun cancelDownload(bookId: String): ReaderResult<Unit> = storage {
        local.mutateTransfer(bookId) { it.copy(phase = if (it.phase == "Ready") "Ready" else "Cancelled", pinned = false, error = null) }
        scheduler.cancel(bookId)
        trimCache()
        mutableChanges.emit(Unit)
    }
    override suspend fun removeDownload(bookId: String): ReaderResult<Unit> = storage {
        scheduler.cancel(bookId)
        if (active.containsKey(bookId)) local.mutateTransfer(bookId) { it.copy(pinned = false) }
        else { directory(bookId).deleteRecursively(); local.dao.deleteTransfer(local.userId, bookId) }
        mutableChanges.emit(Unit)
    }
    override suspend fun updateLibraryState(bookId: String, shelves: BookShelves, pagesRead: Int?): ReaderResult<Unit> = storage {
        var changed = false
        local.database.withTransaction {
            val row = local.dao.transfer(local.userId, bookId) ?: return@withTransaction
            if (row.phase != "Ready" || row.manifestJson == null) return@withTransaction
            val manifest = manifest(row)
            require(pagesRead == null || pagesRead in 0..manifest.book.pages)
            val next = manifest.book.copy(shelves = shelves, pagesRead = pagesRead ?: manifest.book.pagesRead)
            changed = next != manifest.book || pagesRead != null
            if (changed) local.dao.putTransfer(row.copy(manifestJson = local.gson.toJson(manifest.copy(book = next))))
            if (pagesRead != null) {
                val chapter = row.directory?.let { processor.read(File(it)).chapters.firstOrNull()?.id }.orEmpty()
                local.savePosition(ReaderPosition(bookId, manifest.version, chapter, 0, pagesRead.toDouble() / next.pages, pagesRead, System.currentTimeMillis()))
            }
        }
        if (pagesRead != null) synchronize(bookId)
        if (changed) mutableChanges.emit(Unit)
    }
    override suspend fun downloadedBooks(): ReaderResult<List<OfflineBook>> = storage {
        local.dao.allTransfers(local.userId).filter { it.pinned && it.phase == "Ready" }.mapNotNull { row ->
            val directory = row.directory?.let(::File)
            if (directory == null || !File(directory, "publication.json").isFile) null
            else {
                val manifest = manifest(row)
                OfflineBook(manifest.book, local.position(row.bookId)?.sourcePagesRead?.coerceIn(0, manifest.book.pages) ?: manifest.book.pagesRead, row.totalBytes, row.completedAt)
            }
        }
    }
    override suspend fun openBook(bookId: String): ReaderResult<ReaderDocument> {
        val cached = local.dao.transfer(local.userId, bookId)?.let { it.phase == "Ready" && it.directory?.let { path -> File(path, "publication.json").isFile } == true } == true
        if (!cached) {
            val connectivity = context.getSystemService(android.net.ConnectivityManager::class.java)
            val network = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
            if (network?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) != true) return ReaderResult.Failure(ReaderError.Offline)
        }
        val requested = requestDownload(bookId, false)
        if (requested is ReaderResult.Failure) return requested
        return try {
            val ready = local.transfers.first { rows -> rows.any { it.bookId == bookId && it.phase in listOf("Ready", "Failed", "Cancelled") } }.first { it.bookId == bookId }
            if (ready.phase != "Ready") ReaderResult.Failure(ready.error?.let { ReaderError.valueOf(it) } ?: ReaderError.Cancelled)
            else storage {
                val manifest = manifest(ready)
                val folder = File(requireNotNull(ready.directory))
                val prepared = processor.read(folder)
                require(File(folder, "content.html").isFile && prepared.chapters.isNotEmpty())
                local.mutateTransfer(bookId) { it.copy(lastAccess = System.currentTimeMillis()) }
                synchronize(bookId)
                ReaderDocument(manifest.book, manifest.version, folder.path, prepared.chapters, local.position(bookId))
            }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: IOException) { ReaderResult.Failure(ReaderError.Storage) }
    }
    override fun retainBook(bookId: String, leaseId: String) {
        active.compute(bookId) { _, owners -> (owners ?: ConcurrentHashMap.newKeySet()).apply { add(leaseId) } }
    }
    override fun releaseBook(bookId: String, leaseId: String) {
        active.computeIfPresent(bookId) { _, owners -> owners.apply { remove(leaseId) }.takeUnless { it.isEmpty() } }
        scope.launch { synchronize(bookId); mutableChanges.emit(Unit); trimCache() }
    }
    override suspend fun saveSettings(value: ReaderSettings): ReaderResult<Unit> {
        if (!ReaderRules.valid(value)) return ReaderResult.Failure(ReaderError.InvalidResponse)
        return storage { local.dao.putSettings(SettingsEntity(local.userId, local.gson.toJson(value))) }
    }
    override suspend fun savePosition(value: ReaderPosition): ReaderResult<Unit> {
        if (!ReaderRules.valid(value)) return ReaderResult.Failure(ReaderError.InvalidResponse)
        return storage {
            local.savePosition(value)
            val now = System.currentTimeMillis()
            if (now - (lastSync[value.bookId] ?: 0) > 15_000) { lastSync[value.bookId] = now; scheduler.sync(value.bookId) }
        }
    }
    override suspend fun saveAnnotation(value: ReaderAnnotation): ReaderResult<Unit> {
        if (!ReaderRules.valid(value.anchor) || value.note?.length?.let { it > ReaderRules.MAX_NOTE } == true || value.id.isBlank()) return ReaderResult.Failure(ReaderError.InvalidAnnotation)
        return storage { local.saveAnnotation(value); scheduler.sync(value.bookId) }
    }
    override suspend fun deleteAnnotation(id: String): ReaderResult<Unit> = storage {
        local.dao.annotation(local.userId, id)?.let { row ->
            val note = local.gson.fromJson(row.payload, ReaderAnnotation::class.java)
            local.saveAnnotation(note.copy(deleted = true)); scheduler.sync(note.bookId)
        }
    }
    override suspend fun synchronize(bookId: String) { scheduler.sync(bookId) }

    suspend fun downloadTask(bookId: String, requestId: String, progress: suspend (BookTransfer) -> Unit): ReaderResult<Unit> = withContext(Dispatchers.IO) {
        try {
            if (local.dao.transfer(local.userId, bookId)?.let { it.requestId == requestId && it.phase != "Cancelled" } != true) return@withContext ReaderResult.Failure(ReaderError.Cancelled)
            val fetched = remote.manifest(bookId)
            if (fetched is ReaderResult.Failure) return@withContext fetched
            val manifest = (fetched as ReaderResult.Success).value
            currentCoroutineContext().ensureActive()
            if (local.dao.transfer(local.userId, bookId)?.let { it.requestId != requestId || it.phase == "Cancelled" } != false) return@withContext ReaderResult.Failure(ReaderError.Cancelled)
            require(manifest.book.id == bookId)
            val version = File(File(directory(bookId), hash(manifest.version + manifest.sha256)), hash(requestId)).apply { mkdirs() }
            if (version.usableSpace < minOf(manifest.byteSize * 4 + 2_000_000, 160_000_000)) return@withContext ReaderResult.Failure(ReaderError.OutOfSpace)
            local.mutateTransfer(bookId) { row -> if (row.requestId != requestId || row.phase == "Cancelled") row else row.copy(title = manifest.book.title, phase = "Downloading", manifestJson = local.gson.toJson(manifest), totalBytes = manifest.byteSize, error = null) }
            val partial = File(version, "book.epub.part")
            val complete = File(version, "book.epub")
            if (!partial.exists() && complete.exists()) complete.renameTo(partial)
            coroutineScope {
                val channel = Channel<Pair<Long, Long>>(Channel.CONFLATED)
                val updates = launch {
                    for ((read, total) in channel) {
                        val row = local.mutateTransfer(bookId) { if (it.phase == "Cancelled" || it.requestId != requestId) it else it.copy(phase = "Downloading", bytesRead = read, totalBytes = total) }
                        progress(transfer(row))
                    }
                }
                try { downloader.download(manifest, partial) { read, total -> channel.trySend(read to total) } }
                finally { channel.close(); updates.join() }
            }
            currentCoroutineContext().ensureActive()
            val preparing = local.mutateTransfer(bookId) { if (it.requestId != requestId || it.phase == "Cancelled") it else it.copy(phase = "Preparing", bytesRead = manifest.byteSize) }
            if (preparing.requestId != requestId || preparing.phase == "Cancelled") return@withContext ReaderResult.Failure(ReaderError.Cancelled)
            progress(transfer(preparing))
            val staging = File(version, "staging")
            staging.deleteRecursively(); staging.mkdirs()
            processor.prepare(partial, staging)
            currentCoroutineContext().ensureActive()
            val prepared = File(version, "prepared")
            if (prepared.exists()) prepared.deleteRecursively()
            if (!staging.renameTo(prepared) || !partial.renameTo(complete)) throw IOException("Не удалось завершить подготовку книги")
            val latest = local.dao.transfer(local.userId, bookId)
            if (latest?.let { it.phase == "Cancelled" || it.requestId != requestId } != false) return@withContext ReaderResult.Failure(ReaderError.Cancelled)
            val completed = local.mutateTransfer(bookId) { if (it.requestId != requestId || it.phase == "Cancelled") it else it.copy(phase = "Ready", directory = prepared.path, bytesRead = manifest.byteSize, completedAt = System.currentTimeMillis(), lastAccess = System.currentTimeMillis(), error = null) }
            if (completed.requestId != requestId || completed.phase != "Ready") return@withContext ReaderResult.Failure(ReaderError.Cancelled)
            directory(bookId).listFiles()?.filter { it.canonicalPath != version.parentFile!!.canonicalPath }?.forEach { it.deleteRecursively() }
            version.parentFile!!.listFiles()?.filter { it.canonicalPath != version.canonicalPath }?.forEach { it.deleteRecursively() }
            trimCache()
            mutableChanges.emit(Unit)
            ReaderResult.Success(Unit)
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (failed: ReaderFileException) { ReaderResult.Failure(failed.error)
        } catch (_: java.io.InterruptedIOException) { ReaderResult.Failure(ReaderError.Timeout)
        } catch (failed: IOException) { ReaderResult.Failure(if (failed.message.orEmpty().contains("ENOSPC")) ReaderError.OutOfSpace else ReaderError.Offline)
        } catch (_: RuntimeException) { ReaderResult.Failure(ReaderError.CorruptBook) }
    }
    suspend fun downloadFailed(bookId: String, requestId: String, error: ReaderError, retry: Boolean) {
        local.mutateTransfer(bookId) { if (it.phase == "Cancelled" || it.requestId != requestId) it else it.copy(phase = if (retry) "Queued" else "Failed", error = error.name) }
    }
    suspend fun downloadStopped(bookId: String, requestId: String, cancelledByUser: Boolean) = withContext(NonCancellable + Dispatchers.IO) {
        local.mutateTransfer(bookId) { if (it.phase in listOf("Ready", "Cancelled") || it.requestId != requestId) it else it.copy(phase = if (cancelledByUser) "Cancelled" else "Queued", pinned = if (cancelledByUser) false else it.pinned) }
    }
    suspend fun syncTask(bookId: String): ReaderResult<Unit> = syncMutex.withLock {
        val books = if (bookId == "*") (local.dao.dirtyPositions(local.userId).map { it.bookId } + local.dao.dirtyAnnotations(local.userId).map { it.bookId }).distinct() else listOf(bookId)
        for (id in books) {
            syncing.update { it + (id to true) }; errors.update { it - id }
            try {
                val pendingPosition = local.dao.position(local.userId, id)?.let { it.revision > it.syncedRevision } == true
                val result = synchronizeOne(id)
                if (result is ReaderResult.Success && pendingPosition && !active.containsKey(id)) mutableChanges.emit(Unit)
                if (result is ReaderResult.Failure) { errors.update { it + (id to result.error) }; return@withLock result }
            } finally { syncing.update { it - id } }
        }
        ReaderResult.Success(Unit)
    }
    private suspend fun synchronizeOne(bookId: String): ReaderResult<Unit> {
        for (row in local.dao.dirtyAnnotations(local.userId).filter { it.bookId == bookId }) {
            val value = local.gson.fromJson(row.payload, ReaderAnnotation::class.java)
            val result = if (value.deleted) remote.delete(value) else remote.saveAnnotation(value)
            if (result is ReaderResult.Failure) return result
            local.dao.acknowledgeAnnotation(local.userId, row.id, row.revision)
        }
        local.dao.position(local.userId, bookId)?.takeIf { it.revision > it.syncedRevision }?.let { row ->
            val result = remote.savePosition(local.gson.fromJson(row.payload, ReaderPosition::class.java), row.revision)
            if (result is ReaderResult.Failure) return result
            local.dao.acknowledgePosition(local.userId, bookId, row.revision)
        }
        when (val result = remote.progress(bookId)) {
            is ReaderResult.Success -> local.database.withTransaction {
                val row = local.dao.position(local.userId, bookId)
                val previous = row?.let { local.gson.fromJson(it.payload, ReaderPosition::class.java) }
                if (row == null || (row.revision == row.syncedRevision && result.value.updatedAt > (previous?.updatedAt ?: 0))) local.dao.putPosition(PositionEntity(local.userId, bookId, local.gson.toJson(result.value), 1, 1))
            }
            is ReaderResult.Failure -> if (result.error != ReaderError.NotFound) return result
        }
        var cursor: String? = null
        val seen = mutableSetOf<String>()
        do {
            val result = remote.annotations(bookId, cursor)
            if (result is ReaderResult.Failure) return result
            val page = (result as ReaderResult.Success).value
            page.first.forEach { annotation -> if (annotation.bookId != bookId) return ReaderResult.Failure(ReaderError.InvalidResponse) else local.mergeAnnotation(annotation) }
            cursor = page.second
            if (cursor != null && (!seen.add(cursor) || seen.size > 100)) return ReaderResult.Failure(ReaderError.InvalidResponse)
        } while (cursor != null)
        return ReaderResult.Success(Unit)
    }
    private suspend fun trimCache() {
        val unpinned = local.dao.allTransfers(local.userId).filter { ((!it.pinned && it.phase == "Ready") || it.phase in listOf("Failed", "Cancelled")) && !active.containsKey(it.bookId) }.sortedBy { it.lastAccess }
        var count = unpinned.size
        var bytes = unpinned.sumOf { directory(it.bookId).walkTopDown().filter(File::isFile).sumOf(File::length) }
        for (row in unpinned) {
            if (count <= 3 && bytes <= 160_000_000) break
            val directory = directory(row.bookId)
            bytes -= directory.walkTopDown().filter(File::isFile).sumOf(File::length)
            directory.deleteRecursively(); local.dao.deleteTransfer(local.userId, row.bookId); count--
        }
    }
    private fun directory(bookId: String) = File(context.filesDir, "reader-content/${hash(local.userId)}/${hash(bookId)}")
    private fun manifest(row: TransferEntity) = local.gson.fromJson(requireNotNull(row.manifestJson), ReadingManifest::class.java)
    private fun transfer(row: TransferEntity) = BookTransfer(row.bookId, row.title, TransferPhase.valueOf(row.phase), row.pinned, row.bytesRead, row.totalBytes, row.error?.let(ReaderError::valueOf))
    private suspend fun <T> storage(action: suspend () -> T): ReaderResult<T> = withContext(Dispatchers.IO) {
        try { ReaderResult.Success(action())
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: Exception) { ReaderResult.Failure(ReaderError.Storage) }
    }
    private fun hash(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
