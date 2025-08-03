package com.example.medimind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Top greeting from included top_nav_bar
        val greeting = view.findViewById<TextView>(R.id.topGreetingText)
        greeting.text = "Hello, Grandpa"

        // Static placeholder values for now
        view.findViewById<TextView>(R.id.txtUsername).text = "Email/Username: grandpa@example.com"
        view.findViewById<TextView>(R.id.txtNRIC).text = "NRIC: S1234567A"
        view.findViewById<TextView>(R.id.txtFullName).text = "Name: Grandpa Tan"
        view.findViewById<TextView>(R.id.txtGender).text = "Gender: Male"
        view.findViewById<TextView>(R.id.txtDOB).text = "DOB: 1950-01-01"
        view.findViewById<TextView>(R.id.txtClinic).text = "Clinic: Happy Health Clinic"

        // Edit profile button (placeholder)
        val editButton = view.findViewById<Button>(R.id.editProfileButton)
        editButton.setOnClickListener {
            // Future implementation: navigate to edit screen
        }

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            val rootNavController = requireActivity().findNavController(R.id.nav_host_fragment)
            rootNavController.navigate(R.id.action_global_logout)
        }
    }
}
