package com.example.atm

import Bank
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import com.example.atm.adapters.GridAdapter
import com.example.atm.objects.NumBtn
import java.text.NumberFormat

class WithdrawActivity : AppCompatActivity() {

    val bank = Bank()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)

        val intent = intent
        val account_name = intent.getStringExtra("account")

        var btnList = listOf<Button>(
            NumBtn(this, 0), NumBtn(this, 1),
            NumBtn(this, 2), NumBtn(this, 3), NumBtn(this, 4),
            NumBtn(this, 5), NumBtn(this, 6), NumBtn(this, 7), NumBtn(this, 8),
            NumBtn(this, 9), NumBtn(this, 20), NumBtn(this, 100)
        )

        val gridView = findViewById<GridView>(R.id.grid)
        val back = findViewById<Button>(R.id.back)
        val txtView = findViewById<TextView>(R.id.textView)
        val clear = findViewById<Button>(R.id.clear)
        val withdr = findViewById<Button>(R.id.withdraw)

        gridView.adapter = GridAdapter(btnList, this) { clickedText ->
            var currentText = txtView.text.toString()

            if (currentText.contains("Enter")) {
                currentText = ""
                txtView.text = ""
            }

            txtView.text = currentText + clickedText
        }

        back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        clear.setOnClickListener {
            txtView.text = ""
        }

        withdr.setOnClickListener {
            val amount = txtView.text.toString().toDouble()
            val defaultFormat = NumberFormat.getCurrencyInstance().format(amount)
            // This runs when they press "OK"
            if (account_name != null) {
                bank.deductFunds(account_name, amount, this) { updatedHistory ->
                    runOnUiThread {
                        Toast.makeText(this, "Withdrew: $defaultFormat", Toast.LENGTH_SHORT).show()
                        txtView.text = "Enter"
                    }
                }
            }
        }

    }

}