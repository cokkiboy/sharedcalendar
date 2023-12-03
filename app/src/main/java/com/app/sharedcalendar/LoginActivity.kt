package com.app.sharedcalendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var joinButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)
        // Firebase Authentication 초기화
        firebaseAuth = FirebaseAuth.getInstance()

        // XML 레이아웃 파일에 정의된 UI 요소들을 연결합니다.
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        joinButton = findViewById(R.id.join_button)

        // 로그인 버튼 클릭 리스너를 설정합니다.
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Firebase Authentication을 사용하여 이메일 및 비밀번호로 로그인 시도
            if (username.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            showToast("로그인 완료")
                            navigateMainActivity()
                        } else {
                            showToast("로그인 실패: ${task.exception?.message}")
                            Log.e("LoginActivity", "Firebase 로그인 실패: ${task.exception}", task.exception)
                        }
                    }
            } else {
                showToast("이메일 또는 비밀번호를 입력하세요.")
            }
        }

        joinButton.setOnClickListener {
            navigateToRegisterActivity()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userID", firebaseAuth.currentUser?.uid)
        startActivity(intent)
        finish() // LoginActivity를 종료하여 뒤로 가기 버튼으로 돌아갈 수 없도록 합니다.
    }

    private fun navigateToRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun saveUserInfoAndNavigateToMainActivity(currentUser: FirebaseUser?) {
        currentUser?.let {
            val userId = it.uid
            val username = it.displayName ?: "DefaultUsername"

            // Firebase Realtime Database에 사용자 정보 저장
            val userInfo = UserInfo(userId, username)
            val databaseReference = FirebaseDatabase.getInstance().reference
            databaseReference.child("users").child(userId).setValue(userInfo)

            // MainActivity를 시작
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("userID", userId)
            startActivity(intent)
            finish() // LoginActivity를 종료하여 뒤로 가기 버튼으로 돌아갈 수 없도록 합니다.
        }
    }
}
