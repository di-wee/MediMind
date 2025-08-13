package com.example.medimind

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.medimind.ReminderUtils.scheduleAlarm
import androidx.navigation.fragment.findNavController
import com.example.medimind.network.ApiClient
import com.example.medimind.network.MLApiClient
import com.example.medimind.network.newMedicationRequest
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Calendar
import androidx.fragment.app.viewModels
import com.example.medimind.viewmodel.MedicationViewModel
import com.example.medimind.viewmodel.SaveMedResult

class ImageDetailsFragment : Fragment() {

    // ▼ NEW: ViewModel instance (uses ApiClient.retrofitService by default)
    private val medicationViewModel: MedicationViewModel by viewModels()

    private fun getRealPathFromURI(uri: Uri): String? {
        val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
        val path = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return path
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Back arrow
        view.findViewById<MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val imageUriString = arguments?.getString("imageUri")
        val imageUri = imageUriString?.let { Uri.parse(it) }

        val sharedPreference = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPreference.getString("patientId", null)

        val imagePreview = view.findViewById<ImageView>(R.id.imagePreview)

        val nameInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.medicationNameInput)
        val intakeQuantityInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.intakeQuantityInput)
        val frequencyInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.frequencyInput)
        val instructionInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.instructionInput)
        val noteInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.noteInput)

        if (imageUri != null) {
            imagePreview.setImageURI(imageUri)

            //Lewis: Instead of using getRealPathFromURI (which may crash on Android 10+), copy content URI to temp file
            //prediction 404. came here to fix image related problem
            lifecycleScope.launch {
                try {
                    val resolver = requireContext().contentResolver
                    val rawMime = resolver.getType(imageUri)?.lowercase() ?: "image/jpeg"
                    val mime = when {
                        rawMime == "image/jpg" -> "image/jpeg"
                        rawMime in setOf("image/jpeg", "image/png", "image/webp") -> rawMime
                        else -> "image/jpeg"
                    }

                    val suffix = when (mime) {
                        "image/png"  -> ".png"
                        "image/webp" -> ".webp"
                        else         -> ".jpg"
                    }
                    val tempFile = File.createTempFile("upload_", suffix, requireContext().cacheDir)
                    resolver.openInputStream(imageUri)!!.use { input ->
                        tempFile.outputStream().use { output -> input.copyTo(output) }
                    }

                    val reqFile = tempFile.asRequestBody(mime.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", tempFile.name, reqFile)

                    // Call API
                    val response = MLApiClient.mlApiService.predictImage(part)
                    if (response.isSuccessful) {
                        val prediction = response.body()
                        if (prediction != null) {
                            nameInput.setText(prediction.medicationName ?: "")
                            intakeQuantityInput.setText(prediction.intakeQuantity ?: "")
                            frequencyInput.setText(prediction.frequency.toString())
                            instructionInput.setText(prediction.instructions ?: "")
                            noteInput.setText(prediction.notes ?: "")
                        }
                    } else {
                        Toast.makeText(requireContext(), "Prediction failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Prediction failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            val saveBtnFromImageDetails = view.findViewById<Button>(R.id.btnSaveFromImageDetails)
            saveBtnFromImageDetails.setOnClickListener {
                var instruction = instructionInput.text.toString()
                var note = noteInput.text.toString()
                var frequency = frequencyInput.text.toString().toIntOrNull() ?:0
                var times = generateDefaultTimes(frequency)
                val medicationName = nameInput.text.toString()

                val dosage = intakeQuantityInput.text.toString()
                val dosageNum = dosage.toDoubleOrNull() ?:0.0
                //this make sure when insert to DB is like xx tablets
                val dosageDisplay = when {
                    dosageNum == 1.0 -> "1 tablet"
                    dosageNum == 0.5 -> "0.5 tablet"
                    dosageNum % 1 == 0.0 -> "${dosageNum.toInt()} tablets"
                    else -> "$dosage tablets"
                }

                if (patientId == null || medicationName.isBlank() || dosage.isBlank() || frequency == 0) {
                    Toast.makeText(requireContext(), "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // ▼ CHANGED: use MedicationViewModel to save; keep alarms + UI flow the same
                lifecycleScope.launch {
                    try {
                        val result = medicationViewModel.saveMedication(
                            medicationName = medicationName,
                            patientId = patientId,
                            dosage = dosageDisplay,
                            frequency = frequency,
                            instructions = instruction,
                            notes = note,
                            times = times // ViewModel normalizes "HHmm" -> "HH:mm" if needed
                        )

                        when (result) {
                            is SaveMedResult.Success -> {
                                for (time in times) {
                                    val timeMilli = convertToScheduleList(time)
                                    scheduleAlarm(requireContext(), timeMilli, patientId)
                                }
                                Toast.makeText(requireContext(), result.message.ifBlank { "Medication saved" }, Toast.LENGTH_SHORT).show()
                                requestNotificationPermissionIfNeeded()
                                parentFragmentManager.popBackStack()
                            }
                            is SaveMedResult.Duplicate -> {
                                // 409 duplicate from backend — surface inline + toast
                                nameInput.error = "An active medication with this name already exists."
                                Toast.makeText(
                                    requireContext(),
                                    if (result.message.isNotBlank()) result.message else "Duplicate active medication name.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is SaveMedResult.NotFound -> {
                                Toast.makeText(
                                    requireContext(),
                                    if (result.message.isNotBlank()) result.message else "Patient not found. Please re-login.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is SaveMedResult.Error -> {
                                Toast.makeText(
                                    requireContext(),
                                    result.message.ifBlank { "Failed to save medication." },
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun generateDefaultTimes(frequency: Int): List<String> {
        val times = mutableListOf<String>()

        try {
            if (frequency <= 0) return emptyList()

            if (frequency == 1) {
                times.add("09:00")
            } else {
                val totalMinutes = 12 * 60
                val freqGap = totalMinutes / (frequency - 1)

                for (i in 0 until frequency) {
                    val timeInMinutes = 540 + i * freqGap
                    val hour = timeInMinutes / 60
                    val minute = timeInMinutes % 60
                    val timeStr = String.format("%02d:%02d", hour, minute)  // HH:MM format
                    times.add(timeStr)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return times
    }

    fun convertToScheduleList(time: String): Long {

        val now = Calendar.getInstance()
        val (hour, minute) = time.split(":").map { it.toInt() }
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DATE, 1)
            }
        }
        return cal.timeInMillis
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val asked = prefs.getBoolean("asked_notification_permission", false)
            if (!asked) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
                prefs.edit {
                    putBoolean("asked_notification_permission", true)
                }
            }
        }
    }
}
