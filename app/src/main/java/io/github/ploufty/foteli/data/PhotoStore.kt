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

    /** Robots ajoutés par l'enseignant (images de la tablette), nommés par leur numéro. */
    private val robots = File(root, "robots")

    /** Photos des élèves (représentation sur l'accueil). */
    private val faces = File(root, "faces")

    /** Photo d'élève : enregistrée (nom simple) ou tout juste prise ([ImageKeys.PENDING]). */
    fun faceFile(key: String): File =
        if (key.startsWith(ImageKeys.PENDING)) pendingFile(key.removePrefix(ImageKeys.PENDING)) else File(faces, key)

    /** Photo d'élève définitive : renvoie le nom à garder dans la fiche. */
    fun keepFace(key: String): String? =
        if (key.startsWith(ImageKeys.PENDING)) moveTo(pendingFile(key.removePrefix(ImageKeys.PENDING)), faces) else key

    fun deleteFace(name: String?) {
        if (name != null && !name.startsWith(ImageKeys.PENDING)) File(faces, name).delete()
    }

    fun robotFile(id: Int) = File(robots.apply { mkdirs() }, "$id.png")

    fun customRobotIds(): List<Int> =
        robots.listFiles()?.mapNotNull { it.name.removeSuffix(".png").toIntOrNull() }?.sorted() ?: emptyList()

    fun facesFolder() = faces

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

    /** Restauration : les photos, modèles et avatars de la sauvegarde ([staging]) remplacent ceux de la tablette. */
    fun replaceAllFrom(staging: File) {
        mapOf("photos" to photos, "modeles" to models, "avatars" to robots, "visages" to faces).forEach { (from, to) ->
            to.listFiles()?.forEach { it.delete() }
            to.mkdirs()
            File(staging, from).listFiles()?.forEach { f ->
                val target = File(to, f.name)
                if (!f.renameTo(target)) {
                    f.copyTo(target, overwrite = true)
                    f.delete()
                }
            }
        }
        staging.deleteRecursively()
    }

    /** « Tout effacer » : plus aucune photo sur la tablette. */
    fun deleteAll() {
        listOf(pending, photos, models, faces).forEach { dir -> dir.listFiles()?.forEach { it.delete() } }
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
