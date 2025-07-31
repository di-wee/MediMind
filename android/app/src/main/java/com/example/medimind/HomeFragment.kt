package com.example.medimind

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.view.Gravity
import androidx.navigation.findNavController

class HomeFragment : Fragment() {

    // Buttons under add new Med FAB
    private lateinit var addMedButton: FloatingActionButton
    private lateinit var cameraButton: FloatingActionButton
    private lateinit var galleryButton: FloatingActionButton
    private lateinit var manualButton: FloatingActionButton
    private lateinit var cameraBtnText: TextView
    private lateinit var galleryBtnText: TextView
    private lateinit var manualBtnText: TextView
    // Camera image box
    private lateinit var cameraBox: ImageView

    // Animations for FAB
    private val rotateOpen by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_anim) }
    private val rotateClose by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_anim) }
    private val fromBottom by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom_anim) }
    private val toBottom by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom_anim) }
    private var clicked = false

    // Camera permission code and launcher
    private val CAMERA_PERMISSION_CODE = 1
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var imageUri: Uri? = null

    // Gallery launcher
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var pendingImageUri: Uri? = null
    private var shouldNavigateToImageDetails = false

    // Calendar formats and state
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
        // Top navbar greeting
        val greeting = view.findViewById<TextView>(R.id.topGreetingText)
        greeting.text = "Hello, Grandpa"

        // Logout action
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            val rootNavController = requireActivity().findNavController(R.id.nav_host_fragment)
            rootNavController.navigate(R.id.action_global_logout)
        }

        // Today label
        val todayLabel = view.findViewById<TextView>(R.id.todayLabel)
        todayLabel.text = "Today, ${fullDateFormat.format(Date())}"

        // Populate horizontal calendar
        val calendarStrip = view.findViewById<LinearLayout>(R.id.calendarStrip)
        populateCalendarStrip(calendarStrip)

        // Buttons
        addMedButton = view.findViewById(R.id.addMedButton)
        cameraButton = view.findViewById(R.id.cameraButton)
        galleryButton = view.findViewById(R.id.galleryButton)
        manualButton = view.findViewById(R.id.manualButton)
        cameraBtnText = view.findViewById(R.id.cameraBtnText)
        galleryBtnText = view.findViewById(R.id.galleryBtnText)
        manualBtnText = view.findViewById(R.id.manualBtnText)

        addMedButton.setOnClickListener { onAddMedButtonClicked() }

        cameraButton.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
            Toast.makeText(requireContext(), "Camera button clicked", Toast.LENGTH_SHORT).show()
        }

        galleryButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
            Toast.makeText(requireContext(), "Gallery button clicked", Toast.LENGTH_SHORT).show()
        }

        manualButton.setOnClickListener {
            // Navigate to manual medication entry fragment
            findNavController().navigate(R.id.action_homeFragment_to_newMedManualFragment)
        }

        // Camera image box
        cameraBox = view.findViewById(R.id.cameraBox)
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
                dayText.setTextColor(Color.parseColor("#1E88E5")) // blue text for selected day

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

    private fun onAddMedButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            cameraButton.visibility = View.VISIBLE
            galleryButton.visibility = View.VISIBLE
            manualButton.visibility = View.VISIBLE
            cameraBtnText.visibility = View.VISIBLE
            galleryBtnText.visibility = View.VISIBLE
            manualBtnText.visibility = View.VISIBLE
        } else {
            cameraButton.visibility = View.INVISIBLE
            galleryButton.visibility = View.INVISIBLE
            manualButton.visibility = View.INVISIBLE
            cameraBtnText.visibility = View.INVISIBLE
            galleryBtnText.visibility = View.INVISIBLE
            manualBtnText.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            cameraButton.startAnimation(fromBottom)
            galleryButton.startAnimation(fromBottom)
            manualButton.startAnimation(fromBottom)
            galleryBtnText.startAnimation(fromBottom)
            cameraBtnText.startAnimation(fromBottom)
            manualBtnText.startAnimation(fromBottom)
            addMedButton.startAnimation(rotateOpen)
        } else {
            cameraButton.startAnimation(toBottom)
            galleryButton.startAnimation(toBottom)
            manualButton.startAnimation(toBottom)
            cameraBtnText.startAnimation(toBottom)
            galleryBtnText.startAnimation(toBottom)
            manualBtnText.startAnimation(toBottom)
            addMedButton.startAnimation(rotateClose)
        }
    }

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
}
