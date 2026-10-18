package ru.mareanexx.book_api.domain.validation

import ru.mareanexx.book_api.domain.model.*

object BookInputRules {
    const val MAX_REVIEW_LENGTH = 2000
    const val MAX_REPLY_LENGTH = 1000
    fun review(input: ReviewInput): BookError? = when {
        input.rating !in 1..5 -> BookError.RatingRequired
        input.text.length > MAX_REVIEW_LENGTH -> BookError.ReviewTooLong
        else -> null
    }
    fun reply(text: String): BookError? = when {
        text.isBlank() -> BookError.ReplyRequired
        text.length > MAX_REPLY_LENGTH -> BookError.ReplyTooLong
        else -> null
    }
}
