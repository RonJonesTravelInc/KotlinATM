package com.example.atm

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sql.DBFunctions
import java.text.NumberFormat

class MainActivity : ComponentActivity() {

    private var currentAmount: Double = 1000000.0
    var userId: Int? = 0
    private val chooseActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
             currentAmount = data?.getDoubleExtra("amount", 0.0) ?: 0.0
            Log.d("Amount2", currentAmount.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        val txtView = findViewById<TextView>(R.id.textView)
        userId = intent.getIntExtra("user_id", -1)

        if (intent.hasExtra("amount")) {
            currentAmount = intent.getDoubleExtra("amount", 0.0)
            txtView.text = "Your balance is ${NumberFormat.getCurrencyInstance().format(currentAmount)}"
        } else if (currentAmount != 0.0) {
            txtView.text = "Your balance is ${NumberFormat.getCurrencyInstance().format(currentAmount)}"
        } else {
            txtView.text = "Welcome!"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        currentAmount = intent.getDoubleExtra("amount", 0.0)
        userId = intent.getIntExtra("user_id", -1)

        if (userId != null) {
            val dbFunctions = DBFunctions.getInstance(this)
            val currentClient = dbFunctions.getClient(userId)

            if (currentClient != null) {
                currentAmount = currentClient.amount
            }
        }

        val addAcc = findViewById<TextView>(R.id.addAcc)
        val txtView = findViewById<TextView>(R.id.textView)
        val hist = findViewById<Button>(R.id.history)
        val balance = findViewById<Button>(R.id.balance)
        val depo = findViewById<Button>(R.id.deposit)
        val withdr = findViewById<Button>(R.id.withdraw)
        val help = findViewById<Button>(R.id.help)

        if (intent.hasExtra("amount")) {
            val amountExtra = intent.getDoubleExtra("amount", 0.0)
            txtView.text = "Your balance is ${NumberFormat.getCurrencyInstance().format(amountExtra)}"
        } else if (currentAmount != 0.0) {
            txtView.text = "Your balance is ${NumberFormat.getCurrencyInstance().format(currentAmount)}"
        } else {
            txtView.text = "Welcome!"
        }

        addAcc.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setTitle("Open a New Account")
            builder.setMessage("Which type of account would you like to open?")

            builder.setPositiveButton("Checking") { dialog, which ->
                val db = DBFunctions.getInstance(this)

                // SQLite will auto-increment account_id automatically on insertion
                val result = db.insertAccount(
                    userId = userId,
                    accountTypeId = 1, // 1 = Checking
                    balance = 0.0,
                    dateOpened = java.util.Date().toString()
                )

                if (result != -1L) {
                    Toast.makeText(this, "New Checking Account created successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to create account.", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Savings") { dialog, which ->
                val db = DBFunctions.getInstance(this)

                // SQLite will auto-increment account_id automatically on insertion
                val result = db.insertAccount(
                    userId = userId,
                    accountTypeId = 2, // 2 = Savings
                    balance = 0.0,
                    dateOpened = java.util.Date().toString()
                )

                if (result != -1L) {
                    Toast.makeText(this, "New Savings Account created successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to create account.", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNeutralButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }

        help.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("ron.jones@travelinc.com", "rich.blaske@travelinc.com"))
                putExtra(Intent.EXTRA_SUBJECT, "New atm message")
                putExtra(Intent.EXTRA_TEXT, "This is a message from the atm app")
            }
            startActivity(intent)
        }

        hist.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java).apply {
                putExtra("user_id", userId)
            }
            startActivity(intent)
        }

        depo.setOnClickListener {
            val intent = Intent(this, ChooseActivity::class.java).apply {
                putExtra("from_page", "deposit")
                putExtra("user_id", userId)
            }
            chooseActivityLauncher.launch(intent)
        }

        withdr.setOnClickListener {
            val intent = Intent(this, ChooseActivity::class.java).apply {
                putExtra("from_page", "withdraw")
                putExtra("user_id", userId)
            }
            chooseActivityLauncher.launch(intent)
        }

        balance.setOnClickListener {
            val intent = Intent(this, ChooseActivity::class.java).apply {
                putExtra("from_page", "balance")
                putExtra("user_id", userId)
            }
            chooseActivityLauncher.launch(intent)
            Log.d("Amount", currentAmount.toString())
        }
    }
}