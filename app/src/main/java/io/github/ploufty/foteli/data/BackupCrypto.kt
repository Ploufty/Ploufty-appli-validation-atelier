package io.github.ploufty.foteli.data

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Chiffrement des sauvegardes (format propre à Foteli, outils Java standard uniquement).
 * Clé : PBKDF2 du mot de passe de sauvegarde. Données : AES-256-GCM par blocs de 1 Mo,
 * pour qu'une sauvegarde de fin d'année (≈ 1 Go) ne remplisse jamais la mémoire de la tablette.
 * Chaque bloc est authentifié : un mauvais mot de passe, un fichier modifié ou tronqué est refusé.
 */
object BackupCrypto {
    private val MAGIC = "FOTELI-SAUVEGARDE-1".toByteArray()
    private const val ITERATIONS = 120_000
    private const val CHUNK = 1 shl 20
    private val random = SecureRandom()

    class WrongPasswordException : IOException("Mot de passe incorrect ou fichier abîmé.")
    class NotABackupException : IOException("Ce fichier n'est pas une sauvegarde Foteli.")

    fun encrypt(out: OutputStream, password: String): OutputStream {
        val salt = ByteArray(16).also(random::nextBytes)
        val nonce = ByteArray(8).also(random::nextBytes)
        val header = ByteArrayOutputStream().also { b ->
            DataOutputStream(b).apply { write(MAGIC); write(salt); writeInt(ITERATIONS); write(nonce) }
        }.toByteArray()
        out.write(header)
        return ChunkWriter(out, key(password, salt, ITERATIONS), nonce, header)
    }

    fun decrypt(input: InputStream, password: String): InputStream {
        val data = DataInputStream(input)
        val magic = ByteArray(MAGIC.size)
        try {
            data.readFully(magic)
        } catch (e: EOFException) {
            throw NotABackupException()
        }
        if (!magic.contentEquals(MAGIC)) throw NotABackupException()
        val salt = ByteArray(16).also(data::readFully)
        val iterations = data.readInt()
        val nonce = ByteArray(8).also(data::readFully)
        val header = ByteArrayOutputStream().also { b ->
            DataOutputStream(b).apply { write(MAGIC); write(salt); writeInt(iterations); write(nonce) }
        }.toByteArray()
        return ChunkReader(data, key(password, salt, iterations), nonce, header)
    }

    private fun key(password: String, salt: ByteArray, iterations: Int): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
        try {
            return SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }

    private fun cipher(mode: Int, key: SecretKeySpec, nonce: ByteArray, index: Int, header: ByteArray, last: Boolean): Cipher =
        Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(mode, key, GCMParameterSpec(128, ByteBuffer.allocate(12).put(nonce).putInt(index).array()))
            // L'en-tête et le drapeau « dernier bloc » sont authentifiés : on ne peut ni les changer ni couper le fichier.
            updateAAD(header)
            updateAAD(byteArrayOf(if (last) 1 else 0))
        }

    private class ChunkWriter(
        private val out: OutputStream,
        private val key: SecretKeySpec,
        private val nonce: ByteArray,
        private val header: ByteArray,
    ) : OutputStream() {
        private val buffer = ByteArray(CHUNK)
        private var size = 0
        private var index = 0
        private var closed = false

        override fun write(b: Int) = write(byteArrayOf(b.toByte()), 0, 1)

        override fun write(b: ByteArray, off: Int, len: Int) {
            var o = off
            var l = len
            while (l > 0) {
                if (size == CHUNK) flushChunk(last = false)
                val n = minOf(l, CHUNK - size)
                System.arraycopy(b, o, buffer, size, n)
                size += n
                o += n
                l -= n
            }
        }

        private fun flushChunk(last: Boolean) {
            val sealed = cipher(Cipher.ENCRYPT_MODE, key, nonce, index++, header, last).doFinal(buffer, 0, size)
            DataOutputStream(out).apply {
                writeByte(if (last) 1 else 0)
                writeInt(sealed.size)
                write(sealed)
            }
            size = 0
        }

        override fun close() {
            if (closed) return
            closed = true
            flushChunk(last = true)
            out.close()
        }
    }

    private class ChunkReader(
        private val input: DataInputStream,
        private val key: SecretKeySpec,
        private val nonce: ByteArray,
        private val header: ByteArray,
    ) : InputStream() {
        private var plain = ByteArray(0)
        private var pos = 0
        private var index = 0
        private var done = false

        private fun next(): Boolean {
            if (done) return false
            val last = try {
                input.readByte().toInt() == 1
            } catch (e: EOFException) {
                throw WrongPasswordException()
            }
            val length = input.readInt()
            if (length < 0 || length > CHUNK + 16) throw WrongPasswordException()
            val sealed = ByteArray(length).also(input::readFully)
            plain = try {
                cipher(Cipher.DECRYPT_MODE, key, nonce, index++, header, last).doFinal(sealed)
            } catch (e: AEADBadTagException) {
                throw WrongPasswordException()
            }
            pos = 0
            done = last
            return true
        }

        override fun read(): Int {
            val one = ByteArray(1)
            return if (read(one, 0, 1) == -1) -1 else one[0].toInt() and 0xFF
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            while (pos == plain.size) if (!next()) return -1
            val n = minOf(len, plain.size - pos)
            System.arraycopy(plain, pos, b, off, n)
            pos += n
            return n
        }

        override fun close() = input.close()
    }
}
