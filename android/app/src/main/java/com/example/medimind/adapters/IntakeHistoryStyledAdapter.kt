package com.example.medimind.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.R

// 3 view types to match the design
sealed class HistoryRow {
    data class DateHeader(val dateLabel: String) : HistoryRow()
    data class StatusHeader(val label: String, val count: Int, val isMissed: Boolean) : HistoryRow()
    data class MedRow(val name: String, val time: String, val taken: Boolean) : HistoryRow()
}

class IntakeHistoryStyledAdapter(
    private val items: List<HistoryRow>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TYPE_DATE = 0
        const val TYPE_STATUS = 1
        const val TYPE_MED = 2
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is HistoryRow.DateHeader -> TYPE_DATE
        is HistoryRow.StatusHeader -> TYPE_STATUS
        is HistoryRow.MedRow -> TYPE_MED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_DATE -> DateVH(inf.inflate(R.layout.item_history_date_header, parent, false))
            TYPE_STATUS -> StatusVH(inf.inflate(R.layout.item_history_status_header, parent, false))
            else -> MedVH(inf.inflate(R.layout.item_history_row, parent, false))
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = items[position]) {
            is HistoryRow.DateHeader -> (holder as DateVH).bind(row)
            is HistoryRow.StatusHeader -> (holder as StatusVH).bind(row)
            is HistoryRow.MedRow -> (holder as MedVH).bind(row)
        }
    }

    private class DateVH(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.txtDateHeader)
        fun bind(row: HistoryRow.DateHeader) {
            title.text = row.dateLabel
        }
    }

    private class StatusVH(view: View) : RecyclerView.ViewHolder(view) {
        private val label = view.findViewById<TextView>(R.id.txtStatusHeader)
        fun bind(row: HistoryRow.StatusHeader) {
            val text = "${row.label.uppercase()} (${row.count})"
            label.text = text
            // subtle color difference: missed = grey, taken = greenish
            val color = if (row.isMissed)
                label.context.getColor(R.color.md_grey_500)
            else
                label.context.getColor(R.color.md_theme_light_primary)
            label.setTextColor(color)
        }
    }

    private class MedVH(view: View) : RecyclerView.ViewHolder(view) {
        private val icon = view.findViewById<ImageView>(R.id.imgMedIcon)
        private val title = view.findViewById<TextView>(R.id.txtMedTitle)
        fun bind(row: HistoryRow.MedRow) {
            // “Panadol, 1:02 PM”
            title.text = "${row.name}, ${row.time}"
            icon.setImageResource(if (row.taken) R.drawable.med_taken_ic else R.drawable.med_miss_ic)
            // (Optional) content description for accessibility
            icon.contentDescription = if (row.taken) "Taken" else "Missed"
        }
    }
}
