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

class ReminderDialogFragment(
    private val meds: List<MedResponse>,
    private val localDate: LocalDate,
    private val patientId: String,
    private val medToSchedule: Map<String, ScheduleResponse>,
    private val timeMillis: Long
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.findViewById<LinearLayout>(R.id.med_list_container)
        val saveButton = view.findViewById<Button>(R.id.btn_save)
        val snoozeButton = view.findViewById<Button>(R.id.btn_snooze)

        val selected = BooleanArray(meds.size) { false }
        var snoozableforsnooze = true
        var snoozableforsave =true

        meds.forEachIndexed { index, med ->
            val title = "${med.medicationName}  —  ${med.intakeQuantity ?: ""}".trim()
            val instr = med.instructions?.takeIf { it.isNotBlank() } ?: ""
            val note  = med.note?.takeIf { it.isNotBlank() } ?: ""

            val sb = android.text.SpannableStringBuilder()

            val tStart = sb.length
            sb.append(title)
            val tEnd = sb.length
            sb.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), tStart, tEnd, 0)
            sb.setSpan(android.text.style.RelativeSizeSpan(1.25f), tStart, tEnd, 0)

            if (instr.isNotEmpty()) {
                val iStart = sb.length
                sb.append("\n• ").append(instr)
                val iEnd = sb.length
                sb.setSpan(android.text.style.RelativeSizeSpan(0.92f), iStart, iEnd, 0)
                sb.setSpan(android.text.style.ForegroundColorSpan(0xFF666666.toInt()), iStart, iEnd, 0)
            }

            if (note.isNotEmpty()) {
                val nStart = sb.length
                sb.append("\n📝 ").append(note)
                val nEnd = sb.length
                sb.setSpan(android.text.style.RelativeSizeSpan(0.92f), nStart, nEnd, 0)
                sb.setSpan(android.text.style.ForegroundColorSpan(0xFF666666.toInt()), nStart, nEnd, 0)
            }

            val checkBox = CheckBox(requireContext()).apply {
                textSize = 16f
                setLineSpacing(4f, 1.0f)
                setPadding(16, 20, 16, 20)
                setText(sb, TextView.BufferType.SPANNABLE)

                setOnCheckedChangeListener { _, isChecked ->
                    selected[index] = isChecked
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 6, 0, 6) }
            }

            container.addView(checkBox)
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
                            snoozableforsave = false
                            service.createMedicationLog(intake.copy(isTaken = false))
                            clearSnoozeCount(requireContext(), schedule.scheduleId)
                        }
                    }
                }

                val unselected = meds.filterIndexed { i, _ -> !selected[i] }.map { it.id }
                if (unselected.isNotEmpty()&&snoozableforsave) {
                    ReminderUtils.snoozeReminder(requireContext(), unselected, patientId, timeMillis)
                }
                dismiss()
                requireActivity().finish()
            }
        }

        snoozeButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val service = ApiClient.retrofitService
                val unselected = mutableListOf<String>()
                meds.forEach { med ->
                    val schedule = medToSchedule[med.id] ?: return@forEach
                    val count = getSnoozeCount(requireContext(), schedule.scheduleId)
                    if (count < 4) {
                        increaseSnoozeCount(requireContext(), schedule.scheduleId)
                        unselected.add(med.id)
                    } else {
                        val intake = IntakeMedRequest(
                            medicationId = med.id,
                            loggedDate = localDate.toString(),
                            isTaken = false,
                            patientId = patientId,
                            scheduleId = schedule.scheduleId,
                            clientRequestId = UUID.randomUUID().toString()
                        )
                        snoozableforsnooze =false
                        service.createMedicationLog(intake)
                        clearSnoozeCount(requireContext(), schedule.scheduleId)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Too many snoozes!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if(snoozableforsnooze){ ReminderUtils.snoozeReminder(requireContext(), unselected, patientId, timeMillis)}
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
}