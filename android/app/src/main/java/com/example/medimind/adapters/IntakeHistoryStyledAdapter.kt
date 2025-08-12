package com.example.medimind.adapters

import android.graphics.Typeface
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.R
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

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
            TYPE_STATUS -> StatusVH(makeText(parent, 16f, bold = true))
            TYPE_MED_GROUP -> MedGroupVH(makeText(parent, 18f, bold = true))
            TYPE_TIME -> TimeVH(makeTimeRow(parent))
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is HistoryRow.DateHeader -> (holder as DateVH).bind(row)
            is HistoryRow.StatusHeader -> (holder as StatusVH).bind(row, position)
            is HistoryRow.MedGroupHeader -> (holder as MedGroupVH).bind(row, position)
            is HistoryRow.TimeRow -> (holder as TimeVH).bind(row, position)
        }
    }

    override fun getItemCount(): Int = rows.size

    // ---------- Section boundary helpers ----------

    private fun isWithinStatusSection(pos: Int): Boolean {
        if (pos < 0 || pos >= rows.size) return false
        val type = getItemViewType(pos)
        if (type == TYPE_STATUS || type == TYPE_DATE) return false
        for (i in pos - 1 downTo 0) {
            when (getItemViewType(i)) {
                TYPE_STATUS -> return true
                TYPE_DATE -> return false
            }
        }
        return false
    }

    private fun isLastInSection(pos: Int): Boolean {
        val next = pos + 1
        if (next >= rows.size) return true
        return when (getItemViewType(next)) {
            TYPE_STATUS, TYPE_DATE -> true
            else -> false
        }
    }

    private fun isSectionSingleItem(pos: Int): Boolean = isLastInSection(pos)

    // ---------- ViewHolders ----------

    private class DateVH(private val tv: TextView) : RecyclerView.ViewHolder(tv) {
        fun bind(row: HistoryRow.DateHeader) {
            tv.text = row.date
            (tv.layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(dp(tv.context, 16), dp(tv.context, 16), dp(tv.context, 16), dp(tv.context, 8))
            }
            tv.background = null
            tv.setPadding(0, 0, 0, 0)
            ViewCompat.setElevation(tv, 0f)
        }
    }

    private inner class StatusVH(private val tv: TextView) : RecyclerView.ViewHolder(tv) {
        fun bind(row: HistoryRow.StatusHeader, position: Int) {
            val color = if (row.isMissed) android.R.color.darker_gray else R.color.md_theme_light_primary
            tv.setTextColor(ContextCompat.getColor(tv.context, color))
            tv.text = "${row.label.uppercase()} (${row.count})"

            val single = isSectionSingleItem(position)
            applyCardBackground(
                view = tv,
                role = if (single) CardRole.SINGLE else CardRole.TOP
            )
        }
    }

    private inner class MedGroupVH(private val tv: TextView) : RecyclerView.ViewHolder(tv) {
        fun bind(row: HistoryRow.MedGroupHeader, position: Int) {
            tv.text = row.name
            if (isWithinStatusSection(position)) {
                val last = isLastInSection(position)
                applyCardBackground(
                    view = tv,
                    role = if (last) CardRole.BOTTOM else CardRole.MIDDLE
                )
            } else {
                clearCardBackground(tv, top = 8, bottom = 4)
            }
        }
    }

    private inner class TimeVH(private val root: LinearLayout) : RecyclerView.ViewHolder(root) {
        private val icon: ImageView = root.getChildAt(0) as ImageView
        private val text: TextView = root.getChildAt(1) as TextView

        fun bind(row: HistoryRow.TimeRow, position: Int) {
            val ctx = root.context
            val iconRes = if (row.taken) R.drawable.med_taken_ic else R.drawable.med_miss_ic
            icon.setImageResource(iconRes)
            text.text = "${row.time}  " + if (row.taken) "Taken" else "Missed"
            text.setTextColor(ContextCompat.getColor(ctx, R.color.md_theme_light_onBackground))

            if (isWithinStatusSection(position)) {
                val last = isLastInSection(position)
                applyCardBackground(
                    view = root,
                    role = if (last) CardRole.BOTTOM else CardRole.MIDDLE
                )
            } else {
                clearCardBackground(root, top = 0, bottom = 6)
            }
        }
    }

    // ---------- Card styling (Material shadow/rounded corners) ----------

    private enum class CardRole { TOP, MIDDLE, BOTTOM, SINGLE }

    private fun applyCardBackground(view: View, role: CardRole) {
        val ctx = view.context
        val radius = dp(ctx, 16).toFloat()

        val shape = ShapeAppearanceModel.Builder().apply {
            when (role) {
                CardRole.TOP -> {
                    setTopLeftCornerSize(radius)
                    setTopRightCornerSize(radius)
                    setBottomLeftCornerSize(0f)
                    setBottomRightCornerSize(0f)
                }
                CardRole.MIDDLE -> {
                    setTopLeftCornerSize(0f); setTopRightCornerSize(0f)
                    setBottomLeftCornerSize(0f); setBottomRightCornerSize(0f)
                }
                CardRole.BOTTOM -> {
                    setTopLeftCornerSize(0f); setTopRightCornerSize(0f)
                    setBottomLeftCornerSize(radius); setBottomRightCornerSize(radius)
                }
                CardRole.SINGLE -> {
                    setTopLeftCornerSize(radius); setTopRightCornerSize(radius)
                    setBottomLeftCornerSize(radius); setBottomRightCornerSize(radius)
                }
            }
        }.build()

        val bg = MaterialShapeDrawable(shape).apply {
            setTint(ContextCompat.getColor(ctx, android.R.color.white))
        }

        view.background = bg
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.clipToOutline = true
        }

        val padH = dp(ctx, 16)
        val padV = dp(ctx, 12)
        view.setPadding(padH, padV, padH, padV)

        val lp = (view.layoutParams as? ViewGroup.MarginLayoutParams)
            ?: ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val mH = dp(ctx, 16)
        val topM = if (role == CardRole.TOP || role == CardRole.SINGLE) dp(ctx, 8) else 0
        val bottomM = if (role == CardRole.BOTTOM || role == CardRole.SINGLE) dp(ctx, 8) else 0
        lp.setMargins(mH, topM, mH, bottomM)
        view.layoutParams = lp

        // Real shadow:
        ViewCompat.setElevation(view, dp(ctx, 6).toFloat())
    }

    private fun clearCardBackground(view: View, top: Int, bottom: Int) {
        val ctx = view.context
        view.background = null
        view.setPadding(0, 0, 0, 0)
        (view.layoutParams as ViewGroup.MarginLayoutParams).apply {
            setMargins(dp(ctx, 16), dp(ctx, top), dp(ctx, 16), dp(ctx, bottom))
        }
        ViewCompat.setElevation(view, 0f)
    }

    // ---------- Row builders ----------

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
            if (bold) setTypeface(typeface, Typeface.BOLD)
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
                setMargins(dp(ctx, 20), dp(ctx, 0), dp(ctx, 16), dp(ctx, 0))
            }
        }

        val icon = ImageView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(ctx, 30), dp(ctx, 30)).apply {
                marginEnd = dp(ctx, 12)
            }
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(dp(ctx, 2), dp(ctx, 2), dp(ctx, 2), dp(ctx, 2))
        }

        val text = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 16f
            setTextColor(ContextCompat.getColor(ctx, R.color.md_theme_light_onBackground))
        }

        container.addView(icon)
        container.addView(text)
        return container
    }
}

// Single dp helper (use everywhere)
private fun dp(ctx: android.content.Context, value: Int): Int =
    (value * ctx.resources.displayMetrics.density).toInt()
