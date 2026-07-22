package com.example.atm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atm.adapters.AccountsAdapter
import com.example.sql.DBFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountsActivity : AppCompatActivity() {

    private var page: String? = null
    private var userId: Int = -1
    private var account_type: Int = 1

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        page = intent.getStringExtra("page")
        userId = intent.getIntExtra("user_id", -1)
        account_type = intent.getIntExtra("account_type", -1)

        val accounts = DBFunctions.getInstance(this@AccountsActivity).getClientAccountsByType(
            userId,
            accountTypeId = account_type
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = AccountsAdapter(accounts) { selectedAccount ->
            navigateToDestination(selectedAccount)
        }
    }

    private fun navigateToDestination(account: DBFunctions.Account) {
        when (page) {
            "deposit" -> {
                val intent = Intent(this, DepositActivity::class.java).apply {
                    putExtra("account_id", account.account_id)
                    putExtra("account_type", account.account_type_id)
                    putExtra("user_id", userId)
                }
                startActivity(intent)
                finish()
            }
            "withdraw" -> {
                val intent = Intent(this, WithdrawActivity::class.java).apply {
                    putExtra("account_id", account.account_id)
                    putExtra("account_type", account.account_type_id)
                    putExtra("user_id", userId)
                }
                startActivity(intent)
                finish()
            }
            else -> {
                lifecycleScope.launch {
                    val amount = withContext(Dispatchers.IO) {
                        Bank().getBalance(this@AccountsActivity, account)
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