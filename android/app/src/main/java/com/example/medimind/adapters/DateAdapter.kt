package com.example.medimind.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.R
import com.example.medimind.model.DateGroup

class DateAdapter(private val dateGroups: List<DateGroup>) :
    RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val intakeGroupRecyclerView: RecyclerView = view.findViewById(R.id.intakeGroupRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_date_group, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val dateGroup = dateGroups[position]
        holder.dateTextView.text = dateGroup.date
        holder.intakeGroupRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.intakeGroupRecyclerView.adapter = IntakeGroupAdapter(dateGroup.intakeGroups)
    }

    override fun getItemCount(): Int = dateGroups.size
}
