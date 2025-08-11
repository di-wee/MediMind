package com.example.medimind

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.adapters.GroupedScheduleAdapter
import com.example.medimind.adapters.ScheduleListItem
import com.example.medimind.network.ApiClient
import com.example.medimind.network.ScheduleItem
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.view.Gravity
import com.google.android.material.appbar.MaterialToolbar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    // Empty state view
    private lateinit var emptyStateContainer: LinearLayout

    // Camera permission code and launcher
    private val CAMERA_PERMISSION_CODE = 1
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var imageUri: Uri? = null

    // Gallery launcher
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var pendingImageUri: Uri? = null
    private var shouldNavigateToImageDetails = false

    // Calendar formats and state (for the calendar strip + mid "Today, 11 Aug" label)
    private val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    private var selectedDateView: View? = null
    private var selectedCalendar: Calendar? = null

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
        // ===== Top App Bar (replace old greeting/logout UI) =====
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar?.navigationIcon = null // no back arrow

        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPref.getString("patientId", null)

        // Left-side greeting (toolbar title)
        if (patientId != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val profile = ApiClient.retrofitService.getPatient(patientId)
                    toolbar?.title = "Hello, ${profile.firstName ?: "User"}"
                } catch (e: Exception) {
                    toolbar?.title = "Hello, User"
                    Toast.makeText(requireContext(), "Failed to load profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            toolbar?.title = "Hello, User"
        }

        // Right-side date in toolbar action view (menu item)
        toolbar?.let {
            val item = it.menu.findItem(R.id.action_today)
            val tv = item?.actionView?.findViewById<TextView>(R.id.tvToday)
            val today = LocalDate.now()
            val fmt = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())
            tv?.text = today.format(fmt)
        }

        // ===== Rest of your existing UI =====

        // Add New Med sheet
        val addNewMedButton = view.findViewById<Button>(R.id.addNewMedButton)
        addNewMedButton?.setOnClickListener { showAddMedPopupMenu(it) }

        // Mid-page "Today, 11 Aug" label (keep or remove if redundant with toolbar date)
        val todayLabel = view.findViewById<TextView>(R.id.todayLabel)
        todayLabel?.text = "Today, ${fullDateFormat.format(Date())}"

        // Calendar strip
        val calendarStrip = view.findViewById<LinearLayout>(R.id.calendarStrip)
        calendarStrip?.let { populateCalendarStrip(it) }

        // Empty state + schedule list
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer)
        val scheduleRecyclerView = view.findViewById<RecyclerView>(R.id.scheduleRecyclerView)
        scheduleRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        if (patientId != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val rawSchedule = ApiClient.retrofitService.getDailySchedule(patientId)
                    val groupedList = groupScheduleItems(rawSchedule)

                    emptyStateContainer.visibility = if (rawSchedule.isEmpty()) View.VISIBLE else View.GONE
                    scheduleRecyclerView?.adapter = GroupedScheduleAdapter(groupedList)
                } catch (e: Exception) {
                    emptyStateContainer.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Failed to load schedule: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
            != PackageManager.PERMISSION_GRANTED) {
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
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val uri = createUri()
            imageUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(requireContext(), "Please allow camera permission", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateCalendarStrip(calendarLayout: LinearLayout) {
        val today = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }

        for (i in 0..6) {
            val dayCopy = calendar.clone() as Calendar

            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(16, 8, 16, 8)
            }

            val dayText = TextView(requireContext()).apply {
                text = dayFormat.format(dayCopy.time)
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                gravity = Gravity.CENTER
            }

            val dateText = TextView(requireContext()).apply {
                text = dateFormat.format(dayCopy.time)
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                gravity = Gravity.CENTER
            }

            container.addView(dayText)
            container.addView(dateText)

            container.setOnClickListener {
                (selectedDateView as? LinearLayout)?.let { prev ->
                    val prevDate = prev.getChildAt(1) as TextView
                    prevDate.background = null
                    prevDate.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                    val prevDay = prev.getChildAt(0) as TextView
                    prevDay.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }

                dateText.background = ContextCompat.getDrawable(requireContext(), R.drawable.circle_blue_bg)
                dateText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                dayText.setTextColor(Color.parseColor("#1E88E5"))

                selectedDateView = container
                selectedCalendar = dayCopy
            }

            if (isSameDay(dayCopy, today)) {
                container.post { container.performClick() }
            }

            calendarLayout.addView(container)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

    override fun onResume() {
        super.onResume()

        if (shouldNavigateToImageDetails && pendingImageUri != null) {
            val bundle = Bundle().apply {
                putString("imageUri", pendingImageUri.toString())
            }
            findNavController().navigate(R.id.action_homeFragment_to_imageDetailsFragment, bundle)

            shouldNavigateToImageDetails = false
            pendingImageUri = null
        }
    }

    private fun showAddMedPopupMenu(anchorView: View) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.add_med_popup)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<View>(R.id.cameraOption).setOnClickListener {
            dialog.dismiss(); checkCameraPermissionAndOpenCamera()
        }
        dialog.findViewById<View>(R.id.galleryOption).setOnClickListener {
            dialog.dismiss(); pickImageLauncher.launch("image/*")
        }
        dialog.findViewById<View>(R.id.manualOption).setOnClickListener {
            dialog.dismiss(); findNavController().navigate(R.id.action_homeFragment_to_newMedManualFragment)
        }
        dialog.findViewById<View>(R.id.cancelOption).setOnClickListener { dialog.dismiss() }

        dialog.show()

        val popupView = dialog.findViewById<View>(R.id.popupContainer)
        val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_anim)
        popupView?.startAnimation(slideUpAnimation)

        dialog.setOnDismissListener {
            val slideDownAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down_anim)
            popupView?.startAnimation(slideDownAnimation)
        }
    }
}
