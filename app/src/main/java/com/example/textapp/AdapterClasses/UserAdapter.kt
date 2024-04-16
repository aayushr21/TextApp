package com.example.textapp.AdapterClasses

import com.example.textapp.MessageActivity
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.textapp.ModelClasses.Users
import com.example.textapp.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(mContext: android.content.Context, mUsers: List<Users>, isChatCheck: Boolean )
    : RecyclerView.Adapter<UserAdapter.ViewHolder?>()
{
    private val mContext: android.content.Context
    private val mUsers: List<Users>
    private var isChatCheck: Boolean
    init {
        this.mUsers = mUsers
        this.mContext = mContext
        this.isChatCheck = isChatCheck

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val user: Users = mUsers[position]
        holder.usernameTxt.text = user!!.getUsername()
        Picasso.get().load(user.getProfile()).into(holder.profileImageView)
        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "No, Take me back"
            )
            val builder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(mContext)
            builder.setTitle("Please, Select an option!")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, position ->
                if (position == 0)
                {
                    val intent = Intent(mContext, MessageActivity::class.java)
                    intent.putExtra("visit_id", user.getUID())
                    mContext.startActivity(intent)
                }
                if (position == 1)
                {

                }
            })
            builder.show()
        }
    }
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
        {
            var usernameTxt: TextView
            var profileImageView: CircleImageView
            var onlineImageView: CircleImageView
            var offlineImageView: CircleImageView
            var lastMessageTxt: TextView

            init {
                usernameTxt = itemView.findViewById(R.id.username)
                profileImageView = itemView.findViewById(R.id.profile_image)
                onlineImageView = itemView.findViewById(R.id.image_online)
                offlineImageView = itemView.findViewById(R.id.image_offline)
                lastMessageTxt = itemView.findViewById(R.id.message_last)
            }
        }


}