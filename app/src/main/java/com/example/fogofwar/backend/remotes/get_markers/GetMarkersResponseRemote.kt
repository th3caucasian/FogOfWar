package com.example.fogofwar.backend.remotes.get_markers

import com.example.database.columns.marker.MarkerDTO

data class GetMarkersResponseRemote(
    val markers: List<MarkerDTO>
)