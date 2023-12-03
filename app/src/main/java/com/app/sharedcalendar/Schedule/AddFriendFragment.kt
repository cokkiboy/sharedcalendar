package com.app.sharedcalendar.Schedule

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

import com.app.sharedcalendar.R

class AddFriendFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_friend, container, false)

        // Find the "Add Friend" button
        val addFriendButton: Button = view.findViewById(R.id.addFriendButton)

        // Set a click listener for the "Add Friend" button
        addFriendButton.setOnClickListener {
            // Navigate to AddFriendActivity when the button is clicked
            navigateToAddFriendActivity()
        }

        return view
    }

    private fun navigateToAddFriendActivity() {
        // Create an intent to start AddFriendActivity
        val intent = Intent(activity, AddFriendActivity::class.java)
        startActivity(intent)
    }
}
