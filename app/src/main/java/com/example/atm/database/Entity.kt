package com.example.atm.database

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "actions")
data class ActionEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val date: String
)

// FIX 1: Changed table name to "balance_table"
@Entity(tableName = "balance_table")
data class Balance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Every entity needs a primary key ID
    val amount: Double = 0.0
)

// FIX 2: Added Balance::class to the entities list
@Database(entities = [ActionEntry::class, Balance::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun actionDao(): ActionDao
    abstract fun balanceDao(): BalanceDao
    // If you created a BalanceDao, you would add it here too

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "action_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}