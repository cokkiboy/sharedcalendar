package com.app.sharedcalendar.Schedule

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.sharedcalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddFriendActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        val addFriendButton: Button = findViewById(R.id.addFriendButton)

        addFriendButton.setOnClickListener {
            addFriend()
        }
    }

    private fun addFriend() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.let { user ->
            val currentUserId = user.uid

            // 원하는 친구의 사용자 ID로 변경
            val friendUserId = "user_id_2"

            // Firebase Realtime Database에 친구 관계 추가
            val databaseReference = FirebaseDatabase.getInstance().reference
            val friendRelationRef = databaseReference.child("friendships").push()

            // 사용자의 친구 목록에 친구 추가
            friendRelationRef.child(currentUserId).setValue(true)

            // 친구의 친구 목록에 사용자 추가 (양방향 관계 설정)
            val friendUserRef = databaseReference.child("users").child(friendUserId).child("friends").child(currentUserId)
            friendUserRef.setValue(true)

            showToast("친구 추가 완료")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
