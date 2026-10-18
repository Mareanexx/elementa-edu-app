package ru.mareanexx.book_impl.data

import android.content.Context
import okhttp3.OkHttpClient
import ru.mareanexx.book_impl.data.mock.DemoBookInterceptor

fun configureBookTransport(context: Context, builder: OkHttpClient.Builder, enabled: Boolean, userId: String, fullName: String) {
    if (enabled) builder.addInterceptor(DemoBookInterceptor(context.applicationContext, userId, fullName))
}
