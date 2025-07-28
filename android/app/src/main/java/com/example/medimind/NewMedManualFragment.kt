package com.example.medimind

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


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

        val saveBtnFromManual = view.findViewById<Button>(R.id.btnSaveFromManual)
        saveBtnFromManual.setOnClickListener{
            //toDo and save info to database
        }
    }



}