package ru.mareanexx.book_impl.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.mareanexx.book_impl.R
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookDescriptionSheet(description: String, onClose: () -> Unit) {
    val strings = LocalResources.current
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var closing by remember { mutableStateOf(false) }
    val height = LocalConfiguration.current.screenHeightDp.dp * .85f
    fun close() {
        if (!closing) {
            closing = true
            scope.launch { sheet.hide(); onClose() }
        }
    }
    // Скролл текста не передаёт панели жест вниз на верхней границе описания.
    // Саму панель можно потянуть за заголовок или ручку.
    val textScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
                Offset(0f, available.y.coerceAtLeast(0f))
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
                Velocity(0f, available.y.coerceAtLeast(0f))
        }
    }
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheet, containerColor = backgroundSecondary,
        dragHandle = { Box(Modifier.padding(top = 8.dp, bottom = 4.dp).size(32.dp, 4.dp).background(descriptionBackground, RoundedCornerShape(2.dp))) }) {
        Column(Modifier.fillMaxWidth().heightIn(max = height).testTag("book_description_sheet")) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(strings.getString(R.string.book_description), Modifier.weight(1f), style = SecondaryScreenTitle, color = titleMain)
                IconButton(::close, enabled = !closing) {
                    Icon(painterResource(CommonR.drawable.close_icon), strings.getString(R.string.book_close), tint = username)
                }
            }
            LazyColumn(Modifier.weight(1f, fill = false).fillMaxWidth().nestedScroll(textScroll).testTag("book_description_scroll"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
                item {
                    SelectionContainer {
                        Text(description.ifBlank { strings.getString(R.string.book_no_description) }, Modifier.testTag("book_full_description"),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp, lineHeight = 24.sp), color = titleMain)
                    }
                }
            }
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 16.dp)) {
                BookPrimaryButton(strings.getString(R.string.book_done), ::close, Modifier.fillMaxWidth(), enabled = !closing)
            }
        }
    }
}
