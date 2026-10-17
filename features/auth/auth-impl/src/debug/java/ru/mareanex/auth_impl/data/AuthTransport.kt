package ru.mareanex.auth_impl.data

import android.content.Context
import okhttp3.OkHttpClient
import ru.mareanex.auth_impl.data.mock.DemoAuthInterceptor

fun configureAuthTransport(context: Context, builder: OkHttpClient.Builder, useMocks: Boolean) {
    if (useMocks) builder.addInterceptor(DemoAuthInterceptor(context))
}
