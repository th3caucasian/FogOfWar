package com.example.fogofwar.backend


import com.example.features.updatePoints.UpdatePointsReceiveRemote
import com.example.fogofwar.backend.remotes.add_marker.AddMarkerReceiveRemote
import com.example.fogofwar.backend.remotes.add_marker.AddMarkerResponseRemote
import com.example.fogofwar.backend.remotes.delete_marker.DeleteMarkerReceiveRemote
import com.example.fogofwar.backend.remotes.get_markers.GetMarkersReceiveRemote
import com.example.fogofwar.backend.remotes.get_markers.GetMarkersResponseRemote
import com.example.fogofwar.backend.remotes.get_points.GetPointsReceiveRemote
import com.example.fogofwar.backend.remotes.get_points.GetPointsResponseRemote
import com.example.fogofwar.backend.remotes.register.RegisterReceiveRemote
import com.example.fogofwar.backend.remotes.register.RegisterResponseRemote
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

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

    @POST("/markers/get")
    suspend fun getMarkers(
        @Body userMarkers: GetMarkersReceiveRemote
    ): Response<GetMarkersResponseRemote>

    @POST("/markers/add")
    suspend fun addMarkers(
        @Body userMarkers: AddMarkerReceiveRemote
    ): Response<AddMarkerResponseRemote>

    @POST("/markers/delete")
    suspend fun deleteMarkers(
        @Body userMarkers: DeleteMarkerReceiveRemote
    ): Response<Unit>
}