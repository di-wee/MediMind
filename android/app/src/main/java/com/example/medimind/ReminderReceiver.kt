package com.example.medimind

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.medimind.R
import androidx.lifecycle.lifecycleScope
import com.example.medimind.network.ScheduleListRequest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import com.example.medimind.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "== onReceive TRIGGERED ==")

        val timeMillis = intent.getLongExtra("time_millis", -1)
        val patientId = intent.getStringExtra("patient_id") ?: return
        val scheduleId = intent.getStringExtra("schedule_id") ?: return
        if (timeMillis == -1L) return

        // Convert timeMillis to LocalTime in system default timezone
        val localTime = Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()

        val timeStr = localTime.toString() // Format: HH:mm:ss
        val service = ApiClient.retrofitService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Query backend for all active schedules at this time
                val schedules = service.getSchedulesByTime(ScheduleListRequest(timeStr, patientId))

                // If there are no active schedules, skip notification
                if (schedules.isEmpty()) {
                    Log.d("ReminderReceiver", "No active schedule at $timeStr for patient $patientId. Skipping notification.")
                    return@launch
                }

                // Active schedules found → trigger notification
                val activityIntent = Intent(context, ReminderActivity::class.java).apply {
                    putExtra("time_millis", timeMillis)
                    putExtra("patient_id", patientId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    timeMillis.hashCode(), // Use hash as requestCode
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channelId = "med_channel_debug"

                // Create notification channel (Android 8+)
                val channel = NotificationChannel(
                    channelId,
                    "Medication Reminder",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableVibration(true)
                    setSound(soundUri, AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                }
                manager.createNotificationChannel(channel)

                // Build and show notification
                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Medication Reminder")
                    .setContentText("It's time to take your medicine!")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

                manager.notify(timeMillis.hashCode(), builder.build())

                // Schedule the same alarm for tomorrow
                val tomorrow = LocalDate.now().plusDays(1)
                val nextTriggerMillis = localTime.atDate(tomorrow)
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()

                ReminderUtils.scheduleAlarm(context, nextTriggerMillis, patientId)

            } catch (e: Exception) {
                Log.e("ReminderReceiver", "API error: ${e.message}", e)
            }
        }
    }
}
