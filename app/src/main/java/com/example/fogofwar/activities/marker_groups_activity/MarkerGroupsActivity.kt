package com.example.fogofwar.activities.marker_groups_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.R
import com.example.fogofwar.activities.friends_activity.RecycleViewAdapterFriends
import com.example.fogofwar.additions.MarkerGroupDTO
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.add_marker_group.AddMarkerGroupReceiveRemote
import com.example.fogofwar.backend.remotes.get_friends.GetFriendsReceiveRemote
import com.example.fogofwar.backend.remotes.get_marker_groups.GetMarkerGroupsReceiveRemote
import com.example.fogofwar.databinding.ActivityFriendsBinding
import com.example.fogofwar.databinding.ActivityMarkerGroupsBinding
import com.example.fogofwar.databinding.AlertDialogAddGroupBinding
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
    private lateinit var buttonAddGroup: ImageButton
    private lateinit var bindingDialog: AlertDialogAddGroupBinding
    private lateinit var buttonDialogAddGroup: Button
    private lateinit var groupNameView: EditText
    private lateinit var checkBoxView: CheckBox
    private lateinit var groupDescriptionView: EditText


    private lateinit var backendAPI: BackendAPI
    private lateinit var markerGroups: List<MarkerGroupDTO>
    private lateinit var markerGroupsNames: MutableList<String>

    private var userPhoneNumber = "89880888306"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkerGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buttonAddGroup = binding.buttonAddGroup

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.69.194:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        backendAPI = retrofit.create(BackendAPI::class.java)

        val markerId = intent.getLongExtra("marker_id", -1)
        val action = intent.getStringExtra("action")
        CoroutineScope(Dispatchers.IO).launch {
            markerGroups = backendAPI.getMarkerGroups(GetMarkerGroupsReceiveRemote(userPhoneNumber)).body()!!.markerGroups
            markerGroupsNames = markerGroups.map { it.name }.toMutableList()
            withContext(Dispatchers.Main) {
                adapter = RecycleViewAdapterMarkerGroups(markerGroupsNames, this@MarkerGroupsActivity, markerId, action!!)
                layoutManager = LinearLayoutManager(this@MarkerGroupsActivity)
                recyclerView = binding.recyclerView
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = adapter
            }
        }


        bindingDialog = AlertDialogAddGroupBinding.inflate(layoutInflater)
        buttonDialogAddGroup = bindingDialog.buttonDialogAddGroup
        groupNameView = bindingDialog.groupName
        groupDescriptionView = bindingDialog.groupDescription
        checkBoxView = bindingDialog.checkBox




        buttonAddGroup.setOnClickListener {
            var groupName: String
            var groupDescription: String
            var privacy: Boolean
            val alertDialogBuilder = AlertDialog.Builder(this)
                .setView(bindingDialog.root)
                .setTitle("Что сделать с маркером?")
            val alertDialog = alertDialogBuilder.create()

            buttonDialogAddGroup.setOnClickListener {
                groupName = groupNameView.text.toString()
                groupDescription = groupDescriptionView.text.toString()
                privacy = checkBoxView.isChecked
                if (groupName.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        backendAPI.addMarkerGroup(AddMarkerGroupReceiveRemote(userPhoneNumber, groupName, groupDescription, privacy))
                        withContext(Dispatchers.Main) {
                            alertDialog.hide()
                            adapter.addGroup(groupName)
                        }
                    }
                }
            }
            alertDialog.show()
        }
    }
}