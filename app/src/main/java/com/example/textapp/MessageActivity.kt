package com.example.textapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.textapp.AdapterClasses.ChatsAdapter
import com.example.textapp.ModelClasses.Chat
import com.example.textapp.ModelClasses.Users
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
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MessageActivity : AppCompatActivity() {
    var userIdVisit : String? = ""
    var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatsAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var recyclerview_mchat: RecyclerView
    var reference : DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@MessageActivity, WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recyclerview_mchat = findViewById(R.id.recyclerview_mchat)
        recyclerview_mchat.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerview_mchat.layoutManager = linearLayoutManager

        reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit!!)
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val user: Users? = snapshot.getValue(Users::class.java)
                val username_mchat : TextView = findViewById(R.id.username_mchat)
                username_mchat.text = user!!.getUsername()
                val profile_image_mchat : CircleImageView = findViewById(R.id.profile_image_mchat)
                Picasso.get().load(user.getProfile()).into(profile_image_mchat)

                retrieveMessages(firebaseUser!!.uid, userIdVisit, user.getProfile())
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        val send_message_btn : ImageView = findViewById(R.id.send_message_btn)
        send_message_btn.setOnClickListener {
            val text_message : EditText = findViewById(R.id.text_message)
            val message = text_message.text.toString()
            if (message == "")
            {
                Toast.makeText(this@MessageActivity, "Please write a message.. ", Toast.LENGTH_LONG).show()
            }
            else
            {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            text_message.setText("")
        }

        val attach_image_file_btn : ImageView = findViewById(R.id.attach_image_file_btn)
        attach_image_file_btn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"), 438)
        }

        seenMessage(userIdVisit!!)
    }


    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String)
    {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if(task.isSuccessful)
                {
                    val chatsListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit!!)
                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists())
                            {
                                chatsListReference.child("id").setValue(userIdVisit)
                            }
                            val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatList")
                                .child(userIdVisit!!)
                                .child(firebaseUser!!.uid)
                            chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                    val reference = FirebaseDatabase.getInstance().reference
                        .child("Users").child(firebaseUser!!.uid)

                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data!!.data!=null)
        {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Your Picture is Uploading")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            val uploadTask : StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask<Uri?>(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful)
                {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "Sent you an image"
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = ""
                    messageHashMap["messageId"] = messageId

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                }
            }
        }
    }

    private fun retrieveMessages(senderId: String, receiverId: String?, receiverImageUrl: String?)
    {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in snapshot.children)
                {
                    val chat = snapshot.getValue(Chat::class.java)

                    if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                        || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId))
                    {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = ChatsAdapter(this@MessageActivity, (mChatList as ArrayList<Chat>), receiverImageUrl!!)
                    recyclerview_mchat.adapter = chatsAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    var seenListener: ValueEventListener? = null
    private fun seenMessage(userId: String)
    {
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                for (dataSnapshot in snapshot.children)
                {
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId)){
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onPause() {
        super.onPause()

        reference!!.removeEventListener(seenListener!!)
    }
//    private fun retreiveMessages(senderId: String, receiverId: String?, receiverImageUrl: String?) {
//        mChatList = ArrayList()
//        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
//
//        reference.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                (mChatList as ArrayList<Text>).clear()
//                for (snapshot in snapshot.children)
//                {
//                    val text = snapshot.getValue(Text::class.java)
//
//                    if (text!!.getReceiver().equals(senderId) && text.getSender().equals(receiverId)
//                        || text.getReceiver().equals(receiverId) && text.getSender().equals(senderId))
//                    {
//                        (mChatList as ArrayList<Text>).add(text)
//                    }
//                    textsAdapter = TextsAdapter(this@MessageActivity, (mChatList as ArrayList<Text>), receiverImageUrl!!)
//                    recyclerview_mchat.adapter = textsAdapter
//
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
//    }
}