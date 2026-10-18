package ru.mareanexx.reader_impl.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.reader_api.domain.model.ReadingMode
import ru.mareanexx.reader_impl.R
import ru.mareanexx.reader_impl.presentation.model.ReaderState
import ru.mareanexx.common_ui.R as CommonR

private val ChromeColor = Color(0xFF48474C)
private val ReaderButtonSize = 51.dp

@Composable
fun ReaderHeader(
    state: ReaderState,
    palette: ReaderPalette,
    closing: Boolean,
    onClose: () -> Unit,
) {
    Box(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(palette.paper.copy(alpha = .94f), Color.Transparent)))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(ReaderButtonSize)) {
            // Одинаковый резерв с двух сторон сохраняет центр экрана при кнопке только справа.
            Surface(
                modifier = Modifier.align(Alignment.Center).widthIn(max = (maxWidth - (ReaderButtonSize + 10.dp) * 2).coerceAtLeast(0.dp))
                    .height(ReaderButtonSize).testTag("reader_heading"),
                color = ChromeColor,
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = .08f)),
                shadowElevation = 2.dp,
            ) {
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        state.document?.book?.title ?: state.transfer?.title?.takeIf(String::isNotBlank) ?: stringResource(R.string.reader_loading),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        lineHeight = 18.sp,
                    )
                    if (state.chapter.isNotBlank()) Text(
                        state.chapter,
                        color = Color(0xFFDDDCE1),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                    )
                }
            }
            ReaderChromeButton(
                CommonR.drawable.close_icon,
                R.string.reader_close,
                Modifier.align(Alignment.CenterEnd).testTag("reader_close"),
                enabled = !closing,
                onClick = onClose,
            )
        }
    }
}

@Composable
fun ReaderFooter(
    state: ReaderState,
    palette: ReaderPalette,
    onStep: (Int) -> Unit,
    onMenu: () -> Unit,
) {
    BoxWithConstraints(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, palette.paper.copy(alpha = .94f))))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp),
    ) {
        val paged = state.settings.mode == ReadingMode.Pages
        val narrow = maxWidth < 328.dp && paged
        Box(Modifier.fillMaxWidth().height(if (narrow) ReaderButtonSize * 2 + 8.dp else ReaderButtonSize)) {
            // Счётчик не участвует в строке с меню: его центр всегда совпадает с центром страницы.
            Row(
                Modifier.align(if (narrow) Alignment.TopCenter else Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (paged) ReaderChromeButton(
                    CommonR.drawable.arrow_back_icon,
                    R.string.reader_previous,
                    Modifier.testTag("reader_page_previous"),
                    enabled = state.page > 1,
                ) { onStep(-1) }
                if (state.settings.showPageNumber) Surface(
                    color = ChromeColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.widthIn(min = 95.dp).heightIn(min = 30.dp).testTag("reader_page_count"),
                ) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                        if (state.completed) Icon(painterResource(R.drawable.reader_check), stringResource(R.string.reader_completed_title), Modifier.size(14.dp), tint = Color.White)
                        Text(
                            stringResource(R.string.reader_page, state.page, state.pages),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                            lineHeight = 20.sp,
                        )
                    }
                }
                if (paged) ReaderChromeButton(
                    CommonR.drawable.keyboard_arrow_right_icon,
                    R.string.reader_next,
                    Modifier.testTag("reader_page_next"),
                    enabled = state.page < state.pages,
                ) { onStep(1) }
            }
            ReaderChromeButton(
                R.drawable.reader_menu,
                R.string.reader_menu,
                Modifier.align(Alignment.BottomEnd).testTag("reader_settings"),
                iconSize = 31.dp,
                onClick = onMenu,
            )
        }
    }
}

@Composable
private fun ReaderChromeButton(
    icon: Int,
    description: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick,
        modifier.size(ReaderButtonSize),
        enabled = enabled,
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = ChromeColor,
            contentColor = Color.White,
            disabledContainerColor = ChromeColor.copy(alpha = .5f),
            disabledContentColor = Color.White.copy(alpha = .5f),
        ),
    ) {
        Icon(painterResource(icon), stringResource(description), Modifier.size(iconSize))
    }
}
