package ru.mareanexx.reader_impl.data

import android.content.Context
import androidx.work.*
import ru.mareanexx.reader_api.domain.repository.ReaderRepository
import ru.mareanexx.reader_impl.data.work.*

class ReaderWorkerFactory(
    private val repository: suspend (String) -> ReaderRepository?,
) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? = when (workerClassName) {
        ReaderDownloadWorker::class.java.name -> ReaderDownloadWorker(appContext, workerParameters, repository)
        ReaderSyncWorker::class.java.name -> ReaderSyncWorker(appContext, workerParameters, repository)
        else -> null
    }
}
