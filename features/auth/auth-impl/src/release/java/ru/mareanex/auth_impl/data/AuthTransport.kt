package ru.mareanex.auth_impl.data

import android.content.Context
import okhttp3.OkHttpClient

@Suppress("UNUSED_PARAMETER")
fun configureAuthTransport(context: Context, builder: OkHttpClient.Builder, useMocks: Boolean) {
    require(!useMocks) { "Mock authentication is unavailable in release builds" }
}

@Suppress("UNUSED_PARAMETER")
fun updateDemoAccount(context: Context, id: String, fullName: String, username: String): Boolean = false
