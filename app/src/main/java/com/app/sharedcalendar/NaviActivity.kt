package com.app.sharedcalendar.Schedule

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import com.app.sharedcalendar.R
import com.app.sharedcalendar.databinding.ActivityNaviBinding




private const val TAG_ADD_FRIEND = "add_friend_fragment"

class NaviActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNaviBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFragment(TAG_ADD_FRIEND, AddFriendFragment())

        binding.navigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.addFriendButton -> setFragment(TAG_ADD_FRIEND, AddFriendFragment())

            }
            true
        }
    }

    private fun setFragment(tag: String, fragment: Fragment) {
        val manager: FragmentManager = supportFragmentManager
        val fragTransaction = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null) {
            fragTransaction.add(R.id.mainFrameLayout, fragment, tag)
        }


        val addFriend = manager.findFragmentByTag(TAG_ADD_FRIEND)



        if (addFriend != null) {
            fragTransaction.hide(addFriend)
        }

        when (tag) {

            TAG_ADD_FRIEND -> if (addFriend != null) fragTransaction.show(addFriend)
        }

        fragTransaction.commitAllowingStateLoss()
    }
}
