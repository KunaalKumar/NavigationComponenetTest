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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nav =  Navigation.getInstance(supportFragmentManager)

        nav.pushFragment(FirstFragment())
    }

//    private fun popFragment() {
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, backStack.pop())
//                .commit()
//    }
}