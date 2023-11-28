package com.app.sharedcalendar

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.sharedcalendar.Schedule.ScheduleInputActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    override fun onBackPressed() {
        // 여기에 뒤로가기 버튼을 눌렀을 때의 동작을 정의합니다.
        // 여기에서는 아무 동작도 하지 않도록 설정했습니다.
    }

    lateinit var saveBtn: Button
    lateinit var updateBtn: Button
    lateinit var deleteBtn: Button
    lateinit var diaryContent: TextView
    lateinit var contextEditText: EditText
    private lateinit var database: DatabaseReference
    private lateinit var userID: String
    private lateinit var calendarView: CalendarView
    private lateinit var currentDate: String
    private var selectedDate: String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveBtn = findViewById(R.id.saveBtn)
        updateBtn = findViewById(R.id.updateBtn)
        deleteBtn = findViewById(R.id.deleteBtn)
        diaryContent = findViewById(R.id.diaryContent)
        contextEditText = findViewById(R.id.contextEditText)
        calendarView = findViewById(R.id.calendarView)

        //selectedDateTextView = findViewById(R.id.selectedDateTextView)

        userID = intent.getStringExtra("userID") ?: ""
        database = FirebaseDatabase.getInstance().reference
        val firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            navigateToLoginActivity()
            return
        }
        // 여기에 사용자의 고유 ID를 설정하세요.
        updateButtonVisibility(false)


        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val date = "$year-${month + 1}-$dayOfMonth"
            currentDate = date
            loadDiary(date)
        }





        deleteBtn.setOnClickListener {
            deleteDiary(currentDate)
        }
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = "$year-${month + 1}-$dayOfMonth"
            selectedDate = date

            // 일정 입력 액티비티로 전달할 Intent 생성

        }
        saveBtn.setOnClickListener {
            val intent = Intent(this, ScheduleInputActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)

        }

    }

    private fun getCurrentSelectedDate(): String {
        val date = calendarView.date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 여기 수정
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-${String.format("%02d", month)}-$dayOfMonth"
    }

    private fun loadDiary(date: String) {
        val diaryRef = database.child("diaries").child(userID).child(date)

        diaryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val content = dataSnapshot.getValue(String::class.java)
                if (content != null) {
                    contextEditText.visibility = View.INVISIBLE
                    diaryContent.visibility = View.VISIBLE
                    saveBtn.visibility = View.INVISIBLE
                    updateBtn.visibility = View.VISIBLE
                    deleteBtn.visibility = View.VISIBLE
                    diaryContent.text = content
                } else {
                    diaryContent.visibility = View.INVISIBLE
                    contextEditText.visibility = View.VISIBLE
                    saveBtn.visibility = View.VISIBLE
                    updateBtn.visibility = View.INVISIBLE
                    deleteBtn.visibility = View.INVISIBLE
                    contextEditText.setText("")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 오류 처리
            }
        })
    }


    private fun saveDiary(date: String, content: String) {
        val diaryRef = database.child("diaries").child(userID).child(date)
        diaryRef.setValue(content)
    }
    private fun setSaveButtonVisibility(editMode: Boolean) {
        if (editMode) {
            saveBtn.visibility = View.VISIBLE
            updateBtn.visibility = View.INVISIBLE
            deleteBtn.visibility = View.INVISIBLE
        } else {
            saveBtn.visibility = View.INVISIBLE
            updateBtn.visibility = View.VISIBLE
            deleteBtn.visibility = View.VISIBLE
        }
    }
    private fun updateDiary(date: String, content: String) {
        val diaryRef = database.child("diaries").child(userID).child(date)

        // 기존 내용을 불러옵니다.
        diaryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val existingContent = dataSnapshot.getValue(String::class.java)
                // 기존 내용을 EditText에 바인딩합니다.
                contextEditText.setText(existingContent)

                // EditText는 편집 가능하게, diaryContent는 비표시로 변경합니다.
                contextEditText.visibility = View.VISIBLE
                diaryContent.visibility = View.INVISIBLE

                // 버튼 상태 업데이트 (saveBtn은 비표시, updateBtn은 표시, deleteBtn은 비표시)
                saveBtn.visibility = View.INVISIBLE
                updateBtn.visibility = View.VISIBLE
                deleteBtn.visibility = View.INVISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 오류 처리
            }
        })

        // 수정 버튼 (updateBtn) 클릭 시 처리합니다.
        updateBtn.setOnClickListener {
            val updatedContent = contextEditText.text.toString()
            val existingContent = diaryContent.text.toString()
            if (updatedContent != existingContent) {
                // 내용이 수정되었을 경우에만 Firebase에서 업데이트합니다.
                saveOrUpdateDiary(currentDate, updatedContent)
            } else {
                // 내용이 변경되지 않았으면 뷰 모드로 전환합니다.
                updateButtonVisibility(false)
                contextEditText.visibility = View.INVISIBLE
                diaryContent.visibility = View.VISIBLE
            }
            // 수정된 내용을 다시 Firebase에 업데이트합니다.
            diaryRef.setValue(updatedContent).addOnSuccessListener {
                // 업데이트 성공 시 수행할 작업을 여기에 추가합니다.

                // 업데이트된 내용을 EditText에 설정합니다.
                contextEditText.setText(updatedContent)

                // EditText는 편집 가능하게, diaryContent는 비표시로 변경합니다.
                contextEditText.visibility = View.VISIBLE
                diaryContent.visibility = View.INVISIBLE

                // 버튼 상태 업데이트 (saveBtn은 비표시, updateBtn은 표시, deleteBtn은 비표시)
                saveBtn.visibility = View.INVISIBLE
                updateBtn.visibility = View.VISIBLE
                deleteBtn.visibility = View.INVISIBLE
            }.addOnFailureListener { e ->
                // 업데이트 실패 시 오류 처리를 수행합니다.
                // e에는 실패에 관한 정보가 포함됩니다.
                // 실패 시 사용자에게 메시지를 표시하거나 필요한 오류 처리를 수행합니다.
            }
            contextEditText.isEnabled = false
        }
    }





    private fun deleteDiary(date: String) {
        val diaryRef = database.child("diaries").child(userID).child(date)
        diaryRef.removeValue()
        contextEditText.visibility = View.VISIBLE
        diaryContent.visibility = View.INVISIBLE
        saveBtn.visibility = View.VISIBLE
        updateBtn.visibility = View.INVISIBLE
        deleteBtn.visibility = View.INVISIBLE
        contextEditText.setText("")
    }
    private fun saveOrUpdateDiary(date: String, content: String) {
        val diaryRef = database.child("diaries").child(userID).child(date)

        // 내용이 비어있지 않은 경우에만 처리
        if (content.isNotEmpty()) {
            diaryRef.setValue(content).addOnSuccessListener {
                // UI 업데이트 및 성공 메시지 표시 (필요한 경우)
                updateButtonVisibility(false)

                // 일정을 저장 또는 업데이트한 후 EditText 비활성화
                contextEditText.visibility = View.INVISIBLE
                diaryContent.visibility = View.VISIBLE
                diaryContent.text = content
                contextEditText.setText("")

                // 새로운 날짜를 선택할 수 있도록 CalendarView 활성화
                calendarView.isEnabled = true
            }.addOnFailureListener { e ->
                // 실패 시 처리 및 오류 메시지 표시 (필요한 경우)
            }
        } else {
            // 내용이 비어있으면 항목 삭제
            deleteDiary(date)

            // 새로운 날짜를 선택할 수 있도록 CalendarView 활성화
            calendarView.isEnabled = true
        }
    }
    private fun updateButtonVisibility(editMode: Boolean) {
        if (editMode) {
            this.saveBtn.visibility = View.INVISIBLE
            updateBtn.visibility = View.VISIBLE
            deleteBtn.visibility = View.VISIBLE
        } else {
            saveBtn.visibility = View.VISIBLE
            updateBtn.visibility = View.INVISIBLE
            deleteBtn.visibility = View.INVISIBLE
        }
    }
    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // LoginActivity로 이동한 후 현재 Activity를 종료하여 뒤로 돌아갈 수 없도록 합니다.
    }


}