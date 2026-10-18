package ru.mareanexx.reader_impl.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.reader_api.domain.model.BookTransfer
import ru.mareanexx.reader_api.domain.model.TransferPhase
import ru.mareanexx.reader_impl.R
import ru.mareanexx.ui.theme.*

@Composable
fun BookDownloadMenu(
    expanded: Boolean,
    transfer: BookTransfer?,
    downloaded: Boolean,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onRemove: () -> Unit,
    onRetry: () -> Unit,
) {
    val active = transfer?.active == true
    DropdownMenu(
        expanded,
        onDismiss,
        Modifier.widthIn(min = 260.dp, max = 300.dp).testTag("book_download_menu"),
        shape = RoundedCornerShape(24.dp),
        containerColor = backgroundSecondary,
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(painterResource(if (downloaded) R.drawable.reader_check else R.drawable.reader_download), null, Modifier.size(20.dp), tint = if (downloaded) Color(0xFF4F806B) else bodySecondary)
                Text(
                    stringResource(when { downloaded -> R.string.reader_downloaded; active && transfer?.phase == TransferPhase.Queued -> R.string.reader_download_wait; active -> R.string.reader_download_running; else -> R.string.reader_download_error }, ((transfer?.progress ?: 0f) * 100).toInt()),
                    fontFamily = ExtraBoldNunito, fontSize = 15.sp, lineHeight = 20.sp, color = titleMain,
                )
            }
            Text(
                if (downloaded) stringResource(R.string.reader_download_local_hint) else transfer?.error?.let { readerErrorText(it) }.orEmpty(),
                fontFamily = NormalNunito, fontSize = 13.sp, lineHeight = 18.sp, color = authorsText,
            )
        }
        HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = titleMain.copy(alpha = .08f))
        DropdownMenuItem(
            modifier = Modifier.padding(horizontal = 6.dp).testTag(if (downloaded) "book_download_remove" else "book_download_retry"),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(when { active -> R.string.reader_cancel_download; downloaded -> R.string.reader_download_remove; else -> R.string.reader_download_retry }), fontFamily = SemiBoldNunito, fontSize = 15.sp, lineHeight = 20.sp)
                    if (downloaded) Text(stringResource(R.string.reader_download_remove_hint), fontFamily = NormalNunito, fontSize = 12.sp, lineHeight = 17.sp, color = authorsText)
                }
            },
            leadingIcon = { Icon(painterResource(if (active || downloaded) R.drawable.reader_delete else R.drawable.reader_download), null, Modifier.size(22.dp)) },
            onClick = { onDismiss(); when { active -> onCancel(); downloaded -> onRemove(); else -> onRetry() } },
            colors = MenuDefaults.itemColors(textColor = if (active || downloaded) Color(0xFFAC586B) else titleMain, leadingIconColor = if (active || downloaded) Color(0xFFAC586B) else bodySecondary),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        )
    }
}
