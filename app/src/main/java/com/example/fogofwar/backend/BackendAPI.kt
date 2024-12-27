package com.example.fogofwar.backend


import com.example.features.updatePoints.UpdatePointsReceiveRemote
import com.example.fogofwar.backend.remotes.add_friend.AddFriendReceiveRemote
import com.example.fogofwar.backend.remotes.add_marker.AddMarkerReceiveRemote
import com.example.fogofwar.backend.remotes.add_marker.AddMarkerResponseRemote
import com.example.fogofwar.backend.remotes.add_marker_group.AddMarkerGroupReceiveRemote
import com.example.fogofwar.backend.remotes.add_marker_group.AddMarkerGroupResponseRemote
import com.example.fogofwar.backend.remotes.add_marker_to_group.AddMarkerToGroupReceiveRemote
import com.example.fogofwar.backend.remotes.delete_friend.DeleteFriendReceiveRemote
import com.example.fogofwar.backend.remotes.delete_marker.DeleteMarkerReceiveRemote
import com.example.fogofwar.backend.remotes.delete_marker_from_group.DeleteMarkerFromGroupReceiveRemote
import com.example.fogofwar.backend.remotes.delete_marker_group.DeleteMarkerGroupReceiveRemote
import com.example.fogofwar.backend.remotes.get_friends.GetFriendsReceiveRemote
import com.example.fogofwar.backend.remotes.get_friends.GetFriendsResponseRemote
import com.example.fogofwar.backend.remotes.get_groups_of_marker.GetGroupsOfMarkerReceiveRemote
import com.example.fogofwar.backend.remotes.get_groups_of_marker.GetGroupsOfMarkerResponseRemote
import com.example.fogofwar.backend.remotes.get_marker_groups.GetMarkerGroupsReceiveRemote
import com.example.fogofwar.backend.remotes.get_marker_groups.GetMarkerGroupsResponseRemote
import com.example.fogofwar.backend.remotes.get_markers.GetMarkersReceiveRemote
import com.example.fogofwar.backend.remotes.get_markers.GetMarkersResponseRemote
import com.example.fogofwar.backend.remotes.get_points.GetPointsReceiveRemote
import com.example.fogofwar.backend.remotes.get_points.GetPointsResponseRemote
import com.example.fogofwar.backend.remotes.get_user.GetUserReceiveRemote
import com.example.fogofwar.backend.remotes.register.RegisterReceiveRemote
import com.example.fogofwar.backend.remotes.register.RegisterResponseRemote
import com.example.fogofwar.backend.remotes.share_marker_group.ShareMarkerGroupReceiveRemote
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
    suspend fun addMarker(
        @Body marker: AddMarkerReceiveRemote
    ): Response<AddMarkerResponseRemote>

    @POST("/markers/delete")
    suspend fun deleteMarkers(
        @Body marker: DeleteMarkerReceiveRemote
    ): Response<Unit>

    @POST("/friends/add")
    suspend fun addFriend(
        @Body friend: AddFriendReceiveRemote
    ): Response<Unit>

    @POST("/users/get")
    suspend fun getUser(
        @Body user: GetUserReceiveRemote
    ): Response<Unit>

    @POST("/friends/get")
    suspend fun getFriends(
        @Body friends: GetFriendsReceiveRemote
    ): Response<GetFriendsResponseRemote>

    @POST("/marker-group/get")
    suspend fun getMarkerGroups(
        @Body markerGroup: GetMarkerGroupsReceiveRemote
    ): Response<GetMarkerGroupsResponseRemote>

    @POST("/marker-group/share")
    suspend fun shareMarkerGroups(
        @Body markerGroup: ShareMarkerGroupReceiveRemote
    ): Response<Unit>

    @POST("/marker-group/delete")
    suspend fun deleteMarkerGroup(
        @Body markerGroup: DeleteMarkerGroupReceiveRemote
    ): Response<Unit>

    @POST("/friends/delete")
    suspend fun deleteFriend(
        @Body friend: DeleteFriendReceiveRemote
    ): Response<Unit>

    @POST("/marker-group/add")
    suspend fun addMarkerGroup(
        @Body group: AddMarkerGroupReceiveRemote
    ): Response<AddMarkerGroupResponseRemote>

    @POST("/marker/marker-group")
    suspend fun getGroupsOfMarker(
        @Body groups: GetGroupsOfMarkerReceiveRemote
    ): Response<GetGroupsOfMarkerResponseRemote>

    @POST("/marker-group/add-marker")
    suspend fun addMarkerToGroup(
        @Body markerInfo: AddMarkerToGroupReceiveRemote
    ): Response<Unit>

    @POST("/marker-group/delete-marker")
    suspend fun deleteMarkerFromGroup(
        @Body markerInfo: DeleteMarkerFromGroupReceiveRemote
    ): Response<Unit>

}