package com.app.sharedcalendar.Friend

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

    // 가정: FriendManager가 싱글톤으로 구현되어 있다고 가정
    private val friendManager: FriendManager = FriendManager()

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
                // 친구 추가 요청 보내기
                friendManager.sendFriendRequest(currentUserId, friendName)

                showToast("친구 추가 요청을 보냈습니다.")
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
