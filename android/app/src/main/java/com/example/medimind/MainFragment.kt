package com.example.medimind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainFragment : Fragment() //OnMedicineSelectedListener, OnEditMedicineRequestedListener {
{
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Load HomeFragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_log -> {
                    loadFragment(IntakeHistoryFragment())
                    true
                }
                R.id.nav_medication -> {
                    //loadFragment(ActiveMedicineListFragment())
                    Toast.makeText(requireContext(), "Medication tab clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment)
            .commit()
    }

    //Lst I add these function to make sure I can navigate to the nested fragment, and keep the navBar

/*    fun openFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }*/

    /*override fun onMedicineSelected(medicineName: String) {
        val fragment = ViewMedicineDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("medicineName", medicineName)
            }
        }
        openFragment(fragment)
    }

    override fun onEditMedicineRequested(medicineName: String) {
        val fragment = EditMedicineDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("medicineName", medicineName)
            }
        }
        openFragment(fragment)
    }*/
}
