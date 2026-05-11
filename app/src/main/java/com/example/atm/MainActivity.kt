package com.example.atm

import Bank
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.atm.adapters.HisotryAdapter
import com.example.atm.database.AppDatabase
import com.example.atm.objects.History
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 4. Extract the amount (or default to 0.0 if the table is empty)

        val txtView = findViewById<TextView>(R.id.textView)
        val hist = findViewById<Button>(R.id.history)
        val balance = findViewById<Button>(R.id.balance)
        val depo = findViewById<Button>(R.id.deposit)
        val withdr = findViewById<Button>(R.id.withdraw)
        val help = findViewById<Button>(R.id.help)

        kotlin.concurrent.thread {
            val bank = Bank()
            val currentAmount = bank.getBalance(this, "checkings")
            val defaultFormat = NumberFormat.getCurrencyInstance().format(currentAmount)

            runOnUiThread {
                val txtView = findViewById<TextView>(R.id.textView)
                txtView.text = "Your checkings balance is: $defaultFormat"
                android.widget.Toast.makeText(this, "${"checkings".replaceFirstChar { it.uppercase() }} balance updated", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        help.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // Only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf("ron.jones@travelinc.com", "rich.blaske@travelinc.com"))
                putExtra(Intent.EXTRA_SUBJECT, "New atm message")
                putExtra(Intent.EXTRA_TEXT, "This is a message from the atm app")
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
            else {
                Toast.makeText(this, "No email account", Toast.LENGTH_SHORT).show()
            }
        }


        hist.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        depo.setOnClickListener {
            val intent = Intent(this, ChooseActivity::class.java)
            intent.putExtra("from_page", "deposit");
            startActivity(intent)
        }

        withdr.setOnClickListener {
            val intent = Intent(this, ChooseActivity::class.java)
            intent.putExtra("from_page", "withdraw");
            startActivity(intent)
        }

        balance.setOnClickListener {
            val accounts = arrayOf("Checkings", "Savings")

            android.app.AlertDialog.Builder(this)
                .setTitle("Select Account")
                .setItems(accounts) { _, which ->
                    // 'which' is the index (0 for Checkings, 1 for Savings)
                    val acc = if (which == 0) "checkings" else "savings"

                    // Now run your existing logic with the chosen 'acc'
                    kotlin.concurrent.thread {
                        val bank = Bank()
                        val currentAmount = bank.getBalance(this, acc)
                        val defaultFormat = NumberFormat.getCurrencyInstance().format(currentAmount)

                        runOnUiThread {
                            txtView.text = "Your balance is: $defaultFormat"
                            android.widget.Toast.makeText(this, "${acc.replaceFirstChar { it.uppercase() }} balance updated", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .show()
        }

    }


    override fun onResume() {
        super.onResume()
        kotlin.concurrent.thread {
            val bank = Bank()
            val currentAmount = bank.getBalance(this, "checkings")
            val defaultFormat = NumberFormat.getCurrencyInstance().format(currentAmount)

            runOnUiThread {
                val txtView = findViewById<TextView>(R.id.textView)
                txtView.text = "Your checkings balance is: $defaultFormat"
                android.widget.Toast.makeText(this, "${"checkings".replaceFirstChar { it.uppercase() }} balance updated", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}