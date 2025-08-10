package com.example.medimind.adapters

import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.R
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import androidx.core.content.ContextCompat
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.view.animation.DecelerateInterpolator

class MedReminderDialogAdapter(
    private val items: List<Spannable>,
    private val onCheckedChanged: (position: Int, isChecked: Boolean) -> Unit
) : RecyclerView.Adapter<MedReminderDialogAdapter.ViewHolder>() {

    private val checkedStates: MutableList<Boolean> = MutableList(items.size) { false }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView? = itemView.findViewById(R.id.medicationCard)
        val check: CheckBox = itemView.findViewById(R.id.check)
        val medicationText: TextView = itemView.findViewById(R.id.medicationText)
        val divider: View = itemView.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_med_reminder_checkbox, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.medicationText.text = items[position]
        holder.check.isChecked = checkedStates[position]
        holder.divider.visibility = if (position == items.lastIndex) View.GONE else View.VISIBLE

        // Enhanced card styling based on checked state
        holder.card?.let { updateCardAppearance(holder, checkedStates[position]) }

        holder.check.setOnCheckedChangeListener(null)
        holder.check.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            checkedStates[position] = isChecked
            holder.card?.let { updateCardAppearance(holder, isChecked) }
            holder.card?.let { animateCardSelection(holder, isChecked) }
            onCheckedChanged(position, isChecked)
        }

        holder.itemView.setOnClickListener {
            val newState = !holder.check.isChecked
            holder.check.isChecked = newState
        }

        // Add subtle hover effect on card
        holder.card?.setOnClickListener {
            val newState = !holder.check.isChecked
            holder.check.isChecked = newState
        }
    }

    private fun updateCardAppearance(holder: ViewHolder, isChecked: Boolean) {
        val context = holder.itemView.context
        val card = holder.card ?: return

        if (isChecked) {
            // Use more compatible color resources
            card.setCardBackgroundColor(
                ContextCompat.getColor(context, android.R.color.holo_blue_light)
            )
            card.strokeColor = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
            card.strokeWidth = 3
        } else {
            card.setCardBackgroundColor(
                ContextCompat.getColor(context, android.R.color.white)
            )
            card.strokeColor = ContextCompat.getColor(context, android.R.color.darker_gray)
            card.strokeWidth = 1
        }
    }

    private fun animateCardSelection(holder: ViewHolder, isChecked: Boolean) {
        val card = holder.card ?: return

        val scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.98f, 1f)
        val scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.98f, 1f)
        val elevation = ObjectAnimator.ofFloat(
            card,
            "cardElevation",
            card.cardElevation,
            if (isChecked) 8f else 4f
        )

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY, elevation)
        animatorSet.duration = 200
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.start()
    }

    fun setAllChecked(value: Boolean) {
        for (i in checkedStates.indices) checkedStates[i] = value
        notifyItemRangeChanged(0, itemCount)
    }

    fun getCheckedStates(): List<Boolean> = checkedStates.toList()

    override fun getItemCount(): Int = items.size
}