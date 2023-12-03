package com.app.sharedcalendar.Friend

import com.app.sharedcalendar.User.User
import com.google.firebase.database.*

class FriendManager {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    // 친구 요청 보내기
    fun sendFriendRequest(senderUid: String, receiverUid: String) {
        val friendRequestRef = databaseReference.child("friendRequests").push()
        friendRequestRef.child("senderUid").setValue(senderUid)
        friendRequestRef.child("receiverUid").setValue(receiverUid)
    }

    // 친구 요청 수락
    fun acceptFriendRequest(senderUid: String, receiverUid: String) {
        // FriendRequest를 업데이트하고 사용자 모델의 friends 필드를 업데이트하는 작업 추가
        val friendRequestQuery = databaseReference.child("friendRequests")
            .orderByChild("senderUid").equalTo(senderUid)

        friendRequestQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (requestSnapshot in dataSnapshot.children) {
                    val requestSenderUid = requestSnapshot.child("senderUid").getValue(String::class.java)
                    val requestReceiverUid = requestSnapshot.child("receiverUid").getValue(String::class.java)

                    if (requestSenderUid == senderUid && requestReceiverUid == receiverUid) {
                        // 친구 요청을 수락하면 FriendRequest 삭제
                        requestSnapshot.ref.removeValue()

                        // 사용자 모델의 friends 필드 업데이트
                        updateFriendList(senderUid, receiverUid)
                        updateFriendList(receiverUid, senderUid)
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 처리 중 에러 발생 시 작업
            }
        })
    }

    // 친구 목록 가져오기
    fun getFriendList(uid: String, callback: (List<User>) -> Unit) {
        val friendList = mutableListOf<User>()
        val friendListRef = databaseReference.child("users").child(uid).child("friends")

        friendListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (friendSnapshot in dataSnapshot.children) {
                    val friendUid = friendSnapshot.key
                    friendUid?.let {
                        // 실제로는 여기에서 사용자 정보를 조회하여 User 객체를 생성하는 작업이 필요
                        // 이 예시에서는 User 모델이 어떻게 생겼는지 알 수 없으므로 가정으로 둡니다.
                        val friend = User(uid = it, username = "Friend")
                        friendList.add(friend)
                    }
                }
                callback(friendList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 처리 중 에러 발생 시 작업
                callback(emptyList())
            }
        })
    }

    // 사용자 모델의 friends 필드 업데이트
    private fun updateFriendList(uid: String, friendUid: String) {
        val friendListRef = databaseReference.child("users").child(uid).child("friends")
        friendListRef.child(friendUid).setValue(true)
    }
}
