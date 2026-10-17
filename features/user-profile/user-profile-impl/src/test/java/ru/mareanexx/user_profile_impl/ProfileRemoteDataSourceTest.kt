package ru.mareanexx.user_profile_impl

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_impl.data.remote.api.ProfileApi
import ru.mareanexx.user_profile_impl.data.remote.mapper.ProfileErrorMapper
import ru.mareanexx.user_profile_impl.data.remote.source.ProfileRemoteDataSource

class ProfileRemoteDataSourceTest {
    private val server = MockWebServer()
    private lateinit var source: ProfileRemoteDataSource
    private val profile = """{"id":"1","full_name":"Мария Химик","username":"chemist","description":"Привет","avatar_url":null,"statistics":{"books_read":24,"notes_published":41,"tests_passed":8}}"""
    @Before fun setup() {
        server.start()
        val api = Retrofit.Builder().baseUrl(server.url("/")).addConverterFactory(GsonConverterFactory.create()).build().create(ProfileApi::class.java)
        source = ProfileRemoteDataSource(api, ProfileErrorMapper(Gson()))
    }
    @After fun cleanup() { server.shutdown() }

    @Test fun `профиль запрашивается по согласованному адресу`() = runBlocking {
        server.enqueue(MockResponse().setBody(profile))
        val value = (source.getProfile() as ProfileResult.Success).value
        assertEquals("/api/v1/users/me", server.takeRequest().path)
        assertEquals(24, value.statistics.readBooksNumber)
        assertNull(value.avatarUrl)
    }

    @Test fun `изменение профиля передаёт только редактируемые поля`() = runBlocking {
        server.enqueue(MockResponse().setBody(profile))
        source.updateProfile(ProfileEditForm("Мария Химик", "chemist", "Привет"))
        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/api/v1/users/me", request.path)
        val json = Gson().fromJson(request.body.readUtf8(), Map::class.java)
        assertEquals(setOf("full_name", "username", "description"), json.keys)
        assertEquals("Мария Химик", json["full_name"])
    }

    @Test fun `фото загружается отдельной multipart ручкой`() = runBlocking {
        server.enqueue(MockResponse().setBody(profile))
        source.updateAvatar(AvatarUpload(byteArrayOf(10, 20, 30)))
        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/users/me/avatar", request.path)
        assertTrue(request.getHeader("Content-Type")!!.startsWith("multipart/form-data"))
        val body = request.body.readUtf8()
        assertTrue(body.contains("name=\"file\""))
        assertTrue(body.contains("image/jpeg"))
    }

    @Test fun `ошибки статусов не выдаются за успешное сохранение`() = runBlocking {
        for ((status, error) in mapOf(401 to ProfileError.Unauthorized, 409 to ProfileError.UsernameTaken, 413 to ProfileError.TooLarge,
            415 to ProfileError.InvalidImage, 429 to ProfileError.RateLimited, 503 to ProfileError.Unavailable, 504 to ProfileError.Timeout)) {
            server.enqueue(MockResponse().setResponseCode(status))
            assertEquals(error, (source.getProfile() as ProfileResult.Failure).reason.error)
        }
    }

    @Test fun `ошибки полей преобразуются без внутренних текстов сервера`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(422).setBody("""{"error":{"code":"VALIDATION_ERROR","field_errors":{"username":"USERNAME_TAKEN","description":"DESCRIPTION_TOO_LONG","secret":"private"}}}"""))
        val reason = (source.updateProfile(ProfileEditForm()) as ProfileResult.Failure).reason
        assertEquals(mapOf(ProfileField.Username to ProfileError.UsernameTaken, ProfileField.Description to ProfileError.DescriptionTooLong), reason.fields)
    }

    @Test fun `неполные и повреждённые данные не создают профиль`() = runBlocking {
        for (body in listOf("{}", "not json", profile.replace("24", "-1"))) {
            server.enqueue(MockResponse().setBody(body))
            assertEquals(ProfileError.InvalidResponse, (source.getProfile() as ProfileResult.Failure).reason.error)
        }
    }

    @Test fun `достижения проверяют прогресс и дату получения`() = runBlocking {
        val item = """{"id":"book","title":"Книжный химик","description":"Прочитай 10 книг","category":"reading","art":"blue","current":3,"target":10,"unit":"книг"}"""
        server.enqueue(MockResponse().setBody("""{"items":[$item]}"""))
        val value = (source.getAchievements() as ProfileResult.Success).value.single()
        assertEquals(7, value.remaining)
        assertFalse(value.isUnlocked)
        server.enqueue(MockResponse().setBody("""{"items":[${item.dropLast(1)},"unlocked_at":"2026-10-12"}]}"""))
        assertTrue(source.getAchievements() is ProfileResult.Failure)
    }

    @Test fun `настройки проверяют диапазон и обязательные значения`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"text_scale":1.1,"reduce_motion":true,"keep_screen_on":false,"haptic_feedback":true}"""))
        assertEquals(1.1f, (source.getSettings() as ProfileResult.Success).value.textScale)
        server.enqueue(MockResponse().setBody("""{"text_scale":8,"reduce_motion":true,"keep_screen_on":false,"haptic_feedback":true}"""))
        assertTrue(source.getSettings() is ProfileResult.Failure)
        server.enqueue(MockResponse().setBody("{}"))
        assertTrue(source.getSettings() is ProfileResult.Failure)
    }
}
