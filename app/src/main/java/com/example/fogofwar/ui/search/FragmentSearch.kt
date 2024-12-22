package com.example.fogofwar.ui.search

import android.net.http.HttpResponseCache
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SearchView
import androidx.collection.emptyLongSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.add_friend.AddFriendReceiveRemote
import com.example.fogofwar.backend.remotes.get_user.GetUserReceiveRemote
import com.example.fogofwar.databinding.FragmentSearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale


class FragmentSearch : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchView: SearchView

    private lateinit var backendAPI: BackendAPI
    private var userList = mutableListOf<String>()
    private lateinit var adapter: RecycleViewAdapterSearch
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView

    private var userPhoneNumber = "89880888306"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root = binding.root
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.69.194:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        backendAPI = retrofit.create(BackendAPI::class.java)

        searchView = binding.searchView
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        adapter = RecycleViewAdapterSearch(userList)
        layoutManager = LinearLayoutManager(requireActivity())
        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        return root
    }

    private fun filterList(queryPhoneNumber: String?) {
        queryPhoneNumber?.let {
            CoroutineScope(Dispatchers.IO).launch {
                if (backendAPI.getUser(GetUserReceiveRemote(queryPhoneNumber)).isSuccessful && queryPhoneNumber != userPhoneNumber) {
                    if (userList.size == 0)
                        userList += queryPhoneNumber
                    else
                        userList[0] = queryPhoneNumber
                }
                withContext(Dispatchers.Main) {
                    adapter.setFilteredList(userList)
                }
            }
        }

    }

}