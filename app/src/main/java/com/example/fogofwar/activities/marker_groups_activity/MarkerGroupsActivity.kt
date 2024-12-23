package com.example.fogofwar.activities.marker_groups_activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.R
import com.example.fogofwar.activities.friends_activity.RecycleViewAdapterFriends
import com.example.fogofwar.additions.MarkerGroupDTO
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.get_friends.GetFriendsReceiveRemote
import com.example.fogofwar.backend.remotes.get_marker_groups.GetMarkerGroupsReceiveRemote
import com.example.fogofwar.databinding.ActivityFriendsBinding
import com.example.fogofwar.databinding.ActivityMarkerGroupsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MarkerGroupsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMarkerGroupsBinding
    private lateinit var adapter: RecycleViewAdapterMarkerGroups
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView

    private lateinit var backendAPI: BackendAPI
    private lateinit var markerGroups: List<MarkerGroupDTO>
    private lateinit var markerGroupsNames: List<String>

    private var userPhoneNumber = "89880888306"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkerGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.69.194:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        backendAPI = retrofit.create(BackendAPI::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            markerGroups = backendAPI.getMarkerGroups(GetMarkerGroupsReceiveRemote(userPhoneNumber)).body()!!.markerGroups
            markerGroupsNames = markerGroups.map { it.name }
            withContext(Dispatchers.Main) {
                adapter = RecycleViewAdapterMarkerGroups(markerGroupsNames, this@MarkerGroupsActivity)
                layoutManager = LinearLayoutManager(this@MarkerGroupsActivity)
                recyclerView = binding.recyclerView
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = adapter
            }
        }
    }
}