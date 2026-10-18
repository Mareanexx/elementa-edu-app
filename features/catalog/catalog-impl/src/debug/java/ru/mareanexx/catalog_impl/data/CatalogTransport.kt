package ru.mareanexx.catalog_impl.data

import android.content.Context
import okhttp3.OkHttpClient
import ru.mareanexx.catalog_impl.data.mock.DemoCatalogInterceptor

fun configureCatalogTransport(context: Context, builder: OkHttpClient.Builder, mock: Boolean, userId: String) {
    if (mock) builder.addInterceptor(DemoCatalogInterceptor(context, userId))
}
