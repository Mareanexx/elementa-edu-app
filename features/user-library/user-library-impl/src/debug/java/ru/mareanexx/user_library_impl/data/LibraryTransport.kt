package ru.mareanexx.user_library_impl.data

import android.content.Context
import okhttp3.OkHttpClient
import ru.mareanexx.user_library_impl.data.mock.DemoLibraryInterceptor

fun configureLibraryTransport(context: Context, builder: OkHttpClient.Builder, enabled: Boolean, userId: String) {
    if (enabled) builder.addInterceptor(DemoLibraryInterceptor(context, userId))
}
