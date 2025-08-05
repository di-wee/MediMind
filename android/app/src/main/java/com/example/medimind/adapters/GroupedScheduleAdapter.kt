package com.example.medimind.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.R
import com.example.medimind.network.ScheduleItem

sealed class ScheduleListItem {
    data class TimeHeader(val time: String) : ScheduleListItem()
    data class MedicationEntry(val item: ScheduleItem) : ScheduleListItem()
}

class GroupedScheduleAdapter(private val items: List<ScheduleListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ScheduleListItem.TimeHeader -> TYPE_HEADER
            is ScheduleListItem.MedicationEntry -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_schedule_group_header, parent, false)
            TimeHeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_schedule_medication, parent, false)
            MedicationViewHolder(view)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ScheduleListItem.TimeHeader -> {
                (holder as TimeHeaderViewHolder).bind(item)
            }
            is ScheduleListItem.MedicationEntry -> {
                (holder as MedicationViewHolder).bind(item.item)
            }
        }
    }

    class TimeHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView = itemView.findViewById(R.id.groupHeader)
        fun bind(header: ScheduleListItem.TimeHeader) {
            headerText.text = header.time
        }
    }

    class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.medicationNameText)
        private val quantityText: TextView = itemView.findViewById(R.id.quantityText)
        fun bind(item: ScheduleItem) {
            nameText.text = item.medicationName
            quantityText.text = "take ${item.quantity} each time."
        }
    }
}
