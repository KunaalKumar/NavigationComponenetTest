package com.example.memoryleaktest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_first_child.*

class FirstChildFragment : Fragment(R.layout.fragment_first_child) {

    private val nav by lazy { Navigation.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener {
            nav.popFragment()
        }
    }
}