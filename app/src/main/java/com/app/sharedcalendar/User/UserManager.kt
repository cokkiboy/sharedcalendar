package com.app.sharedcalendar.User

import com.google.firebase.auth.FirebaseUser
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
                    val email = userSnapshot.child("email").getValue(String::class.java)

                    if (uid != null && email != null) {
                        val user = User(uid = uid, email = email, firebaseUser = null)
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

    fun getUserByEmail(email: String, callback: (User?) -> Unit) {
        val usersRef = databaseReference.child("users")
        val query = usersRef.orderByChild("email").equalTo(email)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val uid = userSnapshot.key
                    val userEmail = userSnapshot.child("email").getValue(String::class.java)

                    if (uid != null && userEmail != null) {
                        val user = User(uid = uid, email = userEmail, firebaseUser = null)
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

    fun FirebaseUser(user: FirebaseUser?): Boolean {
        // FirebaseUser 객체가 null이 아니고, providerData 목록에 "firebase"가 포함되어 있는지 확인
        return user != null && user.providerData.any { it.providerId == "firebase" }
    }


}
