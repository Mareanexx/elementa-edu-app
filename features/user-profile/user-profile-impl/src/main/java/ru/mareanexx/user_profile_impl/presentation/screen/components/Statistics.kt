package ru.mareanexx.user_profile_impl.presentation.screen.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.theme.statisticsDivider
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING
import ru.mareanexx.user_profile_impl.presentation.model.ProfileStatistics
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun ProfileStatistics(
    statistics: ProfileStatistics,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HORIZONTAL_PADDING),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OneProfileStatistic(statistics.readBooksNumber, CommonUiR.string.books_read_number, CommonUiR.drawable.menu_book_icon)
        VerticalDivider(
            modifier = Modifier
                .padding(horizontal = 25.dp)
                .height(58.dp), color = statisticsDivider,
        )
        OneProfileStatistic(statistics.publishedNotesNumber, CommonUiR.string.notes_published_number, CommonUiR.drawable.stylus_note_icon)
        VerticalDivider(
            modifier = Modifier
                .padding(horizontal = 25.dp)
                .height(58.dp), color = statisticsDivider,
        )
        OneProfileStatistic(statistics.testsPassedNumber, CommonUiR.string.tests_passed_number, CommonUiR.drawable.trophy_icon)
    }
}

@Composable
private fun OneProfileStatistic(
    number: Int,
    @StringRes title: Int,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.widthIn(max = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            tint = titleMain,
            contentDescription = null,
            modifier = Modifier.size(46.dp),
        )
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = titleMain,
        )
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = titleMain,
        )
    }
}
