package com.app.sharedcalendar
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var joinButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase Authentication 초기화
        firebaseAuth = FirebaseAuth.getInstance()

        // XML 레이아웃 파일에 정의된 UI 요소들을 연결합니다.
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        joinButton = findViewById(R.id.join)
        // 로그인 버튼 클릭 리스너를 설정합니다.
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()


            // Firebase Authentication을 사용하여 이메일 및 비밀번호로 로그인 시도
            firebaseAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 로그인 성공
                        showToast("로그인 성공")

                        // 여기에서 로그인 이후의 화면으로 이동하거나 작업을 수행할 수 있습니다.
                        // 예를 들어, MainActivity로 이동하고 userID를 전달할 수 있습니다.
                        val userID = firebaseAuth.currentUser?.uid
                        if (userID != null) {
                            navigateMainActivity()
                        }
                    } else {
                        // 로그인 실패
                        showToast("로그인 실패: ${task.exception?.message}")
                    }
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
        intent.putExtra("userID",firebaseAuth.currentUser?.uid)
        startActivity(intent)
        finish() // Optional: Finish the current activity so that it can't be navigated back to.
    }
    private fun navigateToRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
