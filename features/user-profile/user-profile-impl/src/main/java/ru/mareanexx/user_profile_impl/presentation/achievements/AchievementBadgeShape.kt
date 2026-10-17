package ru.mareanexx.user_profile_impl.presentation.achievements

import androidx.compose.foundation.shape.GenericShape

/** Обрезает прозрачные углы исходных значков вместе с наложением для закрытых достижений. */
internal val AchievementBadgeShape = GenericShape { size, _ ->
    moveTo(size.width * 0.5f, 0f)
    lineTo(size.width * 0.94f, size.height * 0.25f)
    lineTo(size.width * 0.94f, size.height * 0.75f)
    lineTo(size.width * 0.5f, size.height)
    lineTo(size.width * 0.06f, size.height * 0.75f)
    lineTo(size.width * 0.06f, size.height * 0.25f)
    close()
}
