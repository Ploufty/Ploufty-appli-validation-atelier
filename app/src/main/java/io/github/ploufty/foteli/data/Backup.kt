package io.github.ploufty.foteli.data

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.json.JSONArray
import org.json.JSONObject

/** Ce que contient une sauvegarde (jamais le PIN ni le code de secours : docs/v0/donnees.md § 6). */
class BackupContent(
    val date: Long,
    val className: String,
    val freeMode: Boolean,
    val freeModeFrontCamera: Boolean,
    val autoCloseTeacher: Boolean,
    val students: List<Student>,
    val workshops: List<Workshop>,
    val traces: List<Trace>,
)

/**
 * Sauvegarde Foteli : une archive ZIP (données + photos + modèles + avatars ajoutés),
 * entièrement chiffrée par [BackupCrypto]. Elle ne se relit que dans Foteli.
 */
object Backup {
    private const val DATA = "donnees.json"
    private val SAFE_NAME = Regex("[A-Za-z0-9._-]+")

    fun write(out: OutputStream, password: String, content: BackupContent, store: PhotoStore) {
        ZipOutputStream(BackupCrypto.encrypt(out, password)).use { zip ->
            // Photos déjà compressées : on les range sans les recompresser.
            zip.setLevel(0)
            zip.putNextEntry(ZipEntry(DATA))
            zip.write(toJson(content).toString().toByteArray())
            zip.closeEntry()
            content.traces.forEach { add(zip, "photos/${it.file}", store.photo(it.file)) }
            content.workshops.mapNotNull { w -> store.imageFile(w.image)?.takeIf { w.image.startsWith(ImageKeys.MODEL) } }
                .forEach { add(zip, "modeles/${it.name}", it) }
            store.customRobotIds().forEach { add(zip, "avatars/$it.png", store.robotFile(it)) }
            content.students.mapNotNull { it.photo }.forEach { add(zip, "visages/$it", File(store.facesFolder(), it)) }
        }
    }

    /** Lit la sauvegarde et range ses fichiers dans [staging] (photos/, modeles/, avatars/). Rien n'est encore remplacé. */
    fun read(input: InputStream, password: String, staging: File): BackupContent {
        staging.deleteRecursively()
        var content: BackupContent? = null
        ZipInputStream(BackupCrypto.decrypt(input, password)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val name = entry.name
                if (name == DATA) {
                    content = fromJson(JSONObject(zip.readBytes().toString(Charsets.UTF_8)))
                    continue
                }
                val folder = name.substringBefore('/', "")
                val file = name.substringAfter('/')
                // Seuls des noms simples dans trois dossiers connus sont acceptés (aucun chemin piégé).
                if (folder !in setOf("photos", "modeles", "avatars", "visages") || !SAFE_NAME.matches(file)) continue
                val target = File(staging, "$folder/$file").apply { parentFile?.mkdirs() }
                target.outputStream().use { zip.copyTo(it) }
            }
        }
        return content ?: throw BackupCrypto.NotABackupException()
    }

    private fun add(zip: ZipOutputStream, name: String, file: File) {
        if (!file.exists()) return
        zip.putNextEntry(ZipEntry(name))
        file.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }

    private fun toJson(c: BackupContent) = JSONObject().apply {
        put("version", 1)
        put("date", c.date)
        put("classe", c.className)
        put("modeLibre", c.freeMode)
        put("modeLibreCameraAvant", c.freeModeFrontCamera)
        put("fermetureAuto", c.autoCloseTeacher)
        put("eleves", JSONArray(c.students.map {
            JSONObject().put("id", it.id).put("prenom", it.firstName).put("representation", it.look.name)
                .put("robot", it.robot).put("creation", it.createdAt).put("photo", it.photo ?: JSONObject.NULL)
        }))
        put("ateliers", JSONArray(c.workshops.map {
            JSONObject().put("id", it.id).put("titre", it.title).put("image", it.image)
                .put("competences", JSONArray(it.competencies)).put("cameraAvant", it.frontCamera)
                .put("actif", it.active).put("creation", it.createdAt)
        }))
        put("traces", JSONArray(c.traces.map {
            JSONObject().put("id", it.id).put("eleve", it.studentId).put("atelier", it.workshopId ?: JSONObject.NULL)
                .put("fichier", it.file).put("competences", JSONArray(it.competencies)).put("date", it.takenAt)
        }))
    }

    private fun fromJson(j: JSONObject): BackupContent {
        fun strings(a: JSONArray) = (0 until a.length()).map { a.getString(it) }
        fun objects(a: JSONArray) = (0 until a.length()).map { a.getJSONObject(it) }
        return BackupContent(
            date = j.getLong("date"),
            className = j.getString("classe"),
            freeMode = j.optBoolean("modeLibre"),
            freeModeFrontCamera = j.optBoolean("modeLibreCameraAvant"),
            autoCloseTeacher = j.optBoolean("fermetureAuto", true),
            students = objects(j.getJSONArray("eleves")).map {
                Student(
                    id = it.getLong("id"),
                    firstName = it.getString("prenom"),
                    look = runCatching { StudentLook.valueOf(it.getString("representation")) }.getOrDefault(StudentLook.NAME),
                    robot = it.getInt("robot"),
                    createdAt = it.getLong("creation"),
                    photo = if (it.isNull("photo")) null else it.optString("photo").takeIf { p -> SAFE_NAME.matches(p) },
                )
            },
            workshops = objects(j.getJSONArray("ateliers")).map {
                Workshop(
                    id = it.getLong("id"),
                    title = it.getString("titre"),
                    image = it.getString("image"),
                    competencies = strings(it.getJSONArray("competences")),
                    frontCamera = it.getBoolean("cameraAvant"),
                    active = it.getBoolean("actif"),
                    createdAt = it.getLong("creation"),
                )
            },
            traces = objects(j.getJSONArray("traces")).map {
                Trace(
                    id = it.getLong("id"),
                    studentId = it.getLong("eleve"),
                    workshopId = if (it.isNull("atelier")) null else it.getLong("atelier"),
                    file = it.getString("fichier"),
                    competencies = strings(it.getJSONArray("competences")),
                    takenAt = it.getLong("date"),
                )
            },
        )
    }
}
