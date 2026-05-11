package ru.mareanexx.ui.modifiers

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.theme.outerShadow
import ru.mareanexx.ui.theme.titleMain

/**
 * Тень для блоков
 */
fun Modifier.elementaShadow(
    shape: Shape = RoundedCornerShape(16.dp),
): Modifier {
    return this.shadow(
        elevation = 4.dp,
        shape = shape,
        ambientColor = outerShadow, spotColor = titleMain.copy(alpha = 0.6f),
    )
}
