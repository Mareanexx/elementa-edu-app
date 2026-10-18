package ru.mareanexx.reader_impl.data

import android.app.NotificationManager
import android.content.Context
import androidx.work.WorkManager
import ru.mareanexx.reader_impl.data.work.ReaderWorkScheduler

object ReaderBackgroundTasks {
    fun resumeForUser(context: Context, userId: String) {
        val scheduler = ReaderWorkScheduler(context, userId)
        scheduler.maintenance()
        scheduler.sync("*")
    }

    /** Выход из аккаунта останавливает его задачи, не удаляя локальные заметки и книги. */
    fun cancelForUser(context: Context, userId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(ReaderWorkScheduler.userTag(userId))
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.activeNotifications.filter { it.notification.extras.getString("reader_owner") == userId }.forEach { manager.cancel(it.tag, it.id) }
    }
}
