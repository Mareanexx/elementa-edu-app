package ru.mareanexx.elementa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.mareanexx.elementa.presentation.ElementaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as ElementaApplication).container.authRepository
        setContent { ElementaApp(repository) }
    }
}
