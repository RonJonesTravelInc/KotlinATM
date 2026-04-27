package com.example.atm.database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ActionDao {
    @Insert
    fun insert(action: ActionEntry) // No 'suspend'

    @Query("SELECT * FROM actions")
    fun getAllActions(): List<ActionEntry> // No 'suspend'
}

@Dao
interface BalanceDao {
    @Query("SELECT * FROM balance_table LIMIT 1")
    fun getBalance(): Balance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateBalance(balance: Balance)
}
