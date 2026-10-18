package ru.mareanexx.book_impl.data

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.book_impl.data.remote.api.BookApi
import ru.mareanexx.book_impl.data.remote.source.BookRemoteDataSource
import ru.mareanexx.book_impl.data.repository.DefaultBookRepository

object BookRepositoryFactory {
    fun create(client: OkHttpClient, baseUrl: String): BookRepository {
        val api = Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create())).build().create(BookApi::class.java)
        return DefaultBookRepository(BookRemoteDataSource(api))
    }
}
