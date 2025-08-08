package com.example.medimind

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
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

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.CENTER)
    }

    private fun increaseSnoozeCount(context: Context, scheduleId: String) {
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        val current = prefs.getInt(scheduleId, 0)
        prefs.edit().putInt(scheduleId, current + 1).apply()
    }

    private fun clearSnoozeCount(context: Context, scheduleId: String,timeMillis: Long){
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        prefs.edit().remove(scheduleId).apply()
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("snooze_${scheduleId}_$timeMillis")
        Log.d("ReminderDialog", "🔕 Snooze work cancelled for scheduleId=$scheduleId")
    }

    private fun getSnoozeCount(context: Context, scheduleId: String): Int {
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        return prefs.getInt(scheduleId, 0)
    }
}
