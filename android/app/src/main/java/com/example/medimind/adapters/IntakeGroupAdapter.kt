package com.example.medimind.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.R
import com.example.medimind.model.IntakeGroup

class IntakeGroupAdapter(private val intakeGroups: List<IntakeGroup>) :
    RecyclerView.Adapter<IntakeGroupAdapter.IntakeGroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntakeGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_intake_group, parent, false)
        return IntakeGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: IntakeGroupViewHolder, position: Int) {
        val intakeGroup = intakeGroups[position]
        holder.timeTextView.text = intakeGroup.time
        holder.medicineRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.medicineRecyclerView.adapter = MedicineIntakeItemAdapter(intakeGroup.medicines)
    }

    override fun getItemCount(): Int = intakeGroups.size

    class IntakeGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val medicineRecyclerView: RecyclerView = itemView.findViewById(R.id.medicineRecyclerView)
    }
}
