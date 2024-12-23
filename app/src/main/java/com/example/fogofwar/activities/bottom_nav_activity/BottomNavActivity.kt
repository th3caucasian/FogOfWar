package com.example.fogofwar.activities.bottom_nav_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.fogofwar.R
import com.example.fogofwar.databinding.ActivityBottomNavBinding
import com.example.fogofwar.ui.AdapterViewPager
import com.example.fogofwar.ui.maps.FragmentMaps
import com.example.fogofwar.ui.profile.FragmentProfile
import com.example.fogofwar.ui.search.FragmentSearch
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavActivity : AppCompatActivity() {
    lateinit var viewPager: ViewPager2
    lateinit var bottomNavView: BottomNavigationView
    var fragmentsArrayList = ArrayList<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavView = binding.bottomNavView
        viewPager = binding.viewPager
        viewPager.offscreenPageLimit = 3

        fragmentsArrayList += FragmentMaps()
        fragmentsArrayList += FragmentSearch()
        fragmentsArrayList += FragmentProfile()

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