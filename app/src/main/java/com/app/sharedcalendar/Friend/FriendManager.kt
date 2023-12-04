package com.app.sharedcalendar.Friend


import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class FriendManager {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    fun getInstance(): FriendManager {
        return this
    }
    // 친구 요청 보내기


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


    // 사용자 모델의 friends 필드 업데이트
    private fun updateFriendList(uid: String, friendUid: String) {
        val friendListRef = databaseReference.child("users").child(uid).child("friends")
        friendListRef.child(friendUid).setValue(true)
    }
    fun getFriendRequests(currentUserId: String, callback: (List<FriendRequest>) -> Unit) {
        val friendRequests = mutableListOf<FriendRequest>()
        val friendRequestRef = databaseReference.child("friendRequests")
            .orderByChild("receiverUid").equalTo(currentUserId)

        friendRequestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (requestSnapshot in dataSnapshot.children) {
                    val senderUid = requestSnapshot.child("senderUid").getValue(String::class.java)
                    val receiverUid = requestSnapshot.child("receiverUid").getValue(String::class.java)

                    senderUid?.let {
                        receiverUid?.let {
                            val friendRequest = FriendRequest(senderUid, receiverUid)
                            friendRequests.add(friendRequest)
                        }
                    }
                }
                callback(friendRequests)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 처리 중 에러 발생 시 작업
                callback(emptyList())
            }
        })
    }





    fun inviteFriendToGroup(currentUserId: String, uid: String, groupId: String, callback: (Boolean) -> Unit) {
        // Check if the user is already a member of the group
        databaseReference.child("groups").child(groupId).child("members").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // User is already a member, don't send an invitation again
                        callback(true)
                    } else {
                        // User is not a member, send an invitation
                        val invitationRef = databaseReference.child("groupInvitations").child(uid).child(groupId)
                        invitationRef.setValue(currentUserId)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    callback(true)
                                } else {
                                    callback(false)
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                    callback(false)
                }
            })
    }


}