package com.example.database.columns.marker

import com.example.fogofwar.additions.Point
import com.google.gson.annotations.SerializedName


class MarkerDTO(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("location")
    val location: Point,
    @SerializedName("description")
    val description: String?,
)