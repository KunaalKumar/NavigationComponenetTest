package com.example.memoryleaktest

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

// Navigation component with fragments which can have only one instance
object Navigation {

    private var fragmentManager: FragmentManager? = null
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

    fun pushFragment(fragment: Fragment, saveState: Boolean = true) {
        // Nothing to do if current fragment is the same as to-be-replaced fragment
        if(backStack.isNotEmpty() && backStack.peek() == fragment::class)
            return

        if(saveState)
            saveCurrentFragmentState()

        // Restore state if previous fragment state exists
        if(fragmentStateMap.containsKey(fragment.javaClass.kotlin)) {
            fragment.setInitialSavedState(fragmentStateMap[fragment.javaClass.kotlin])
        }

        // If fragment was found in memory, remove it
        if(backStack.contains(fragment::class)) {
            backStack.remove(fragment::class)
        }

        backStack.push(fragment.javaClass.kotlin)

        fragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.commit()
    }

    // Returns true if handled, false otherwise
    fun popFragment() : Boolean {
        if(backStack.size > 1) {
            backStack.pop()
            val fragmentToShow = backStack.pop().java.newInstance()
            fragmentToShow.setInitialSavedState(fragmentStateMap[fragmentToShow.javaClass.kotlin])
            fragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, fragmentToShow)
                ?.commitNow()
            return true
        }
        return false
    }

    // Save current fragment state in fragmentStateMap
    fun saveCurrentFragmentState() {
        val currentFragment = fragmentManager?.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            fragmentStateMap[currentFragment.javaClass.kotlin] =
                fragmentManager?.saveFragmentInstanceState(currentFragment)
        }
    }

    // Stores state of current fragment and loses fragmentManager reference
    // To be called from MainActivity
    fun onFragmentManagerDestroy() {
        fragmentManager = null
    }
}