package ru.mareanexx.book_impl.data.mock

import android.content.Context
import com.google.gson.Gson
import java.io.IOException
import java.time.Instant
import kotlin.math.roundToInt

/** Отзывы общие для книги, а авторство и реакции привязаны к аккаунту. */
internal class DemoBookStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences("book_demo_server", Context.MODE_PRIVATE)
    private val gson = Gson()
    fun reviews(id: String, count: Int, average: Double?): List<DemoStoredReview> {
        preferences.getString("reviews_$id", null)?.let { return gson.fromJson(it, Array<DemoStoredReview>::class.java).toList() }
        val ratings = MutableList(count) { 5 }
        var deficit = (count * 5 - (count * (average ?: 0.0)).roundToInt()).coerceAtLeast(0)
        for ((index, value) in listOf(5, 4, 5, 3, 2, 1).withIndex()) {
            if (index < count && deficit >= 5 - value) { ratings[index] = value; deficit -= 5 - value }
        }
        for (index in 6 until count) {
            val change = minOf(2, deficit)
            ratings[index] -= change
            deficit -= change
        }
        val names = listOf("Анна Морозова", "Илья Соколов", "Екатерина Лебедева", "Дмитрий Орлов", "София Волкова", "Алексей Ковалёв")
        val texts = listOf(
            "Понравилось, что теория сопровождается понятными примерами. Возвращаюсь к схемам реакций, когда разбираю задания самостоятельно.",
            "Хорошая книга для вдумчивого чтения. Некоторые главы удобнее проходить вместе с конспектом и дополнительными упражнениями.",
            "Иллюстрации помогают связать формулы с тем, что происходит в реакции. Для повторения основных понятий оказалось очень удобно.",
            "Материал интересный, но мне не хватило более подробного разбора нескольких задач. Будет полезнее после знакомства с базовой теорией.",
            "Читаю небольшими разделами и делаю заметки. Хотелось бы больше практических примеров в конце каждой темы.",
            "Структура понятная. Для первого знакомства с темой книга показалась сложной, поэтому сначала повторил основные определения.",
        )
        return ratings.mapIndexed { index, rating -> DemoStoredReview(
            "$id-review-${index + 1}", "demo-reviewer-${index + 1}", names[index % names.size], null, rating, texts[index % texts.size],
            Instant.now().minusSeconds((index + 1L) * 86400).toString(), null, maxOf(0, 36 - index), index % 3,
        ) }.also { saveReviews(id, it) }
    }
    fun saveReviews(id: String, items: List<DemoStoredReview>) = save("reviews_$id", items)
    fun replies(reviewId: String): List<DemoStoredReply> {
        preferences.getString("replies_$reviewId", null)?.let { return gson.fromJson(it, Array<DemoStoredReply>::class.java).toList() }
        val items = if (reviewId.endsWith("-review-1")) listOf(
            DemoStoredReply("$reviewId-reply-1", "demo-commenter-1", "Мария Белова", null, "Спасибо за отзыв! А какую главу посоветуешь повторить перед практикумом?", Instant.now().minusSeconds(7200).toString()),
            DemoStoredReply("$reviewId-reply-2", "demo-reviewer-1", "Анна Морозова", null, "Я начала с раздела о химических связях — дальше стало проще разбирать механизмы.", Instant.now().minusSeconds(3600).toString()),
        ) else emptyList()
        return items.also { saveReplies(reviewId, it) }
    }
    fun saveReplies(id: String, items: List<DemoStoredReply>) = save("replies_$id", items)
    private fun save(key: String, value: Any) {
        if (!preferences.edit().putString(key, gson.toJson(value)).commit()) throw IOException("Не удалось сохранить демонстрационные отзывы")
    }
    companion object { val lock = Any() }
}
