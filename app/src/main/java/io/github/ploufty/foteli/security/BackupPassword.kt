package io.github.ploufty.foteli.security

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Mot de passe de sauvegarde retenu par Foteli, chiffré par une clé du coffre-fort d'Android
 * (la clé ne quitte jamais la tablette ; docs/v0/technique.md § 4).
 */
class BackupPassword(private val prefs: SharedPreferences) {
    private val alias = "foteli-sauvegarde"

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
        }.generateKey()
    }

    fun isSet() = prefs.contains("mdp_sauvegarde")

    fun save(password: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val sealed = cipher.iv + cipher.doFinal(password.toByteArray())
        prefs.edit().putString("mdp_sauvegarde", Base64.encodeToString(sealed, Base64.NO_WRAP)).apply()
    }

    fun read(): String? = runCatching {
        val sealed = Base64.decode(prefs.getString("mdp_sauvegarde", null) ?: return null, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, sealed.copyOfRange(0, 12)))
        String(cipher.doFinal(sealed, 12, sealed.size - 12))
    }.getOrNull()
}
