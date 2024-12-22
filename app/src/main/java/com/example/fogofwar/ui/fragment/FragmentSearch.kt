package com.example.fogofwar.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fogofwar.R
import com.example.fogofwar.databinding.FragmentMapsBinding
import com.example.fogofwar.databinding.FragmentSearchBinding


class FragmentSearch : Fragment() {
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root = binding.root
        return root
    }

}