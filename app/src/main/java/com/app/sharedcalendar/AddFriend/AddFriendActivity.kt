package com.app.sharedcalendar.AddFriend

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.sharedcalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

import com.google.firebase.database.FirebaseDatabase

class AddFriendActivity : AppCompatActivity() {

    private lateinit var friendNameEditText: EditText
    private lateinit var addFriendButton: Button
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        friendNameEditText = findViewById(R.id.friendNameEditText)
        addFriendButton = findViewById(R.id.addFriendButton)

        databaseReference = FirebaseDatabase.getInstance().reference

        addFriendButton.setOnClickListener {
            addFriend()
        }
    }

    private fun addFriend() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            val currentUserId = user.uid
            val friendName = friendNameEditText.text.toString().trim()

            if (friendName.isNotEmpty()) {
                // Assuming "friendships" is the node where friend relationships are stored
                val friendRelationRef = databaseReference.child("friendships").push()

                // Store friend relationship information
                friendRelationRef.child(currentUserId).setValue(true)

                // Add friend to user's friend list
                val currentUserFriendRef =
                    databaseReference.child("users").child(currentUserId).child("friends")
                        .child(friendRelationRef.key!!)
                currentUserFriendRef.setValue(true)

                showToast("친구 추가 완료")
                finish()
            } else {
                showToast("친구 이름을 입력하세요.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
