package com.app.sharedcalendar.Friend

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.sharedcalendar.R
import com.app.sharedcalendar.User.UserManager
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
        addFriendButton.setOnClickListener {
            showAddFriendDialog()
        }
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
    private fun showAddFriendDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("친구 추가")
        alertDialogBuilder.setMessage("친구의 이메일을 입력하세요.")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        alertDialogBuilder.setView(input)

        alertDialogBuilder.setPositiveButton("추가") { _, _ ->
            val friendEmail = input.text.toString()
            addFriendByEmail(friendEmail)
        }

        alertDialogBuilder.setNegativeButton("취소") { _, _ ->
            // 다이얼로그를 닫거나 추가 작업 수행
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun addFriendByEmail(email: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val currentUserId = currentUser.uid

            val userManager = UserManager()
            userManager.getUserByEmail(email) { friend ->
                friend?.let {
                    friendManager.addFriend(currentUserId, friend.uid) { success ->
                        if (success) {
                            showToast("친구 추가 성공")
                        } else {
                            showToast("친구 추가 실패")
                        }
                    }
                } ?: showToast("존재하지 않는 사용자입니다.")
            }
        }
    }
}
