package com.example.atm

import Bank
import android.os.Bundle
import android.widget.Button
import android.widget.GridView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.atm.R
import com.example.atm.adapters.HisotryAdapter
import com.example.atm.databinding.ActivityHistoryBinding
import com.example.atm.objects.History

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val gridView = findViewById<GridView>(R.id.grid)
        val bank = Bank()

        val back = findViewById<Button>(R.id.backButton)

        back.setOnClickListener {
            finish()
        }

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

        bank.getHistory(this) { historyArray ->
            // 2. Wait for the background thread to finish, then jump to UI thread
            runOnUiThread {
                // 3. NOW set the adapter because we finally have the data
                gridView.adapter = HisotryAdapter(historyArray, this)
            }
        }

        gridView.adapter = HisotryAdapter(history, this)
    }


}