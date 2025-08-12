package com.example.medimind.adapters

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.R

class IntakeHistoryStyledAdapter(
    private val rows: List<HistoryRow>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TYPE_DATE = 1
        const val TYPE_STATUS = 2
        const val TYPE_MED_GROUP = 3
        const val TYPE_TIME = 4
    }

    override fun getItemViewType(position: Int): Int = when (rows[position]) {
        is HistoryRow.DateHeader     -> TYPE_DATE
        is HistoryRow.StatusHeader   -> TYPE_STATUS
        is HistoryRow.MedGroupHeader -> TYPE_MED_GROUP
        is HistoryRow.TimeRow        -> TYPE_TIME
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE -> DateVH(makeText(parent, 22f, bold = true, top = 16, bottom = 8))
            TYPE_STATUS -> StatusVH(makeText(parent, 16f, bold = true, top = 8, bottom = 4))
            TYPE_MED_GROUP -> MedGroupVH(makeText(parent, 18f, bold = true, left = 8, top = 8, bottom = 4))
            TYPE_TIME -> TimeVH(makeTimeRow(parent))
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is HistoryRow.DateHeader -> (holder as DateVH).bind(row)
            is HistoryRow.StatusHeader -> (holder as StatusVH).bind(row)
            is HistoryRow.MedGroupHeader -> (holder as MedGroupVH).bind(row)
            is HistoryRow.TimeRow -> (holder as TimeVH).bind(row)
        }
    }

    override fun getItemCount(): Int = rows.size

    // --- ViewHolders ---

    private class DateVH(private val tv: TextView) : RecyclerView.ViewHolder(tv) {
        fun bind(row: HistoryRow.DateHeader) {
            tv.text = row.date
        }
    }

    private class StatusVH(private val tv: TextView) : RecyclerView.ViewHolder(tv) {
        fun bind(row: HistoryRow.StatusHeader) {
            val color = if (row.isMissed)
                android.R.color.darker_gray
            else
                R.color.md_theme_light_primary
            tv.setTextColor(ContextCompat.getColor(tv.context, color))
            tv.text = "${row.label.uppercase()} (${row.count})"
        }
    }

    private class MedGroupVH(private val tv: TextView) : RecyclerView.ViewHolder(tv) {
        fun bind(row: HistoryRow.MedGroupHeader) {
            tv.text = row.name
        }
    }

    private class TimeVH(private val root: LinearLayout) : RecyclerView.ViewHolder(root) {
        private val icon: ImageView = root.getChildAt(0) as ImageView
        private val text: TextView = root.getChildAt(1) as TextView

        fun bind(row: HistoryRow.TimeRow) {
            val ctx = root.context
            val iconRes = if (row.taken) R.drawable.med_taken_ic else R.drawable.med_miss_ic
            icon.setImageResource(iconRes)
            text.text = "${row.time}  " + if (row.taken) "Taken" else "Missed"
            text.setTextColor(ContextCompat.getColor(ctx, R.color.md_theme_light_onBackground))
        }
    }

    // --- helpers to build simple rows programmatically ---

    private fun makeText(
        parent: ViewGroup,
        sizeSp: Float,
        bold: Boolean = false,
        left: Int = 0,
        top: Int = 0,
        right: Int = 0,
        bottom: Int = 0
    ): TextView {
        val ctx = parent.context
        return TextView(ctx).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(ctx, left), dp(ctx, top), dp(ctx, right), dp(ctx, bottom))
            }
            textSize = sizeSp
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(ctx, R.color.md_theme_light_onBackground))
        }
    }

    private fun makeTimeRow(parent: ViewGroup): LinearLayout {
        val ctx = parent.context
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                // slightly more start margin so icons don't touch the edge
                setMargins(dp(ctx, 20), dp(ctx, 6), dp(ctx, 16), dp(ctx, 6))
            }
        }

        val icon = ImageView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(ctx, 30), dp(ctx, 30)).apply {
                marginEnd = dp(ctx, 12)
            }
            // Prevent cropping
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            // tiny padding avoids circular edges from touching bounds
            setPadding(dp(ctx, 2), dp(ctx, 2), dp(ctx, 2), dp(ctx, 2))
        }

        val text = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 16f
        }

        container.addView(icon)
        container.addView(text)
        return container
    }

    private fun dp(v: View, value: Int) = dp(v.context, value)
    private fun dp(ctx: android.content.Context, value: Int): Int =
        (value * ctx.resources.displayMetrics.density).toInt()
}
