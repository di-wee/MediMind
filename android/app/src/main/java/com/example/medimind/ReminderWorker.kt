package com.example.medimind.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.medimind.R
import com.example.medimind.ReminderActivity


class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        Log.d("ReminderWorker", "🔥 doWork triggered")

        val medIdListString = inputData.getString("med_id_list")
        val patientId = inputData.getString("patient_id")
        val timeMillis = inputData.getLong("time_millis", -1L)

        Log.d("ReminderWorker", "📥 Received data -> med_id_list: $medIdListString, patientId: $patientId, timeMillis: $timeMillis")

        if (medIdListString == null || patientId == null || timeMillis == -1L) {
            Log.e("ReminderWorker", "❌ Missing input data. Work failed.")
            return Result.failure()
        }

        showNotification(applicationContext, medIdListString, timeMillis, patientId)
        return Result.success()
    }

    private fun showNotification(context: Context, medIdListString: String, timeMillis: Long, patientId: String) {
        Log.d("ReminderWorker", "🔔 Preparing notification for timeMillis = $timeMillis, patientId = $patientId")

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "med_channel",
            "Medication Reminder",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for medication reminders"
            enableVibration(true)
            setSound(
                soundUri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }

        manager.createNotificationChannel(channel)

        val intent = Intent(context, ReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("time_millis", timeMillis)
            putExtra("patient_id", patientId)
            putExtra("med_id_list", medIdListString)
        }

        Log.d("ReminderWorker", "🎯 Intent created with time_millis=$timeMillis, med_id_list=$medIdListString")

        val pendingIntent = PendingIntent.getActivity(
            context,
            timeMillis.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "med_channel")
            .setSmallIcon(R.drawable.ic_med)
            .setContentTitle("Snoozed Reminder")
            .setContentText("It's time to take your medicine (snoozed)!")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())

        Log.d("ReminderWorker", "✅ Notification shown")
    }
}
