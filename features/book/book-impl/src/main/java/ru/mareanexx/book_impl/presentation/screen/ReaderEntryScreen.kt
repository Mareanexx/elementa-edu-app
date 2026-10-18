package ru.mareanexx.book_impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.mareanexx.book_api.domain.model.BookDetails
import ru.mareanexx.book_impl.R
import ru.mareanexx.book_impl.presentation.components.*
import ru.mareanexx.ui.theme.*

/** Точка входа подготовлена к подключению модуля ридера; прогресс здесь не изменяется. */
@Composable
internal fun ReaderEntryScreen(book: BookDetails, returnToLibrary: Boolean = false, onBack: () -> Unit) {
    val strings = LocalResources.current
    val page = if (book.pagesRead >= book.pages) 1 else (book.pagesRead + 1).coerceAtMost(book.pages)
    Column(Modifier.fillMaxSize().background(backgroundMain).testTag("book_reader_entry")) {
        BookTopBar(strings.getString(R.string.book_reader_title), onBack)
        Column(Modifier.weight(1f).fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            BookCover(book.images.firstOrNull()?.url, book.title, Modifier.width(112.dp).height(168.dp))
            Text(book.title, Modifier.padding(top = 20.dp), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, color = titleMain)
            Text(strings.getString(R.string.book_reader_heading), Modifier.padding(top = 28.dp), style = MaterialTheme.typography.titleSmall, color = titleMain)
            Text(strings.getString(R.string.book_reader_hint), Modifier.padding(top = 10.dp), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, color = username)
            Text(strings.getString(R.string.book_reader_page, page), Modifier.padding(top = 12.dp), style = MaterialTheme.typography.labelSmall, color = bodySecondary)
        }
        BookPrimaryButton(strings.getString(if (returnToLibrary) R.string.book_reader_library else R.string.book_reader_back), onBack, Modifier.fillMaxWidth().padding(16.dp))
    }
}
