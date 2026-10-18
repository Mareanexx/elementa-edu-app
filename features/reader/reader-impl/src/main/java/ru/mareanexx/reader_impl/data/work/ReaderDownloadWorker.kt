package ru.mareanexx.reader_impl.data.work

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import ru.mareanexx.reader_api.domain.repository.ReaderRepository
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_impl.data.repository.DefaultReaderRepository

internal class ReaderDownloadWorker(
    appContext: Context,
    parameters: WorkerParameters,
    private val repository: suspend (String) -> ReaderRepository?,
) : CoroutineWorker(appContext, parameters) {
    override suspend fun doWork(): Result {
        val owner = inputData.getString("user") ?: return Result.failure()
        val book = inputData.getString("book") ?: return Result.failure()
        val requestId = inputData.getString("request") ?: return Result.failure()
        val source = repository(owner) as? DefaultReaderRepository ?: return Result.failure()
        val notifications = ReaderNotifications(applicationContext, owner)
        return try {
            setForeground(notifications.foreground(book, id, source.transfers.first().firstOrNull { it.bookId == book }))
            when (val result = source.downloadTask(book, requestId) { transfer -> setForeground(notifications.foreground(book, id, transfer)) }) {
                is ReaderResult.Success -> { val ready = source.transfers.first().firstOrNull { it.bookId == book }; if (ready?.pinned == true) notifications.ready(book, ready.title); Result.success() }
                is ReaderResult.Failure -> {
                    val retry = result.error in listOf(ReaderError.Offline, ReaderError.Timeout, ReaderError.Unavailable, ReaderError.RateLimited) && runAttemptCount < 3
                    source.downloadFailed(book, requestId, result.error, retry)
                    if (retry) Result.retry() else Result.failure()
                }
            }
        } catch (cancelled: CancellationException) {
            val cancelledByUser = kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                if (android.os.Build.VERSION.SDK_INT >= 31) stopReason == WorkInfo.STOP_REASON_CANCELLED_BY_APP
                else WorkManager.getInstance(applicationContext).getWorkInfoByIdFlow(id).first()?.state == WorkInfo.State.CANCELLED
            }
            source.downloadStopped(book, requestId, cancelledByUser)
            throw cancelled
        } catch (_: Exception) {
            source.downloadFailed(book, requestId, ReaderError.Storage, false)
            Result.failure()
        }
    }
}
