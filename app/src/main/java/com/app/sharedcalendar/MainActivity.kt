package com.app.sharedcalendar

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sharedcalendar.Schedule.ScheduleAdapter
import com.app.sharedcalendar.Schedule.ScheduleInputActivity
import com.app.sharedcalendar.Schedule.ScheduleItem
import com.app.sharedcalendar.Schedule.ScheduleListActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    lateinit var saveBtn: Button
    lateinit var updateBtn: Button
    lateinit var deleteBtn: Button
    lateinit var diaryContent: TextView
    private lateinit var database: DatabaseReference
    private lateinit var userID: String
    private lateinit var calendarView: CalendarView
    private lateinit var currentDate: String
    private var selectedDate: String = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveBtn = findViewById(R.id.saveBtn)
        updateBtn = findViewById(R.id.updateBtn)
        deleteBtn = findViewById(R.id.deleteBtn)
        diaryContent = findViewById(R.id.diaryContent)
        calendarView = findViewById(R.id.calendarView)
        bottomNavigationView = findViewById(R.id.navigationView)

        userID = intent.getStringExtra("userID") ?: ""
        database = FirebaseDatabase.getInstance().reference
        val firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            navigateToLoginActivity()
            return
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = "$year-${month + 1}-$dayOfMonth"
            selectedDate = date
            currentDate = date
            loadDiary(date)
        }

        deleteBtn.setOnClickListener {
            navigateToScheduleList()
            loadDiary(selectedDate)
        }

        updateBtn.setOnClickListener {
            navigateToScheduleList()
            loadDiary(selectedDate)
        }

        saveBtn.setOnClickListener {
            val intent = Intent(this, ScheduleInputActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
            loadDiary(selectedDate)
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        scheduleAdapter = ScheduleAdapter(ArrayList())
        recyclerView.adapter = scheduleAdapter

        loadDiary(selectedDate)

        // BottomNavigationView 아이템 선택 리스너 설정
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.calenderFragment -> {
                    showLogoutConfirmationDialog()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.homeFragment -> {
                    // Home 아이템이 선택되었을 때 추가 작업 또는 화면 전환 코드 작성
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.myPageFragment -> {
                    // 검색 아이템이 선택되었을 때 추가 작업 또는 화면 전환 코드 작성
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("로그아웃 확인")
        alertDialogBuilder.setMessage("로그아웃 하시겠습니까?")

        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            logoutUser()
        }

        alertDialogBuilder.setNegativeButton("No") { _, _ ->
            // 다이얼로그를 닫거나 추가 작업 수행
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        navigateToLoginActivity()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loadDiary(date: String) {
        // 일정 불러오는 코드
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToScheduleList() {
        val intent = Intent(this, ScheduleListActivity::class.java)
        intent.putExtra("userID", userID)
        startActivity(intent)
    }

    private fun navigateToScheduleInputActivity() {
        val intent = Intent(this, ScheduleInputActivity::class.java)
        intent.putExtra("selectedDate", selectedDate)
        startActivity(intent)
    }
}
