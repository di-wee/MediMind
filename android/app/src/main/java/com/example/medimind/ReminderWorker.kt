package com.example.medimind.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
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
        if (medId == -1) return Result.failure()

        // 可选：发送广播再次触发 ReminderReceiver
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("from_worker", true)
            putExtra("med_id", medId)
        }
        context.sendBroadcast(intent)

        showNotification(context)

        return Result.success()
    }

    private fun showNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "med_channel",
                "Medication Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for medication reminders"
            }
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, "med_channel")
            .setSmallIcon(R.drawable.ic_med)
            .setContentTitle("Snoozed Reminder")
            .setContentText("It's time to take your medicine (snoozed)!")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
