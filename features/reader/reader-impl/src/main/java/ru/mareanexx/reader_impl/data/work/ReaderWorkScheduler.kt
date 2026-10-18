package ru.mareanexx.reader_impl.data.work

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

internal class ReaderWorkScheduler(
    private val context: Context,
    private val userId: String,
) {
    private val manager get() = WorkManager.getInstance(context)
    fun download(bookId: String, requestId: String, replace: Boolean = false) {
        val request = OneTimeWorkRequestBuilder<ReaderDownloadWorker>()
            .setInputData(workDataOf("user" to userId, "book" to bookId, "request" to requestId))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .addTag(userTag(userId)).build()
        manager.enqueueUniqueWork(downloadName(userId, bookId), if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP, request)
    }
    fun cancel(bookId: String) { manager.cancelUniqueWork(downloadName(userId, bookId)) }
    fun sync(bookId: String) {
        val request = OneTimeWorkRequestBuilder<ReaderSyncWorker>().setInputData(workDataOf("user" to userId, "book" to bookId))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .addTag(userTag(userId)).build()
        manager.enqueueUniqueWork("reader_sync_${userId}_$bookId", ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }
    fun maintenance() {
        val request = PeriodicWorkRequestBuilder<ReaderSyncWorker>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf("user" to userId, "book" to "*"))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag(userTag(userId)).build()
        manager.enqueueUniquePeriodicWork("reader_maintenance_$userId", ExistingPeriodicWorkPolicy.KEEP, request)
    }
    companion object {
        fun userTag(userId: String) = "reader_user_$userId"
        fun downloadName(userId: String, bookId: String) = "reader_download_${userId}_$bookId"
    }
}
