package com.example.fogofwar.activities.bottom_nav_activity.fragments.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.get_user.GetUserReceiveRemote
import com.example.fogofwar.databinding.FragmentSearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class FragmentSearch : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchView: SearchView

    private lateinit var adapter: RecycleViewAdapterSearch
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView

    private lateinit var backendAPI: BackendAPI
    private var userList = mutableListOf<String>()
    private lateinit var userPhoneNumber: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root = binding.root
        val retrofit = Retrofit.Builder()
            .baseUrl("http://45.91.8.232:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        backendAPI = retrofit.create(BackendAPI::class.java)
        userPhoneNumber = arguments?.getString("user_phone_number")!!


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

        adapter = RecycleViewAdapterSearch(userPhoneNumber, userList)
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