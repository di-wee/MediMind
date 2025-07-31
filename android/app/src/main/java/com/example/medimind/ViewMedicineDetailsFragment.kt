package com.example.medimind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ViewMedicineDetailsFragment : Fragment() {

    private lateinit var medicineName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_medicine_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        medicineName = arguments?.getString("medicineName") ?: "Unknown Medicine"
        view.findViewById<TextView>(R.id.medicineNameTitle).text = medicineName

        // Navigate to EditMedicineDetailsFragment passing medicineName
        view.findViewById<Button>(R.id.btnEditDetails).setOnClickListener {
            val bundle = Bundle().apply {
                putString("medicineName", medicineName)
            }
            findNavController().navigate(R.id.action_viewMedicineDetailsFragment_to_editMedicineDetailsFragment, bundle)
        }

        view.findViewById<Button>(R.id.btnMedicationPurpose).setOnClickListener {
            Toast.makeText(context, "Opening medication purpose...", Toast.LENGTH_SHORT).show()

            // Capitalize first letter
            val formattedName = medicineName.trim().replaceFirstChar { it.uppercase() }

            // Example URL
            val url = "https://en.wikipedia.org/wiki/$formattedName"

            // Assuming you have a WebViewFragment with a newInstance(url) method
            val fragment = WebViewFragment.newInstance(url)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            Toast.makeText(context, "Delete Button selected", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
