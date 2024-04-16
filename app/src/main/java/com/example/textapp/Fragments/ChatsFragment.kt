package com.example.textapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.textapp.AdapterClasses.UserAdapter
import com.example.textapp.ModelClasses.Chat
import com.example.textapp.ModelClasses.ChatList
import com.example.textapp.ModelClasses.Users
import com.example.textapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatsFragment : Fragment()
{
    private var userAdapter: UserAdapter? = null
    private var mUsers: ArrayList<Users> = ArrayList()
    private var usersChatList: ArrayList<ChatList> = ArrayList()
    lateinit var recyclerview_chatlist: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        recyclerview_chatlist = view.findViewById(R.id.recyclerview_chatlist)
        recyclerview_chatlist.setHasFixedSize(true)
        recyclerview_chatlist.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                (usersChatList as ArrayList).clear()
                for (dataSnapshot in snapshot.children)
                {
                    val chatList = dataSnapshot.getValue(ChatList::class.java)
                    (usersChatList as ArrayList).add(chatList!!)
                }
                retreiveChatLists()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        return view
    }

    private fun retreiveChatLists()
    {
        mUsers = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                (mUsers as ArrayList).clear()
                for (dataSnapshot in snapshot.children)
                {
                    val user = dataSnapshot.getValue(Users::class.java)

                    for (eachChatList in usersChatList!!)
                    {
                        if (user!!.getUID().equals(eachChatList.getId()))
                        {
                            (mUsers as ArrayList).add(user!!)
                        }
                    }
                }
                userAdapter = UserAdapter(context!!, (mUsers as ArrayList<Users>), true)
                recyclerview_chatlist.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}