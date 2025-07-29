package com.example.medimind.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.R
import com.example.medimind.model.MedicineIntakeItem

class MedicineIntakeItemAdapter(
    private val medicineList: List<MedicineIntakeItem>
) : RecyclerView.Adapter<MedicineIntakeItemAdapter.MedicineViewHolder>() {

    class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medNameTextView: TextView = itemView.findViewById(R.id.medicineName)
        val hasTakenTextView: TextView = itemView.findViewById(R.id.hasTakenTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_intake_medicine, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicineList[position]
        holder.medNameTextView.text = medicine.name
        holder.hasTakenTextView.text = if (medicine.hasTaken) "Taken" else "Not Taken"
    }

    override fun getItemCount(): Int {
        return medicineList.size
    }
}
