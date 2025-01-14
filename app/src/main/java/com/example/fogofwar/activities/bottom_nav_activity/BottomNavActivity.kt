package com.example.fogofwar.activities.bottom_nav_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.fogofwar.R
import com.example.fogofwar.databinding.ActivityBottomNavBinding
import com.example.fogofwar.activities.bottom_nav_activity.fragments.AdapterViewPager
import com.example.fogofwar.activities.bottom_nav_activity.fragments.maps.FragmentMaps
import com.example.fogofwar.activities.bottom_nav_activity.fragments.profile.FragmentProfile
import com.example.fogofwar.activities.bottom_nav_activity.fragments.search.FragmentSearch
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavActivity : AppCompatActivity() {
    lateinit var viewPager: ViewPager2
    lateinit var bottomNavView: BottomNavigationView
    var fragmentsArrayList = ArrayList<Fragment>()

    var userPhoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userPhoneNumber = intent.getStringExtra("user_phone_number")
        val bundle = Bundle()
        bundle.putString("user_phone_number", userPhoneNumber)

        bottomNavView = binding.bottomNavView
        viewPager = binding.viewPager
        viewPager.offscreenPageLimit = 3

        fragmentsArrayList += FragmentMaps()
        fragmentsArrayList += FragmentSearch()
        fragmentsArrayList += FragmentProfile()
        for (fragment in fragmentsArrayList)
            fragment.arguments = bundle

        val adapterViewPager = AdapterViewPager(this, fragmentsArrayList)
        viewPager.adapter = adapterViewPager
        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> bottomNavView.selectedItemId = R.id.navigationMaps
                    1 -> bottomNavView.selectedItemId = R.id.navigationSearch
                    2 -> bottomNavView.selectedItemId = R.id.navigationProfile
                }
                super.onPageSelected(position)
            }
        })

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigationMaps -> viewPager.currentItem = 0
                R.id.navigationSearch -> viewPager.currentItem = 1
                R.id.navigationProfile -> viewPager.currentItem = 2
            }
            true
        }
    }
}