package com.example.medimind

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.TextPaint
import androidx.core.content.ContextCompat
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
        val registerText = view.findViewById<TextView>(R.id.registerText)


        val fullText = "Don’t have an account yet? Sign up"
        val spannable = SpannableString(fullText)

        // Make "Sign up" clickable and blue
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigate(R.id.action_loginFragment_to_registerUserFragment)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.link_blue) // Optional custom color
                ds.isUnderlineText = false
            }
        }

        val signUpStart = fullText.indexOf("Sign up")
        val signUpEnd = signUpStart + "Sign up".length

        spannable.setSpan(clickableSpan, signUpStart, signUpEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        registerText.text = spannable
        registerText.movementMethod = LinkMovementMethod.getInstance()
        registerText.highlightColor = Color.TRANSPARENT

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

        // Register text -> navigate to register screen
        registerText.setOnClickListener {
            val currentId = findNavController().currentDestination?.id
            if (currentId == R.id.loginFragment) {
                findNavController().navigate(R.id.action_loginFragment_to_registerUserFragment)
            }
        }

    }
}
