package com.example.medimind.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.R
import com.example.medimind.service.MedicationResponse

class MedicineAdapter(
    private val medicines: List<MedicationResponse>,
    private val onItemClick:(MedicationResponse) -> Unit
) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    inner class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineNameTextView: TextView = itemView.findViewById(R.id.medicationNameText)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantityText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule_medication, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.medicineNameTextView.text = medicine.medicationName
        holder.quantityTextView.text = "(Take ${medicine.intakeQuantity} each time.)"
        holder.itemView.setOnClickListener{
            onItemClick(medicine)
        }
    }

    override fun getItemCount() = medicines.size
}