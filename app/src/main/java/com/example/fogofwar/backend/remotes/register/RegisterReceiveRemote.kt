package com.example.fogofwar.backend.remotes.register

data class RegisterReceiveRemote(
    val login: String,
    val phoneNumber: String,
    val password: String
)
