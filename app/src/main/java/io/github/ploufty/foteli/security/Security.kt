package io.github.ploufty.foteli.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Empreintes PBKDF2 salées : le secret lui-même n'est jamais conservé. */
object Secrets {
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256
    private val random = SecureRandom()

    fun hash(secret: String): String {
        val salt = ByteArray(16).also(random::nextBytes)
        return "pbkdf2:$ITERATIONS:${b64(salt)}:${b64(derive(secret, salt, ITERATIONS))}"
    }

    fun verify(secret: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 4 || parts[0] != "pbkdf2") return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = Base64.getDecoder().decode(parts[2])
        val expected = Base64.getDecoder().decode(parts[3])
        return MessageDigest.isEqual(derive(secret, salt, iterations), expected)
    }

    private fun derive(secret: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(secret.toCharArray(), salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun b64(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)
}

/** Règles du code PIN (docs/v0/donnees.md § 3). */
object PinRules {
    const val LENGTH = 4
    private const val ATTEMPTS_PER_LOCK = 5

    fun isWellFormed(pin: String) = pin.length == LENGTH && pin.all(Char::isDigit)

    /** Codes trop faciles à deviner : chiffres identiques, suites, motifs courants. */
    fun isWeak(pin: String): Boolean {
        if (pin.toSet().size == 1) return true
        val steps = pin.zipWithNext { a, b -> b - a }.toSet()
        if (steps == setOf(1) || steps == setOf(-1)) return true
        return pin in setOf("1212", "2580", "0852", "1122", "1004", "2000", "1010", "6969")
    }

    /** Blocage après chaque série de 5 essais faux : 1 min, puis 5 min, puis 15 min. */
    fun lockDurationMillis(failedAttempts: Int): Long {
        if (failedAttempts == 0 || failedAttempts % ATTEMPTS_PER_LOCK != 0) return 0
        return when (failedAttempts / ATTEMPTS_PER_LOCK) {
            1 -> 60_000L
            2 -> 5 * 60_000L
            else -> 15 * 60_000L
        }
    }
}

/**
 * Code de secours : 8 chiffres, affichés en deux groupes (ex. « 4827 1953 »),
 * tapés avec le clavier de Foteli. Facile à recopier pour un adulte, inutile sans le papier.
 */
object RescueCode {
    const val LENGTH = 8

    fun generate(random: SecureRandom = SecureRandom()): String {
        val digits = (0 until LENGTH).map { random.nextInt(10) }.joinToString("")
        return digits.chunked(4).joinToString(" ")
    }

    /** Ce qui est réellement comparé : les chiffres seuls. */
    fun normalize(input: String): String = input.filter(Char::isDigit)
}
