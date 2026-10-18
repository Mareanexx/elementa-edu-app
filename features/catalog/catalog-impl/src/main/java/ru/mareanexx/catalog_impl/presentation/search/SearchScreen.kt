package ru.mareanexx.catalog_impl.presentation.search

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_api.domain.validation.CatalogSearchRules
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.catalog_impl.presentation.components.*
import ru.mareanexx.catalog_impl.presentation.model.SearchUiState
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SearchScreen(state: SearchUiState, intent: (SearchIntent) -> Unit, onBack: () -> Unit, onBook: (String) -> Unit, onMenu: (CatalogBook) -> Unit) {
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val list = rememberLazyListState()
    val latestMore by rememberUpdatedState({ intent(SearchIntent.More) })
    val query = CatalogSearchRules.normalize(state.query)
    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(query) { list.scrollToItem(0) }
    LaunchedEffect(list, state.page.nextCursor, state.page.loadingMore, state.page.appendFailed, state.page.items.size) {
        if (state.page.nextCursor != null && !state.page.loadingMore && !state.page.appendFailed) snapshotFlow { list.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }.distinctUntilChanged().collect { if (it >= state.page.items.size - 2) latestMore() }
    }
    Column(Modifier.fillMaxSize().imePadding().testTag("catalog_search_screen")) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton({ keyboard?.hide(); onBack() }) { Icon(painterResource(CommonR.drawable.arrow_back_icon), stringResource(R.string.catalog_back), tint = titleMain) }
            TextField(state.query, { intent(SearchIntent.Edit(it)) }, Modifier.weight(1f).focusRequester(focus).testTag("catalog_search_input"),
                placeholder = { Text(stringResource(R.string.search_hint), color = searchHint, fontFamily = SemiBoldNunito, fontSize = 13.sp) },
                leadingIcon = { Icon(painterResource(R.drawable.catalog_search), null, tint = searchHint, modifier = Modifier.size(21.dp)) },
                trailingIcon = { if (state.query.isNotEmpty()) IconButton({ intent(SearchIntent.Edit("")) }) { Icon(painterResource(CommonR.drawable.close_icon), stringResource(R.string.search_clear), tint = searchHint) } },
                shape = RoundedCornerShape(16.dp), singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(fontFamily = SemiBoldNunito, fontSize = 16.sp, color = titleMain),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false),
                keyboardActions = KeyboardActions(onSearch = { intent(SearchIntent.Submit); keyboard?.hide() }),
                colors = TextFieldDefaults.colors(focusedContainerColor = authFieldBackground, unfocusedContainerColor = authFieldBackground, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = bodySecondary))
        }
        LazyColumn(Modifier.fillMaxSize().testTag("catalog_search_results"), state = list, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.historyError?.let { error -> item { Text(errorText(error), color = Color(0xFF9D4F62), style = MaterialTheme.typography.labelMedium) } }
            if (query.isEmpty()) {
                item { CatalogEmpty(stringResource(R.string.search_zero_title), stringResource(R.string.search_zero_body), Modifier.padding(top = 12.dp), R.drawable.catalog_search) }
                if (state.history.isNotEmpty()) {
                    item {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.search_recent), Modifier.weight(1f), color = titleMain, style = MaterialTheme.typography.titleSmall)
                            TextButton({ intent(SearchIntent.ClearHistory) }) { Text(stringResource(R.string.search_clear_history), color = bodySecondary, fontFamily = BoldNunito, fontSize = 11.sp) }
                        }
                    }
                    items(state.history, key = { "history_$it" }) { value ->
                        Row(Modifier.fillMaxWidth().catalogClick { intent(SearchIntent.ChooseQuery(value)); keyboard?.hide() }.padding(start = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(painterResource(R.drawable.catalog_history), null, Modifier.size(19.dp), tint = username)
                            Text(value, Modifier.weight(1f), color = titleMain, style = MaterialTheme.typography.labelMedium)
                            IconButton({ intent(SearchIntent.RemoveQuery(value)) }) { Icon(painterResource(CommonR.drawable.close_icon), stringResource(R.string.search_remove_query, value), tint = username, modifier = Modifier.size(18.dp)) }
                        }
                    }
                }
                state.suggestionError?.let { error -> item { CatalogErrorBanner(error, { intent(SearchIntent.Retry) }) } }
                if (state.suggestions.isNotEmpty()) item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.search_popular), color = titleMain, style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.suggestions.forEach { value -> Surface(color = backgroundSecondary, shape = RoundedCornerShape(14.dp)) {
                                Text(value, Modifier.catalogClick { intent(SearchIntent.ChooseQuery(value)); keyboard?.hide() }.padding(horizontal = 14.dp, vertical = 12.dp), color = bodySecondary, fontFamily = BoldNunito, fontSize = 13.sp)
                            } }
                        }
                    }
                }
            } else if (!CatalogSearchRules.valid(query)) item { Text(stringResource(R.string.search_short), Modifier.padding(vertical = 20.dp), color = username, style = MaterialTheme.typography.labelMedium) }
            else {
                state.page.error?.let { error -> item { CatalogErrorBanner(error, { intent(SearchIntent.Retry) }) } }
                if (state.page.loading) item { CatalogSkeleton() }
                if (state.page.loaded) {
                    item { Text(stringResource(R.string.search_results, state.page.total), Modifier.padding(vertical = 8.dp), color = username, style = MaterialTheme.typography.labelMedium) }
                    if (state.page.items.isEmpty()) item { CatalogEmpty(stringResource(R.string.search_empty_title), stringResource(R.string.search_empty_body), icon = R.drawable.catalog_search) }
                    items(state.page.items, key = { it.id }) { book -> CatalogBookRow(book, { intent(SearchIntent.Remember); keyboard?.hide(); onBook(book.id) }, { keyboard?.hide(); onMenu(book) }) }
                }
                if (state.page.loadingMore) item { Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(22.dp), color = bodySecondary, strokeWidth = 2.dp) } }
            }
        }
    }
}
