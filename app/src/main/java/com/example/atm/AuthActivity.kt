// AuthActivity.kt
package com.example.atm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sql.DBFunctions
import androidx.core.content.edit

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        val getFirstName = findViewById<EditText>(R.id.etFirstName)
        val getLastName = findViewById<EditText>(R.id.getLastName)
        val getEmail = findViewById<EditText>(R.id.getEmail)
        val getDob = findViewById<EditText>(R.id.getDob)
        val getAdress = findViewById<EditText>(R.id.getAddress)
        val getUsername = findViewById<EditText>(R.id.getUsername)
        val getPassword = findViewById<EditText>(R.id.getPassword)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        val dbFunctions = DBFunctions.getInstance(this)

        btnSignIn.setOnClickListener {
            val username = getUsername.text.toString().trim()
            val password = getPassword.text.toString().trim()
           //dbFunctions.clearAllData()
            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Error", "Please fill out both username and password fields.")
                return@setOnClickListener
            }

            val hashedPasswordInput = dbFunctions.hashPassword(password)
            val userId = dbFunctions.checkAuth(username, hashedPasswordInput)

            if (userId != -1) {
                navigateToMain(userId)
            } else {
                showAlert("Error", "Incorrect username or password.")
            }
        }

        btnSignUp.setOnClickListener {
            val username = getUsername.text.toString().trim()
            val password = getPassword.text.toString().trim()
            val firstName = getFirstName.text.toString().trim()
            val lastName = getLastName.text.toString().trim()
            val email = getEmail.text.toString().trim()
            val dob = getDob.text.toString().trim()
            val address = getAdress.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() ||
                lastName.isEmpty() || email.isEmpty() || dob.isEmpty() || address.isEmpty()
            ) {
                showAlert("Error", "All fields are required.")
                return@setOnClickListener
            }

            if (dbFunctions.isUsernameExists(username)) {
                showAlert("Account Exists", "An account already exists with this username.")
            } else {
                //hashed
                val hashedPassword = dbFunctions.hashPassword(password)

                val cli = dbFunctions.insertClient(
                    username = username,
                    passwordHash = hashedPassword,
                    dob = dob,
                    email = email,
                    fn = firstName,
                    ln = lastName,
                    addr = address
                )

                if (cli != -1L) {
                    showAlert("Success", "Account created successfully!")
                        navigateToMain(cli.toInt())
                } else {
                    showAlert("Error", "Database insertion error occurred.")
                }
            }
        }
    }

    private fun showAlert(title: String, message: String, onDismiss: (() -> Unit)? = null) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToMain(userId: Int) {
        val sharedPreferences = this.getSharedPreferences("LocalAuth", Context.MODE_PRIVATE)

        sharedPreferences.edit { putInt("user_id", userId) }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("user_id", userId)
        }
        startActivity(intent)
        finish()
    }

    fun deleteData() {
        val dbFunctions = DBFunctions.getInstance(this)
        dbFunctions.clearAllData()
    }
}