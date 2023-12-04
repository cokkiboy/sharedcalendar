package com.app.sharedcalendar.Friend

data class Group(
    val groupId: String,
    val groupName: String,
    val members: List<String> = emptyList(),
    val invitedUsers: List<String> = emptyList() // 초대받은 사용자를 저장
)
