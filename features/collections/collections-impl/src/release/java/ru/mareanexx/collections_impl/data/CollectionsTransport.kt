package ru.mareanexx.collections_impl.data

import android.content.Context
import okhttp3.OkHttpClient

@Suppress("UNUSED_PARAMETER")
fun configureCollectionsTransport(context: Context, builder: OkHttpClient.Builder, useMocks: Boolean, userId: String) {
    require(!useMocks) { "Демонстрационный сервер недоступен в release" }
}
