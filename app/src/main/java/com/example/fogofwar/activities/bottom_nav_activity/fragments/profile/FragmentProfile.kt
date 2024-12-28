package com.example.fogofwar.activities.bottom_nav_activity.fragments.profile

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
    private lateinit var userPhoneNumber: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root = binding.root
        buttonFriends = binding.Friends
        buttonMarkerGroups = binding.markerGroups
        userPhoneNumber = arguments?.getString("user_phone_number")!!


        buttonFriends.setOnClickListener {
            val intent = Intent(requireActivity(), FriendsActivity::class.java)
            intent.putExtra("caller_activity", "ButtomNavActivity")
            intent.putExtra("user_phone_number", userPhoneNumber)
            startActivity(intent)
        }

        buttonMarkerGroups.setOnClickListener {
            val intent = Intent(requireActivity(), MarkerGroupsActivity::class.java)
            intent.putExtra("action", "show")
            intent.putExtra("user_phone_number", userPhoneNumber)
            startActivity(intent)
        }

        return root
    }


}