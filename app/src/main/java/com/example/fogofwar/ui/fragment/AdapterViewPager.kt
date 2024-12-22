package com.example.fogofwar.ui.fragment

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AdapterViewPager(fragmentActivity: FragmentActivity, inFragmentsList: ArrayList<Fragment>): FragmentStateAdapter(fragmentActivity) {
    var fragmentsList: ArrayList<Fragment> = inFragmentsList

    override fun createFragment(position: Int): Fragment {
        return fragmentsList[position]
    }

    override fun getItemCount(): Int {
        return fragmentsList.size
    }

}