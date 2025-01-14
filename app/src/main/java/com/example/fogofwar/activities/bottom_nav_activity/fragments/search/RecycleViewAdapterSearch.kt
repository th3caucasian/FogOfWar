package com.example.fogofwar.activities.bottom_nav_activity.fragments.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.R
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.add_friend.AddFriendReceiveRemote
import com.example.fogofwar.backend.remotes.get_friends.GetFriendsReceiveRemote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecycleViewAdapterSearch(private var userPhoneNumber: String, private var mDataset: MutableList<String>?): RecyclerView.Adapter<RecycleViewAdapterSearch.MyViewHolder>() {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://45.91.8.232:8081/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val backendAPI = retrofit.create(BackendAPI::class.java)
    private lateinit var userFriends: List<String>


    init {
        CoroutineScope(Dispatchers.IO).launch {
            userFriends = backendAPI.getFriends(GetFriendsReceiveRemote(userPhoneNumber)).body()!!.friendsNumbers
        }
    }

    class MyViewHolder(v: View, private val vhUserPhoneNumber: String): RecyclerView.ViewHolder(v) {
        private val textView: TextView = v.findViewById(R.id.textView)
        private val buttonAdd: Button = v.findViewById(R.id.buttonAdd)


        fun bind(item: String?, _mDataset: MutableList<String>?, backendAPI: BackendAPI, userFriends: List<String>) {
            textView.text = item
            if (userFriends.contains(item)) {
                buttonAdd.text = "ADDED"
                buttonAdd.isClickable = false
            }
            else {
                buttonAdd.text = "+Add"
                buttonAdd.isClickable = true
                buttonAdd.setOnClickListener {
                    try {
                        CoroutineScope(Dispatchers.IO).launch {
                            backendAPI.addFriend(AddFriendReceiveRemote(vhUserPhoneNumber, _mDataset!![0]))
                        }
                        buttonAdd.text = "ADDED"
                        buttonAdd.isClickable = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        val vh = MyViewHolder(v, userPhoneNumber)
        return vh
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(mDataset!![position], mDataset, backendAPI, userFriends)
    }

    override fun getItemCount(): Int {
        return mDataset!!.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilteredList(filteredList: MutableList<String>?) {
        mDataset = filteredList
        notifyDataSetChanged()
    }


}