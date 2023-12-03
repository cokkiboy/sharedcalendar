package com.app.sharedcalendar.User

data class User(
    val uid:String,
    val username: String,
    val friends: List<String> = emptyList()
)
