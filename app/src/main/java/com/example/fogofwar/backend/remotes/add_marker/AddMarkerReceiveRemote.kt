package com.example.fogofwar.backend.remotes.add_marker

import com.example.fogofwar.additions.Point

data class AddMarkerReceiveRemote(
    val phoneNumber: String,
    val markerLocation: Point,
    val description: String
)