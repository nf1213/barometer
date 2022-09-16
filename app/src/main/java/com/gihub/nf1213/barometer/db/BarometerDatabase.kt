package com.gihub.nf1213.barometer.db

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
import androidx.room.TypeConverters
import com.gihub.nf1213.barometer.DATABASE_NAME
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Entity(tableName = "pressure")
data class PressureEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") val dateTime: LocalDateTime,
    @ColumnInfo(name = "pressure_value") val value: Float
)

@Dao
interface PressureDao {
    @Query("SELECT * FROM pressure ORDER BY date DESC LIMIT 10")
    fun getAll(): Flow<List<PressureEntry>>

    @Insert
    fun insert(entry: PressureEntry)
}

@Database(entities = [PressureEntry::class], version = 1)
@TypeConverters(LocalDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .build()
    }
    abstract fun pressureDao(): PressureDao
}
