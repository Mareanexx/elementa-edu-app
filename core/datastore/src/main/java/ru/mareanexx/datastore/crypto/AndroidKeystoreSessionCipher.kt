package ru.mareanexx.datastore.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Шифрует данные AES-GCM. Неэкспортируемый ключ остаётся в Android Keystore. */
class AndroidKeystoreSessionCipher : SessionCipher {
    override fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return encode(cipher.iv) + ":" + encode(cipher.doFinal(value.toByteArray(Charsets.UTF_8)))
    }

    override fun decrypt(value: String): String? = try {
        val parts = value.split(":")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, decode(parts[0])))
        cipher.doFinal(decode(parts[1])).toString(Charsets.UTF_8)
    } catch (_: java.security.GeneralSecurityException) {
        // После сброса или аннулирования ключа требуется повторный вход.
        null
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: IndexOutOfBoundsException) {
        null
    }

    private fun key(): SecretKey {
        val keystore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keystore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(value: String) = Base64.decode(value, Base64.NO_WRAP)
    private companion object { const val KEY_ALIAS = "elementa_auth_session_v1" }
}
