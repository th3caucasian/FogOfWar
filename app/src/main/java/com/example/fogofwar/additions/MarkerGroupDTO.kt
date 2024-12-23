package com.example.fogofwar.additions

import com.google.gson.annotations.SerializedName

class MarkerGroupDTO(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("privacy")
    val privacy: Boolean
)