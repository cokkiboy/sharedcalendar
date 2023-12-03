package com.app.sharedcalendar.Friend

data class Friend(
    val Friend: String,
    val FriendName:String,
    val friends: List<String> = emptyList()
)
