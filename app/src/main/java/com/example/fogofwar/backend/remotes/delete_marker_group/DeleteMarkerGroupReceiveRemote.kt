package com.example.fogofwar.backend.remotes.delete_marker_group

data class DeleteMarkerGroupReceiveRemote(
    val phoneNumber: String,
    val markerGroupId: Long
)