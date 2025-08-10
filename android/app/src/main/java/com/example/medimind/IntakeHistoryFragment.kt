package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.adapters.HistoryRow
import com.example.medimind.adapters.IntakeHistoryStyledAdapter
import com.example.medimind.network.ApiClient
import com.example.medimind.service.IntakeHistoryResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class IntakeHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intake_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Top bar views (from included layout)
        val greetingTextView = view.findViewById<TextView>(R.id.topGreetingText)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)

        // Content views
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val emptyState = view.findViewById<LinearLayout>(R.id.emptyState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.intakeHistoryRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }

        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPref.getString("patientId", null)

        if (patientId != null) {
            // Greeting
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val profile = ApiClient.retrofitService.getPatient(patientId)
                    greetingTextView.text = "Hello, ${profile.firstName}"
                } catch (_: Exception) {
                    greetingTextView.text = "Hello"
                }
            }

            // Load & style intake history
            progressBar.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.GONE

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response: List<IntakeHistoryResponse> =
                        ApiClient.retrofitService.getIntakeHistory(patientId)

                    // Build flat list for styled adapter
                    val flatRows = buildStyledRows(response)

                    progressBar.visibility = View.GONE

                    if (flatRows.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        recyclerView.adapter = IntakeHistoryStyledAdapter(flatRows)
                        recyclerView.visibility = View.VISIBLE
                        emptyState.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "Failed to load intake history: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            greetingTextView.text = "Hello"
        }

        // Logout
        logoutButton.setOnClickListener {
            sharedPref.edit().clear().apply()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            val navController = requireActivity().findNavController(R.id.nav_host_fragment)
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.mainFragment, true)
                .build()
            navController.navigate(R.id.loginFragment, null, navOptions)
        }
    }

    /**
     * Convert API list into a flat list of:
     *  DateHeader -> StatusHeader(MISSED) -> missed rows -> StatusHeader(TAKEN) -> taken rows
     */
    private fun buildStyledRows(response: List<IntakeHistoryResponse>): List<HistoryRow> {
        if (response.isEmpty()) return emptyList()

        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())

        val rows = mutableListOf<HistoryRow>()

        // Group by date string "yyyy-MM-dd" then sort by date desc
        response.groupBy { it.scheduledTime.substring(0, 10) }
            .toSortedMap(compareByDescending { it }) // newest first
            .forEach { (dateStr, itemsForDate) ->

                // Nice date label like "Sunday, 10 Aug"
                val dateLabel = runCatching { sdfOut.format(sdfIn.parse(dateStr)!!) }
                    .getOrElse { dateStr }
                rows += HistoryRow.DateHeader(dateLabel)

                // Split into missed / taken
                val (taken, missed) = itemsForDate.partition { it.status.equals("TAKEN", true) }

                // MISSED section
                rows += HistoryRow.StatusHeader(label = "Missed", count = missed.size, isMissed = true)
                rows += missed.map {
                    val time = it.scheduledTime.substring(11, 16) // "HH:mm"
                    HistoryRow.MedRow(name = it.medicationName, time = time, taken = false)
                }

                // TAKEN section
                rows += HistoryRow.StatusHeader(label = "Taken", count = taken.size, isMissed = false)
                rows += taken.map {
                    val time = it.scheduledTime.substring(11, 16)
                    HistoryRow.MedRow(name = it.medicationName, time = time, taken = true)
                }
            }

        return rows
    }
}
