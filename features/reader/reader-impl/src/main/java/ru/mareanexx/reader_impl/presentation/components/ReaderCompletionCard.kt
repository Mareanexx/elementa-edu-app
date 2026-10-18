package ru.mareanexx.reader_impl.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.reader_impl.R
import ru.mareanexx.reader_impl.presentation.model.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
fun ReaderCompletionCard(
    state: ReaderState,
    accept: (ReaderIntent) -> Unit,
) {
    val palette = readerPalette(state.settings.theme)
    val finished = state.completion == ReaderCompletion.Finished
    val saving = state.completion == ReaderCompletion.Saving
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("reader_completion").semantics { liveRegion = LiveRegionMode.Polite },
        color = palette.surface,
        contentColor = palette.ink,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, palette.ink.copy(alpha = .08f)),
        shadowElevation = 4.dp,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = palette.ink.copy(alpha = .07f), shape = RoundedCornerShape(14.dp)) {
                    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(painterResource(if (finished) R.drawable.reader_check else CommonR.drawable.menu_book_icon), null, Modifier.size(23.dp))
                    }
                }
                Text(stringResource(if (finished) R.string.reader_completed_title else R.string.reader_end_title), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                IconButton({ accept(ReaderIntent.DismissCompletion) }, Modifier.size(36.dp), enabled = !saving) {
                    Icon(painterResource(CommonR.drawable.close_icon), stringResource(R.string.reader_completion_dismiss), Modifier.size(20.dp))
                }
            }
            Text(stringResource(if (finished) R.string.reader_completed_text else R.string.reader_end_text), color = palette.muted, style = MaterialTheme.typography.labelLarge, fontSize = 14.sp, lineHeight = 20.sp)
            state.completionError?.let { Text(readerErrorText(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (finished && state.annotations.isNotEmpty()) TextButton({
                    accept(ReaderIntent.DismissCompletion)
                    accept(ReaderIntent.Panel(ReaderPanel.Notes))
                }) { Text(stringResource(R.string.reader_completion_notes)) }
                Button(
                    { accept(if (finished) ReaderIntent.DismissCompletion else ReaderIntent.CompleteBook) },
                    Modifier.weight(1f).heightIn(min = 46.dp).testTag("reader_complete_book"),
                    enabled = !saving,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.ink, contentColor = palette.surface),
                ) {
                    if (saving) CircularProgressIndicator(Modifier.size(20.dp), color = palette.muted, strokeWidth = 2.dp)
                    else Text(stringResource(if (finished) R.string.reader_completion_done else R.string.reader_complete_book), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
