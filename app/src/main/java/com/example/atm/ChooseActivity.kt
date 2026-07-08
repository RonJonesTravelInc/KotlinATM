package com.example.atm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sql.DBFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseActivity : AppCompatActivity() {
    var page = ""
    var userId: Int? = 0

    override fun onResume() {
        super.onResume()
        userId = intent.getIntExtra("user_id", -1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)
        userId = intent.getIntExtra("user_id", -1)
        Log.d("id", userId.toString())
        val fromPage = intent.getStringExtra("from_page")
        if (fromPage != null) {
            page = fromPage
        }

        val checkings = findViewById<ImageButton>(R.id.checkings)
        val savings = findViewById<ImageButton>(R.id.savings)
        val backs = findViewById<Button>(R.id.back)

        backs.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        //1 = Checking, 2 = Savings
        checkings.setOnClickListener { handleAccountClick(1) }
        savings.setOnClickListener { handleAccountClick(2) }
    }

    private fun handleAccountClick(accountType: Int) {
        lifecycleScope.launch {
            val accounts = withContext(Dispatchers.IO) {
                //get all accounts
                DBFunctions.getInstance(this@ChooseActivity).getClientAccountsByType(userId, accountType)
            }

            if (accounts.isEmpty()) {
                val typeName = if (accountType == 1) "Checking" else "Savings"
                AlertDialog.Builder(this@ChooseActivity)
                    .setTitle("No Account Found")
                    .setMessage("You don't have any active $typeName accounts.")
                    .setPositiveButton("OK", null)
                    .show()
                return@launch
            }

            if (accounts.size == 1) {
                navigateToDestination(accounts[0], accountType)
            } else {
                showAccountSelectionDialog(accounts, accountType)
            }
        }
    }

    private fun showAccountSelectionDialog(accounts: List<DBFunctions.Account>, accountType: Int) {
        //looked up the formatting
        val accountLabels = accounts.map { "Account #${it.account_id} (Balance: \$${String.format("%.2f", it.balance)})" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select an Account")
            .setItems(accountLabels) { _, ind ->
                val selectedAccount = accounts[ind]
                navigateToDestination(selectedAccount, accountType)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToDestination(account: DBFunctions.Account, accountType: Int) {
        when (page) {
            "deposit" -> {
                val intent = Intent(this, DepositActivity::class.java).apply {
                    putExtra("account_id", account.account_id)
                    putExtra("account_type", accountType)
                    putExtra("user_id", userId)
                }
                startActivity(intent)
            }
            "withdraw" -> {
                val intent = Intent(this, WithdrawActivity::class.java).apply {
                    putExtra("account_id", account.account_id)
                    putExtra("account_type", accountType)
                    putExtra("user_id", userId)
                }
                startActivity(intent)
            }
            else -> {
                lifecycleScope.launch {
                    val amount = withContext(Dispatchers.IO) {
                        Bank().getBalance(this@ChooseActivity, account)
                    }
                    val resultIntent = Intent().apply {
                        putExtra("amount", amount)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }
}