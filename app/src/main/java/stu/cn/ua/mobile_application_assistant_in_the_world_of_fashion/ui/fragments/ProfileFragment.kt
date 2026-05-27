package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.LocaleHelper
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateLanguageLabel()
        updateThemeLabel()
        updateCityLabel()
        updateAccountLabel()
        setupClickListeners()
    }

    private fun updateThemeLabel() {
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val currentTheme = prefs.getInt("app_theme", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        binding.tvThemeCurrent.text = when (currentTheme) {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.theme_light)
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.theme_dark)
            else -> getString(R.string.theme_system)
        }
    }

    private fun updateAccountLabel() {
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isConnected = prefs.getBoolean("account_connected", false)
        if (isConnected) {
            val name = prefs.getString("account_name", "User")
            binding.tvAccountName.text = "${getString(R.string.profile_account_connected)} $name"
        } else {
            binding.tvAccountName.text = getString(R.string.profile_account_google)
        }
    }

    // ── Language label ───────────────────────────────────────────────────────

    private fun updateLanguageLabel() {
        val lang = LocaleHelper.getSavedLanguage(requireContext())
        binding.tvLanguageCurrent.text = if (lang == LocaleHelper.LANG_EN) "EN" else "UA"
    }

    // ── Click handlers ───────────────────────────────────────────────────────

    private fun setupClickListeners() {
        // Account button
        binding.btnAccount.setOnClickListener {
            connectGoogleAccount()
        }

        // Language picker
        binding.itemLanguage.setOnClickListener { showLanguageDialog() }

        // Theme picker
        binding.itemTheme.setOnClickListener { showThemeDialog() }

        // City picker
        binding.itemCity.setOnClickListener { showCityDialog() }

        // Rate app
        binding.itemRate.setOnClickListener { showRateDialog() }

        // Contact developer
        binding.itemContact.setOnClickListener { showContactDialog() }
    }

    private fun connectGoogleAccount() {
        // Automatic connection placeholder
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isConnected = prefs.getBoolean("account_connected", false)
        
        if (!isConnected) {
            prefs.edit().putBoolean("account_connected", true).apply()
            prefs.edit().putString("account_name", "Mira Romanenko").apply()
            updateAccountLabel()
            Toast.makeText(requireContext(), "Google Account Connected Automatically", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Account already connected", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
    private fun showLanguageDialog() {
        val currentLang = LocaleHelper.getSavedLanguage(requireContext())
        val options = arrayOf(
            getString(R.string.lang_ukrainian),
            getString(R.string.lang_english)
        )
        val currentIndex = if (currentLang == LocaleHelper.LANG_EN) 1 else 0

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.lang_dialog_title))
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                dialog.dismiss()
                val chosenLang = if (which == 1) LocaleHelper.LANG_EN else LocaleHelper.LANG_UA
                if (chosenLang == currentLang) return@setSingleChoiceItems
                applyLanguage(chosenLang)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun applyLanguage(langCode: String) {
        LocaleHelper.setLocale(requireContext(), langCode)
        val toastRes = if (langCode == LocaleHelper.LANG_EN) R.string.lang_toast_en else R.string.lang_toast_ua
        Toast.makeText(requireContext(), getString(toastRes), Toast.LENGTH_SHORT).show()
        requireActivity().recreate()
    }

    private fun showThemeDialog() {
        val options = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )
        
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val currentTheme = prefs.getInt("app_theme", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        val currentIndex = when (currentTheme) {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO -> 0
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.theme_dialog_title))
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                dialog.dismiss()
                val selectedTheme = when (which) {
                    0 -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                    1 -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                    else -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                prefs.edit().putInt("app_theme", selectedTheme).apply()
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(selectedTheme)
                updateThemeLabel()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showCityDialog() {
        val context = requireContext()
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val countryInput = android.widget.AutoCompleteTextView(context).apply {
            hint = getString(R.string.dialog_country_title)
            val adapter = android.widget.ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, resources.getStringArray(R.array.countries_array))
            setAdapter(adapter)
            threshold = 0 // Show all options
            setOnClickListener { showDropDown() }
        }

        val cityInput = android.widget.AutoCompleteTextView(context).apply {
            hint = getString(R.string.dialog_city_title)
            val adapter = android.widget.ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, resources.getStringArray(R.array.cities_array))
            setAdapter(adapter)
            threshold = 0 // Show all options
            setOnClickListener { showDropDown() }
        }

        layout.addView(countryInput)
        
        // Add spacer
        val spacer = android.view.View(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(1, 40) // 40px height spacer
        }
        layout.addView(spacer)
        
        layout.addView(cityInput)
        
        val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        countryInput.setText(prefs.getString("saved_country", ""))
        cityInput.setText(prefs.getString("saved_city", ""))

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.profile_city))
            .setView(layout)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val country = countryInput.text.toString()
                val city = cityInput.text.toString()
                prefs.edit().putString("saved_country", country).apply()
                prefs.edit().putString("saved_city", city).apply()
                updateCityLabel()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateCityLabel() {
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val city = prefs.getString("saved_city", "")
        val country = prefs.getString("saved_country", "")
        if (city?.isNotEmpty() == true) {
            binding.tvCityCurrent.text = city
        } else {
            binding.tvCityCurrent.text = ""
        }
    }

    private fun showRateDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_rate_title))
            .setMessage(getString(R.string.dialog_rate_message))
            .setPositiveButton(getString(R.string.dialog_rate_yes)) { _, _ ->
                val appPackageName = requireContext().packageName
                try {
                    startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$appPackageName")))
                } catch (e: android.content.ActivityNotFoundException) {
                    startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                }
            }
            .setNegativeButton(getString(R.string.dialog_rate_no), null)
            .show()
    }

    private fun showContactDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_contact_title))
            .setMessage(getString(R.string.dialog_contact_message))
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
