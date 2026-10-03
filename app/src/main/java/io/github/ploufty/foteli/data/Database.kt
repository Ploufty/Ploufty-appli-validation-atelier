package io.github.ploufty.foteli.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.Upsert
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
}

// Les évolutions du schéma se feront par migrations écrites à la main :
// aucune donnée ne doit être perdue lors d'une mise à jour.
@Database(entities = [Settings::class, Student::class], version = 1, exportSchema = false)
abstract class FoteliDatabase : RoomDatabase() {
    abstract fun dao(): FoteliDao

    companion object {
        @Volatile
        private var instance: FoteliDatabase? = null

        fun get(context: Context): FoteliDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, FoteliDatabase::class.java, "foteli.db")
                    .build()
                    .also { instance = it }
            }
    }
}
