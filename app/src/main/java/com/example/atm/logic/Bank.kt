package com.example.atm

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.atm.objects.History
import com.example.sql.DBFunctions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Bank {
    var balance: Double = 0.0

    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("MM-dd-yy-HH-mm-ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    private fun getLoggedInUserId(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("LocalAuth", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }

    fun getBalance(cont: Context, acc: DBFunctions.Account?): Double {
        val db = DBFunctions.getInstance(cont)
        val userId = getLoggedInUserId(cont)

        val dbReadable = db.readableDatabase

        var balanceAmount = 0.0
        val cursor = dbReadable.rawQuery(
            "SELECT balance FROM Accounts WHERE account_id = ?",
            arrayOf(acc?.account_id.toString())
        )

        if (cursor.moveToFirst()) {
            balanceAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"))
        } else {
            cursor.close()
            db.insertAccount(
                userId = userId,
                accountTypeId = acc?.account_type_id ?: 0 ,
                balance = 0.0,
                dateOpened = getCurrentDateTime()
            )
            return 1000.0
        }
        cursor.close()
        return balanceAmount
    }

    fun getAccounts(cont: Context): List<com.example.sql.DBFunctions.Account> {
        val db = DBFunctions.getInstance(cont)
        val userId = getLoggedInUserId(cont)
        val dbReadable = db.readableDatabase
        val accountList = mutableListOf<com.example.sql.DBFunctions.Account>()

        val cursor = dbReadable.rawQuery(
            "SELECT * FROM Accounts WHERE user_id = ?",
            arrayOf(userId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val account = com.example.sql.DBFunctions.Account(
                    account_id = cursor.getInt(cursor.getColumnIndexOrThrow("account_id")),
                    user_id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    account_type_id = cursor.getInt(cursor.getColumnIndexOrThrow("account_type_id")),
                    balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance")),
                    date_opened = cursor.getString(cursor.getColumnIndexOrThrow("date_opened"))
                )
                accountList.add(account)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return accountList
    }

    fun getHistory(cont: Context, accountId: Int? = null, onComplete: (List<History>) -> Unit) {
        val db = DBFunctions.getInstance(cont)
        val userId = getLoggedInUserId(cont)
        val dbReadable = db.readableDatabase
        val historyList = mutableListOf<History>()

        val query = if (accountId != null) {
            "SELECT * FROM Transactions WHERE user_id = ? AND account_id = ?"
        } else {
            "SELECT * FROM Transactions WHERE user_id = ?"
        }

        val args = if (accountId != null) {
            arrayOf(userId.toString(), accountId.toString())
        } else {
            arrayOf(userId.toString())
        }

        val cursor = dbReadable.rawQuery(query, args)

        if (cursor.moveToFirst()) {
            do {
                val history = History(
                    txt = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    dte = cursor.getString(cursor.getColumnIndexOrThrow("date_time")),
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("transaction_id"))
                )
                historyList.add(history)
            } while (cursor.moveToNext())
        }
        cursor.close()

        historyList.sortByDescending { it.dte }
        onComplete(historyList)
    }

    fun addFunds(acc: Int, type: Int, num: Double, cont: Context, onComplete: (Boolean) -> Unit) {
        Log.d("adding", num.toString())
        val db = DBFunctions.getInstance(cont)
        val userId = getLoggedInUserId(cont)
        val accountTypeId = type
        val accs = db.getAccount(userId, acc)
        val currentAmount = accs?.balance
        val newAmount = currentAmount?.plus(num)

        val accountId = acc
        val updatedAccount = com.example.sql.DBFunctions.Account(
            account_id = accountId,
            user_id = userId,
            account_type_id = accountTypeId,
            balance = newAmount ?: 0.0,
            date_opened = getCurrentDateTime()
        )
        db.updateAccount(updatedAccount)

        db.insertTransaction(
            transactionID = System.currentTimeMillis().toString(),
            userID = userId.toString(),
            accountId = accountId,
            amount = num,
            description = "Deposit to ${acc.toString().replaceFirstChar { it.uppercase() }}",
            dateTime = getCurrentDateTime()
        )

        onComplete(true)
    }

    fun deductFunds(acc: Int, type: Int, num: Double, cont: Context, onComplete: (Boolean) -> Unit) {
        val db = DBFunctions.getInstance(cont)
        val userId = getLoggedInUserId(cont)
        val accountTypeId = type
        val accs = db.getAccount(userId, acc)
        val currentAmount = accs?.balance

        currentAmount?.minus(num)?.let {
            if (it < 0) {
                Toast.makeText(cont, "Insufficient Funds", Toast.LENGTH_SHORT).show()
                onComplete(false)
            } else {
                val newAmount = currentAmount?.minus(num)
                val accountId = acc

                val updatedAccount = com.example.sql.DBFunctions.Account(
                    account_id = accountId,
                    user_id = userId,
                    account_type_id = accountTypeId,
                    balance = newAmount ?: 0.0,
                    date_opened = getCurrentDateTime()
                )
                db.updateAccount(updatedAccount)

                db.insertTransaction(
                    transactionID = System.currentTimeMillis().toString(),
                    userID = userId.toString(),
                    accountId = accountId,
                    amount = num,
                    description = "Withdrawal from ${acc.toString().replaceFirstChar { it.uppercase() }}",
                    dateTime = getCurrentDateTime()
                )

                onComplete(true)
            }
        }
    }
}