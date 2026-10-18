package ru.mareanexx.elementa.di

import ru.mareanexx.collections_api.domain.repository.CollectionsRepository
import ru.mareanexx.collections_impl.data.CollectionsRepositoryFactory
import ru.mareanexx.collections_impl.data.configureCollectionsTransport
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.book_impl.data.BookRepositoryFactory
import ru.mareanexx.book_impl.data.configureBookTransport
import ru.mareanexx.user_library_api.domain.repository.LibraryRepository
import ru.mareanexx.user_library_impl.data.LibraryRepositoryFactory
import ru.mareanexx.user_library_impl.data.configureLibraryTransport
import ru.mareanexx.catalog_api.domain.repository.CatalogRepository
import ru.mareanexx.catalog_impl.data.CatalogRepositoryFactory
import ru.mareanexx.catalog_impl.data.configureCatalogTransport
import kotlinx.coroutines.flow.first
import ru.mareanexx.reader_api.domain.repository.ReaderRepository
import ru.mareanexx.reader_impl.data.ReaderRepositoryFactory
import ru.mareanexx.reader_impl.data.configureReaderTransport
import android.app.Application
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanex.auth_impl.data.remote.api.AuthApi
import ru.mareanex.auth_impl.data.AuthRepositoryFactory
import ru.mareanex.auth_impl.data.configureAuthTransport
import ru.mareanex.auth_impl.data.updateDemoAccount
import ru.mareanexx.auth_api.domain.model.AuthUser
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository
import ru.mareanexx.user_profile_impl.data.ProfileRepositoryFactory
import ru.mareanexx.user_profile_impl.data.configureProfileTransport
import ru.mareanexx.elementa.di.network.SessionAuthorizationInterceptor
import ru.mareanexx.auth_api.domain.repository.AuthRepository
import ru.mareanexx.datastore.SecureSessionStore
import ru.mareanexx.datastore.crypto.AndroidKeystoreSessionCipher
import ru.mareanexx.elementa.BuildConfig
import java.util.concurrent.TimeUnit

/** Собирает зависимости уровня приложения. Остальные слои получают доменные контракты. */
class AppContainer(
    private val application: Application,
) {
    private val sessionStore by lazy { SecureSessionStore(application, AndroidKeystoreSessionCipher()) }

    private val readerRepositories = mutableMapOf<String, ReaderRepository>()

    @Synchronized
    fun readerRepository(user: AuthUser): ReaderRepository = readerRepositories.getOrPut(user.id) {
        val api = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS).retryOnConnectionFailure(false)
            .addInterceptor(SessionAuthorizationInterceptor(AuthRepositoryFactory.tokenProvider(sessionStore, user.id)))
        // Файл приходит по ограниченной ссылке; bearer-токен никогда не передаётся хранилищу файлов.
        val content = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(45, TimeUnit.SECONDS)
            .callTimeout(0, TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false)
        configureReaderTransport(application, api, content, BuildConfig.MOCK_AUTH, user.id)
        ReaderRepositoryFactory.create(application, user.id, api.build(), content.build(), BuildConfig.AUTH_BASE_URL)
    }

    suspend fun readerRepositoryForWorker(userId: String): ReaderRepository? {
        val user = authRepository.session.first()?.user?.takeIf { it.id == userId } ?: return null
        return readerRepository(user)
    }

    private val catalogRepositories = mutableMapOf<String, CatalogRepository>()

    @Synchronized
    fun catalogRepository(user: AuthUser): CatalogRepository = catalogRepositories.getOrPut(user.id) {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor(SessionAuthorizationInterceptor(AuthRepositoryFactory.tokenProvider(sessionStore)))
            .also { configureCatalogTransport(application, it, BuildConfig.MOCK_AUTH, user.id) }
            .build()
        CatalogRepositoryFactory.create(application, user.id, client, BuildConfig.AUTH_BASE_URL)
    }

    private val libraryRepositories = mutableMapOf<String, LibraryRepository>()

    @Synchronized
    fun libraryRepository(user: AuthUser): LibraryRepository = libraryRepositories.getOrPut(user.id) {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor(SessionAuthorizationInterceptor(AuthRepositoryFactory.tokenProvider(sessionStore)))
            .also { configureLibraryTransport(application, it, BuildConfig.MOCK_AUTH, user.id) }
            .build()
        LibraryRepositoryFactory.create(application, user.id, client, BuildConfig.AUTH_BASE_URL, readerRepository(user))
    }

    private val bookRepositories = mutableMapOf<String, BookRepository>()

    @Synchronized
    fun bookRepository(user: AuthUser): BookRepository = bookRepositories.getOrPut(user.id) {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor(SessionAuthorizationInterceptor(AuthRepositoryFactory.tokenProvider(sessionStore)))
            .also { configureBookTransport(application, it, BuildConfig.MOCK_AUTH, user.id, user.fullName) }.build()
        ReaderAwareBookRepository(BookRepositoryFactory.create(client, BuildConfig.AUTH_BASE_URL), readerRepository(user))
    }

    fun profileRepository(user: AuthUser): ProfileRepository {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor(SessionAuthorizationInterceptor(AuthRepositoryFactory.tokenProvider(sessionStore)))
            .also { builder ->
                configureProfileTransport(application, builder, BuildConfig.MOCK_AUTH, user.id, user.fullName, user.username) { id, name, username ->
                    updateDemoAccount(application, id, name, username)
                }
            }
            .build()
        return ProfileRepositoryFactory.create(application, user.id, client, BuildConfig.AUTH_BASE_URL)
    }

    fun collectionsRepository(user: AuthUser): CollectionsRepository {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor(SessionAuthorizationInterceptor(AuthRepositoryFactory.tokenProvider(sessionStore)))
            .also { configureCollectionsTransport(application, it, BuildConfig.MOCK_AUTH, user.id) }
            .build()
        return CollectionsRepositoryFactory.create(application, user.id, client, BuildConfig.AUTH_BASE_URL)
    }

    val authRepository: AuthRepository by lazy {
        val gson = Gson()
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            // Регистрация не должна повторяться автоматически после сетевого сбоя.
            .retryOnConnectionFailure(false)
            .also { configureAuthTransport(application, it, BuildConfig.MOCK_AUTH) }
            .build()
        val api = Retrofit.Builder().baseUrl(BuildConfig.AUTH_BASE_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create(gson)).build().create(AuthApi::class.java)
        AuthRepositoryFactory.create(api, sessionStore, gson)
    }
}
