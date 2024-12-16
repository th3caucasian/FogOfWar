package com.example.fogofwar.backend.remotes.delete_marker

data class DeleteMarkerReceiveRemote(
    val phoneNumber: String,
    val markerId: Long
)