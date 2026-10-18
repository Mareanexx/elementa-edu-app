package ru.mareanexx.catalog_impl.data

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanexx.catalog_api.domain.repository.CatalogRepository
import ru.mareanexx.catalog_impl.data.local.CatalogSearchHistory
import ru.mareanexx.catalog_impl.data.remote.api.CatalogApi
import ru.mareanexx.catalog_impl.data.remote.source.CatalogRemoteDataSource
import ru.mareanexx.catalog_impl.data.repository.DefaultCatalogRepository

object CatalogRepositoryFactory {
    fun create(context: Context, userId: String, client: OkHttpClient, baseUrl: String): CatalogRepository {
        val api = Retrofit.Builder().baseUrl(baseUrl).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(CatalogApi::class.java)
        return DefaultCatalogRepository(CatalogRemoteDataSource(api), CatalogSearchHistory(context, userId))
    }
}
