package com.example.fogofwar.activities.friends_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.get_friends.GetFriendsReceiveRemote
import com.example.fogofwar.databinding.ActivityFriendsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FriendsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFriendsBinding
    private lateinit var adapter: RecycleViewAdapterFriends
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView

    private lateinit var backendAPI: BackendAPI
    private lateinit var userFriends: MutableList<String>
    private var callerActivity: String? = null
    private var markerName: String? = null

    private lateinit var userPhoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://45.91.8.232:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        backendAPI = retrofit.create(BackendAPI::class.java)
        userPhoneNumber = intent.getStringExtra("user_phone_number")!!

        callerActivity = intent.getStringExtra("caller_activity")
        if (callerActivity == "MarkerGroupsActivity")
            markerName = intent.getStringExtra("marker_name")
        CoroutineScope(Dispatchers.IO).launch {
            userFriends = backendAPI.getFriends(GetFriendsReceiveRemote(userPhoneNumber)).body()!!.friendsNumbers
            withContext(Dispatchers.Main) {
                adapter = RecycleViewAdapterFriends(userPhoneNumber, userFriends, callerActivity, markerName, this@FriendsActivity)
                layoutManager = LinearLayoutManager(this@FriendsActivity)
                recyclerView = binding.recyclerView
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = adapter
            }
        }
    }
}