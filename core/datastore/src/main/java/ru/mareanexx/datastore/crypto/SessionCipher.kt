package ru.mareanexx.datastore.crypto

interface SessionCipher {
    fun encrypt(value: String): String
    fun decrypt(value: String): String?
}
