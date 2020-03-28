package com.example.memoryleaktest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {

    private lateinit var nav: Navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav =  Navigation.getInstance(supportFragmentManager)

        nav.pushFragment(FirstFragment(), savedInstanceState == null)
    }

    override fun onPause() {
        super.onPause()
        nav.saveCurrentFragmentState()
    }

    override fun onDestroy() {
        super.onDestroy()
        nav.onFragmentManagerDestroy()
    }
}