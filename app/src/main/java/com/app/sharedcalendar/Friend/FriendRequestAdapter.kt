package com.app.sharedcalendar.Friend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.sharedcalendar.R

class FriendRequestAdapter(
    private var friendRequests: List<FriendRequest>,
    private var listener: OnItemClickListener? = null
) : RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder>() {

    interface OnItemClickListener {
        fun onAcceptClick(request: FriendRequest)
        fun onDeclineClick(request: FriendRequest)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun setFriendRequestList(friendRequests: List<FriendRequest>) {
        this.friendRequests = friendRequests
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_request_item, parent, false)
        return FriendRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val request = friendRequests[position]
        holder.bind(request)
    }

    override fun getItemCount(): Int {
        return friendRequests.size
    }

    inner class FriendRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderUidTextView: TextView = itemView.findViewById(R.id.senderUidTextView)
        private val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        private val declineButton: Button = itemView.findViewById(R.id.declineButton)

        fun bind(request: FriendRequest) {
            senderUidTextView.text = request.senderUid

            acceptButton.setOnClickListener {
                listener?.onAcceptClick(request)
            }

            declineButton.setOnClickListener {
                listener?.onDeclineClick(request)
            }
        }
    }
}
