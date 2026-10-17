package ru.mareanexx.user_profile_impl.data

import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository
import ru.mareanexx.user_profile_impl.data.local.*
import ru.mareanexx.user_profile_impl.data.remote.api.ProfileApi
import ru.mareanexx.user_profile_impl.data.remote.mapper.ProfileErrorMapper
import ru.mareanexx.user_profile_impl.data.remote.source.ProfileRemoteDataSource
import ru.mareanexx.user_profile_impl.data.repository.DefaultProfileRepository

object ProfileRepositoryFactory {
    fun create(context: Context, userId: String, client: OkHttpClient, baseUrl: String): ProfileRepository {
        val gson = Gson()
        val api = Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(GsonConverterFactory.create(gson)).build().create(ProfileApi::class.java)
        return DefaultProfileRepository(
            ProfileRemoteDataSource(api, ProfileErrorMapper(gson)),
            SettingsLocalDataSource(context, userId, gson),
            ProfileImageCache(context),
        )
    }
}
