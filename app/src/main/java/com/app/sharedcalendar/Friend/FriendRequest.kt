package com.app.sharedcalendar.Friend

data class FriendRequest(
    val senderUid: String,
    val receiverUid: String,
    val status: FriendRequestStatus = FriendRequestStatus.PENDING
)
