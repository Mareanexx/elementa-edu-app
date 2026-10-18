package ru.mareanexx.elementa

import android.app.Application
import androidx.work.Configuration
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.mareanexx.reader_impl.data.ReaderBackgroundTasks
import ru.mareanexx.reader_impl.data.ReaderWorkerFactory
import ru.mareanexx.elementa.di.AppContainer

class ElementaApplication : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(ReaderWorkerFactory { container.readerRepositoryForWorker(it) }).build()
    val container by lazy { AppContainer(this) }
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var previous: String? = null
            container.authRepository.session.map { it?.user?.id }.distinctUntilChanged().collect { current ->
                previous?.takeIf { it != current }?.let { ReaderBackgroundTasks.cancelForUser(this@ElementaApplication, it) }
                if (current != null) ReaderBackgroundTasks.resumeForUser(this@ElementaApplication, current)
                previous = current
            }
        }
    }
}
