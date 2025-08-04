package com.example.medimind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medimind.network.ApiClient
import kotlinx.coroutines.launch

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
        val medicineId = arguments?.getString("medicineId") ?: return

        view.findViewById<TextView>(R.id.medicineNameTitle).text = medicineName

        // Navigate to EditMedicineDetailsFragment passing medicineName
        view.findViewById<Button>(R.id.btnEditDetails).setOnClickListener {
            val bundle = Bundle().apply {
                putString("medicineName", medicineName)
                putString("medicineId", medicineId)
            }
            findNavController().navigate(
                R.id.action_viewMedicineDetailsFragment_to_editMedicineDetailsFragment,
                bundle
            )
        }

        // Navigate to WebViewFragment passing URL as argument using NavController
        view.findViewById<Button>(R.id.btnMedicationPurpose).setOnClickListener {
            Toast.makeText(context, "Opening medication purpose...", Toast.LENGTH_SHORT).show()

            // Capitalize first letter for URL
            val formattedName = medicineName.trim().replaceFirstChar { it.uppercase() }
            val url = "https://en.wikipedia.org/wiki/$formattedName"

            val bundle = Bundle().apply {
                putString("EXTERNAL_URL", url)
            }
            findNavController().navigate(R.id.action_viewMedicineDetailsFragment_to_webViewFragment, bundle)
        }

        view.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Remove Medication")
                .setMessage("Are you sure you want to remove this medication?")
                .setPositiveButton("Yes") {_, _ ->
                    lifecycleScope.launch {
                        try {
                            val api = ApiClient.retrofitService
                            api.deactivateMedication(medicineId)
                            Toast.makeText(requireContext(), "Medication removed", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Failed to remove: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
