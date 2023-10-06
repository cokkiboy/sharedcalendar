package com.app.sharedcalendar
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity: AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Authentication 초기화
        firebaseAuth = FirebaseAuth.getInstance()

        // 현재 로그인한 사용자 확인
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            // 사용자가 로그인한 경우 MainActivity로 이동
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("userID", currentUser.uid)
            startActivity(intent)
        } else {
            // 사용자가 로그인하지 않은 경우 LoginActivity로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // 현재 액티비티를 종료합니다.
        finish()
    }

}