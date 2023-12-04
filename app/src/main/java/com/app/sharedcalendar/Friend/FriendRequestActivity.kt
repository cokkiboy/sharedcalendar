package com.app.sharedcalendar.Friend

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sharedcalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FriendRequestActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var friendRequestAdapter: FriendRequestAdapter
    private lateinit var friendManager: FriendManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)

        recyclerView = findViewById(R.id.friendRequestRecyclerView)
        friendRequestAdapter = FriendRequestAdapter(emptyList())
        friendManager = FriendManager()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = friendRequestAdapter

        loadFriendRequests()

        friendRequestAdapter.setOnItemClickListener(object : FriendRequestAdapter.OnItemClickListener {
            override fun onAcceptClick(request: FriendRequest) {
                acceptFriendRequest(request)
            }

            override fun onDeclineClick(request: FriendRequest) {
                // You can add functionality to decline the friend request if needed
                // For example: declineFriendRequest(request)
                Toast.makeText(this@FriendRequestActivity, "Friend request declined", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadFriendRequests() {
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            val currentUserId = user.uid

            friendManager.getFriendRequests(currentUserId) { friendRequests ->
                friendRequestAdapter.setFriendRequestList(friendRequests)
            }
        }
    }

    private fun acceptFriendRequest(request: FriendRequest) {
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            val currentUserId = user.uid

            friendManager.acceptFriendRequest(request.senderUid, currentUserId)

            Toast.makeText(this, "Friend request accepted", Toast.LENGTH_SHORT).show()
            loadFriendRequests()
        }
    }
}
