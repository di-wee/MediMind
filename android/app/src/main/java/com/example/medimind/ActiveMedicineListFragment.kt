package com.example.medimind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ActiveMedicineListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicineAdapter

    // Dummy list of medicines
    private val dummyMedicineList = listOf("Panadol", "Aspirin", "Metformin")

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
        adapter = MedicineAdapter(dummyMedicineList) { medicineName ->
            val bundle = Bundle().apply {
                putString("medicineName", medicineName)
            }
            findNavController().navigate(R.id.action_activeMedicineListFragment_to_viewMedicineDetailsFragment, bundle)
        }
        recyclerView.adapter = adapter
    }
}
