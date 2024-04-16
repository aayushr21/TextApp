package com.example.textapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

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

    private fun loginUser()
    {
        val email_login: EditText = findViewById(R.id.email_login)
        val password_login: EditText = findViewById(R.id.password_login)
        val email: String = email_login.text.toString()
        val password: String = password_login.text.toString()

        if (email.equals("")) {
            Toast.makeText(this@LoginActivity, "Enter your Email", Toast.LENGTH_LONG).show()
        } else if (password.equals("")) {
            Toast.makeText(this@LoginActivity, "Enter your Password", Toast.LENGTH_LONG).show()
        }
        else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    Toast.makeText(this@LoginActivity, "Error Occurred" + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}