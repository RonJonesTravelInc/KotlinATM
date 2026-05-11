package com.example.atm

import Bank
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 4. Extract the amount (or default to 0.0 if the table is empty)
        val intent = intent
        val amount = intent.getDoubleExtra("amount", 1000000.0)
        val txtView = findViewById<TextView>(R.id.textView)
        val hist = findViewById<Button>(R.id.history)
        val balance = findViewById<Button>(R.id.balance)
        val depo = findViewById<Button>(R.id.deposit)
        val withdr = findViewById<Button>(R.id.withdraw)
        val help = findViewById<Button>(R.id.help)

        if (intent.hasExtra("amount")) {
            val amount = intent.getDoubleExtra("amount", 0.0)
            txtView.text = "Your balance is ${NumberFormat.getCurrencyInstance().format(amount)}"
        } else {
            txtView.text = "Welcome!"
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
            val intent = Intent(this, ChooseActivity::class.java)
            intent.putExtra("from_page", "balance");
            startActivity(intent)
        }

    }
}