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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav =  Navigation.getInstance(supportFragmentManager)

        nav.pushFragment(FirstFragment(), savedInstanceState == null, Navigation.TabIdentifiers.FIRST)

        bottom_nav_bar.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.first_item -> nav.pushFragment(FirstFragment(), tab = Navigation.TabIdentifiers.FIRST)
                R.id.second_item -> nav.pushFragment(SecondFragment(), tab = Navigation.TabIdentifiers.SECOND)
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    override fun onPause() {
        super.onPause()
        nav.saveCurrentFragmentState()
    }

    override fun onDestroy() {
        super.onDestroy()
        nav.onFragmentManagerDestroy()
    }

    override fun onBackPressed() {
        if(!nav.popFragment())
            finishAffinity()
    }
}