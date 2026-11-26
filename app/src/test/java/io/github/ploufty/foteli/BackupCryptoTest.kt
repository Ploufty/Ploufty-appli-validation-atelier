package io.github.ploufty.foteli

import io.github.ploufty.foteli.data.BackupCrypto
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.random.Random
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BackupCryptoTest {

    private val data = Random(42).nextBytes(3 * (1 shl 20) + 1234) // plusieurs blocs de 1 Mo

    private fun seal(password: String): ByteArray =
        ByteArrayOutputStream().also { out -> BackupCrypto.encrypt(out, password).use { it.write(data) } }.toByteArray()

    @Test
    fun roundTripOverSeveralChunks() {
        val sealed = seal("mot de passe")
        assertFalse(String(sealed, Charsets.ISO_8859_1).contains(String(data.copyOfRange(0, 64), Charsets.ISO_8859_1)))
        val back = BackupCrypto.decrypt(ByteArrayInputStream(sealed), "mot de passe").readBytes()
        assertArrayEquals(data, back)
    }

    @Test(expected = BackupCrypto.WrongPasswordException::class)
    fun wrongPasswordIsRefused() {
        BackupCrypto.decrypt(ByteArrayInputStream(seal("bon mot de passe")), "mauvais").readBytes()
    }

    @Test(expected = BackupCrypto.WrongPasswordException::class)
    fun truncatedFileIsRefused() {
        val sealed = seal("mot de passe")
        BackupCrypto.decrypt(ByteArrayInputStream(sealed.copyOf(sealed.size - 2_000_000)), "mot de passe").readBytes()
    }

    @Test(expected = BackupCrypto.NotABackupException::class)
    fun otherFilesAreRefused() {
        BackupCrypto.decrypt(ByteArrayInputStream("bonjour".toByteArray()), "x").readBytes()
    }
}
