package com.app.sharedcalendar.User

import com.google.firebase.auth.FirebaseUser

data class User(
    val uid: String,
    val email: String,
    val firebaseUser: FirebaseUser?
    )
