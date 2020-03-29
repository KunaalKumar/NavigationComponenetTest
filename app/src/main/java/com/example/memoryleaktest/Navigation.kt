package com.example.memoryleaktest

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

// Navigation component with fragments which can have only one instance
object Navigation {

    enum class TabIdentifiers {
        FIRST, SECOND
    }

    private var fragmentManager: FragmentManager? = null
    private var bottomNavBar: BottomNavigationView? = null

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

    fun getInstance(fragmentManager: FragmentManager? = null,
                    bottomNavigationView: BottomNavigationView? = null): Navigation {
        if(fragmentManager != null && bottomNavigationView != null) {
            this.fragmentManager = fragmentManager
            this.bottomNavBar = bottomNavigationView
            setupBottomNavBar()
            instance = this
        }
        return instance
    }


    private fun setupBottomNavBar() {
        bottomNavBar?.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.first_item -> loadTab(TabIdentifiers.FIRST)
                R.id.second_item -> loadTab(TabIdentifiers.SECOND)
                else -> return@setOnNavigationItemSelectedListener false
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    // Loads tab stack into current view, makes a new parent tab fragment if stack is null
    private fun loadTab(tabIdentifier: TabIdentifiers) {
        if(tabBackStack[tabIdentifier].isNullOrEmpty()) {
            when(tabIdentifier) {
                TabIdentifiers.FIRST -> pushFragment(FirstFragment(), tabIdentifier)
                TabIdentifiers.SECOND -> pushFragment(SecondFragment(), tabIdentifier)
            }
        } else {
            showTab(tabIdentifier)
        }
    }

    /**
     * fragment: Fragment to put into container
     * saveState: Whether or not to save previous fragment state
     * tab: Tab to put fragment into
     */
    fun pushFragment(fragment: Fragment, tab: TabIdentifiers, savePreviousFragmentState: Boolean = true) {

        Log.d("HOAL", "Putting ${fragment.javaClass.name} into ${currentTab.name}")

        // Check to a void making another instance of top fragment
        if(currentTab== tab) {
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

        // Save current fragment state
        if(savePreviousFragmentState)
            saveCurrentFragmentState()

        // Restore state if previous fragment state exists
        if(fragmentStateMap.containsKey(fragment.javaClass.kotlin)) {
            fragment.setInitialSavedState(fragmentStateMap[fragment.javaClass.kotlin])
        }

        // If fragment was found in backstack, remove it; Will be added to the top
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
            val fragmentRemoved = backStack.pop()
            fragmentStateMap.remove(fragmentRemoved)
            showTab(currentTab)
            return true
        } else {
            // Clear out backstack if not empty
            if(backStack.isNotEmpty()) {
                val fragmentRemoved = backStack.pop()
                fragmentStateMap.remove(fragmentRemoved)
            }

            return if(tabStack.size > 1) {
                tabStack.pop()
                currentTab = tabStack.peek()
                showTab(currentTab)
                true
            } else {
                // Clear out backstack if not empty
                if(backStack.isNotEmpty()) {
                    val fragmentRemoved = backStack.pop()
                    fragmentStateMap.remove(fragmentRemoved)
                }
                false
            }
        }
    }

    // Show current tab and restore state its state; if null, loads current tab
    fun showTab(tabIdentifier: TabIdentifiers? = null) {
        if(tabIdentifier != null)
            currentTab = tabIdentifier

        val backStack = tabBackStack[currentTab]!!
        val fragmentToShow = backStack.peek().java.newInstance()
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
        bottomNavBar = null
    }
}