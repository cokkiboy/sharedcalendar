package com.app.sharedcalendar.User

data class User(
    val userId: String,
    val userName: String,
    val friends: List<String> = emptyList()
)
