package ru.mareanexx.user_profile_impl.data

import android.content.Context
import okhttp3.OkHttpClient
import ru.mareanexx.user_profile_impl.data.mock.DemoProfileInterceptor

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
    if (useMocks) builder.addInterceptor(DemoProfileInterceptor(context, userId, name, username, updateAccount))
}
