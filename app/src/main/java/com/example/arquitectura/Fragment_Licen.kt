package com.example.arquitectura

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.arquitectura.R

class LicenciasFragment : Fragment(R.layout.fragment_licen) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        return inflater.inflate(R.layout.fragment_licen, container, false)
    }
}
