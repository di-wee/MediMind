package com.example.medimind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    private var selectedDateView: View? = null
    private var selectedCalendar: Calendar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Top navbar greeting
        val greeting = view.findViewById<TextView>(R.id.topGreetingText)
        greeting.text = "Hello, Grandpa"

        // Logout action
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
        }

        // Today label
        val todayLabel = view.findViewById<TextView>(R.id.todayLabel)
        todayLabel.text = "Today, ${fullDateFormat.format(Date())}"

        // Populate horizontal calendar
        val calendarStrip = view.findViewById<LinearLayout>(R.id.calendarStrip)
        populateCalendarStrip(calendarStrip)

        // CTA Button
        val addMedButton = view.findViewById<Button>(R.id.addMedButton)
        addMedButton.setOnClickListener {
            Toast.makeText(context, "Add a med clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateCalendarStrip(calendarLayout: LinearLayout) {
        val today = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }

        for (i in 0..6) {
            val dayCopy = calendar.clone() as Calendar

            val dayView = TextView(requireContext()).apply {
                val day = dayFormat.format(dayCopy.time)
                val date = dateFormat.format(dayCopy.time)
                text = "$day\n$date"
                setPadding(24, 8, 24, 8)
                textAlignment = View.TEXT_ALIGNMENT_CENTER

                setOnClickListener {
                    selectedDateView?.setBackgroundColor(0x00000000)
                    (selectedDateView as? TextView)?.setTextColor(
                        ContextCompat.getColor(requireContext(), android.R.color.black)
                    )

                    setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light))
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

                    selectedDateView = this
                    selectedCalendar = dayCopy

                    Toast.makeText(
                        context,
                        "Selected: ${SimpleDateFormat("EEE, dd MMM").format(dayCopy.time)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                if (isSameDay(dayCopy, today)) {
                    post { performClick() }
                } else {
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }
            }

            calendarLayout.addView(dayView)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
