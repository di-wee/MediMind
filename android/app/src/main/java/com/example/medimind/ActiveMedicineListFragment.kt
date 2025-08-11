package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.adapters.MedicineAdapter
import com.example.medimind.service.MedicationResponse
import com.example.medimind.network.ApiClient
import kotlinx.coroutines.launch

class ActiveMedicineListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicineAdapter
    private var medicineList = mutableListOf<MedicationResponse>()

    // NEW: views for empty/loading states
    private lateinit var emptyState: View
    private var progressBar: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_active_medicine_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.medicineListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // NEW: grab empty/progress views (progressBar optional)
        emptyState = view.findViewById(R.id.emptyState)
        progressBar = view.findViewById(R.id.progressBar)

        // Set up adapter with click listener to navigate and pass medicineName
        adapter = MedicineAdapter(medicineList) { medicine ->
            val bundle = Bundle().apply {
                putString("medicineName", medicine.medicationName)
                putString("medicineId", medicine.id)
            }
            findNavController().navigate(
                R.id.action_activeMedicineListFragment_to_viewMedicineDetailsFragment,
                bundle
            )
        }
        recyclerView.adapter = adapter

        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPreferences.getString("patientId", null)

        if (patientId != null) {
            showLoading() // NEW
            fetchActiveMedications(patientId)
        } else {
            showEmpty()   // NEW: show empty instead of a blank list
            Toast.makeText(context, "No patientId found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchActiveMedications(patientId: String){
        lifecycleScope.launch {
            try {
                val response = ApiClient.retrofitService.getPatientMedications(patientId)
                val activeNames = response
                    .filter { it.active }
                    .sortedBy { it.medicationName.lowercase() }

                medicineList.clear()
                medicineList.addAll(activeNames)
                adapter.notifyDataSetChanged()

                // NEW: toggle empty/list
                if (medicineList.isEmpty()) showEmpty() else showList()
            } catch (e: Exception) {
                e.printStackTrace()
                showEmpty() // NEW: fall back to empty state on error
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- NEW: simple UI state helpers ---
    private fun showLoading() {
        progressBar?.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    private fun showEmpty() {
        progressBar?.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun showList() {
        progressBar?.visibility = View.GONE
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}
