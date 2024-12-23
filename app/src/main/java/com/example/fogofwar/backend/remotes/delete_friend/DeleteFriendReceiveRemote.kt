package com.example.fogofwar.backend.remotes.delete_friend

data class DeleteFriendReceiveRemote(
    val phoneNumber: String,
    val friendNumber: Long
)
