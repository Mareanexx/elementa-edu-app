package ru.mareanexx.reader_impl.data

import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanexx.reader_api.domain.repository.ReaderRepository
import ru.mareanexx.reader_impl.data.local.*
import ru.mareanexx.reader_impl.data.remote.api.ReaderApi
import ru.mareanexx.reader_impl.data.remote.source.*
import ru.mareanexx.reader_impl.data.repository.DefaultReaderRepository
import ru.mareanexx.reader_impl.data.work.ReaderWorkScheduler
import java.util.UUID

object ReaderRepositoryFactory {
    fun create(context: Context, userId: String, apiClient: OkHttpClient, contentClient: OkHttpClient, baseUrl: String): ReaderRepository {
        val preferences = context.getSharedPreferences("reader_installation", Context.MODE_PRIVATE)
        val deviceId = synchronized(this) {
            preferences.getString("device", null) ?: UUID.randomUUID().toString().also { preferences.edit().putString("device", it).commit() }
        }
        val gson = Gson()
        val api = Retrofit.Builder().baseUrl(baseUrl).client(apiClient).addConverterFactory(GsonConverterFactory.create(gson)).build().create(ReaderApi::class.java)
        return DefaultReaderRepository(context.applicationContext, ReaderLocalStore(ReaderDatabaseProvider.get(context), userId, gson),
            ReaderRemoteDataSource(api, baseUrl, deviceId), PublicationDownloader(contentClient), ReaderWorkScheduler(context, userId))
    }
}
