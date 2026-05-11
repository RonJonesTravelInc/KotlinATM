import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import com.example.atm.database.ActionEntry
import com.example.atm.database.AppDatabase
import com.example.atm.database.Balance
import com.example.atm.objects.History
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

    fun getBalance(cont: Context, acc: String): Double {
        val db = AppDatabase.getDatabase(cont)

        // Map the string input to your database IDs
        var accountId = 1

        if (acc == "savings") {
            accountId = 2
        }
        // Call the DAO method that fetches by ID
        val balanceEntry = db.balanceDao().getBalanceById(accountId)

        return balanceEntry?.amount ?: 0.0
    }




    fun getHistory(cont: Context, onComplete: (List<History>) -> Unit) {
        val db = AppDatabase.getDatabase(cont)
        val actionDao = db.actionDao()

        kotlin.concurrent.thread {
            // 1. Get the raw entries from Room
            val entries = actionDao.getAllActions()

            // 2. Convert ActionEntry objects into History objects
            val historyList = entries.mapIndexed { index, action ->
                History(
                    txt = action.title,
                    dte = action.date,
                    id = action.id // or use 'index' if you want the list position
                )
            }

            // 3. Send the completed list back
            onComplete(historyList)
        }
    }

    fun addFunds(acc: String, num: Double, cont: Context, onComplete: (List<History>) -> Unit) {
        val db = AppDatabase.getDatabase(cont)
        val actionDao = db.actionDao()
        val balanceDao = db.balanceDao()
        val bank = Bank()

        kotlin.concurrent.thread {
            var accountId = 1
            if (acc == "savings") {
                accountId = 2
            }

            val currentBalanceEntry = bank.getBalance(cont, acc)
            val currentAmount = currentBalanceEntry
            val newAmount = currentAmount + num

            balanceDao.updateBalance(Balance(id = accountId, amount = newAmount))

            val newAction = ActionEntry(
                title = "Deposit to ${acc.replaceFirstChar { it.uppercase() }}",
                date = getCurrentDateTime()
            )
            actionDao.insert(newAction)

            // 4. Retrieve history and map to UI model
            val allItems = actionDao.getAllActions()
            val hist = allItems.mapIndexed { index, action ->
                History(action.title, action.date, index)
            }

            // 5. Return the result
            onComplete(hist)
        }
    }

    fun deductFunds(acc: String, num: Double, cont: Context, onComplete: (List<History>) -> Unit) {
        val db = AppDatabase.getDatabase(cont)
        val actionDao = db.actionDao()
        val balanceDao = db.balanceDao()
        val bank = Bank()

        kotlin.concurrent.thread {
            // 1. Determine ID based on account type
            val accountId = if (acc.lowercase() == "savings") 2 else 1

            // 2. Fetch current balance using your helper
            val currentAmount = bank.getBalance(cont, acc)

            // 3. Check for insufficient funds
            if (currentAmount - num < 0) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(cont, "Insufficient Funds", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                // 4. Perform the deduction
                val newAmount = currentAmount - num
                balanceDao.updateBalance(Balance(id = accountId, amount = newAmount))

                // 5. Log the action with the account name
                val newAction = ActionEntry(
                    title = "Withdrawal from ${acc.replaceFirstChar { it.uppercase() }}",
                    date = getCurrentDateTime()
                )
                actionDao.insert(newAction)

                // 6. Retrieve updated history
                val allItems = actionDao.getAllActions()
                val hist = allItems.mapIndexed { index, action ->
                    History(action.title, action.date, index)
                }

                // 7. Return the result to the UI
                onComplete(hist)
            }
        }
    }

     fun showTransactionDialog(cont: Context, title: String, onConfirm: (Double) -> Unit) {
        val builder = AlertDialog.Builder(cont)
        builder.setTitle(title)

        // Create the input field
        val input = EditText(cont)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Enter amount"
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { _, _ ->
            val amount = input.text.toString().toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                onConfirm(amount)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}