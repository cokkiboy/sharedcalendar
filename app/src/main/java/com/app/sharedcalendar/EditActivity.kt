package com.app.sharedcalendar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class EditActivity : AppCompatActivity() {
    lateinit var editedContent: EditText
    lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        editedContent = findViewById(R.id.editedContent)
        saveButton = findViewById(R.id.saveButton)

        // 수정된 내용을 받아옵니다.
        val updatedContent = intent.getStringExtra("updatedContent")

        // EditText에 수정된 내용을 설정합니다.
        editedContent.setText(updatedContent)

        saveButton.setOnClickListener {
            // 수정된 내용을 저장하고 이전 액티비티로 돌아갑니다.
            val resultIntent = Intent()
            resultIntent.putExtra("editedContent", editedContent.text.toString())
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
