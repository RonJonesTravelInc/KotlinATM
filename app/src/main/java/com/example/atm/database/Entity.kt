// DBFunctions.kt
package com.example.sql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize
//hashing
import java.security.MessageDigest

class DBFunctions(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "DB.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: DBFunctions? = null

        fun getInstance(context: Context): DBFunctions {
            return INSTANCE ?: synchronized(this) {
                val instance = DBFunctions(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.let { createDb(it) }
    }

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun checkAuth(username: String, passwordHash: String): Int {
        val db = this.readableDatabase
        var userId = -1
        val cursor = db.rawQuery(
            "SELECT user_id FROM Clients WHERE username = ? AND password_hash = ?",
            arrayOf(username, passwordHash)
        )
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
        }
        cursor.close()
        //this returns -1 for null
        return userId
    }

    fun createDb(db: SQLiteDatabase) {
        // Updated to include username and password_hash columns
        val createClientTableQuery = "CREATE TABLE Clients (user_id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password_hash TEXT, dob TEXT, email TEXT, first_name TEXT, last_name TEXT, address TEXT)"
        val createAccountsTableQuery = "CREATE TABLE Accounts (account_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, account_type_id INTEGER, balance REAL, date_opened TEXT)"
        val createTransactionTableQuery = "CREATE TABLE Transactions (transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, account_id INTEGER, date_time TEXT, description TEXT, amount REAL, type INTEGER)"
        val createAccountTypesTableQuery = "CREATE TABLE AccountTypes (account_type_id INTEGER PRIMARY KEY AUTOINCREMENT, limits INTEGER, minimum INTEGER, type TEXT)"

        db.execSQL(createClientTableQuery)
        db.execSQL(createAccountsTableQuery)
        db.execSQL(createTransactionTableQuery)
        db.execSQL(createAccountTypesTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, newVersion: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS Clients")
        db.execSQL("DROP TABLE IF EXISTS Accounts")
        db.execSQL("DROP TABLE IF EXISTS Transactions")
        db.execSQL("DROP TABLE IF EXISTS AccountTypes")
        createDb(db)
    }

    fun closeDb() {
        this.writableDatabase.close()
    }

    fun isUsernameExists(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM Clients WHERE username = ?", arrayOf(username))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun insertClient(username: String, passwordHash: String, dob: String, email: String, fn: String, ln: String, addr: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("username", username)
            put("password_hash", passwordHash)
            put("first_name", fn)
            put("last_name", ln)
            put("dob", dob)
            put("email", email)
            put("address", addr)
        }
        // Returns the row ID (user_id) of the newly inserted row
        return db.insert("Clients", null, contentValues)
    }

    fun insertAccount(userId: Int?, accountTypeId: Int, balance: Double, dateOpened: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("user_id", userId)
            put("account_type_id", accountTypeId)
            put("balance", balance)
            put("date_opened", dateOpened)
        }
        return db.insert("Accounts", null, contentValues)
    }

    fun insertTransaction(transactionID: String, userID: String, accountId: Int, amount: Double, description: String, dateTime: String, types: Int): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("transaction_id", transactionID)
            put("user_id", userID)
            put("account_id", accountId)
            put("description", description)
            put("amount", amount)
            put("date_time", dateTime)
            put("type", types)
        }
        return db.insert("Transactions", null, contentValues)
    }

    fun updateAccount(account: Account): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("user_id", account.user_id)
            put("account_type_id", account.account_type_id)
            put("balance", account.balance)
            put("date_opened", account.date_opened)
        }
        val rowsAffected = db.update("Accounts", values, "account_id = ?", arrayOf(account.account_id.toString()))
        Log.d("account", "Rows affected: $rowsAffected")
        return rowsAffected
    }


    fun clearAllData() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS Clients")
        db.execSQL("DROP TABLE IF EXISTS Accounts")
        db.execSQL("DROP TABLE IF EXISTS Transactions")
        db.execSQL("DROP TABLE IF EXISTS AccountTypes")
        createDb(db)
    }

    fun getAccount(userId: Int, accountId: Int): Account? {
        val db = this.readableDatabase
        var account: Account? = null
        val cursor = db.rawQuery(
            "SELECT * FROM Accounts WHERE user_id = ? AND account_id = ? LIMIT 1",
            arrayOf(userId.toString(), accountId.toString())
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                account = Account(
                    account_id = c.getInt(c.getColumnIndexOrThrow("account_id")),
                    user_id = c.getInt(c.getColumnIndexOrThrow("user_id")),
                    account_type_id = c.getInt(c.getColumnIndexOrThrow("account_type_id")),
                    balance = c.getDouble(c.getColumnIndexOrThrow("balance")),
                    date_opened = c.getString(c.getColumnIndexOrThrow("date_opened"))
                )
            }
        }
        return account
    }

    fun getClient(userId: Int?): Client? {
        val db = this.readableDatabase
        var client: Client? = null
        val cursor = db.rawQuery("SELECT * FROM Clients WHERE user_id = ?", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            client = Client(
                user_id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                dob = cursor.getString(cursor.getColumnIndexOrThrow("dob")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                first_name = cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                last_name = cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                amount = 0.0,
                address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
            )
        }
        cursor.close()
        return client
    }

    fun getClientAccountsByType(userId: Int?, accountTypeId: Int): List<Account> {
        val accountList = mutableListOf<Account>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM Accounts WHERE user_id = ? AND account_type_id = ?",
            arrayOf(userId.toString(), accountTypeId.toString())
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    accountList.add(
                        Account(
                            account_id = c.getInt(c.getColumnIndexOrThrow("account_id")),
                            user_id = c.getInt(c.getColumnIndexOrThrow("user_id")),
                            account_type_id = c.getInt(c.getColumnIndexOrThrow("account_type_id")),
                            balance = c.getDouble(c.getColumnIndexOrThrow("balance")),
                            date_opened = c.getString(c.getColumnIndexOrThrow("date_opened"))
                        )
                    )
                } while (c.moveToNext())
            }
        }
        return accountList
    }


    data class Client(
        val user_id: Int,
        var dob: String,
        var email: String,
        var first_name: String,
        var last_name: String,
        val amount: Double,
        var address: String
    )

    @Parcelize
    data class Account(val account_id: Int, val user_id: Int, val account_type_id: Int, val balance: Double, val date_opened: String) : Parcelable
}