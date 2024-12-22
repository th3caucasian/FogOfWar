package com.example.fogofwar.backend.remotes.add_friend

data class AddFriendReceiveRemote(
    val userNumber: String,
    val userNumberToAdd: String
)