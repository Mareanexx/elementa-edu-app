package ru.mareanexx.user_library_impl.data

import android.content.Context
import ru.mareanexx.reader_api.domain.repository.ReaderRepository
import ru.mareanexx.user_library_impl.data.local.DownloadedLibrarySource
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanexx.user_library_api.domain.repository.LibraryRepository
import ru.mareanexx.user_library_impl.data.local.LibrarySearchHistory
import ru.mareanexx.user_library_impl.data.remote.api.LibraryApi
import ru.mareanexx.user_library_impl.data.remote.source.LibraryRemoteDataSource
import ru.mareanexx.user_library_impl.data.repository.DefaultLibraryRepository

object LibraryRepositoryFactory {
    fun create(context: Context, userId: String, client: OkHttpClient, baseUrl: String, reader: ReaderRepository): LibraryRepository {
        val api = Retrofit.Builder().baseUrl(baseUrl).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(LibraryApi::class.java)
        return DefaultLibraryRepository(LibraryRemoteDataSource(api), LibrarySearchHistory(context, userId), DownloadedLibrarySource(reader))
    }
}
