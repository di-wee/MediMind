package com.example.medimind

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.medimind.ReminderUtils.scheduleAlarm
import com.example.medimind.data.AppDatabase
import com.example.medimind.data.Medication
import com.example.medimind.data.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class DebugFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_debug, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkAndRequestNotificationPermission()

        val testReminderButton = view.findViewById<Button>(R.id.btnTestReminder)
        val backToLoginButton = view.findViewById<Button>(R.id.login)
        testReminderButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(requireContext())

                    val med = Medication(
                        name = "Test Medicine",
                        dosage = "1 pill",
                        frequency = "1",
                        scheduleTime = "",
                        date = Date().time,
                        isTaken = false
                    )
                    val medId = db.medicationDao().insertMedication(med).toInt()

                    val futureTime = System.currentTimeMillis() + 5 * 1000
                    val schedule = Schedule(medId = medId, timeMillis = futureTime)
                    db.scheduleDao().insert(schedule)

                    withContext(Dispatchers.Main) {
                        scheduleAlarm(requireContext(), schedule)
                        Toast.makeText(requireContext(), "Alert is created", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        backToLoginButton.setOnClickListener {
            findNavController().navigate(R.id.action_debugFragment_to_loginFragment)
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "notification permission on", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "notification permission is rejected，will intent to setting page", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                startActivity(intent)
            }
        }
    }
}
