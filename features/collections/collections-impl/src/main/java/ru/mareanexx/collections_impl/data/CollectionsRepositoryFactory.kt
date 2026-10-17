package ru.mareanexx.collections_impl.data

import com.google.gson.GsonBuilder
import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanexx.collections_api.domain.repository.CollectionsRepository
import ru.mareanexx.collections_impl.data.local.CollectionsPreferencesSource
import ru.mareanexx.collections_impl.data.remote.api.CollectionsApi
import ru.mareanexx.collections_impl.data.remote.source.CollectionsRemoteDataSource
import ru.mareanexx.collections_impl.data.repository.DefaultCollectionsRepository

object CollectionsRepositoryFactory {
    fun create(context: Context, userId: String, client: OkHttpClient, baseUrl: String): CollectionsRepository {
        val api = Retrofit.Builder().baseUrl(baseUrl).client(client).addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create())).build().create(CollectionsApi::class.java)
        return DefaultCollectionsRepository(CollectionsRemoteDataSource(api), CollectionsPreferencesSource(context, userId))
    }
}
