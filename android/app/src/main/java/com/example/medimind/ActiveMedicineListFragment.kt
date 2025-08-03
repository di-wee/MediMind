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
import com.example.medimind.network.ApiClient
import kotlinx.coroutines.launch

class ActiveMedicineListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicineAdapter
    private var medicineList = mutableListOf<String>()

    // Dummy list of medicines
//    private val dummyMedicineList = listOf("Panadol", "Aspirin", "Metformin")

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

        // Set up adapter with click listener to navigate and pass medicineName
        adapter = MedicineAdapter(medicineList) { medicineName ->
            val bundle = Bundle().apply {
                putString("medicineName", medicineName)
            }
            findNavController().navigate(R.id.action_activeMedicineListFragment_to_viewMedicineDetailsFragment, bundle)
        }
        recyclerView.adapter = adapter

        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPreferences.getString("patientId", null)

        //for test, I hard code a patientId to make it work, will change after we have sharedpreference
        // Lewis: changed patient ID to 0048c909-b76e-4db3-9c71-fa23df0b4f2e for testing.
        fetchActiveMedications(patientId)

//        if (patientId != null) {
//            fetchActiveMedications(patientId)
//        } else {
//            Toast.makeText(context, "No patientId found", Toast.LENGTH_SHORT).show()
//        }
    }

    private fun fetchActiveMedications(patientId: String? = null){
        lifecycleScope.launch {
            try {
                //for test, I hard code a patientId to make it work, will change after we have sharedpreference
                val realPatientId = patientId ?: "0595556f-43c6-4469-aa68-a86753b0a558"
                val response = ApiClient.retrofitService.getPatientMedications(realPatientId)
                val activeNames = response
                    .filter { it.isActive }
                    .sortedBy { it.medicationName.lowercase() }
                    .map { it.medicationName }

                medicineList.clear()
                medicineList.addAll(activeNames)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

