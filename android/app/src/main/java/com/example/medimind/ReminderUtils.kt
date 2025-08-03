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
import com.example.medimind.data.Schedule
import com.example.medimind.reminder.ReminderWorker
import java.util.concurrent.TimeUnit

object ReminderUtils {

    fun scheduleAlarm(context: Context, schedule: Schedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.d("ReminderUtils", "== Alarm Schedule Called ==")
        Log.d("ReminderUtils", "timeMillis = ${schedule.timeMillis}")
        Log.d("ReminderUtils", "canScheduleExact = ${
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                alarmManager.canScheduleExactAlarms()
            else "N/A"
        }")

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("time_millis", schedule.timeMillis)
        }

        val requestCode = schedule.timeMillis.hashCode()

        val existingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        //if (existingIntent != null) {
           // return // exists,will not be registered repeatedly
        //}

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
                    schedule.timeMillis,
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
                schedule.timeMillis,
                pendingIntent
            )
        }
    }

    /**
     * Delay the reminder of a certain medication by 15 minutes (executed by ReminderWorker）
     */
    fun snoozeReminder(context: Context, medId: Int) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(15, TimeUnit.MINUTES)
            .setInputData(workDataOf("med_id" to medId))
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}

