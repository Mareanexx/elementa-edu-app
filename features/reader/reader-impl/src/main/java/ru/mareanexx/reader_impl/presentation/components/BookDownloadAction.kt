package ru.mareanexx.reader_impl.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_api.domain.repository.ReaderRepository
import ru.mareanexx.reader_impl.R
import ru.mareanexx.ui.theme.*

@Composable
fun BookDownloadAction(bookId: String, repository: ReaderRepository) {
    val transfers by repository.transfers.collectAsStateWithLifecycle(initialValue = emptyList())
    val transfer = transfers.firstOrNull { it.bookId == bookId }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var menu by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<ReaderError?>(null) }
    val downloaded = transfer?.phase == TransferPhase.Ready && transfer.pinned
    val active = transfer?.active == true
    val actionLabel = stringResource(if (downloaded) R.string.reader_downloaded else R.string.reader_download)
    val progressLabel = if (active) stringResource(if (transfer?.phase == TransferPhase.Queued) R.string.reader_download_wait else R.string.reader_download_running, ((transfer?.progress ?: 0f) * 100).toInt()) else actionLabel
    fun download() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        scope.launch { val result = repository.requestDownload(bookId); if (result is ReaderResult.Failure) failure = result.error }
    }
    Box {
        FilledTonalIconButton({ if (active || downloaded || transfer?.phase == TransferPhase.Failed) menu = true else download() }, Modifier.size(48.dp).testTag("book_download").semantics { contentDescription = actionLabel; stateDescription = progressLabel },
            shape = RoundedCornerShape(16.dp), colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = backgroundSecondary, contentColor = bodySecondary)) {
            if (active && transfer?.phase == TransferPhase.Queued) CircularProgressIndicator(Modifier.size(24.dp), color = bodySecondary, strokeWidth = 2.dp)
            else if (active) CircularProgressIndicator(progress = { if (transfer?.phase == TransferPhase.Preparing) 1f else transfer?.progress?.toFloat() ?: 0f }, Modifier.size(24.dp), color = bodySecondary, strokeWidth = 2.dp)
            else Icon(painterResource(if (downloaded) R.drawable.reader_check else R.drawable.reader_download), stringResource(if (downloaded) R.string.reader_downloaded else R.string.reader_download), Modifier.size(24.dp))
        }
        BookDownloadMenu(
            menu, transfer, downloaded,
            onDismiss = { menu = false },
            onCancel = { scope.launch { val result = repository.cancelDownload(bookId); if (result is ReaderResult.Failure) failure = result.error } },
            onRemove = { scope.launch { val result = repository.removeDownload(bookId); if (result is ReaderResult.Failure) failure = result.error } },
            onRetry = ::download,
        )
    }
    failure?.let { error -> AlertDialog(onDismissRequest = { failure = null }, title = { Text(stringResource(R.string.reader_download_error)) }, text = { Text(readerErrorText(error)) }, confirmButton = { TextButton({ failure = null }) { Text(stringResource(R.string.reader_close)) } }) }
}
