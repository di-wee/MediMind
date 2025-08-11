package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.medimind.network.ApiClient
import com.example.medimind.network.IntakeMedRequest
import com.example.medimind.network.MedResponse
import com.example.medimind.network.ScheduleResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.text.style.RelativeSizeSpan
import android.text.style.ForegroundColorSpan
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class ReminderDialogFragment(
    private val meds: List<MedResponse>,
    private val localDate: LocalDate,
    private val patientId: String,
    private val medToSchedule: Map<String, ScheduleResponse>,
    private val timeMillis: Long
) : DialogFragment() {
    override fun onStart() {
        super.onStart()
        val w = (resources.displayMetrics.widthPixels * 0.88f).toInt()
        dialog?.window?.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder_bottom_sheet, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container = view.findViewById<LinearLayout>(R.id.med_list_container)
        val saveButton = view.findViewById<Button>(R.id.btn_save)
        val titleText = view.findViewById<TextView>(R.id.titleText)
        val timeText = view.findViewById<TextView>(R.id.timeText)
        val header = view.findViewById<MaterialCardView>(R.id.headerCard)
        val scroll = view.findViewById<ScrollView>(R.id.reminderScroll)

        scroll.viewTreeObserver.addOnGlobalLayoutListener {
            val maxH = (resources.displayMetrics.heightPixels * 0.50f).toInt()
            if (scroll.height > maxH) {
                scroll.layoutParams = scroll.layoutParams.apply { height = maxH }
            }
        }

        titleText.text = "Medication Reminder"
        timeText.text = formatTimeForDisplay(timeMillis)
        header.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light))

        val selected = BooleanArray(meds.size) { false }
        var selectedCount = 0
        var snoozable = true
        val inflater = LayoutInflater.from(requireContext())

        fun updateSaveText() {
            saveButton.text = when (selectedCount) {
                0 -> "Snooze All"
                else -> "Save"
            }
        }
        updateSaveText()

        meds.forEachIndexed { index, med ->
            val itemView = inflater.inflate(R.layout.med_reminder_item, container, false)

            val card = itemView.findViewById<MaterialCardView>(R.id.itemCard)
            val cb = itemView.findViewById<MaterialCheckBox>(R.id.checkbox)
            val tvTitle = itemView.findViewById<TextView>(R.id.titleText)
            val tvInstr = itemView.findViewById<TextView>(R.id.instructionText)
            val tvNote  = itemView.findViewById<TextView>(R.id.noteText)

            applyCardState(card, false)

            val dose = med.intakeQuantity?.toString().orEmpty()
            tvTitle.text = if (dose.isBlank()) med.medicationName else "${med.medicationName}： $dose"

            if (med.instructions.isNullOrBlank()) {
                tvInstr.visibility = View.GONE
            } else {
                tvInstr.visibility = View.VISIBLE
                tvInstr.text = "Instructions: ${med.instructions}"
            }

            if (med.note.isNullOrBlank()) {
                tvNote.visibility = View.GONE
            } else {
                tvNote.visibility = View.VISIBLE
                tvNote.text = "Notes: ${med.note}"
            }

            tvTitle.setOnClickListener { cb.isChecked = !cb.isChecked }

            cb.setOnCheckedChangeListener { _, isChecked ->
                if (selected[index] != isChecked) {
                    selected[index] = isChecked
                    selectedCount += if (isChecked) 1 else -1
                    applyCardState(card, isChecked)
                    updateSaveText()
                }
            }

            container.addView(itemView)
        }

        saveButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val service = ApiClient.retrofitService
                meds.forEachIndexed { i, med ->
                    val schedule = medToSchedule[med.id] ?: return@forEachIndexed
                    val uniqueId = UUID.randomUUID().toString()
                    val intake = IntakeMedRequest(
                        medicationId = med.id,
                        loggedDate = localDate.toString(),
                        isTaken = selected[i],
                        patientId = patientId,
                        scheduleId = schedule.scheduleId,
                        clientRequestId = uniqueId
                    )
                    if (selected[i]) {
                        service.createMedicationLog(intake)
                        clearSnoozeCount(requireContext(), schedule.scheduleId)
                    } else {
                        val count = getSnoozeCount(requireContext(), schedule.scheduleId)
                        if (count < 1) {
                            increaseSnoozeCount(requireContext(), schedule.scheduleId)
                        } else {
                            snoozable = false
                            service.createMedicationLog(intake.copy(isTaken = false))
                            clearSnoozeCount(requireContext(), schedule.scheduleId)
                        }
                    }
                }

                val unselected = meds.filterIndexed { i, _ -> !selected[i] }.map { it.id }
                if (unselected.isNotEmpty() && snoozable) {
                    ReminderUtils.snoozeReminder(requireContext(), unselected, patientId, timeMillis)
                }
                dismiss()
                requireActivity().finish()
            }
        }
    }

    private fun increaseSnoozeCount(context: Context, scheduleId: String) {
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        val current = prefs.getInt(scheduleId, 0)
        prefs.edit().putInt(scheduleId, current + 1).apply()
    }

    private fun clearSnoozeCount(context: Context, scheduleId: String) {
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        prefs.edit().remove(scheduleId).apply()
    }

    private fun getSnoozeCount(context: Context, scheduleId: String): Int {
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        return prefs.getInt(scheduleId, 0)
    }
    private fun formatTimeForDisplay(millis: Long): String {
        val f = DateTimeFormatter.ofPattern("h:mm a")
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime().format(f)
    }
    fun applyCardState(card: MaterialCardView, checked: Boolean) {
        if (checked) {
            card.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.med_item_bg_checked)
            )
            card.strokeWidth = resources.getDimensionPixelSize(R.dimen.med_item_stroke_width)
            card.setStrokeColor(
                ContextCompat.getColor(requireContext(), R.color.med_item_stroke)
            )
        } else {
            card.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.med_item_bg_unchecked)
            )
            card.strokeWidth = 0
        }
    }

}