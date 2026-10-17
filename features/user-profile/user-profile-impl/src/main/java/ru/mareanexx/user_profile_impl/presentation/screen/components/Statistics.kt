package ru.mareanexx.user_profile_impl.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.ProfileStatistics
import ru.mareanexx.user_profile_impl.R
import ru.mareanexx.common_ui.R as CommonR

@Composable
fun ProfileStatistics(statistics: ProfileStatistics) {
    val strings = LocalResources.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(strings.getString(R.string.profile_statistics_title), style = MaterialTheme.typography.titleSmall, color = titleMain)
        Surface(Modifier.fillMaxWidth().padding(top = 12.dp), color = backgroundSecondary, shape = RoundedCornerShape(22.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Statistic(statistics.readBooksNumber, strings.getString(R.string.profile_stat_books), CommonR.drawable.menu_book_icon, Color(0xFF8270AF), Modifier.weight(1f))
                StatisticDivider()
                Statistic(statistics.publishedNotesNumber, strings.getString(R.string.profile_stat_notes), CommonR.drawable.stylus_note_icon, Color(0xFF4C8582), Modifier.weight(1f))
                StatisticDivider()
                Statistic(statistics.testsPassedNumber, strings.getString(R.string.profile_stat_tests), CommonR.drawable.trophy_icon, Color(0xFFAA7840), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatisticDivider() {
    VerticalDivider(Modifier.height(68.dp), color = Color(0xFFEAE6F1))
}

@Composable
private fun Statistic(number: Int, label: String, icon: Int, accent: Color, modifier: Modifier) {
    Column(modifier.semantics(mergeDescendants = true) {}, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(54.dp).background(accent.copy(alpha = .1f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), null, Modifier.size(46.dp), tint = accent)
        }
        Text(number.toString(), style = MaterialTheme.typography.titleMedium.copy(fontSize = 26.sp, lineHeight = 30.sp),
            color = titleMain, modifier = Modifier.padding(top = 6.dp, bottom = 4.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 14.sp),
            color = username, textAlign = TextAlign.Center, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
