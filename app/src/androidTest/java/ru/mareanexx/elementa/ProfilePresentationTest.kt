package ru.mareanexx.elementa

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import java.io.File
import ru.mareanexx.ui.components.navigation_bar.BottomNavBar
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs
import ru.mareanexx.ui.theme.ElementaAppTheme
import ru.mareanexx.ui.theme.backgroundMain
import ru.mareanexx.user_profile_impl.presentation.achievements.AchievementsUiState
import ru.mareanexx.user_profile_impl.presentation.profile.ProfileUiState
import ru.mareanexx.user_profile_impl.presentation.screen.UserProfileScreen

class ProfilePresentationTest {
    @get:Rule val rule = createComposeRule()

    @Test fun initialLoadingShowsSkeletonWithoutSpinners() {
        rule.setContent {
            ElementaAppTheme {
                Surface(color = backgroundMain) {
                    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                        Box(Modifier.weight(1f)) {
                            UserProfileScreen(ProfileUiState(), AchievementsUiState(), {}, {}, {}, {}, {}, {})
                        }
                        BottomNavBar(MainNavigationTabs.Profile) {}
                    }
                }
            }
        }
        rule.onNodeWithTag("profile_skeleton").assertIsDisplayed()
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertCountEquals(0)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val file = File(instrumentation.targetContext.filesDir, "qa/profile-skeleton.png").apply { parentFile!!.mkdirs() }
        file.outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
