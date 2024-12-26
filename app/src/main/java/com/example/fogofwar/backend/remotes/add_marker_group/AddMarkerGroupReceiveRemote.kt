package com.example.fogofwar.backend.remotes.add_marker_group

data class AddMarkerGroupReceiveRemote(
    val phoneNumber: String,
    val name: String,
    val description: String,
    val privacy: Boolean
)
