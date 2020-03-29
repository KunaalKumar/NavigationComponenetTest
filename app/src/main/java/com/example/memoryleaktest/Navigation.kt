package com.example.memoryleaktest

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

// Navigation component with fragments which can have only one instance
object Navigation {

    enum class TabIdentifiers {
        FIRST, SECOND
    }

    private var fragmentManager: FragmentManager? = null
    private val tabBackStack = HashMap<TabIdentifiers, Stack<KClass<Fragment>>>().apply {
        TabIdentifiers.values().forEach {
            this[it] = Stack<KClass<Fragment>>()
        }
    }
    private val tabStack = Stack<TabIdentifiers>()

    private var currentTab: TabIdentifiers = TabIdentifiers.FIRST

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


    /**
     * fragment: Fragment to put into container
     * saveState: Whether or not to save previous fragment state
     * tab: Tab to put fragment into
     */
    fun pushFragment(fragment: Fragment, saveState: Boolean = true, tab: TabIdentifiers) {

        Log.d("HOAL", "Putting ${fragment.javaClass.name} into ${currentTab.name}")

        // Check to a void making another instance of top fragment
        if(currentTab == tab) {
            val currentTabBackstack = tabBackStack[currentTab]!!
            // Nothing to do if current fragment is the same as to-be-replaced fragment
            if(currentTabBackstack.isNotEmpty() &&
                currentTabBackstack.peek()
                == fragment::class)
                return
        }

        // Manage tabStack
        // Add to tabStack
        if(tabStack.contains(tab))
            tabStack.remove(tab)
        tabStack.add(tab)

        currentTab = tab
        val backStack = tabBackStack[currentTab]!!

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
        val backStack = tabBackStack[currentTab]!!

        // 1) Check backStack
        //      If nothing to pop, step 2
        //      If something to pop, pop RETURN TRUE
        // 2) Check tabStack
        //      If nothing to pop, RETURN FALSE
        //      If something to pop, pop to current tab and show it

        if(backStack.size > 1) {
            backStack.pop()
            showCurrentTab()
            return true
        } else {
            // Clear out backstack if not empty
            if(backStack.isNotEmpty())
                backStack.pop()

            return if(tabStack.size > 1) {
                tabStack.pop()
                currentTab = tabStack.peek()
                showCurrentTab()
                true
            } else {
                false
            }
        }
    }

    // Show current tab and restore state its state
    private fun showCurrentTab() {
        val backStack = tabBackStack[currentTab]!!
        val fragmentToShow = backStack.pop().java.newInstance()
        fragmentToShow.setInitialSavedState(fragmentStateMap[fragmentToShow.javaClass.kotlin])
        fragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragmentToShow)
            ?.commitNow()
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