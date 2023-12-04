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
import com.app.sharedcalendar.Friend.Group
import com.app.sharedcalendar.Schedule.ScheduleAdapter
import com.app.sharedcalendar.Schedule.ScheduleInputActivity
import com.app.sharedcalendar.Schedule.ScheduleListActivity
import com.app.sharedcalendar.User.UserManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class MainActivity : AppCompatActivity() {

    lateinit var saveBtn: Button
    lateinit var updateBtn: Button
    private lateinit var addFriendButton: Button

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
    private lateinit var selectedGroup: Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveBtn = findViewById(R.id.saveBtn)
        updateBtn = findViewById(R.id.updateBtn)
        addFriendButton = findViewById(R.id.addFriendButton)
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
            showInviteFriendDialog()
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        scheduleAdapter = ScheduleAdapter(ArrayList())
        recyclerView.adapter = scheduleAdapter

        loadDiary(selectedDate)

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

    private fun inviteFriendByEmail(email: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val currentUserId = currentUser.uid

            val userManager = UserManager()
            userManager.getUserByEmail(email) { friend ->
                friend?.let {
                    // 초대할 친구가 현재 그룹에 초대된 멤버인지 확인
                    if (selectedGroup.invitedUsers.contains(friend.email)) {
                        // 초대된 멤버일 때만 초대 가능
                        inviteFriendToGroupByEmail(email) // 초대된 친구를 그룹에 추가
                        showToast("친구가 그룹에 성공적으로 초대되었습니다.")
                    } else {
                        // 초대되지 않은 친구에게도 초대 가능
                        inviteFriendToGroupByEmail(email)
                        showToast("친구에게 초대 메시지가 전송되었습니다.")
                    }
                } ?: showToast("사용자가 존재하지 않습니다.")
            }
        }
    }
    private fun inviteFriendToGroupByEmail(email: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val currentUserId = currentUser.uid

            val userManager = UserManager()
            userManager.getUserByEmail(email) { friend ->
                friend?.let { friendUser ->

                    // 친구가 Firebase 회원인 경우에만 초대 가능
                    if (userManager.FirebaseUser(friendUser.firebaseUser)) {
                        friendManager.inviteFriendToGroup(currentUserId, friendUser.uid, selectedGroup.groupId) { success ->
                            if (success) {
                                // 초대가 성공하면 공유 일정 저장
                                saveSharedSchedule(selectedDate, selectedGroup.groupId, friendUser.uid)
                                showToast("일정이 성공적으로 공유되었습니다.")
                                loadDiary(selectedDate) // 변경 사항을 반영하기 위해 다이어리 다시 로드
                            } else {
                                showToast("친구를 그룹에 초대하는 데 실패했습니다.")
                            }
                        }
                    } else {
                        showToast("Firebase 회원이 아니라 초대할 수 없습니다.")
                    }
                } ?: showToast("사용자가 존재하지 않습니다.")
            }
        }
    }

    // UserManager 클래스에 추가된 메서드
    fun FirebaseUser(user: FirebaseUser?): Boolean {
        // FirebaseUser 객체가 null이 아니고, providerData 목록에 "firebase"가 포함되어 있는지 확인
        return user != null && user.providerData.any { it.providerId == "firebase" }
    }




    private fun showInviteFriendDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("친구 초대")
        alertDialogBuilder.setMessage("초대할 친구의 이메일을 입력하세요.")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        alertDialogBuilder.setView(input)

        alertDialogBuilder.setPositiveButton("초대") { _, _ ->
            val friendEmail = input.text.toString()
            inviteFriendByEmail(friendEmail)
        }

        alertDialogBuilder.setNegativeButton("취소") { _, _ ->
            // 다이얼로그를 닫거나 추가 작업 수행
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }



    private fun saveSharedSchedule(date: String, event: String, groupId: String) {
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
                "groupId" to groupId // 그룹 ID 추가
            )
            sharedScheduleRef.push().setValue(sharedScheduleData)
        }
    }
}
