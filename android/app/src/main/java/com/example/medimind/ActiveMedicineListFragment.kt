package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ActiveMedicineListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicineAdapter

    private var listener: OnMedicineSelectedListener? = null
    private val dummyMedicineList = listOf("Panadol", "Aspirin", "Metformin")

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnMedicineSelectedListener
            ?: throw ClassCastException("Parent fragment must implement OnMedicineSelectedListener")
    }

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

        adapter = MedicineAdapter(dummyMedicineList) { medicineName ->
            listener?.onMedicineSelected(medicineName)
        }
        recyclerView.adapter = adapter
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}