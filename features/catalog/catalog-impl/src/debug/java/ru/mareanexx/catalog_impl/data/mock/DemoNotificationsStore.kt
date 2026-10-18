package ru.mareanexx.catalog_impl.data.mock

import android.content.Context
import ru.mareanexx.catalog_impl.data.remote.dto.*
import java.io.IOException
import java.time.Instant

internal class DemoNotificationsStore(
    context: Context,
    private val userId: String,
) {
    private val preferences = context.getSharedPreferences("catalog_demo_notifications", Context.MODE_PRIVATE)
    private val prefix = "${userId}_"
    private val started = preferences.getLong(prefix + "started", 0).takeIf { it > 0 } ?: Instant.now().epochSecond.also {
        if (!preferences.edit().putLong(prefix + "started", it).commit()) throw IOException("Не удалось сохранить уведомления")
    }
    fun items(): List<NotificationDto> {
        val contents = listOf(
            NotificationDto("notice_6", 6, "Новые маршруты в органической химии", "Собрали книги о реакциях, механизмах и полимерах. Найди следующую тему для изучения.", bookId = null, categoryId = "organic"),
            NotificationDto("notice_5", 5, "Книга для твоего следующего открытия", "Квантовая химия: от строения атома к свойствам молекул.", bookId = "book_3"),
            NotificationDto("notice_4", 4, "Практика помогает понять теорию", "Загляни в аналитическую химию: методы анализа и лабораторный практикум.", categoryId = "analytical"),
            NotificationDto("notice_3", 3, "Химия живых систем", "Открой основы биохимии и изучай молекулы, из которых состоит жизнь.", bookId = "book_2"),
            NotificationDto("notice_2", 2, "Начни с основ", "Подборка по общей химии поможет построить прочную базу знаний.", categoryId = "general"),
            NotificationDto("notice_1", 1, "Добро пожаловать в Elementa", "Сохраняй книги в коллекции и возвращайся к чтению в удобный момент.", bookId = "book_1"),
        )
        val through = preferences.getLong(prefix + "through", 0)
        return contents.map { note ->
            val sequence = requireNotNull(note.sequence)
            val created = Instant.ofEpochSecond(started - (6 - sequence) * 14_400 - 180)
            val read = preferences.getString(prefix + note.id, null)
                ?: if (sequence <= through || sequence <= 3) Instant.ofEpochSecond(started - 60).toString() else null
            note.copy(createdAt = created.toString(), readAt = read)
        }
    }
    fun unread() = UnreadDto(items().count { it.readAt == null }, preferences.getLong(prefix + "revision", 0))
    fun read(id: String?): NotificationReceiptDto {
        val before = items()
        val now = Instant.now().toString()
        val targets = if (id == null) before else before.filter { it.id == id }
        require(targets.isNotEmpty())
        val edited = preferences.edit()
        if (targets.any { it.readAt == null }) {
            targets.filter { it.readAt == null }.forEach { edited.putString(prefix + it.id, now) }
            edited.putLong(prefix + "revision", preferences.getLong(prefix + "revision", 0) + 1)
            if (!edited.commit()) throw IOException("Не удалось сохранить отметку прочтения")
        }
        return NotificationReceiptDto(targets.singleOrNull()?.readAt ?: now, unread())
    }
    fun readThrough(sequence: Long): NotificationReceiptDto {
        require(sequence in 0..6)
        val now = Instant.now().toString()
        val targets = items().filter { requireNotNull(it.sequence) <= sequence && it.readAt == null }
        if (targets.isNotEmpty()) {
            val edited = preferences.edit().putLong(prefix + "through", maxOf(sequence, preferences.getLong(prefix + "through", 0)))
            targets.forEach { edited.putString(prefix + it.id, now) }
            edited.putLong(prefix + "revision", preferences.getLong(prefix + "revision", 0) + 1)
            if (!edited.commit()) throw IOException("Не удалось сохранить отметки прочтения")
        }
        return NotificationReceiptDto(now, unread())
    }
    companion object { val lock = Any() }
}
