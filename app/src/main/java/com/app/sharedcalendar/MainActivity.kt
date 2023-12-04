package com.app.sharedcalendar


import ScheduleItem
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sharedcalendar.Friend.FriendManager
import com.app.sharedcalendar.Schedule.ScheduleAdapter
import com.app.sharedcalendar.Schedule.ScheduleInputActivity
import com.app.sharedcalendar.Schedule.ScheduleListActivity
import com.app.sharedcalendar.User.UserListActivity
import com.app.sharedcalendar.User.UserManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    lateinit var saveBtn: Button
    lateinit var updateBtn: Button
    private lateinit var addFriendButton: Button
    private lateinit var friendNameEditText: EditText
    lateinit var diaryContent: TextView
    private lateinit var database: DatabaseReference
    private lateinit var userID: String
    private lateinit var calendarView: CalendarView
    private lateinit var currentDate: String
    private var selectedDate: String = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private val friendManager: FriendManager = FriendManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveBtn = findViewById(R.id.saveBtn)
        updateBtn = findViewById(R.id.updateBtn)
        addFriendButton =findViewById(R.id.addFriendButton)
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
        addFriendButton.setOnClickListener {
            showAddFriendDialog()
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
        // 개인 일정 로드
        val userScheduleRef = database.child("schedules").child(userID)
        userScheduleRef.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userScheduleList = mutableListOf<ScheduleItem>()

                for (scheduleSnapshot in dataSnapshot.children) {
                    val scheduleItem = scheduleSnapshot.getValue(ScheduleItem::class.java)
                    scheduleItem?.let {
                        userScheduleList.add(it)
                    }
                }

                scheduleAdapter.setData(userScheduleList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("일정 로드 실패: ${databaseError.message}")
            }
        })

        // 공유 일정 로드
        val sharedScheduleRef = database.child("sharedSchedules")
        sharedScheduleRef.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sharedScheduleList = mutableListOf<ScheduleItem>()

                for (scheduleSnapshot in dataSnapshot.children) {
                    val sharedSchedule = scheduleSnapshot.getValue(ScheduleItem::class.java)
                    sharedSchedule?.let {
                        sharedScheduleList.add(it)
                    }
                }

                scheduleAdapter.setData(sharedScheduleList)
                scheduleAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("공유 일정 로드 실패: ${databaseError.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToScheduleList() {
        val intent = Intent(this, ScheduleListActivity::class.java)
        intent.putExtra("userID", userID)
        startActivity(intent)
    }

    private fun navigateToUserListActivity() {
        val intent = Intent(this, UserListActivity::class.java)
        startActivity(intent)
    }

    private fun saveSchedule(date: String, event: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userID = user.uid

            // 개인 일정 저장
            val userScheduleRef = database.child("schedules").child(userID)
            val scheduleId = userScheduleRef.push().key
            val scheduleData = mapOf(
                "date" to date,
                "event" to event,
                "startTime" to "실제_시작_시간으로_업데이트", // 실제 시작 시간으로 업데이트
                "endTime" to "실제_종료_시간으로_업데이트",   // 실제 종료 시간으로 업데이트
                "userId" to userID
            )
            scheduleId?.let {
                userScheduleRef.child(it).setValue(scheduleData)
            }

            // 공유 일정 저장
            val sharedScheduleRef = database.child("sharedSchedules")
            val sharedScheduleData = mapOf(
                "date" to date,
                "event" to event,
                "startTime" to "실제_시작_시간으로_업데이트", // 실제 시작 시간으로 업데이트
                "endTime" to "실제_종료_시간으로_업데이트",   // 실제 종료 시간으로 업데이트
                "userId" to userID,
                "sharedWith" to listOf("user_id_2", "user_id_3") // 공유할 사용자 ID 추가
            )
            sharedScheduleRef.push().setValue(sharedScheduleData)
        }
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
