package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medimind.network.ApiClient
import com.example.medimind.network.LoginRequest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // UI references
        val usernameField = view.findViewById<EditText>(R.id.usernameInput)
        val passwordField = view.findViewById<EditText>(R.id.passwordInput)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val bypassButton = view.findViewById<Button>(R.id.bypassButton)
        val registerButton = view.findViewById<Button>(R.id.registerButton)

        // Login button -> validate credentials with backend
        loginButton.setOnClickListener {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()

            // Build login request body
            val request = LoginRequest(username, password)

            // Launch a coroutine to call backend (Retrofit suspend function)
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Call /api/patient/login
                    val response = ApiClient.retrofitService.login(request)

                    // Save patientId in SharedPreferences for later use (profile/home screens)
                    val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("patientId", response.id)
                        apply()
                    }

                    // Show welcome toast
                    Toast.makeText(requireContext(), "Welcome ${response.firstName}", Toast.LENGTH_SHORT).show()

                    // Navigate to mainFragment (home screen)
                    findNavController().navigate(R.id.action_loginFragment_to_mainFragment)

                } catch (e: Exception) {
                    // Show error if login failed
                    Toast.makeText(requireContext(), "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Bypass button -> navigate directly to mainFragment (no login)
        bypassButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        }

        // Register button -> navigate to register screen
        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerUserFragment)
        }

    }
}
