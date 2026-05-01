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

    fun addFunds(num: Double, cont: Context, onComplete: (List<History>) -> Unit) {
        val db = AppDatabase.getDatabase(cont)
        val actionDao = db.actionDao()
        val balanceDao = db.balanceDao()

        kotlin.concurrent.thread {
            // 1. Update Balance
            val currentBalanceEntry = balanceDao.getBalance()
            val currentAmount = currentBalanceEntry?.amount ?: 0.0
            val newAmount = currentAmount + num
            balanceDao.updateBalance(Balance(id = 1, amount = newAmount))

            val newAction = ActionEntry(title = "Deposit", date = getCurrentDateTime())
            actionDao.insert(newAction)

            val allItems = actionDao.getAllActions()
            val hist = allItems.mapIndexed { index, action ->
                History(action.title, action.date, index)
            }

            balance = db.balanceDao().getBalance()?.amount ?: 0.0
            // 4. Send the result back to the UI
            onComplete(hist)
        }
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

    fun deductFunds(num: Double, cont: Context, onComplete: (List<History>) -> Unit) {
        val db = AppDatabase.getDatabase(cont)
        kotlin.concurrent.thread {
            val balanceDao = db.balanceDao()
            val current = balanceDao.getBalance()?.amount ?: 0.0

            if (current - num < 0) {
                Handler(Looper.getMainLooper()).post {
                Toast.makeText(cont, "Insufficient Funds", Toast.LENGTH_SHORT).show()
                    }
            }
            else {
                balanceDao.updateBalance(Balance(id = 1, amount = current - num))

                db.actionDao()
                    .insert(ActionEntry(title = "Withdrawal", date = getCurrentDateTime()))

                val allItems = db.actionDao().getAllActions()
                val hist = allItems.mapIndexed { index, action ->
                    History(action.title, action.date, index)
                }

                balance = db.balanceDao().getBalance()?.amount ?: 0.0

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