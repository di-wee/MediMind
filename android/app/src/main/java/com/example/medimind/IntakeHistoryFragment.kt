package com.example.medimind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.adapter.DateAdapter
import com.example.medimind.model.DateGroup
import com.example.medimind.model.IntakeGroup
import com.example.medimind.model.MedicineIntakeItem

class IntakeHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intake_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val greeting = view.findViewById<TextView>(R.id.topGreetingText)
        greeting.text = "Hello, Grandpa"

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.intakeHistoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Mock data
        val mockData = listOf(
            DateGroup(
                date = "21/07/2025",
                intakeGroups = listOf(
                    IntakeGroup(
                        time = "0900h",
                        medicines = listOf(
                            MedicineIntakeItem("Amlodipine", true),
                            MedicineIntakeItem("Xanax", true),
                            MedicineIntakeItem("Ibuprofen", false),
                            MedicineIntakeItem("Loratadine", true)
                        )
                    )
                )
            ),
            DateGroup(
                date = "20/07/2025",
                intakeGroups = listOf(
                    IntakeGroup(
                        time = "2100h",
                        medicines = listOf(
                            MedicineIntakeItem("Amlodipine", true),
                            MedicineIntakeItem("Loratadine", true),
                            MedicineIntakeItem("Xanax", false)
                        )
                    )
                )
            )
        )

        recyclerView.adapter = DateAdapter(mockData)
    }
}
