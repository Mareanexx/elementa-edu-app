package ru.mareanexx.user_profile_impl.presentation.screen.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.mareanexx.ui.theme.ElementaAppTheme
import ru.mareanexx.user_profile_impl.R
import ru.mareanexx.user_profile_impl.presentation.model.Achievement
import ru.mareanexx.user_profile_impl.presentation.model.ProfileAchievements
import ru.mareanexx.user_profile_impl.presentation.model.ProfileMainData
import ru.mareanexx.user_profile_impl.presentation.model.ProfileStatistics
import ru.mareanexx.user_profile_impl.presentation.model.UserProfile
import ru.mareanexx.user_profile_impl.presentation.screen.UserProfileScreen

@Preview
@Composable
private fun UserProfileScreenPreview() {
    ElementaAppTheme {
        UserProfileScreen(
            userData = UserProfile(
                mainData = ProfileMainData(
                    fullName = "Тамара Рончевская",
                    username = "@mareanexx",
                    avatarUrl = "mock_photo_url",
                    description = "Люблю учить химию через литературу и исследования",
                ),
                statistics = ProfileStatistics(
                    readBooksNumber = 24,
                    publishedNotesNumber = 41,
                    testsPassedNumber = 1,
                ),
                achievements = ProfileAchievements(
                    list = listOf(
                        Achievement(
                            image = R.drawable.red_achievement,
                            title = "Завершить 20 тестов",
                            receiptDate = "7 марта 2026",
                        ),
                        Achievement(
                            image = R.drawable.blue_achievement,
                            title = "Только светлая тема",
                            receiptDate = "8 марта 2026",
                        ),
                        Achievement(
                            image = R.drawable.orange_achievement,
                            title = "Читать каждый день 1 год",
                            receiptDate = "9 марта 2026",
                        ),
                        Achievement(
                            image = R.drawable.pink_achievement,
                            title = "Написать 50 отзывов",
                            receiptDate = "10 марта 2026",
                        ),
                    )
                ),
            ),
        )
    }
}
