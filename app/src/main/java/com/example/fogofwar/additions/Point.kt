package com.example.fogofwar.additions

import com.google.gson.annotations.SerializedName

data class Point(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double
)
