package ru.mareanexx.ui.components.skeleton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Заготовка в виде серого блока для скелетонов.
 * Имеет фиксированно заданную высоту и ширину
 */
@Composable
fun BoxSkeleton(
    height: Int,
    width: Int,
    shape: Shape = RoundedCornerShape(16.dp),
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = width.dp, height = height.dp)
            .background(color = Color.LightGray, shape = shape),
    )
}

/**
 * Заготовка в виде серого блока для скелетонов.
 * Размеры или что угодно задается модифаером
 */
@Composable
fun BoxSkeleton(
    shape: Shape = RoundedCornerShape(16.dp),
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(color = Color.LightGray, shape = shape))
}