package com.example.medimind

import android.Manifest

import android.content.Context
import android.content.pm.PackageManager

import android.net.Uri
import android.os.Bundle
import android.view.*

import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.adapters.GroupedScheduleAdapter
import com.example.medimind.adapters.ScheduleListItem
import com.example.medimind.network.ApiClient
import com.example.medimind.network.ScheduleItem
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

import java.io.File

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class HomeFragment : Fragment() {

    // --- Views (late init) ---
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var scheduleRecyclerView: RecyclerView

    // FAB speed dial
    private lateinit var fabMain: ExtendedFloatingActionButton
    private lateinit var fabCluster: LinearLayout
    private lateinit var fabScrim: View
    private lateinit var fabCamera: View
    private lateinit var fabGallery: View
    private lateinit var fabManual: View
    private var dialOpen = false

    // Camera permission + launchers
    private val CAMERA_PERMISSION_CODE = 1
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var imageUri: Uri? = null
    private var pendingImageUri: Uri? = null
    private var shouldNavigateToImageDetails = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result && imageUri != null) {
                pendingImageUri = imageUri
                shouldNavigateToImageDetails = true
            }
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                pendingImageUri = uri
                shouldNavigateToImageDetails = true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // ---- Toolbar (left: Hello {user}, right: date) ----
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.navigationIcon = null

        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPref.getString("patientId", null)

        if (patientId != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val profile = ApiClient.retrofitService.getPatient(patientId)
                    toolbar.title = "Hello, ${profile.firstName ?: "User"}"
                } catch (e: Exception) {
                    toolbar.title = "Hello, User"
                    Toast.makeText(requireContext(), "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            toolbar.title = "Hello, User"
        }

        // Right-side date as action view
        toolbar.menu.findItem(R.id.action_today)?.actionView
            ?.findViewById<TextView>(R.id.tvToday)?.apply {
                val today = LocalDate.now()
                val fmt = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())
                text = today.format(fmt)
            }

        // ---- Content views ----
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer)
        scheduleRecyclerView = view.findViewById(R.id.scheduleRecyclerView)
        scheduleRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Load schedule
        if (patientId != null) {
            val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val hasScheduleKey = "hasScheduleData"

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val raw = ApiClient.retrofitService.getDailySchedule(patientId)
                    val grouped = groupScheduleItems(raw)

                    // Persist that this user has have schedule data once code sees any
                    if (raw.isNotEmpty()) {
                        prefs.edit().putBoolean(hasScheduleKey, true).apply()
                    }

                    emptyStateContainer.visibility = if (raw.isEmpty()) View.VISIBLE else View.GONE
                    scheduleRecyclerView.adapter = GroupedScheduleAdapter(grouped)
                } catch (e: Exception) {
                    // If code has not seen data before, treat as "no meds yet" and don't toast
                    val hasEverHadData = prefs.getBoolean(hasScheduleKey, false)

                    emptyStateContainer.visibility = View.VISIBLE
                    scheduleRecyclerView.adapter = GroupedScheduleAdapter(emptyList())

                    if (hasEverHadData) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to load schedule. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }



        // ---- FAB Speed Dial ----
        fabMain = view.findViewById(R.id.fabMain)
        fabCluster = view.findViewById(R.id.fabCluster)
        fabScrim = view.findViewById(R.id.fabScrim)
        fabCamera = view.findViewById(R.id.fabCamera)
        fabGallery = view.findViewById(R.id.fabGallery)
        fabManual = view.findViewById(R.id.fabManual)

        fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

        // Find the bottom nav that lives in MainFragment's layout
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Wait for both bottomNav and fabMain to be laid out, then compute real heights
        bottomNav?.doOnLayout {
            fabMain.doOnLayout {
                val bottomNavH   = bottomNav.height
                val mainFabH     = fabMain.height.takeIf { it > 0 } ?: dp(56)
                val extraCushion = dp(32)

                // 1) Keep main FAB above the bar
                fabMain.updateLayoutParams<FrameLayout.LayoutParams> {
                    // slight cushion so it never touches the bar
                    bottomMargin = bottomNavH + extraCushion
                    marginEnd = dp(16)
                }

                // 2) Push the mini‑FAB cluster high enough so the lowest item clears the bar
                //    We add the mainFab height because your lowest mini‑FAB aligns near the main FAB.
                val clusterClearance = bottomNavH + mainFabH + extraCushion
                fabCluster.setPadding(
                    fabCluster.paddingLeft,
                    fabCluster.paddingTop,
                    fabCluster.paddingRight,
                    clusterClearance
                )
            }
        }

        // Keep some extra space so the list bottom card isn't covered
        scheduleRecyclerView.setPadding(
            scheduleRecyclerView.paddingLeft,
            scheduleRecyclerView.paddingTop,
            scheduleRecyclerView.paddingRight,
            scheduleRecyclerView.paddingBottom + dp(96)
        )

        fun showSpeedDial() {
            fabScrim.visibility = View.VISIBLE
            fabCluster.visibility = View.VISIBLE
            fabCluster.alpha = 0f
            fabCluster.translationY = 24f
            fabCluster.animate().alpha(1f).translationY(0f).setDuration(150).start()
            fabMain.text = getString(R.string.close) // add "Close" in strings or keep "Add New Med"
        }

        fun hideSpeedDial() {
            fabCluster.animate().alpha(0f).translationY(24f).setDuration(120).withEndAction {
                fabCluster.visibility = View.GONE
            }.start()
            fabScrim.visibility = View.GONE
            fabMain.text = getString(R.string.add_new_med) // define in strings.xml -> "Add New Med"
        }

        fun toggleDial() {
            if (dialOpen) hideSpeedDial() else showSpeedDial()
            dialOpen = !dialOpen
        }

        fabMain.setOnClickListener { toggleDial() }
        fabScrim.setOnClickListener { if (dialOpen) toggleDial() }

        fabCamera.setOnClickListener {
            if (dialOpen) toggleDial()
            checkCameraPermissionAndOpenCamera()
        }
        fabGallery.setOnClickListener {
            if (dialOpen) toggleDial()
            pickImageLauncher.launch("image/*")
        }
        fabManual.setOnClickListener {
            if (dialOpen) toggleDial()
            findNavController().navigate(R.id.action_homeFragment_to_newMedManualFragment)
        }
    }

    // -------- Helpers --------

    private fun groupScheduleItems(scheduleList: List<ScheduleItem>): List<ScheduleListItem> {
        val grouped = mutableListOf<ScheduleListItem>()
        scheduleList
            .groupBy { it.scheduledTime }
            .toSortedMap()
            .forEach { (time, meds) ->
                grouped.add(ScheduleListItem.TimeHeader(time))
                grouped.addAll(meds.map { ScheduleListItem.MedicationEntry(it) })
            }
        return grouped
    }

    private fun createUri(): Uri {
        val imageFile = File(requireContext().filesDir, "camera_photo.jpg")
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireActivity().packageName}.fileprovider",
            imageFile
        )
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            val uri = createUri()
            imageUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            val uri = createUri()
            imageUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(requireContext(), "Please allow camera permission", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (shouldNavigateToImageDetails && pendingImageUri != null) {
            val bundle = Bundle().apply { putString("imageUri", pendingImageUri.toString()) }
            findNavController().navigate(R.id.action_homeFragment_to_imageDetailsFragment, bundle)
            shouldNavigateToImageDetails = false
            pendingImageUri = null
        }
    }
}
