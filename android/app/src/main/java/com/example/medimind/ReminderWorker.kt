package com.example.medimind.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.medimind.R
import com.example.medimind.ReminderReceiver

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val context = applicationContext
        val medId = inputData.getInt("med_id", -1)
        val patientId = inputData.getString("patient_id")
        if (medId == -1|| patientId==null) return Result.failure()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("from_worker", true)
            putExtra("med_id", medId)
            putExtra("patientId",patientId)
        }
        context.sendBroadcast(intent)

        showNotification(context)

        return Result.success()
    }

    private fun showNotification(context: Context) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                "med_channel",
                "Medication Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for medication reminders"
                enableVibration(true)
                setSound(soundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            }
            manager.createNotificationChannel(channel)


        val builder = NotificationCompat.Builder(context, "med_channel")
            .setSmallIcon(R.drawable.ic_med)
            .setContentTitle("Snoozed Reminder")
            .setContentText("It's time to take your medicine (snoozed)!")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
