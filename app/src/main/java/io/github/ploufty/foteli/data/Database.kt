package io.github.ploufty.foteli.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Réglages et sécurité (docs/v0/donnees.md § 1 C).
 * Le PIN et le code de secours ne sont jamais stockés en clair : seulement leur empreinte.
 */
@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val className: String = "",
    val pinHash: String = "",
    val rescueHash: String = "",
    val failedPinAttempts: Int = 0,
    val pinLockedUntil: Long = 0,
    val autoCloseTeacher: Boolean = true,
    val setupDone: Boolean = false,
    /** Mode libre « Souvenirs » : désactivé tant que l'enseignant ne l'active pas. */
    @ColumnInfo(defaultValue = "0") val freeMode: Boolean = false,
    @ColumnInfo(defaultValue = "0") val freeModeFrontCamera: Boolean = false,
)

/** Représentation de l'élève sur l'accueil. La photo arrive avec l'appareil photo (version 0.4). */
enum class StudentLook { ROBOT, NAME }

/** Donnée personnelle minimale : prénom et représentation (docs/v0/donnees.md § 1 B). */
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val look: StudentLook,
    val robot: Int,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Atelier (donnée pédagogique, sans donnée d'élève) : docs/v0/donnees.md § 1 A.
 * [image] désigne l'illustration provisoire ; la vraie photo du modèle arrive en 0.4.
 */
@Entity(tableName = "workshops")
data class Workshop(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val image: String,
    val competencies: List<String>,
    val frontCamera: Boolean = false,
    val active: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

class Converters {
    @TypeConverter
    fun fromList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toList(text: String): List<String> = text.split(",").filter { it.isNotBlank() }
}

@Dao
interface FoteliDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun settings(): Flow<Settings?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun settingsNow(): Settings?

    @Upsert
    suspend fun saveSettings(settings: Settings)

    @Query("SELECT * FROM students")
    fun students(): Flow<List<Student>>

    @Query("SELECT * FROM students")
    suspend fun studentsNow(): List<Student>

    @Insert
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudent(id: Long)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()

    @Query("SELECT * FROM workshops ORDER BY createdAt")
    fun workshops(): Flow<List<Workshop>>

    @Insert
    suspend fun insertWorkshop(workshop: Workshop): Long

    @Update
    suspend fun updateWorkshop(workshop: Workshop)

    @Query("UPDATE workshops SET active = :active WHERE id = :id")
    suspend fun setWorkshopActive(id: Long, active: Boolean)

    @Query("UPDATE workshops SET active = 0")
    suspend fun deactivateAllWorkshops()

    @Query("DELETE FROM workshops WHERE id = :id")
    suspend fun deleteWorkshop(id: Long)

    @Query("DELETE FROM workshops")
    suspend fun deleteAllWorkshops()
}

/** 0.2 → 0.3 : ajout des ateliers et du mode libre, sans toucher à la classe existante. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE settings ADD COLUMN freeMode INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE settings ADD COLUMN freeModeFrontCamera INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS workshops (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, image TEXT NOT NULL, " +
                "competencies TEXT NOT NULL, frontCamera INTEGER NOT NULL, active INTEGER NOT NULL, createdAt INTEGER NOT NULL)",
        )
    }
}

// Les évolutions du schéma se feront par migrations écrites à la main :
// aucune donnée ne doit être perdue lors d'une mise à jour.
@Database(entities = [Settings::class, Student::class, Workshop::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FoteliDatabase : RoomDatabase() {
    abstract fun dao(): FoteliDao

    companion object {
        @Volatile
        private var instance: FoteliDatabase? = null

        fun get(context: Context): FoteliDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, FoteliDatabase::class.java, "foteli.db")
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
