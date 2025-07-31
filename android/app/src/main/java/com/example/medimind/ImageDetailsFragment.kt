package com.example.medimind

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.fragment.findNavController

class ImageDetailsFragment : Fragment() {

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
        if (imageUri != null) {
            imagePreview.setImageURI(imageUri)
        }

        // Use NavController to navigate back
        val backBtnFromImageDetails = view.findViewById<Button>(R.id.btnBackFromImageDetails)
        backBtnFromImageDetails.setOnClickListener {
            findNavController().popBackStack()
        }

        val saveBtnFromImageDetails = view.findViewById<Button>(R.id.btnSaveFromImageDetails)
        saveBtnFromImageDetails.setOnClickListener {
            // TODO: Save info to database
        }
    }
}
