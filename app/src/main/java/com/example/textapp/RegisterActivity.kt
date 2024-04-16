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
import com.google.firebase.database.FirebaseDatabase
import org.bouncycastle.crypto.generators.SCrypt

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar : Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        val register_btn: Button = findViewById(R.id.register_btn)
        register_btn.setOnClickListener {
            registerUser()
        }
    }
    private fun hashPassword(password: String): String {
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val salt = ByteArray(16) // Generate salt bytes (you may need to use Firebase salt separator)
        val hashedPasswordBytes = SCrypt.generate(passwordBytes, salt, 16384, 8, 1, 32)
        return hashedPasswordBytes.joinToString("") { "%02x".format(it) }
    }

    private fun registerUser() {
        val username_register: EditText = findViewById(R.id.username_register)
        val email_register: EditText = findViewById(R.id.email_register)
        val password_register: EditText = findViewById(R.id.password_register)
        val username: String = username_register.text.toString().trim()
        val email: String = email_register.text.toString().trim()
        val password: String = password_register.text.toString().trim()

        // Input validation
        if (username.isEmpty()) {
            username_register.error = "Enter your Username"
            username_register.requestFocus()
            return
        }

        if (email.isEmpty()) {
            email_register.error = "Enter your Email"
            email_register.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_register.error = "Enter a valid Email address"
            email_register.requestFocus()
            return
        }

        if (password.isEmpty()) {
            password_register.error = "Enter your Password"
            password_register.requestFocus()
            return
        }

        if (password.length < 6) {
            password_register.error = "Password should be at least 6 characters long"
            password_register.requestFocus()
            return
        }

        val hashedPassword = hashPassword(password)

        mAuth.createUserWithEmailAndPassword(email, hashedPassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get the current user
                val user = mAuth.currentUser

                // Send email verification
                user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                    if (verificationTask.isSuccessful) {
                        // Verification email sent successfully
                        Toast.makeText(this@RegisterActivity, "Verification Email Sent", Toast.LENGTH_LONG).show()
                    } else {
                        // Verification email could not be sent
                        Toast.makeText(this@RegisterActivity, "Failed to Send Verification Email", Toast.LENGTH_LONG).show()
                    }
                }

                // Continue with user registration in the database
                firebaseUserID = user!!.uid
                refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                val userHashMap = HashMap<String, Any>()
                userHashMap["uid"] = firebaseUserID
                userHashMap["username"] = username
                userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/textapp-9eac0.appspot.com/o/profile.png?alt=media&token=174da491-112d-4d01-bdef-c844f0157e6f"
                userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/textapp-9eac0.appspot.com/o/cover.jpg?alt=media&token=b9633279-7994-469d-a692-d1f842b2ff04"
                userHashMap["status"] = "offline"
                userHashMap["search"] = username.toLowerCase()
                userHashMap["facebook"] = "https://www.facebook.com/"
                userHashMap["instagram"] = "https://www.instagram.com/"
                userHashMap["website"] = "https://www.google.com/"

                refUsers.updateChildren(userHashMap).addOnCompleteListener { databaseTask ->
                    if (databaseTask.isSuccessful) {
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        // Handle database registration error
                        Toast.makeText(this@RegisterActivity, "Error: ${databaseTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                // Handle registration error
                Toast.makeText(this@RegisterActivity, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

