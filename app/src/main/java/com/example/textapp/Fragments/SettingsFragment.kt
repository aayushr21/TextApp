package com.example.textapp.Fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.textapp.ModelClasses.Users
import com.example.textapp.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment()
{
    var usersReference: DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null
    private val RequestCode = 987
    private var imageUri: Uri? = null
    private var storageRef : StorageReference? = null
    private var coverExaminer : String? = null


//    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//        if (it.resultCode == Activity.RESULT_OK)
//        {
//            val data: Intent? = it.data
//        }
//
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Assign the current user to firebaseUser
        firebaseUser = FirebaseAuth.getInstance().currentUser
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        // Check if firebaseUser is not null before accessing its properties
        if (firebaseUser != null) {
            usersReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            usersReference!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user: Users? = snapshot.getValue(Users::class.java)
                        if (context != null) {
                            val settings_un: TextView = view.findViewById(R.id.settings_un)
                            settings_un.text = user!!.getUsername()
                            val profileImageSettings: CircleImageView = view.findViewById(R.id.profile_image_settings)
                            Picasso.get().load(user.getProfile()).into(profileImageSettings)
                            val cover_settings: ImageView = view.findViewById(R.id.cover_settings)
                            Picasso.get().load(user.getCover()).into(cover_settings)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }

        val profileImageSettings: CircleImageView = view.findViewById(R.id.profile_image_settings)
        profileImageSettings.setOnClickListener {
            pickImage()
        }

        val cover_settings: ImageView = view.findViewById(R.id.cover_settings)
        cover_settings.setOnClickListener {
            coverExaminer = "cover"
            pickImage()
        }

        return view
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
//        getContent.launch(intent)
        startActivityForResult(intent, RequestCode )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data?.data!=null)
        {
            imageUri = data.data
            Toast.makeText(context, "Transferring", Toast.LENGTH_LONG).show()
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase()
    {
        val progressBar = ProgressDialog(requireContext())
        progressBar.setMessage("Your Picture is Uploading")
        progressBar.show()

        if (imageUri != null) {
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
            val uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }

                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if (coverExaminer == "cover") {
                        val map = HashMap<String, Any>()
                        map["cover"] = url
                        usersReference!!.updateChildren(map).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                progressBar.dismiss()
                            } else {
                                handleUploadFailure(progressBar)
                            }
                        }
                        coverExaminer = ""
                    } else {
                        val mapProfileImg = HashMap<String, Any>()
                        mapProfileImg["profile"] = url
                        usersReference!!.updateChildren(mapProfileImg).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                progressBar.dismiss()
                            } else {
                                handleUploadFailure(progressBar)
                            }
                        }
                        coverExaminer = ""
                    }
                } else {
                    handleUploadFailure(progressBar)
                }
            }
        } else {
            progressBar.dismiss()
            Toast.makeText(context, "Image URI is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleUploadFailure(progressBar: ProgressDialog) {
        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
        progressBar.dismiss()
    }
}