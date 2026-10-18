package ru.mareanexx.reader_impl.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_impl.R
import ru.mareanexx.reader_impl.presentation.model.*
import ru.mareanexx.ui.theme.SecondaryScreenTitle
import ru.mareanexx.common_ui.R as CommonR

@Composable
fun ReaderSettingsPopup(
    settings: ReaderSettings,
    accept: (ReaderIntent) -> Unit,
) {
    val palette = readerPalette(settings.theme)
    val smallerLabel = stringResource(R.string.reader_smaller)
    val largerLabel = stringResource(R.string.reader_larger)
    val density = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize
    val width = with(density) { (windowSize.width.toDp() - 32.dp).coerceAtMost(380.dp) }
    val maxHeight = with(density) { (windowSize.height.toDp() - 128.dp).coerceIn(200.dp, 680.dp) }
    val scroll = rememberScrollState()
    fun change(value: ReaderSettings) = accept(ReaderIntent.Settings(value))
    Popup(
        alignment = Alignment.BottomEnd,
        offset = with(density) { IntOffset(-16.dp.roundToPx(), -79.dp.roundToPx()) },
        onDismissRequest = { accept(ReaderIntent.Panel(ReaderPanel.None)) },
        properties = PopupProperties(focusable = true, dismissOnBackPress = true),
    ) {
        Surface(
            modifier = Modifier.width(width).heightIn(max = maxHeight).testTag("reader_settings_panel"),
            shape = RoundedCornerShape(28.dp),
            color = palette.surface,
            contentColor = palette.ink,
            border = BorderStroke(1.dp, palette.ink.copy(alpha = .06f)),
            shadowElevation = 5.dp,
        ) {
            Column {
                Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.reader_settings), Modifier.weight(1f), style = SecondaryScreenTitle)
                    IconButton({ accept(ReaderIntent.Panel(ReaderPanel.None)) }) {
                        Icon(painterResource(CommonR.drawable.close_icon), stringResource(R.string.reader_settings_close), Modifier.size(22.dp), tint = palette.ink)
                    }
                }
                HorizontalDivider(color = palette.ink.copy(alpha = .06f))
                Column(
                    Modifier.drawWithContent {
                        drawContent()
                        if (scroll.maxValue > 0) {
                            val thumb = (size.height * size.height / (size.height + scroll.maxValue)).coerceAtLeast(32.dp.toPx())
                            val top = (size.height - thumb) * scroll.value / scroll.maxValue
                            drawRoundRect(palette.ink.copy(alpha = .18f), Offset(size.width - 5.dp.toPx(), top), Size(2.dp.toPx(), thumb), CornerRadius(2.dp.toPx()))
                        }
                    }.verticalScroll(scroll).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ReaderSettingLabel(R.string.reader_theme, palette)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ReaderTheme.entries.forEach { theme ->
                                val selected = settings.theme == theme
                                val sample = readerPalette(theme)
                                val label = stringResource(themeText(theme))
                                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Surface(
                                        onClick = { change(settings.copy(theme = theme)) },
                                        modifier = Modifier.fillMaxWidth().height(60.dp).testTag("reader_theme_${theme.name}").semantics { contentDescription = label; this.selected = selected },
                                        shape = RoundedCornerShape(16.dp),
                                        color = sample.paper,
                                        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) palette.ink else palette.muted.copy(alpha = .2f)),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(stringResource(R.string.reader_type_sample), color = sample.ink, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontSize = 23.sp)
                                            if (selected) Icon(painterResource(R.drawable.reader_check), null, Modifier.align(Alignment.TopEnd).padding(5.dp).size(13.dp), tint = sample.ink)
                                        }
                                    }
                                    Text(label, color = if (selected) palette.ink else palette.muted, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ReaderSettingLabel(R.string.reader_font, palette)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ReaderFont.entries.forEach { font ->
                                Surface(
                                    onClick = { change(settings.copy(font = font)) },
                                    modifier = Modifier.weight(1f).semantics { selected = settings.font == font },
                                    color = if (settings.font == font) palette.ink.copy(alpha = .08f) else palette.surface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (settings.font == font) palette.ink.copy(alpha = .4f) else palette.ink.copy(alpha = .1f)),
                                ) {
                                    Text(stringResource(fontText(font)), Modifier.padding(horizontal = 4.dp, vertical = 12.dp), textAlign = TextAlign.Center, color = palette.ink, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Surface(color = palette.paper, shape = RoundedCornerShape(18.dp)) {
                            Row(Modifier.fillMaxWidth().padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    { change(settings.copy(fontSize = (settings.fontSize - 1).coerceAtLeast(14f))) },
                                    Modifier.width(64.dp).height(48.dp).testTag("reader_font_smaller").semantics { contentDescription = smallerLabel },
                                    enabled = settings.fontSize > 14f,
                                ) { Text(stringResource(R.string.reader_a_minus), fontSize = 24.sp, color = palette.ink) }
                                Text(stringResource(R.string.reader_font_size, settings.fontSize.toInt()), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                                TextButton(
                                    { change(settings.copy(fontSize = (settings.fontSize + 1).coerceAtMost(30f))) },
                                    Modifier.width(64.dp).height(48.dp).testTag("reader_font_larger").semantics { contentDescription = largerLabel },
                                    enabled = settings.fontSize < 30f,
                                ) { Text(stringResource(R.string.reader_a_plus), fontSize = 24.sp, color = palette.ink) }
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ReaderSettingLabel(R.string.reader_mode, palette)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ReadingMode.entries.forEach { mode ->
                                val selected = settings.mode == mode
                                Surface(
                                    onClick = { change(settings.copy(mode = mode)) },
                                    modifier = Modifier.weight(1f).testTag("reader_mode_${mode.name}").semantics { this.selected = selected },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selected) palette.ink.copy(alpha = .08f) else palette.surface,
                                    border = BorderStroke(1.dp, if (selected) palette.ink.copy(alpha = .4f) else palette.ink.copy(alpha = .1f)),
                                ) {
                                    Row(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(painterResource(if (mode == ReadingMode.Scroll) R.drawable.reader_scroll else R.drawable.reader_pages), null, Modifier.size(21.dp), tint = palette.ink)
                                        Text(stringResource(if (mode == ReadingMode.Scroll) R.string.reader_scroll else R.string.reader_pages), color = palette.ink, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                    Surface(color = palette.paper, shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            ReaderSettingSlider(R.string.reader_spacing, stringResource(R.string.reader_spacing_value, settings.lineHeight), settings.lineHeight, 1.3f..2f, 6, palette) { change(settings.copy(lineHeight = it)) }
                            Spacer(Modifier.height(8.dp))
                            ReaderSettingSlider(R.string.reader_margins, settings.margin.toString(), settings.margin.toFloat(), 12f..40f, 6, palette) { change(settings.copy(margin = it.toInt())) }
                        }
                    }
                    Column {
                        ReaderToggle(R.string.reader_justify, settings.justify, palette) { change(settings.copy(justify = it)) }
                        ReaderToggle(R.string.reader_formulas, settings.showFormulas, palette) { change(settings.copy(showFormulas = it)) }
                        ReaderToggle(R.string.reader_page_number, settings.showPageNumber, palette) { change(settings.copy(showPageNumber = it)) }
                        ReaderToggle(R.string.reader_keep_screen, settings.keepScreenOn, palette) { change(settings.copy(keepScreenOn = it)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderSettingLabel(label: Int, palette: ReaderPalette) {
    Text(stringResource(label), color = palette.muted, style = MaterialTheme.typography.labelMedium)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ReaderSettingSlider(
    label: Int,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    palette: ReaderPalette,
    change: (Float) -> Unit,
) {
    val description = stringResource(label)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(description, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = palette.ink)
        Text(valueLabel, style = MaterialTheme.typography.labelMedium, color = palette.muted)
    }
    Slider(
        value,
        change,
        modifier = Modifier.height(40.dp).semantics { contentDescription = description },
        valueRange = range,
        steps = steps,
        thumb = {
            Box(Modifier.size(20.dp).background(palette.surface, CircleShape).border(2.dp, palette.accent, CircleShape))
        },
        track = { state ->
            val progress = ((state.value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
            Box(Modifier.fillMaxWidth().height(4.dp).background(palette.ink.copy(alpha = .12f), CircleShape)) {
                Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(palette.accent, CircleShape))
            }
        },
    )
}

@Composable
private fun ReaderToggle(label: Int, checked: Boolean, palette: ReaderPalette, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(label), Modifier.weight(1f).padding(end = 16.dp), style = MaterialTheme.typography.labelMedium, color = palette.ink)
        Switch(checked, change, colors = SwitchDefaults.colors(checkedTrackColor = palette.accent, checkedThumbColor = palette.surface))
    }
}
