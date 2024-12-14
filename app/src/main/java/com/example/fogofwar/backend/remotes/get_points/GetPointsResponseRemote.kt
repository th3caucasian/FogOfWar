package com.example.fogofwar.backend.remotes.get_points

import com.example.fogofwar.Point
import com.google.gson.annotations.SerializedName

data class GetPointsResponseRemote(
    val points: List<Point>?
)
