package com.example.fogofwar.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.databinding.FragmentSearchBinding
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root = binding.root
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.69.194:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        backendAPI = retrofit.create(BackendAPI::class.java)

        userList += "th3caucasian"
        userList += "alan"
        userList += "blan"
        userList += "e"
        userList += "meme"

        searchView = binding.searchView
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
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

    private fun filterList(text: String?) {
        val filteredUserList = mutableListOf<String>()
        text?.let {
            for (name in userList) {
                if (name.lowercase().contains(text.lowercase())) {
                    filteredUserList += name
                }
            }

            if (filteredUserList.isEmpty())
                Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show()
            else
                adapter.setFilteredList(filteredUserList)
        }


    }

}