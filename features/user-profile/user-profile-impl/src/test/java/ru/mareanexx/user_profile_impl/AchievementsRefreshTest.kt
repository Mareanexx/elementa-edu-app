package ru.mareanexx.user_profile_impl

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_impl.presentation.achievements.*

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsRefreshTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }
    @Test fun `обновление сохраняет достижения без повторного скелетона`() = runTest(dispatcher) {
        val item = Achievement("a", "Начало", "Описание", AchievementCategory.Reading, AchievementArt.Blue, 1, 5, "книг", null)
        val repo = FakeProfileRepository().apply { achievements = listOf(item) }
        val model = AchievementsViewModel(repo)
        runCurrent()
        repo.achievementGate = CompletableDeferred()
        model.accept(AchievementsIntent.Reload)
        model.accept(AchievementsIntent.Reload)
        runCurrent()
        assertFalse(model.state.value.loading)
        assertTrue(model.state.value.refreshing)
        assertEquals(listOf(item), model.state.value.items)
        assertEquals(2, repo.achievementCalls)
        repo.achievementGate!!.complete(Unit)
        runCurrent()
        assertFalse(model.state.value.refreshing)
    }
    @Test fun `пустой загруженный список тоже обновляется без скелетона`() = runTest(dispatcher) {
        val repo = FakeProfileRepository()
        val model = AchievementsViewModel(repo)
        runCurrent()
        repo.achievementGate = CompletableDeferred()
        model.accept(AchievementsIntent.Reload)
        assertTrue(model.state.value.loaded)
        assertFalse(model.state.value.loading)
        repo.achievementGate!!.complete(Unit)
        runCurrent()
    }
    @Test fun `ошибка фонового обновления не удаляет полученные достижения`() = runTest(dispatcher) {
        val item = Achievement("a", "Начало", "Описание", AchievementCategory.Reading, AchievementArt.Blue, 1, 5, "книг", null)
        val repo = FakeProfileRepository().apply { achievements = listOf(item) }
        val model = AchievementsViewModel(repo)
        runCurrent()
        repo.achievementFailure = ProfileFailure(ProfileError.Offline)
        model.accept(AchievementsIntent.Reload)
        runCurrent()
        assertEquals(listOf(item), model.state.value.items)
        assertTrue(model.state.value.loaded)
        assertFalse(model.state.value.loading)
        assertFalse(model.state.value.refreshing)
    }
}
