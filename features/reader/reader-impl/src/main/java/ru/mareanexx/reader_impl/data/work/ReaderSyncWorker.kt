package ru.mareanexx.reader_impl.data.work

import android.content.Context
import androidx.work.*
import ru.mareanexx.reader_api.domain.repository.ReaderRepository
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_impl.data.repository.DefaultReaderRepository

internal class ReaderSyncWorker(
    appContext: Context,
    parameters: WorkerParameters,
    private val repository: suspend (String) -> ReaderRepository?,
) : CoroutineWorker(appContext, parameters) {
    override suspend fun doWork(): Result {
        val owner = inputData.getString("user") ?: return Result.failure()
        val source = repository(owner) as? DefaultReaderRepository ?: return Result.failure()
        return when (val result = source.syncTask(inputData.getString("book") ?: "*")) {
            is ReaderResult.Success -> Result.success()
            is ReaderResult.Failure -> if (result.error in listOf(ReaderError.Unauthorized, ReaderError.Forbidden, ReaderError.InvalidResponse)) Result.failure() else Result.retry()
        }
    }
}
