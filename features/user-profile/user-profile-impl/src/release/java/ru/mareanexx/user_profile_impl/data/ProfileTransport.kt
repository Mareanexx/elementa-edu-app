package ru.mareanexx.user_profile_impl.data

import android.content.Context
import okhttp3.OkHttpClient

@Suppress("UNUSED_PARAMETER")
fun configureProfileTransport(
    context: Context,
    builder: OkHttpClient.Builder,
    useMocks: Boolean,
    userId: String,
    name: String,
    username: String,
    updateAccount: (String, String, String) -> Boolean,
) {
    require(!useMocks) { "Моки профиля недоступны в release-сборке" }
}
