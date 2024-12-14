package com.example.fogofwar.backend


import com.example.features.updatePoints.UpdatePointsReceiveRemote
import com.example.fogofwar.backend.remotes.get_points.GetPointsReceiveRemote
import com.example.fogofwar.backend.remotes.get_points.GetPointsResponseRemote
import com.example.fogofwar.backend.remotes.register.RegisterReceiveRemote
import com.example.fogofwar.backend.remotes.register.RegisterResponseRemote
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BackendAPI {
    @POST("/register")
    suspend fun register(
        @Body userInfo: RegisterReceiveRemote
    ): Response<RegisterResponseRemote>

    @POST("/points/update")
    suspend fun updatePoints(
        @Body newUserPoints: UpdatePointsReceiveRemote
    ): Response<Unit>

    @POST("/points/get")
    suspend fun getPoints(
        @Body userPoints: GetPointsReceiveRemote
    ): Response<GetPointsResponseRemote>
}