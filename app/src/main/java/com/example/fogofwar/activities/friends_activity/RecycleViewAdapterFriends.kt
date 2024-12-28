package com.example.fogofwar.activities.friends_activity

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.R
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.delete_friend.DeleteFriendReceiveRemote
import com.example.fogofwar.backend.remotes.delete_marker_group.DeleteMarkerGroupReceiveRemote
import com.example.fogofwar.backend.remotes.get_friends.GetFriendsReceiveRemote
import com.example.fogofwar.backend.remotes.get_marker_groups.GetMarkerGroupsReceiveRemote
import com.example.fogofwar.backend.remotes.share_marker_group.ShareMarkerGroupReceiveRemote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecycleViewAdapterFriends(private var userPhoneNumber: String,
                                private var mDataset: MutableList<String>?,
                                private var callerActivity: String?,
                                private var markerGroupName: String?,
                                private val context: Context): RecyclerView.Adapter<RecycleViewAdapterFriends.MyViewHolder>() {



    class MyViewHolder(private val recycleViewAdapter: RecycleViewAdapterFriends, v: View, private var vhUserPhoneNumber: String): RecyclerView.ViewHolder(v) {
        private val textView: TextView = v.findViewById(R.id.textView)
        private val buttonAdd: Button = v.findViewById(R.id.buttonAdd)
        private val buttonDelete: ImageButton = v.findViewById(R.id.buttonDelete)

        // TODO: Сделать пропажу списка при удалении сразу
        fun bind(item: String?, _callerActivity: String?, _markerGroupName: String?, _context: Context) {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.69.194:8081/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val backendAPI = retrofit.create(BackendAPI::class.java)


            textView.text = item
            buttonAdd.text = "ADDED"
            if (_callerActivity == "MarkerGroupsActivity") {
                textView.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val markerGroups = backendAPI.getMarkerGroups(GetMarkerGroupsReceiveRemote(vhUserPhoneNumber)).body()!!.markerGroups
                        val markerGroupId = markerGroups.find { it.name == _markerGroupName }!!.id!!
                        backendAPI.shareMarkerGroups(ShareMarkerGroupReceiveRemote(markerGroupId, textView.text.toString()))
                        withContext(Dispatchers.Main) {
                            val alertDialogBuilder = AlertDialog.Builder(_context)
                                .setTitle("Группа маркеров была успешно передана пользователю")
                                .setPositiveButton("Ок") { _, _ -> }
                            val alertDialog = alertDialogBuilder.create()
                            alertDialog.show()
                        }
                    }
                    textView.isClickable = false
                }
            } else {
                buttonDelete.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val friends = backendAPI.getFriends(GetFriendsReceiveRemote(vhUserPhoneNumber)).body()!!.friendsNumbers
                        val friendNumber = friends.find { it == textView.text.toString() }!!
                        val response = backendAPI.deleteFriend(DeleteFriendReceiveRemote(vhUserPhoneNumber, friendNumber))
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                recycleViewAdapter.deleteFriend(item)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        val vh = MyViewHolder(this, v, userPhoneNumber)
        return vh
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(mDataset!![position], callerActivity, markerGroupName, context)
    }

    override fun getItemCount(): Int {
        return mDataset!!.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteFriend(friendName: String?) {
        mDataset!!.remove(friendName)
        notifyDataSetChanged()
    }



}