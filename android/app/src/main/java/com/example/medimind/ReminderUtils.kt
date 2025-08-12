package com.example.medimind


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

import com.example.medimind.reminder.ReminderWorker

import java.util.UUID
import java.util.concurrent.TimeUnit


object ReminderUtils {

    fun scheduleAlarm(context: Context, timeMilli: Long, patientId: String) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val adjustedTimeMillis = if (timeMilli < System.currentTimeMillis()) {
            Log.w("ReminderUtils", "⏰ timeMillis is in the past, adjusting to next day.")
            timeMilli + 24 * 60 * 60 * 1000 // ++ 1 day
        } else {
            timeMilli
        }

        Log.d("ReminderUtils", "== Alarm Schedule Called ==")
        Log.d("ReminderUtils", "timeMillis = $adjustedTimeMillis")
        Log.d("ReminderUtils", "canScheduleExact = ${
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                alarmManager.canScheduleExactAlarms()
            else "N/A"
        }")

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.medimind.ACTION_${System.currentTimeMillis()}"
            data = Uri.parse("medimind://${UUID.randomUUID()}")
            putExtra("time_millis", adjustedTimeMillis)
            putExtra("patient_id", patientId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    adjustedTimeMillis,
                    pendingIntent
                )
            } else {
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                adjustedTimeMillis,
                pendingIntent
            )
        }
    }


    /**
     * Delay the reminder of a certain medication by 15 minutes (executed by ReminderWorker）
     */
    fun snoozeReminder(context: Context, medIdList: List<String>, patientId: String,timeMillis: Long) {
        Log.d("ReminderUtils", "✅ Enqueuing snooze work for meds=$medIdList, patientId=$patientId at ${timeMillis}")

        val inputData = workDataOf(
            "med_id_list" to medIdList.joinToString(","),
            "patient_id" to patientId,
            "time_millis" to timeMillis,
        )

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}

