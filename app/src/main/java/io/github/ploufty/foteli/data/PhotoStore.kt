package io.github.ploufty.foteli.data

import java.io.File
import java.util.UUID

/** Valeur de [Workshop.image] quand l'atelier a une photo du modèle plutôt qu'une illustration. */
object ImageKeys {
    /** Photo du modèle enregistrée. */
    const val MODEL = "photo:"

    /** Photo du modèle tout juste prise, pas encore enregistrée avec l'atelier. */
    const val PENDING = "pending:"

    fun isPhoto(key: String) = key.startsWith(MODEL) || key.startsWith(PENDING)
}

/**
 * Photos rangées dans l'espace privé de l'appli, jamais dans la galerie (docs/v0/donnees.md § 2 et § 7).
 * Aucune métadonnée : les photos sont réencodées à l'enregistrement (voir ui/Camera.kt).
 */
class PhotoStore(root: File) {
    /** Photo prise mais pas encore validée : effacée par ↻, le retour ou l'inactivité. */
    private val pending = File(root, "pending")
    private val photos = File(root, "photos")
    private val models = File(root, "models")

    fun newPending(): File = File(pending.apply { mkdirs() }, "${UUID.randomUUID()}.jpg")

    fun clearPending() {
        pending.listFiles()?.forEach { it.delete() }
    }

    fun pendingFile(name: String) = File(pending, name)
    fun photo(name: String) = File(photos, name)
    fun model(name: String) = File(models, name)

    /** Fichier d'une image d'atelier, ou null pour une illustration intégrée. */
    fun imageFile(key: String): File? = when {
        key.startsWith(ImageKeys.MODEL) -> model(key.removePrefix(ImageKeys.MODEL))
        key.startsWith(ImageKeys.PENDING) -> pendingFile(key.removePrefix(ImageKeys.PENDING))
        else -> null
    }

    /** Photo validée : elle quitte le dossier d'attente. Renvoie son nom, ou null si elle a disparu. */
    fun keepPhoto(file: File): String? = moveTo(file, photos)

    /** Image d'atelier définitive : une photo en attente devient la photo du modèle. */
    fun keepImage(key: String): String {
        if (!key.startsWith(ImageKeys.PENDING)) return key
        val name = moveTo(pendingFile(key.removePrefix(ImageKeys.PENDING)), models) ?: return "puzzle"
        return ImageKeys.MODEL + name
    }

    /** Copie la photo du modèle (atelier dupliqué), pour que chaque atelier garde la sienne. */
    fun copyImage(key: String): String {
        if (!key.startsWith(ImageKeys.MODEL)) return key
        val source = model(key.removePrefix(ImageKeys.MODEL))
        if (!source.exists()) return "puzzle"
        val copy = File(models, "${UUID.randomUUID()}.jpg")
        source.copyTo(copy)
        return ImageKeys.MODEL + copy.name
    }

    fun deleteImage(key: String) {
        if (key.startsWith(ImageKeys.MODEL)) model(key.removePrefix(ImageKeys.MODEL)).delete()
    }

    fun deletePhotos(names: List<String>) = names.forEach { photo(it).delete() }

    /** « Tout effacer » : plus aucune photo sur la tablette. */
    fun deleteAll() {
        listOf(pending, photos, models).forEach { dir -> dir.listFiles()?.forEach { it.delete() } }
    }

    private fun moveTo(file: File, dir: File): String? {
        if (!file.exists()) return null
        dir.mkdirs()
        val target = File(dir, file.name)
        if (!file.renameTo(target)) {
            file.copyTo(target, overwrite = true)
            file.delete()
        }
        return target.name
    }
}
