package com.example.medimind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class RegisterUserFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val backButton = view.findViewById<Button>(R.id.backToLoginButton)
        backButton.setOnClickListener {
            findNavController().navigateUp() // navigate back to Login
        }
    }
}
