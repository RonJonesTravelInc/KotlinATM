package com.example.atm

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
    var userId: Int? = 0
    val bank = Bank()

    override fun onResume() {
        super.onResume()
        userId = intent.getIntExtra("user_id", -1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)

        val intent = intent
        userId = intent.getIntExtra("user_id", -1)
        val account_id = intent.getIntExtra("account_id", 0)
        val account_type = intent.getIntExtra("account_type", 0)

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
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("user_id", userId)
            }
            startActivity(intent)
        }

        clear.setOnClickListener {
            txtView.text = ""
        }

        withdr.setOnClickListener {
            val amount = txtView.text.toString().toDouble()
            val defaultFormat = NumberFormat.getCurrencyInstance().format(amount)
            bank.deductFunds(account_id, account_type, amount, this) { isSuccess ->
                runOnUiThread {
                    Toast.makeText(this, "Withdrew: $defaultFormat", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@WithdrawActivity, MainActivity::class.java).apply {
                        putExtra("user_id", userId)
                    }
                    startActivity(intent)
                    this@WithdrawActivity.finish()
                }
            }
        }

    }

}