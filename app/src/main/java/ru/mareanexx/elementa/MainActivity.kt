package ru.mareanexx.elementa

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import ru.mareanexx.elementa.presentation.navigation.ReaderNotificationTarget
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.mareanexx.elementa.presentation.ElementaApp

class MainActivity : ComponentActivity() {
    private val readerNotification = mutableStateOf<ReaderNotificationTarget?>(null)
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); readNotification(intent) }
    private fun readNotification(intent: Intent?) {
        val user = intent?.getStringExtra("reader_notification_user")?.takeIf { it.isNotBlank() && it.length <= 200 }
        val book = intent?.getStringExtra("reader_notification_book")?.takeIf { it.isNotBlank() && it.length <= 200 }
        if (user != null && book != null) readerNotification.value = ReaderNotificationTarget(user, book)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        readNotification(intent)
        val container = (application as ElementaApplication).container
        setContent { ElementaApp(container.authRepository, container::profileRepository, container::collectionsRepository, container::bookRepository, container::libraryRepository, container::catalogRepository, container::readerRepository, readerNotification.value, { readerNotification.value = null; intent.removeExtra("reader_notification_book"); intent.removeExtra("reader_notification_user") }) }
    }
}
