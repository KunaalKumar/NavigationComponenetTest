package com.example.memoryleaktest

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

object Navigation {

    private lateinit var fragmentManager: FragmentManager
    private val backStack = Stack<KClass<Fragment>>()
    private val fragmentStateMap = HashMap<KClass<Fragment>, Fragment.SavedState?>()

    @Volatile
    private lateinit var instance: Navigation

    fun getInstance(fragmentManager: FragmentManager? = null): Navigation {
        if(fragmentManager != null) {
            this.fragmentManager = fragmentManager
            instance = this
        }
        return instance
    }

    fun pushFragment(fragment: Fragment) {
        //TODO: Check for if current fragment is the same as to-be-replaced fragment

        // Restore state if previous fragment state exists
        if(fragmentStateMap.containsKey(fragment.javaClass.kotlin)) {
            fragment.setInitialSavedState(fragmentStateMap[fragment.javaClass.kotlin])
        }

        backStack.push(fragment.javaClass.kotlin)
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Stores state of current fragment
    // To be called from MainActivtiy
    fun onFragmentManagerDestroy() {
        val currentFragment = fragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            fragmentStateMap[currentFragment.javaClass.kotlin] =
                fragmentManager.saveFragmentInstanceState(currentFragment)
        }
    }
}