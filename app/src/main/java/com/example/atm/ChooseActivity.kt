package com.example.atm

import Bank
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ChooseActivity : AppCompatActivity() {
    var page = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)

        val intent = intent
        val fromPage = intent.getStringExtra("from_page")
        if (fromPage != null) {
            page = fromPage
        }
        val checkings = findViewById<ImageButton>(R.id.checkings)
        val savings = findViewById<ImageButton>(R.id.savings)
        val backs = findViewById<Button>(R.id.back)

        backs.setOnClickListener {
            finish()
        }

        checkings.setOnClickListener { handleAccountClick("checkings") }
        savings.setOnClickListener { handleAccountClick("savings") }
    }

    fun handleAccountClick(accountType: String) {
        when (page) {
            "deposit" -> {
                val intent = Intent(this, DepositActivity::class.java).apply {
                    putExtra("account", accountType)
                }
                startActivity(intent)
            }
            "withdraw" -> {
                val intent = Intent(this, WithdrawActivity::class.java).apply {
                    putExtra("account", accountType)
                }
                startActivity(intent)
            }
            else -> {
                lifecycleScope.launch {
                    // Perform IO work
                    val amount = withContext(Dispatchers.IO) {
                        Bank().getBalance(this@ChooseActivity, accountType)
                    }
                    // Back on Main thread automatically after withContext
                    val intent = Intent(this@ChooseActivity, MainActivity::class.java)
                    intent.putExtra("amount", amount)
                    startActivity(intent)
                }
            }
        }
    }
}