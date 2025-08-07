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
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.medimind.ReminderUtils.scheduleAlarm
import androidx.navigation.fragment.findNavController
import com.example.medimind.data.ImageOutput
import com.example.medimind.network.ApiClient
import com.example.medimind.network.newMedicationRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Calendar

class ImageDetailsFragment : Fragment() {

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
        val imageUriString = arguments?.getString("imageUri")
        val imageUri = imageUriString?.let { Uri.parse(it) }

        val sharedPreference = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPreference.getString("patientId", null)

        val imagePreview = view.findViewById<ImageView>(R.id.imagePreview)
        val nameInput = view.findViewById<EditText>(R.id.medicationNameInput)
        val intakeQuantityInput = view.findViewById<EditText>(R.id.intakeQuantityInput)
        val frequencyInput = view.findViewById<EditText>(R.id.frequencyInput)
        val instructionInput = view.findViewById<EditText>(R.id.instructionInput)
        val noteInput = view.findViewById<EditText>(R.id.noteInput)

        if (imageUri != null) {
            imagePreview.setImageURI(imageUri)

            //Lewis: Instead of using getRealPathFromURI (which may crash on Android 10+), copy content URI to temp file
            lifecycleScope.launch {
                try {
                    //Lewis: Open input stream from the content URI
                    val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                    //Lewis: Create a temporary file in cache directory
                    val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
                    //Lewis: Copy the image content into the temp file
                    tempFile.outputStream().use { outputStream ->
                        inputStream?.copyTo(outputStream)
                    }

                    //Lewis: Create the multipart request body for Retrofit
                    val reqFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", tempFile.name, reqFile)

                    //Lewis: Call the ML API and populate UI with the extracted fields
                    val response = ApiClient.retrofitService.uploadImage(body)
                    nameInput.setText(response.medicationName ?: "")
                    intakeQuantityInput.setText(response.intakeQuantity ?: "")
                    frequencyInput.setText(response.frequency.toString())
                    instructionInput.setText(response.instructions ?: "")
                    noteInput.setText(response.notes ?: "")

                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Prediction failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Use NavController to navigate back
            val backBtnFromImageDetails = view.findViewById<Button>(R.id.btnBackFromImageDetails)
            backBtnFromImageDetails.setOnClickListener {
                findNavController().popBackStack()
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

                lifecycleScope.launch {
                    try {
                        val service = ApiClient.retrofitService

                        val request = newMedicationRequest(
                            medicationName = medicationName,
                            patientId = patientId,
                            dosage = dosageDisplay,
                            frequency = frequency,
                            instructions = instruction,
                            notes = note,
                            isActive = true,
                            times = times
                        )
                        service.saveMedication(request)

                        for (time in times) {
                            var timeMilli = convertToScheduleList(time)
                            scheduleAlarm(requireContext(), timeMilli, patientId)
                        }
                        Toast.makeText(requireContext(), "Medication saved", Toast.LENGTH_SHORT).show()
                        requestNotificationPermissionIfNeeded()
                        parentFragmentManager.popBackStack()
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
