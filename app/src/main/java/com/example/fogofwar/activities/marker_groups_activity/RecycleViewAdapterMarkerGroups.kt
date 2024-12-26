package com.example.fogofwar.activities.marker_groups_activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.BoringLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.R
import com.example.fogofwar.activities.friends_activity.FriendsActivity
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.delete_marker_group.DeleteMarkerGroupReceiveRemote
import com.example.fogofwar.backend.remotes.get_marker_groups.GetMarkerGroupsReceiveRemote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecycleViewAdapterMarkerGroups(private var mDataset: MutableList<String>?, private var context: Context): RecyclerView.Adapter<RecycleViewAdapterMarkerGroups.MyViewHolder>() {



    class MyViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val textView: TextView = v.findViewById(R.id.textView)
        private val buttonSend: ImageButton = v.findViewById(R.id.buttonSend)
        private val buttonDelete: ImageButton = v.findViewById(R.id.buttonDelete)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.69.194:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val backendAPI = retrofit.create(BackendAPI::class.java)
        private var userPhoneNumber = "89880888306"


        // TODO: Сделать пропажу списка при удалении сразу
        fun bind(item: String?, _context: Context) {

            textView.text = item
            buttonDelete.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val markerGroups = backendAPI.getMarkerGroups(GetMarkerGroupsReceiveRemote(userPhoneNumber)).body()!!.markerGroups
                    val markerGroupId = markerGroups.find { it.name == textView.text.toString() }!!.id!!
                    backendAPI.deleteMarkerGroup(DeleteMarkerGroupReceiveRemote(userPhoneNumber, markerGroupId))
                }
            }
            buttonSend.setOnClickListener {
                val intent = Intent(_context, FriendsActivity::class.java)
                intent.putExtra("caller_activity", "MarkerGroupsActivity")
                intent.putExtra("marker_name", textView.text.toString())
                _context.startActivity(intent)
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.marker_groups_rec_view_item, parent, false)
        val vh = MyViewHolder(v)
        return vh
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(mDataset!![position], context)
    }

    override fun getItemCount(): Int {
        return mDataset!!.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addGroup(groupName: String) {
        mDataset!!.add(groupName)
        notifyDataSetChanged()
    }


}