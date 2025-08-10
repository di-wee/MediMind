package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class IntakeHistoryFragment : Fragment() {

    // Raw + derived data caches
    private var fullResponse: List<IntakeHistoryResponse> = emptyList()
    private var uniqueDates: List<String> = emptyList() // yyyy-MM-dd (sorted desc)
    private var uniqueMeds: List<String> = emptyList()  // distinct med names (sorted)

    // Current filter selections (null means "All")
    private var selectedDate: String? = null
    private var selectedMed: String? = null

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var dateFilter: MaterialAutoCompleteTextView
    private lateinit var medFilter: MaterialAutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_intake_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Grab views
        recyclerView = view.findViewById<RecyclerView>(R.id.intakeHistoryRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        progressBar = view.findViewById(R.id.progressBar)
        emptyState = view.findViewById(R.id.emptyState)
        dateFilter = view.findViewById(R.id.dateFilterDropdown)
        medFilter = view.findViewById(R.id.medFilterDropdown)

        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPref.getString("patientId", null)

        if (patientId == null) {
            showEmpty()
            return
        }

        // Load data
        showLoading()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                fullResponse = ApiClient.retrofitService.getIntakeHistory(patientId)

                // Build distinct lists for filters
                uniqueDates = fullResponse
                    .map { it.scheduledTime.substring(0, 10) }
                    .distinct()
                    .sortedDescending()

                uniqueMeds = fullResponse
                    .map { it.medicationName }
                    .distinct()
                    .sorted()

                setupFilterDropdowns()
                applyFiltersAndRender()
            } catch (e: Exception) {
                showEmpty()
                Toast.makeText(
                    requireContext(),
                    "Failed to load intake history: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupFilterDropdowns() {
        // Date dropdown: "All dates" + yyyy-MM-dd
        val dateLabels = listOf("All dates") + uniqueDates
        dateFilter.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dateLabels)
        )
        dateFilter.setText("All dates", false)
        dateFilter.setOnItemClickListener { _, _, pos, _ ->
            selectedDate = if (pos == 0) null else uniqueDates[pos - 1]
            applyFiltersAndRender()
        }

        // Medication dropdown: "All" + med names
        val medLabels = listOf("All") + uniqueMeds
        medFilter.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, medLabels)
        )
        medFilter.setText("All", false)
        medFilter.setOnItemClickListener { _, _, pos, _ ->
            selectedMed = if (pos == 0) null else uniqueMeds[pos - 1]
            applyFiltersAndRender()
        }
    }

    private fun applyFiltersAndRender() {
        // Filter raw data
        val filtered = fullResponse.filter { item ->
            (selectedDate == null || item.scheduledTime.startsWith(selectedDate!!)) &&
            (selectedMed == null || item.medicationName == selectedMed)
        }

        // Build rows (grouped by date -> missed/taken -> med name -> times)
        val rows = buildStyledRowsGrouped(filtered)

        if (rows.isEmpty()) {
            showEmpty()
        } else {
            recyclerView.adapter = IntakeHistoryStyledAdapter(rows)
            showList()
        }
    }

    /**
     * Date header -> MISSED section (grouped by med name with times)
     * -> TAKEN section (grouped by med name with times).
     */
    private fun buildStyledRowsGrouped(response: List<IntakeHistoryResponse>): List<HistoryRow> {
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

                // Missed section
                rows += HistoryRow.StatusHeader("Missed", missed.size, isMissed = true)
                missed.groupBy { it.medicationName }.toSortedMap().forEach { (med, medItems) ->
                    rows += HistoryRow.MedGroupHeader(med)
                    medItems.sortedBy { it.scheduledTime.substring(11, 16) }
                        .forEach {
                            rows += HistoryRow.TimeRow(
                                time = it.scheduledTime.substring(11, 16),
                                taken = false
                            )
                        }
                }

                // Taken section
                rows += HistoryRow.StatusHeader("Taken", taken.size, isMissed = false)
                taken.groupBy { it.medicationName }.toSortedMap().forEach { (med, medItems) ->
                    rows += HistoryRow.MedGroupHeader(med)
                    medItems.sortedBy { it.scheduledTime.substring(11, 16) }
                        .forEach {
                            rows += HistoryRow.TimeRow(
                                time = it.scheduledTime.substring(11, 16),
                                taken = true
                            )
                        }
                }
            }

        return rows
    }

    // ----- UI state helpers -----

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    private fun showEmpty() {
        progressBar.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun showList() {
        progressBar.visibility = View.GONE
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}
