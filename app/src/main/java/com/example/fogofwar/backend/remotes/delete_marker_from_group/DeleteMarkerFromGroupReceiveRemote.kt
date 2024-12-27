package com.example.fogofwar.backend.remotes.delete_marker_from_group

data class DeleteMarkerFromGroupReceiveRemote(
    val markerId: Long,
    val groupId: Long
)