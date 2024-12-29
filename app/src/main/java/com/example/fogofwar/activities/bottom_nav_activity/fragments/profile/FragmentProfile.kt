package com.example.fogofwar.activities.bottom_nav_activity.fragments.profile

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.fogofwar.activities.friends_activity.FriendsActivity
import com.example.fogofwar.activities.marker_groups_activity.MarkerGroupsActivity
import com.example.fogofwar.activities.registration_activity.RegistraitionActivity
import com.example.fogofwar.databinding.FragmentProfileBinding

class FragmentProfile : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var buttonFriends: Button
    private lateinit var buttonMarkerGroups: Button
    private lateinit var buttonLogout: Button
    private lateinit var userPhoneNumber: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root = binding.root
        buttonFriends = binding.buttonFriends
        buttonMarkerGroups = binding.buttonMarkerGroups
        buttonLogout = binding.buttonLogout
        userPhoneNumber = arguments?.getString("user_phone_number", "null")!!
        if (userPhoneNumber == "null") {
            val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", MODE_PRIVATE)
            userPhoneNumber = sharedPreferences.getString("user_phone_number", "null")!!
        }


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

        buttonLogout.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            val intent = Intent(requireActivity(), RegistraitionActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }


        return root
    }


}