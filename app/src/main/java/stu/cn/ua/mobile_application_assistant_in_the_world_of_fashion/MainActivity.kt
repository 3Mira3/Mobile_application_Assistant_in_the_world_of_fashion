package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Mapping: nav destination ID → tab container LinearLayout ID
    private val destinationToTab = mapOf(
        R.id.navigation_home      to R.id.tab_home,
        R.id.navigation_history   to R.id.tab_history,
        R.id.navigation_mixer     to R.id.tab_mixer,
        R.id.navigation_stylists  to R.id.tab_stylists,
        R.id.navigation_colors    to R.id.tab_colors,
        R.id.navigation_profile   to R.id.tab_profile
    )

    // All tab containers in order
    private val allTabIds = listOf(
        R.id.tab_home,
        R.id.tab_history,
        R.id.tab_mixer,
        R.id.tab_stylists,
        R.id.tab_colors,
        R.id.tab_profile
    )

    // Apply persisted locale BEFORE layout is inflated
    override fun attachBaseContext(newBase: Context) {
        // Also apply theme here if possible, but standard is setDefaultNightMode
        val prefs = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentTheme = prefs.getInt("app_theme", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(currentTheme)
        
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: android.content.res.Configuration?) {
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupTabClicks()

        // Keep tab indicator in sync when navigating programmatically
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val tabId = destinationToTab[destination.id]
            if (tabId != null) selectTab(tabId)
        }

        // Highlight Home on start
        selectTab(R.id.tab_home)
    }

    // ── Tab setup ────────────────────────────────────────────────────────────

    private fun setupTabClicks() {
        mapOf(
            R.id.tab_home      to R.id.navigation_home,
            R.id.tab_history   to R.id.navigation_history,
            R.id.tab_mixer     to R.id.navigation_mixer,
            R.id.tab_stylists  to R.id.navigation_stylists,
            R.id.tab_colors    to R.id.navigation_colors,
            R.id.tab_profile   to R.id.navigation_profile
        ).forEach { (tabId, destId) ->
            binding.root.findViewById<LinearLayout>(tabId).setOnClickListener {
                navigateTo(destId, tabId)
            }
        }
    }

    private fun navigateTo(destId: Int, tabId: Int) {
        if (navController.currentDestination?.id == destId) return

        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(R.id.navigation_home, inclusive = false, saveState = false)
            .build()

        try {
            navController.navigate(destId, null, navOptions)
            selectTab(tabId)
        } catch (e: Exception) {
            // destination not found — ignore
        }
    }

    // ── Tab visual state ─────────────────────────────────────────────────────

    private fun selectTab(selectedTabId: Int) {
        val primaryColor   = ContextCompat.getColor(this, R.color.primary)
        val secondaryColor = ContextCompat.getColor(this, R.color.text_secondary)

        allTabIds.forEach { tabId ->
            val tab      = binding.root.findViewById<LinearLayout>(tabId)
            val icon     = tab.getChildAt(0) as? ImageView
            val label    = tab.getChildAt(1) as? TextView
            val isActive = (tabId == selectedTabId)

            icon?.setColorFilter(if (isActive) primaryColor else secondaryColor)
            label?.setTextColor(if (isActive) primaryColor else secondaryColor)
        }
    }

    // ── Legacy ───────────────────────────────────────────────────────────────

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DETAIL && resultCode == RESULT_OK) {
            val result = data?.getStringExtra("result_key")
            Toast.makeText(this, "Result: $result", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_DETAIL = 1001
    }
}