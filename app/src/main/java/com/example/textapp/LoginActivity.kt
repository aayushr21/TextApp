package com.example.textapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val toolbar : Toolbar = findViewById(R.id.toolbar_login)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Login"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        val login_btn: Button = findViewById(R.id.login_btn)
        login_btn.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email_login: EditText = findViewById(R.id.email_login)
        val password_login: EditText = findViewById(R.id.password_login)
        val email: String = email_login.text.toString().trim()
        val password: String = password_login.text.toString().trim()

        // Input validation
        if (email.isEmpty()) {
            email_login.error = "Enter your Email"
            email_login.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_login.error = "Enter a valid Email address"
            email_login.requestFocus()
            return
        }

        if (password.isEmpty()) {
            password_login.error = "Enter your Password"
            password_login.requestFocus()
            return
        }

        // Proceed with user login
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                // Handle login error
                Toast.makeText(this@LoginActivity, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
