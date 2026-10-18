package ru.mareanexx.reader_impl.data.work

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import ru.mareanexx.reader_impl.R
import ru.mareanexx.reader_api.domain.model.BookTransfer
import java.util.UUID

internal class ReaderNotifications(
    private val context: Context,
    private val userId: String,
) {
    private val manager = context.getSystemService(NotificationManager::class.java)
    init {
        manager.createNotificationChannel(NotificationChannel(CHANNEL, context.getString(R.string.reader_download_channel), NotificationManager.IMPORTANCE_LOW))
    }
    fun foreground(bookId: String, workId: UUID, value: BookTransfer?): ForegroundInfo {
        val percent = value?.let { (it.progress * 100).toInt() } ?: 0
        val notification = NotificationCompat.Builder(context, CHANNEL).setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(value?.title?.takeIf(String::isNotBlank) ?: context.getString(R.string.reader_preparing_title))
            .setContentText(context.getString(R.string.reader_download_running, percent))
            .addExtras(android.os.Bundle().apply { putString("reader_owner", userId) })
            .setProgress(100, percent, value?.totalBytes == 0L || value == null)
            .setOngoing(true).setOnlyAlertOnce(true).setSilent(true).setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .addAction(0, context.getString(R.string.reader_cancel_download), WorkManager.getInstance(context).createCancelPendingIntent(workId)).build()
        return if (Build.VERSION.SDK_INT >= 29) ForegroundInfo(id(bookId), notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC) else ForegroundInfo(id(bookId), notification)
    }
    fun ready(bookId: String, title: String) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val intent = Intent().setClassName(context.packageName, "${context.packageName}.MainActivity")
            .setData(android.net.Uri.parse("elementa://reader/${android.net.Uri.encode(userId)}/${android.net.Uri.encode(bookId)}"))
            .putExtra(EXTRA_BOOK, bookId).putExtra(EXTRA_USER, userId).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(context, id(bookId), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        manager.notify(id(bookId) + 1, NotificationCompat.Builder(context, CHANNEL).setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.reader_download_ready)).setContentText(title)
            .addExtras(android.os.Bundle().apply { putString("reader_owner", userId) })
            .setAutoCancel(true).setContentIntent(pending).setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build())
    }
    private fun id(bookId: String) = ("reader_${userId}_$bookId".hashCode() and 0x1fffffff) * 2
    companion object {
        private const val CHANNEL = "reader_downloads"
        const val EXTRA_BOOK = "reader_notification_book"
        const val EXTRA_USER = "reader_notification_user"
    }
}
