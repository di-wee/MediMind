package com.example.medimind

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import kotlinx.coroutines.withContext
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import java.util.Date
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext



class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        Log.d("ReminderReceiver", "== onReceive TRIGGERED ==")

        val timeMillis = intent.getLongExtra("time_millis", -1L)
        if (timeMillis == -1L){
            Log.e("ReminderReceiver", "Invalid timeMillis")
            return
        }
        val patientId = intent.getStringExtra("patient_id")
        if (patientId == null) {
            Log.e("ReminderReceiver", "Missing patient_id in intent extras")
            return
        }
        Log.d("ReminderReceiver", "== Alarm TRIGGERED for $patientId at ${Date(timeMillis)} ==")

        val localTime = Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        val timeStr = localTime.toString() // e.g., "08:00:00"

        Log.d("ReminderReceiver", "End of onReceive reached")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                    Log.d("ReminderReceiver", "Calling backend with timeStr=$timeStr patientId=$patientId")

                    val service = ApiClient.retrofitService

                    // Prevent network hang: timeout after 8 seconds
                    val response = withTimeout(8000L) {
                        service.getSchedulesByTime(ScheduleListRequest(timeStr, patientId))
                    }
                    //if null return and notification will not pop up
                    if (response.isSuccessful) {
                        val schedules = response.body()
                        if (schedules.isNullOrEmpty()) {
                            Log.d("ReminderReceiver", "No schedules found or backend returned empty")
                        return@launch
                        }
                        Log.d("ReminderReceiver", "Fetched ${schedules.size} schedules")
                    } else {
                        Log.e("ReminderReceiver", "Error from backend: ${response.code()} ${response.message()}")
                    }

                    // Build notification intent → ReminderActivity
                    val activityIntent = Intent(context, ReminderActivity::class.java).apply {
                        putExtra("time_millis", timeMillis)
                        putExtra("patient_id", patientId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        timeMillis.hashCode(),
                        activityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channelId = "med_channel_debug"
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()

                    // Ensure channel created (for Android 8+)
                    val channel = NotificationChannel(
                        channelId,
                        "Medication Reminder",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Channel for medication reminders"
                        enableLights(true)
                        lightColor = Color.BLUE
                        enableVibration(true)
                        setSound(soundUri, audioAttributes)
                    }
                    manager.createNotificationChannel(channel)

                    // Show notification
                    val builder = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Medication Reminder")
                        .setContentText("It's time to take your medicine!")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                    manager.notify(timeMillis.hashCode(), builder.build())
                    Log.d("ReminderReceiver", "Notification sent at $timeStr for $patientId")

                    // Schedule next day's same alarm
                    val tomorrow = LocalDate.now().plusDays(1)
                    val nextTriggerMillis = localTime.atDate(tomorrow)
                        .atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                    ReminderUtils.scheduleAlarm(context, nextTriggerMillis, patientId)
            } catch (e: TimeoutCancellationException) {
                Log.e("ReminderReceiver", "Network timeout when querying backend", e)
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Error during notification logic", e)
            } finally {
                pendingResult.finish() // IMPORTANT: release the broadcast lock
            }
        }
    }
}
