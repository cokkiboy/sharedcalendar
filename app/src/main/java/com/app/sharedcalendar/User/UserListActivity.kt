package com.app.sharedcalendar.User

// Import R from your app's package
import com.app.sharedcalendar.R

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.app.sharedcalendar.R.layout

class UserListActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_user_list)

        userRecyclerView = findViewById(R.id.userRecyclerView)
        userAdapter = UserAdapter(emptyList())
        userRecyclerView.adapter = userAdapter
        userRecyclerView.layoutManager = LinearLayoutManager(this)

        databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        // Listen for changes in the users node
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (snapshot in dataSnapshot.children) {
                    val userId = snapshot.key.orEmpty()
                    val userName = snapshot.child("userName").getValue(String::class.java).orEmpty()
                    val user = User(userId, userName)
                    users.add(user)
                }
                userAdapter.setUserList(users)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }
}
