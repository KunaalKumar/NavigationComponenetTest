package com.example.memoryleaktest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {

    private lateinit var nav: Navigation
    private var endActivtiy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav =  Navigation.getInstance(supportFragmentManager, bottom_nav_bar)

        if(savedInstanceState == null) {
            // Load first/home fragment on start
            nav.loadTab(Navigation.TabIdentifiers.FIRST)
        } else {
            // Restore view state
            nav.showTab()
        }
    }

    override fun onPause() {
        super.onPause()
        if(!endActivtiy)
            nav.saveCurrentFragmentState()
    }

    override fun onDestroy() {
        super.onDestroy()
        nav.onFragmentManagerDestroy()
    }

    override fun onBackPressed() {
        if(!nav.popFragment()){
            endActivtiy = true
            finishAffinity()
        }
    }
}