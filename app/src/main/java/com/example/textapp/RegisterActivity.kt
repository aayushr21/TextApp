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

    private fun registerUser() {
        val username_register: EditText = findViewById(R.id.username_register)
        val email_register: EditText = findViewById(R.id.email_register)
        val password_register: EditText = findViewById(R.id.password_register)
        val username: String = username_register.text.toString()
        val email: String = email_register.text.toString()
        val password: String = password_register.text.toString()

        if (username.equals(""))
        {
            Toast.makeText(this@RegisterActivity, "Enter your Username", Toast.LENGTH_LONG).show()
        }
        else if (email.equals(""))
        {
            Toast.makeText(this@RegisterActivity, "Enter your Email", Toast.LENGTH_LONG).show()
        }
        else if (password.equals(""))
        {
            Toast.makeText(this@RegisterActivity, "Enter your Password", Toast.LENGTH_LONG).show()
        }
        else
        {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
                if (task.isSuccessful)
                {
                    firebaseUserID = mAuth.currentUser!!.uid
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

                    refUsers.updateChildren(userHashMap).addOnCompleteListener { task ->
                        if (task.isSuccessful)
                        {
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
                else
                {
                    Toast.makeText(this@RegisterActivity, "Error Occurred" + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}