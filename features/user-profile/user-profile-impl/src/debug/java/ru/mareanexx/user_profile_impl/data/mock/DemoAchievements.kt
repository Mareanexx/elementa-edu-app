package ru.mareanexx.user_profile_impl.data.mock

import java.time.LocalDate
import ru.mareanexx.user_profile_impl.data.remote.dto.AchievementDto
import ru.mareanexx.user_profile_impl.data.remote.dto.StatisticsDto

internal object DemoAchievements {
    fun create(stats: StatisticsDto, demo: Boolean): List<AchievementDto> {
        val books = stats.booksRead ?: 0
        val notes = stats.notesPublished ?: 0
        val tests = stats.testsPassed ?: 0
        val days = if (demo) 12 else 0
        return listOf(
            badge("first_book", "Первая глава", "Прочитай первую книгу целиком.", "reading", "blue", books, 1, "книг"),
            badge("reader_10", "Книжный химик", "Заверши чтение 10 книг по химии.", "reading", "orange", books, 10, "книг"),
            badge("reader_50", "Библиотека знаний", "Прочитай 50 книг и собери фундамент знаний.", "reading", "pink", books, 50, "книг"),
            badge("first_note", "Есть мысль", "Опубликуй первую заметку к прочитанному.", "notes", "pink", notes, 1, "заметок"),
            badge("notes_20", "Научный дневник", "Опубликуй 20 заметок с формулами и открытиями.", "notes", "blue", notes, 20, "заметок"),
            badge("notes_50", "Автор идей", "Поделись 50 заметками с сообществом.", "notes", "orange", notes, 50, "заметок"),
            badge("first_test", "Первый эксперимент", "Успешно пройди первый тест по химии.", "tests", "red", tests, 1, "тестов"),
            badge("tests_5", "Точная реакция", "Успешно пройди 5 тестов.", "tests", "blue", tests, 5, "тестов"),
            badge("tests_20", "Мастер формул", "Успешно пройди 20 тестов и закрепи знания.", "tests", "red", tests, 20, "тестов"),
            badge("days_3", "Хорошее начало", "Занимайся химией 3 дня подряд.", "habits", "orange", days, 3, "дней"),
            badge("days_7", "Неделя открытий", "Читай или решай тесты 7 дней подряд.", "habits", "pink", days, 7, "дней"),
            badge("days_30", "Сила привычки", "Сохрани серию занятий в течение 30 дней.", "habits", "orange", days, 30, "дней"),
        )
    }

    private fun badge(id: String, title: String, description: String, category: String, art: String, current: Int, target: Int, unit: String) = AchievementDto(
        id, title, description, category, art, current, target, unit,
        if (current >= target) LocalDate.now().minusDays((target % 7).toLong()).toString() else null,
    )
}
