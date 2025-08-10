package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val emptyState = view.findViewById<LinearLayout>(R.id.emptyState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.intakeHistoryRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }

        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPref.getString("patientId", null)

        if (patientId == null) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        progressBar.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response: List<IntakeHistoryResponse> =
                    ApiClient.retrofitService.getIntakeHistory(patientId)

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
    }

    /**
     * Builds a flat list like:
     * DateHeader -> StatusHeader(MISSED) -> missed meds -> StatusHeader(TAKEN) -> taken meds
     */
    private fun buildStyledRows(response: List<IntakeHistoryResponse>): List<HistoryRow> {
        if (response.isEmpty()) return emptyList()

        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())

        val rows = mutableListOf<HistoryRow>()

        response.groupBy { it.scheduledTime.substring(0, 10) }
            .toSortedMap(compareByDescending { it }) // newest date first
            .forEach { (dateStr, itemsForDate) ->

                val dateLabel = runCatching { sdfOut.format(sdfIn.parse(dateStr)!!) }
                    .getOrElse { dateStr }
                rows += HistoryRow.DateHeader(dateLabel)

                val (taken, missed) = itemsForDate.partition { it.status.equals("TAKEN", true) }

                rows += HistoryRow.StatusHeader(label = "Missed", count = missed.size, isMissed = true)
                rows += missed.map {
                    val time = it.scheduledTime.substring(11, 16)
                    HistoryRow.MedRow(name = it.medicationName, time = time, taken = false)
                }

                rows += HistoryRow.StatusHeader(label = "Taken", count = taken.size, isMissed = false)
                rows += taken.map {
                    val time = it.scheduledTime.substring(11, 16)
                    HistoryRow.MedRow(name = it.medicationName, time = time, taken = true)
                }
            }

        return rows
    }
}
