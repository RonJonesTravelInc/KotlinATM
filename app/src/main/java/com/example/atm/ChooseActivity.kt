package com.example.atm

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity


class ChooseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)

        val intent = intent
        val fromPage = intent.getStringExtra("from_page")

        val checkings = findViewById<ImageButton>(R.id.checkings)
        val savings = findViewById<ImageButton>(R.id.savings)

        checkings.setOnClickListener {
            if (fromPage == "deposit") {
                val intent = Intent(this, DepositActivity::class.java)
                intent.putExtra("account", "checkings");
                startActivity(intent)
            }
            else {
                val intent = Intent(this, WithdrawActivity::class.java)
                intent.putExtra("account", "checkings");
                startActivity(intent)
            }
        }

        savings.setOnClickListener {
            if (fromPage == "deposit") {
                val intent = Intent(this, DepositActivity::class.java)
                intent.putExtra("account", "savings");
                startActivity(intent)
            }
            else {
                val intent = Intent(this, WithdrawActivity::class.java)
                intent.putExtra("account", "savings");
                startActivity(intent)
            }
        }

    }
}