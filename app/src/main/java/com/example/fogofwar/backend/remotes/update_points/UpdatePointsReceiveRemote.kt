package com.example.features.updatePoints

import com.example.fogofwar.additions.Point


data class UpdatePointsReceiveRemote(
    val phoneNumber: String,
    val clearedPoints: List<Point>
)

