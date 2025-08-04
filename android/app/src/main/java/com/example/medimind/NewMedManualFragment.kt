package com.example.medimind

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView


class NewMedManualFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_med_manual, container, false)
    }

    override fun onViewCreated(view:View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val backBtnFromManual = view.findViewById<Button>(R.id.btnBackFromManual)
        backBtnFromManual.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        val medicationName = view.findViewById<TextView>(R.id.medicationNameInputManual)
        val dosage = view.findViewById<TextView>(R.id.dosageInputManual)
        val frequency = view.findViewById<TextView>(R.id.frequencyInputManual)

        val saveBtnFromManual = view.findViewById<Button>(R.id.btnSaveFromManual)
        saveBtnFromManual.setOnClickListener{
            //toDo and save info to database

        }
    }
}