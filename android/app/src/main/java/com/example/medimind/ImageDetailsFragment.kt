package com.example.medimind

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medimind.data.ImageOutput
import com.example.medimind.network.ApiClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


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

        val imagePreview = view.findViewById<ImageView>(R.id.imagePreview)
        val nameInput = view.findViewById<EditText>(R.id.medicationNameInput)
        val intakeQuantityInput = view.findViewById<EditText>(R.id.intakeQuantityInput)
        val frequencyInput = view.findViewById<EditText>(R.id.frequencyInput)
        val instructionInput = view.findViewById<EditText>(R.id.instructionInput)
        val noteInput = view.findViewById<EditText>(R.id.noteInput)

        if (imageUri != null) {
            imagePreview.setImageURI(imageUri)

            val imagePath = getRealPathFromURI(imageUri)
            if (imagePath != null) {
                val file = File(imagePath)
                val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, reqFile)

                lifecycleScope.launch {
                    try {
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
            }

            // Use NavController to navigate back
            val backBtnFromImageDetails = view.findViewById<Button>(R.id.btnBackFromImageDetails)
            backBtnFromImageDetails.setOnClickListener {
                findNavController().popBackStack()
            }

            val saveBtnFromImageDetails = view.findViewById<Button>(R.id.btnSaveFromImageDetails)
            saveBtnFromImageDetails.setOnClickListener {
                Toast.makeText(requireContext(), "Saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
