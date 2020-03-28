package com.example.memoryleaktest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_second.*

class SecondFragment: Fragment() {

    private val nav: Navigation by lazy {
        Navigation.getInstance()
    }

    private var mCounter = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        counter.text = mCounter.toString()

        savedInstanceState?.let {
            mCounter = it.getInt("Counter")
            counter.text = mCounter.toString()
        }

        button.setOnClickListener {
            mCounter++
            counter.text = mCounter.toString()
        }

        view.setOnClickListener {
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("Counter", mCounter)
    }
}