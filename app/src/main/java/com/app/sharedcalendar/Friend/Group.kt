package com.app.sharedcalendar.Friend

data class Group(
    val groupId: String,
    val groupName: String,
    val members: List<Friend> = emptyList()
)
