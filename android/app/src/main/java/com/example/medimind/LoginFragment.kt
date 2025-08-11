package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo   
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medimind.network.ApiClient
import com.example.medimind.network.LoginRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginFragment : Fragment() {

    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val usernameField = view.findViewById<EditText>(R.id.usernameInput)
        val passwordField = view.findViewById<EditText>(R.id.passwordInput)
        val loginButton   = view.findViewById<Button>(R.id.loginButton)
        val registerText  = view.findViewById<TextView>(R.id.registerText)
        val progress      = view.findViewById<ProgressBar>(R.id.loginProgress)

        // Clickable "Sign up"
        val fullText = "Don’t have an account yet? Sign up"
        val spannable = SpannableString(fullText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (findNavController().currentDestination?.id == R.id.loginFragment) {
                    findNavController().navigate(R.id.action_loginFragment_to_registerUserFragment)
                }
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        val start = fullText.indexOf("Sign up")
        if (start >= 0) {
            spannable.setSpan(
                clickableSpan, start, start + "Sign up".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        registerText.text = spannable
        registerText.movementMethod = LinkMovementMethod.getInstance()
        registerText.highlightColor =
            ContextCompat.getColor(requireContext(), android.R.color.transparent)

        registerText.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.loginFragment) {
                findNavController().navigate(R.id.action_loginFragment_to_registerUserFragment)
            }
        }

        // IME "Done" submits
        passwordField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin(usernameField, passwordField, loginButton, progress)
                true
            } else false
        }

        loginButton.setOnClickListener {
            attemptLogin(usernameField, passwordField, loginButton, progress)
        }
    }

    private fun attemptLogin(
        usernameField: EditText,
        passwordField: EditText,
        loginButton: Button,
        progress: ProgressBar
    ) {
        if (isLoading) return

        val username = usernameField.text?.toString()?.trim().orEmpty()
        val password = passwordField.text?.toString().orEmpty()

        if (username.isEmpty()) {
            usernameField.error = "Email is required"
            usernameField.requestFocus()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            usernameField.error = "Enter a valid email"
            usernameField.requestFocus()
            return
        }
        if (password.length < 6) {
            passwordField.error = "Password must be at least 6 characters"
            passwordField.requestFocus()
            return
        }

        setLoading(true, loginButton, progress)
        hideKeyboard()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = ApiClient.retrofitService.login(LoginRequest(username, password))

                // Your response appears to expose: id, firstName
                val patientId = resp.id ?: throw IllegalStateException("Missing patient ID")
                val displayName = resp.firstName ?: "User"

                // Persist
                val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("patientId", patientId)
                    .putString("patientName", displayName)
                    .apply()

                Toast.makeText(requireContext(), "Welcome $displayName", Toast.LENGTH_SHORT).show()

                if (findNavController().currentDestination?.id == R.id.loginFragment) {
                    findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                }
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    401, 403 -> "Invalid email or password"
                    else -> "Login failed (${e.code()})"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Network error. Please check connection.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Unexpected error", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false, loginButton, progress)
            }
        }
    }

    private fun setLoading(loading: Boolean, button: Button, progress: ProgressBar) {
        isLoading = loading
        button.isEnabled = !loading
        button.alpha = if (loading) 0.6f else 1f
        progress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
