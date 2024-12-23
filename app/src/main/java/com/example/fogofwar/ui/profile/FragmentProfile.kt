package com.example.fogofwar.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.fogofwar.activities.friends_activity.FriendsActivity
import com.example.fogofwar.activities.marker_groups_activity.MarkerGroupsActivity
import com.example.fogofwar.databinding.FragmentProfileBinding

class FragmentProfile : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var buttonFriends: Button
    private lateinit var buttonMarkerGroups: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root = binding.root
        buttonFriends = binding.Friends
        buttonMarkerGroups = binding.markerGroups


        buttonFriends.setOnClickListener {
            val intent = Intent(requireActivity(), FriendsActivity::class.java)
            startActivity(intent)
        }

        buttonMarkerGroups.setOnClickListener {
            val intent = Intent(requireActivity(), MarkerGroupsActivity::class.java)
            startActivity(intent)
        }

        return root
    }


}