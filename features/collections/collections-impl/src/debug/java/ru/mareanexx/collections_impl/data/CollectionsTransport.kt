package ru.mareanexx.collections_impl.data

import android.content.Context
import okhttp3.OkHttpClient
import ru.mareanexx.collections_impl.data.mock.DemoCollectionsInterceptor

fun configureCollectionsTransport(context: Context, builder: OkHttpClient.Builder, useMocks: Boolean, userId: String) {
    if (useMocks) builder.addInterceptor(DemoCollectionsInterceptor(context, userId))
}
