package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.adapters.HistoryRow
import com.example.medimind.adapters.IntakeHistoryStyledAdapter
import com.example.medimind.network.ApiClient
import com.example.medimind.service.IntakeHistoryResponse
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class IntakeHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var dateDropdown: MaterialAutoCompleteTextView

    private var allResponses: List<IntakeHistoryResponse> = emptyList()
    private var labelToDateKey: LinkedHashMap<String, String> = linkedMapOf() // "Saturday, 9 Aug" -> "2025-08-09"
    private var adapter: IntakeHistoryStyledAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intake_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Optional simple navbar + back button (only if your layout has these IDs)
        setupSimpleNavbar(view)

        // Views
        progressBar = view.findViewById(R.id.progressBar)
        emptyState = view.findViewById(R.id.emptyState)
        recyclerView = view.findViewById<RecyclerView>(R.id.intakeHistoryRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        dateDropdown = view.findViewById(R.id.dateFilterDropdown)

        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPref.getString("patientId", null)

        // Optional greeting (only if your layout has topGreetingText)
        val greetingTextView = view.findViewById<TextView?>(R.id.topGreetingText)

        if (patientId == null) {
            showEmpty()
            return
        }

        // Load greeting (non-blocking)
        if (greetingTextView != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val profile = ApiClient.retrofitService.getPatient(patientId)
                    greetingTextView.text = "Hello, ${profile.firstName}"
                } catch (_: Exception) {
                    greetingTextView.text = "Hello"
                }
            }
        }

        // Load history data
        loadData(patientId)
    }

    private fun loadData(patientId: String) {
        progressBar.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                allResponses = ApiClient.retrofitService.getIntakeHistory(patientId)
                setupDateDropdown(allResponses)
                applyFilter(null) // null = All dates
                progressBar.visibility = View.GONE
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                showEmpty()
                Toast.makeText(
                    requireContext(),
                    "Failed to load intake history: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /** Build the dropdown items from available dates. */
    private fun setupDateDropdown(responses: List<IntakeHistoryResponse>) {
        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())

        // Collect unique date keys (yyyy-MM-dd), newest first
        val dateKeys = responses.map { it.scheduledTime.substring(0, 10) }
            .distinct()
            .sortedDescending()

        labelToDateKey.clear()
        val labels = mutableListOf<String>()
        labels += "All dates"
        dateKeys.forEach { key ->
            val label = runCatching { sdfOut.format(sdfIn.parse(key)!!) }.getOrElse { key }
            labelToDateKey[label] = key
            labels += label
        }

        dateDropdown.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labels)
        )
        dateDropdown.setText("All dates", false)

        dateDropdown.setOnItemClickListener { _, _, position, _ ->
            val sel = labels[position]
            if (sel == "All dates") {
                applyFilter(null)
            } else {
                val key = labelToDateKey[sel]
                applyFilter(key)
            }
        }
    }

    /** Rebuild the rows for a specific date key (yyyy-MM-dd) or for all if key==null. */
    private fun applyFilter(dateKey: String?) {
        val filtered = if (dateKey == null) {
            allResponses
        } else {
            allResponses.filter { it.scheduledTime.startsWith(dateKey) }
        }

        val rows = buildGroupedRows(filtered)

        if (rows.isEmpty()) {
            showEmpty()
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            adapter = IntakeHistoryStyledAdapter(rows).also { recyclerView.adapter = it }
        }
    }

    private fun showEmpty() {
        emptyState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    /**
     * Grouping: Date -> (Missed by med -> times) + (Taken by med -> times)
     */
    private fun buildGroupedRows(response: List<IntakeHistoryResponse>): List<HistoryRow> {
        if (response.isEmpty()) return emptyList()

        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())

        val rows = mutableListOf<HistoryRow>()

        response.groupBy { it.scheduledTime.substring(0, 10) }
            .toSortedMap(compareByDescending { it })
            .forEach { (dateStr, itemsForDate) ->

                val dateLabel = runCatching { sdfOut.format(sdfIn.parse(dateStr)!!) }
                    .getOrElse { dateStr }
                rows += HistoryRow.DateHeader(dateLabel)

                val (takenList, missedList) =
                    itemsForDate.partition { it.status.equals("TAKEN", ignoreCase = true) }

                rows += HistoryRow.StatusHeader("Missed", missedList.size, true)
                if (missedList.isNotEmpty()) {
                    missedList
                        .groupBy { it.medicationName }
                        .toSortedMap(String.CASE_INSENSITIVE_ORDER)
                        .forEach { (med, items) ->
                            rows += HistoryRow.MedGroupHeader(med)
                            items.sortedBy { it.scheduledTime.substring(11, 16) }
                                .forEach {
                                    rows += HistoryRow.TimeRow(
                                        time = it.scheduledTime.substring(11, 16),
                                        taken = false
                                    )
                                }
                        }
                }

                rows += HistoryRow.StatusHeader("Taken", takenList.size, false)
                if (takenList.isNotEmpty()) {
                    takenList
                        .groupBy { it.medicationName }
                        .toSortedMap(String.CASE_INSENSITIVE_ORDER)
                        .forEach { (med, items) ->
                            rows += HistoryRow.MedGroupHeader(med)
                            items.sortedBy { it.scheduledTime.substring(11, 16) }
                                .forEach {
                                    rows += HistoryRow.TimeRow(
                                        time = it.scheduledTime.substring(11, 16),
                                        taken = true
                                    )
                                }
                        }
                }
            }

        return rows
    }

    private fun setupSimpleNavbar(view: View) {
        // These are optional; ensure your layout contains them
        val backButton = view.findViewById<TextView?>(R.id.btn_back)
        val pageTitle = view.findViewById<TextView?>(R.id.page_title)

        pageTitle?.text = "Intake History"
        backButton?.setOnClickListener { findNavController().navigateUp() }
    }
}
