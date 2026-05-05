package com.example.teamnest

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey val id: Int = 1,
    val isDarkTheme: Boolean = false,
    val userId: String? = null,
    val userName: String? = null,
    val userEmail: String? = null
)

@Entity(tableName = "notified_notifications")
data class NotifiedNotification(
    @PrimaryKey val notificationId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getPreferences(): Flow<UserPreferences?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(preferences: UserPreferences)

    @Query("UPDATE user_preferences SET isDarkTheme = :isDark WHERE id = 1")
    suspend fun updateTheme(isDark: Boolean)

    @Query("UPDATE user_preferences SET userId = :uid, userName = :name, userEmail = :email WHERE id = 1")
    suspend fun updateUserInfo(uid: String?, name: String?, email: String?)
}

@Dao
interface NotifiedNotificationDao {
    @Query("SELECT EXISTS(SELECT 1 FROM notified_notifications WHERE notificationId = :id)")
    suspend fun isNotified(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotified(notified: NotifiedNotification)

    @Query("DELETE FROM notified_notifications WHERE timestamp < :expiry")
    suspend fun cleanOldNotifications(expiry: Long)
}

@Database(entities = [UserPreferences::class, NotifiedNotification::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun notifiedNotificationDao(): NotifiedNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teamnest_database"
                )
                .fallbackToDestructiveMigration() // Simple for development, prevents crash on schema change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
