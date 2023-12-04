package com.app.sharedcalendar.User

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserManager {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun getUserList(callback: (List<User>) -> Unit) {
        val userList = mutableListOf<User>()

        // Assuming you have a "users" node in your database
        val usersRef = databaseReference.child("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val uid = userSnapshot.key
                    val username = userSnapshot.child("username").getValue(String::class.java)

                    if (uid != null && username != null) {
                        val user = User(uid = uid, username = username)
                        userList.add(user)
                    }
                }
                callback(userList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                callback(emptyList())
            }
        })
    }

    // UserManager.kt



        // ...

        fun getUserByEmail(email: String, callback: (User?) -> Unit) {
            val usersRef = databaseReference.child("users")
            val query = usersRef.orderByChild("email").equalTo(email)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.children) {
                        val uid = userSnapshot.key
                        val username = userSnapshot.child("username").getValue(String::class.java)

                        if (uid != null && username != null) {
                            val user = User(uid = uid, username = username)
                            callback(user)
                            return
                        }
                    }
                    callback(null)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                    callback(null)
                }
            })
        }




}
