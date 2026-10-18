package ru.mareanexx.user_library_impl.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.user_library_impl.R
import ru.mareanexx.user_library_impl.presentation.LibraryIntent
import ru.mareanexx.user_library_impl.presentation.model.LibraryUiState
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

/** Поле сохраняет текст и плавно сжимается в кнопку в той же области тулбара. */
@Composable
internal fun LibraryHeader(state: LibraryUiState, collapse: Float, expand: () -> Unit, onIntent: (LibraryIntent) -> Unit) {
    val strings = LocalResources.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val requester = remember { FocusRequester() }
    var requestSearch by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    LaunchedEffect(collapse, requestSearch) {
        if (requestSearch && collapse < .05f) { requester.requestFocus(); keyboard?.show(); requestSearch = false }
    }
    BoxWithConstraints(Modifier.fillMaxWidth().height(128.dp - 64.dp * collapse)) {
        Row(Modifier.fillMaxWidth().height(60.dp).padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(strings.getString(R.string.library_screen_title), Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge,
                fontSize = 28.sp, color = titleMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(48.dp * collapse))
            Box {
                IconButton({ menu = true }, enabled = !state.mutating) {
                    Icon(painterResource(CommonR.drawable.more_vert_icon), strings.getString(R.string.library_menu), tint = titleMain)
                }
                DropdownMenu(menu, { menu = false }, shape = RoundedCornerShape(16.dp), containerColor = backgroundSecondary) {
                    DropdownMenuItem(text = { Text(strings.getString(R.string.library_sort)) }, leadingIcon = { Icon(painterResource(R.drawable.library_sort), null) },
                        onClick = { menu = false; onIntent(LibraryIntent.OpenSort) })
                    DropdownMenuItem(text = { Text(strings.getString(R.string.library_help)) }, leadingIcon = { Icon(painterResource(R.drawable.library_info), null) },
                        onClick = { menu = false; onIntent(LibraryIntent.OpenHelp) })
                }
            }
        }
        Surface(Modifier.align(Alignment.TopEnd).offset(x = -(16.dp + 40.dp * collapse), y = 64.dp - 58.dp * collapse)
            .width((maxWidth - 32.dp) * (1f - collapse) + 48.dp * collapse).height(56.dp - 8.dp * collapse).clipToBounds(),
            shape = RoundedCornerShape(16.dp), color = lerp(authFieldBackground, bodySecondary, collapse)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton({ expand(); requestSearch = true }, Modifier.size(48.dp).testTag("library_search_toggle"), enabled = !state.mutating) {
                    Icon(painterResource(R.drawable.library_search), strings.getString(R.string.library_search_open), Modifier.size(23.dp), tint = lerp(searchHint, Color.White, collapse))
                }
                if (collapse < .98f) {
                    BasicTextField(state.input, { onIntent(LibraryIntent.ChangeSearch(it)) },
                        Modifier.weight(1f).testTag("library_search").focusRequester(requester)
                            .onFocusChanged { onIntent(LibraryIntent.SearchFocus(it.isFocused)); if (it.isFocused) expand() }
                            .graphicsLayer { alpha = (1f - collapse * 1.6f).coerceAtLeast(0f) },
                        enabled = !state.mutating, singleLine = true, cursorBrush = SolidColor(bodySecondary),
                        textStyle = MaterialTheme.typography.labelMedium.copy(color = titleMain, fontFamily = NormalNunito),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); focus.clearFocus(); onIntent(LibraryIntent.SubmitSearch) }),
                        decorationBox = { input -> Box(Modifier.padding(vertical = 16.dp)) {
                            if (state.input.isEmpty()) Text(strings.getString(R.string.library_search_hint), style = MaterialTheme.typography.labelMedium,
                                color = searchHint, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            input()
                        } })
                    if (state.input.isNotEmpty()) IconButton({ onIntent(LibraryIntent.ClearSearch) }) {
                        Icon(painterResource(CommonR.drawable.close_icon), strings.getString(R.string.library_search_clear), Modifier.size(20.dp), tint = username)
                    } else Spacer(Modifier.width(12.dp))
                }
            }
        }
    }
}
