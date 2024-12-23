package com.example.fogofwar.backend.remotes.get_marker_groups

import com.example.fogofwar.additions.MarkerGroupDTO

data class GetMarkerGroupsResponseRemote(
    val markerGroups: List<MarkerGroupDTO>
)
