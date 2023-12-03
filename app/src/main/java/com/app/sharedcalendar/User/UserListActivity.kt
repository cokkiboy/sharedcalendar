package com.app.sharedcalendar.User

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.app.sharedcalendar.databinding.ActivityUserListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult

class UserListActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityUserListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 현재 로그인한 사용자 가져오기
        val currentUser: FirebaseUser? = auth.currentUser

        if (currentUser != null) {
            // 현재 사용자가 로그인한 경우
            currentUser.getIdToken(true)
                .addOnSuccessListener { result: GetTokenResult ->
                    // Extract UID from the token
                    val uid: String? = result.token
                    if (uid != null) {
                        Log.d("UserListActivity", "User UID: $uid")

                        // UID를 TextView에 표시
                        //binding.userRecyclerView.append("$uid\n")
                    } else {
                        Log.e("UserListActivity", "User UID is null")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("UserListActivity", "Error getting user list", e)
                }
        } else {
            // 사용자가 로그인되어 있지 않은 경우
            Log.d("UserListActivity", "User not logged in")
        }
    }
}
