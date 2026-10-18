package ru.mareanexx.reader_impl.data

import android.content.Context
import okhttp3.OkHttpClient
import ru.mareanexx.reader_impl.data.mock.DemoReaderInterceptor

fun configureReaderTransport(context: Context, api: OkHttpClient.Builder, content: OkHttpClient.Builder, mock: Boolean, userId: String) {
    if (mock) {
        val interceptor = DemoReaderInterceptor(context, userId)
        api.addInterceptor(interceptor)
        content.addInterceptor(interceptor)
    }
}
