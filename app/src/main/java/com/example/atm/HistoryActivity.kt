
package com.example.atm

import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import com.example.atm.adapters.HisotryAdapter
import com.example.sql.DBFunctions
import com.google.android.material.bottomnavigation.BottomNavigationView

class HistoryActivity : AppCompatActivity() {

    lateinit var accounts: List<DBFunctions.Account>

    override fun onResume() {
        super.onResume()
        setUpAccounts()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        val back = findViewById<Button>(R.id.backButton)
        back.setOnClickListener {
            finish()
        }

         setUpAccounts()

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == 0) {
                loadHistory(null)
            } else {
                loadHistory(item.itemId)
            }
            true
        }

        loadHistory(null)
    }

    fun loadHistory(accountId: Int?) {
        val bank = Bank()
        val gridView = findViewById<GridView>(R.id.grid)

        bank.getHistory(this, accountId) { historyArray ->
            runOnUiThread {
                gridView.adapter = HisotryAdapter(historyArray, this@HistoryActivity)
            }
        }
    }

    fun setUpAccounts() {
        val bank = Bank()
        accounts = bank.getAccounts(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.menu.clear()

        accounts = accounts.take(5)

        accounts.forEach { account ->
            val typeLabel = if (account.account_type_id == 1) "Checking" else "Savings"
            bottomNav.menu.add(Menu.NONE, account.account_id, Menu.NONE, "$typeLabel (${account.account_id})")
        }
    }
}