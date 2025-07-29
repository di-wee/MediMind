package com.example.medimind

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.medimind.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class ReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val timeMillis = intent.getLongExtra("time_millis", -1)
        if (timeMillis == -1L) {
            finish()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@ReminderActivity)
            val schedules = db.scheduleDao().getByTimeMillis(timeMillis)
            if (schedules.isEmpty()) {
                finish()
                return@launch
            }

            val meds = schedules.mapNotNull { db.medicationDao().getById(it.medId) }
            val names = meds.map { "${it.name}（${it.dosage}）" }.toTypedArray()
            val selected = BooleanArray(names.size)

            withContext(Dispatchers.Main) {
                AlertDialog.Builder(this@ReminderActivity)
                    .setTitle("Medication Reminder")
                    .setMultiChoiceItems(names, selected) { _, which, isChecked ->
                        selected[which] = isChecked
                    }
                    .setPositiveButton("Save") { _, _ ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            for (i in meds.indices) {
                                if (selected[i]) {
                                    db.medicationDao().insertLog(
                                        MedicationLog(
                                            logId = 0,
                                            medId = meds[i].id,
                                            timestamp = LocalDateTime.now().toString(),
                                            status = "TAKEN"
                                        )
                                    )
                                } else {
                                    ReminderUtils.snoozeReminder(this@ReminderActivity, meds[i].id)
                                }
                            }
                            finish()
                        }
                    }
                    .setNegativeButton("Snooze All") { _, _ ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            meds.forEach {
                                ReminderUtils.snoozeReminder(this@ReminderActivity, it.id)
                            }
                            finish()
                        }
                    }
                    .setOnCancelListener {
                        finish()
                    }
                    .show()
            }
        }
    }
}
