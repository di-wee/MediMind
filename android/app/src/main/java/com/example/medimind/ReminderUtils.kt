package com.example.medimind

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.medimind.data.Schedule
import com.example.medimind.reminder.ReminderWorker
import java.util.Date
import java.util.concurrent.TimeUnit


object ReminderUtils {

    fun scheduleAlarm(context: Context, timeMilli: Long, patientId: String) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.d("ReminderUtils", "== Alarm Schedule Called ==")
        Log.d("ReminderUtils", "timeMillis = ${timeMilli}")
        Log.d("ReminderUtils", "canScheduleExact = ${
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                alarmManager.canScheduleExactAlarms()
            else "N/A"
        }")

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("time_millis", timeMilli)
            putExtra("patient_id",patientId)
        }

        val requestCode = timeMilli.hashCode()

        val existingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (existingIntent != null) {
            Log.d("Alarm", "Alarm for ${Date(timeMilli)} already exists, skipping.")
            return
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeMilli,
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
                timeMilli,
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
            .setInitialDelay(1, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}

