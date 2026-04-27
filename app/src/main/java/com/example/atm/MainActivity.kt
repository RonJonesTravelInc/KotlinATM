package com.example.atm

import Bank
import android.os.Bundle
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.atm.adapters.HisotryAdapter
import com.example.atm.database.AppDatabase
import com.example.atm.objects.History

class MainActivity : ComponentActivity() {

    val bank = Bank()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val txtView = findViewById<TextView>(R.id.textView)
        val gridView = findViewById<GridView>(R.id.grid)
        val balance = findViewById<Button>(R.id.checkBalance)
        val increase = findViewById<Button>(R.id.deposit)
        val deduct = findViewById<Button>(R.id.withdraw)

        bank.getHistory(this) { historyArray ->
            // 2. Wait for the background thread to finish, then jump to UI thread
            runOnUiThread {
                // 3. NOW set the adapter because we finally have the data
                gridView.adapter = HisotryAdapter(historyArray, this)
            }
        }


        //set the list for the ui to see
        var history: List<History> = emptyList()
        val histo = bank.getHistory(this) { historyArray ->
            runOnUiThread {
                history = historyArray
            }
        }

        balance.setOnClickListener {
            // 1. Get the database instance
            val db = AppDatabase.getDatabase(this)

            // 2. Launch background thread to avoid "Main Thread" crashes
            kotlin.concurrent.thread {
                // 3. Fetch the balance object from the DB
                val balanceEntry = db.balanceDao().getBalance()

                // 4. Extract the amount (or default to 0.0 if the table is empty)
                val currentAmount = balanceEntry?.amount ?: 0.0

                // 5. Switch back to the UI thread to update the TextView
                runOnUiThread {
                    txtView.text = "Your balance is: $$currentAmount"
                }
            }
        }

        increase.setOnClickListener {
            bank.showTransactionDialog(this,"Deposit Funds") { amount ->
                // This runs when they press "OK"
                bank.addFunds(amount, this) { updatedHistory ->
                    runOnUiThread {
                        Toast.makeText(this, "Deposited: $$amount", Toast.LENGTH_SHORT).show()
                        bank.getHistory(this) { historyArray ->
                            // This runs once the database finishes reading
                            runOnUiThread {
                                history = historyArray
                                gridView.adapter = HisotryAdapter(history, this)
                            }
                        }
                    }
                }
            }
        }

        deduct.setOnClickListener {
            bank.showTransactionDialog(this,"Withdraw Funds") { amount ->
                // This runs when they press "OK"
                bank.deductFunds(amount, this) { updatedHistory ->
                    runOnUiThread {
                        Toast.makeText(this, "Withdrew: $$amount", Toast.LENGTH_SHORT).show()
                        bank.getHistory(this) { historyArray ->
                            // This runs once the database finishes reading
                            runOnUiThread {
                                history = historyArray
                                gridView.adapter = HisotryAdapter(history, this)
                            }
                        }

                    }
                }
            }
        }


        gridView.adapter = HisotryAdapter(history, this)
    }
}