package ru.mareanexx.elementa

import android.app.Application
import ru.mareanexx.elementa.di.AppContainer

class ElementaApplication : Application() {
    val container by lazy { AppContainer(this) }
}
