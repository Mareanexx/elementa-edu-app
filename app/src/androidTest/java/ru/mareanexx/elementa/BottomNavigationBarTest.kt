package ru.mareanexx.elementa

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import ru.mareanexx.ui.components.navigation_bar.BottomNavBar
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs
import ru.mareanexx.ui.theme.*

class BottomNavigationBarTest {
    @get:Rule val rule = createComposeRule()
    private var clicks = 0
    private val tabRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
    private fun tab(title: String) = rule.onNode(hasText(title) and tabRole)
    private fun icon(title: String) = rule.onNodeWithContentDescription(title, useUnmergedTree = true)
    private fun indicator() = rule.onNodeWithTag("bottom_navigation_indicator", useUnmergedTree = true)

    private fun show(reduceMotion: Boolean = false) {
        rule.setContent {
            var selected by remember { mutableStateOf(MainNavigationTabs.Home) }
            ElementaAppTheme {
                CompositionLocalProvider(LocalAppUiPreferences provides AppUiPreferences(reduceMotion = reduceMotion, hapticFeedback = false)) {
                    Box(Modifier.fillMaxSize().background(backgroundMain).safeDrawingPadding(), contentAlignment = Alignment.BottomCenter) {
                        BottomNavBar(selected) { selected = it; clicks++ }
                    }
                }
            }
        }
    }

    @Test fun movingIndicatorDoesNotMoveIconsAndRapidSwitchesKeepOneSelection() {
        show()
        val labels = listOf("Главная", "Библиотека", "Коллекции", "Профиль")
        rule.onAllNodes(tabRole).assertCountEquals(4)
        val original = labels.associateWith { icon(it).fetchSemanticsNode().boundsInRoot }
        labels.forEach {
            val bounds = tab(it).fetchSemanticsNode().boundsInRoot
            assertTrue(bounds.width >= with(rule.density) { 48.dp.toPx() })
            assertTrue(bounds.height >= with(rule.density) { 48.dp.toPx() })
        }
        val start = indicator().fetchSemanticsNode().boundsInRoot.center.x
        rule.mainClock.autoAdvance = false
        try {
            tab("Профиль").performClick()
            rule.waitForIdle()
            rule.mainClock.advanceTimeByFrame()
            rule.mainClock.advanceTimeBy(160)
            val middle = indicator().fetchSemanticsNode().boundsInRoot.center.x
            assertTrue(middle > start && middle < original.getValue("Профиль").center.x)
            labels.forEach { assertEquals(original[it], icon(it).fetchSemanticsNode().boundsInRoot) }
            tab("Библиотека").performClick()
            rule.mainClock.advanceTimeBy(64)
            tab("Коллекции").performClick()
            rule.mainClock.advanceTimeBy(1_000)
            rule.onAllNodes(tabRole and isSelected()).assertCountEquals(1)
            tab("Коллекции").assertIsSelected()
            assertEquals(original.getValue("Коллекции").center.x, indicator().fetchSemanticsNode().boundsInRoot.center.x, 1f)
            labels.forEach { assertEquals(original[it], icon(it).fetchSemanticsNode().boundsInRoot) }
            tab("Коллекции").performClick()
            assertEquals(3, clicks)
        } finally { rule.mainClock.autoAdvance = true }
        capture("bottom-navigation-collections")
    }

    @Test fun reducedMotionChangesSelectionImmediatelyAndFillsTheIcon() {
        show(reduceMotion = true)
        val before = icon("Профиль").captureToImage().asAndroidBitmap()
        assertEquals(navBackground.toArgb(), before.getPixel(before.width / 2, before.height / 3))
        rule.mainClock.autoAdvance = false
        try {
            tab("Профиль").performClick()
            rule.waitForIdle()
            rule.mainClock.advanceTimeByFrame()
            tab("Профиль").assertIsSelected()
            assertEquals(icon("Профиль").fetchSemanticsNode().boundsInRoot.center.x, indicator().fetchSemanticsNode().boundsInRoot.center.x, 1f)
            val filled = icon("Профиль").captureToImage().asAndroidBitmap()
            assertEquals(bodySecondary.toArgb(), filled.getPixel(filled.width / 2, filled.height / 3))
        } finally { rule.mainClock.autoAdvance = true }
        capture("bottom-navigation-profile")
    }

    private fun capture(name: String) {
        val bitmap = rule.onNodeWithTag("bottom_navigation").captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.filesDir, "qa/$name.png").apply { parentFile!!.mkdirs() }.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
