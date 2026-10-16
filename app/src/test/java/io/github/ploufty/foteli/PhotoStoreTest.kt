package io.github.ploufty.foteli

import io.github.ploufty.foteli.data.ImageKeys
import io.github.ploufty.foteli.data.PhotoStore
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoStoreTest {

    private val store = PhotoStore(Files.createTempDirectory("foteli").toFile())

    @Test
    fun validatedPhotoLeavesThePendingFolder() {
        val pending = store.newPending().apply { writeText("jpeg") }
        val name = store.keepPhoto(pending)
        assertNotNull(name)
        assertFalse(pending.exists())
        assertTrue(store.photo(name!!).exists())
    }

    @Test
    fun unvalidatedPhotosAreErased() {
        val a = store.newPending().apply { writeText("a") }
        val b = store.newPending().apply { writeText("b") }
        store.clearPending()
        assertFalse(a.exists())
        assertFalse(b.exists())
        assertNull(store.keepPhoto(a))
    }

    @Test
    fun modelPhotoBecomesWorkshopImageAndIsCopiedOnDuplicate() {
        val pending = store.newPending().apply { writeText("modele") }
        val key = store.keepImage(ImageKeys.PENDING + pending.name)
        assertTrue(key.startsWith(ImageKeys.MODEL))
        val copy = store.copyImage(key)
        assertNotEquals(key, copy)
        store.deleteImage(key)
        assertFalse(store.imageFile(key)!!.exists())
        assertEquals("modele", store.imageFile(copy)!!.readText())
        // Les illustrations intégrées ne sont pas des fichiers.
        assertEquals("kapla", store.keepImage("kapla"))
        assertNull(store.imageFile("kapla"))
    }

    @Test
    fun wipeErasesEveryPhoto() {
        val photo = store.photo(store.keepPhoto(store.newPending().apply { writeText("p") })!!)
        val model = store.imageFile(store.keepImage(ImageKeys.PENDING + store.newPending().apply { writeText("m") }.name))!!
        store.deleteAll()
        assertFalse(photo.exists())
        assertFalse(model.exists())
    }
}
