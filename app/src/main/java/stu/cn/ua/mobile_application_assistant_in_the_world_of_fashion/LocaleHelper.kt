package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

/**
 * Helper that persists the chosen locale in SharedPreferences and applies it
 * to any Context (Activity or Application) on demand.
 */
object LocaleHelper {

    private const val PREFS_NAME = "fashion_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    /** Default language code used when nothing is saved yet. */
    const val LANG_UA = "uk"
    const val LANG_EN = "en"

    // ── Public API ───────────────────────────────────────────────────────────

    /** Wraps [base] context with the persisted locale (call from attachBaseContext). */
    fun wrap(base: Context): Context {
        val lang = getSavedLanguage(base)
        return applyLocale(base, lang)
    }

    /** Saves the chosen language code and returns a localised context. */
    fun setLocale(context: Context, langCode: String): Context {
        saveLanguage(context, langCode)
        return applyLocale(context, langCode)
    }

    /** Returns the currently persisted language code. */
    fun getSavedLanguage(context: Context): String {
        return prefs(context).getString(KEY_LANGUAGE, LANG_UA) ?: LANG_UA
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun applyLocale(context: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    private fun saveLanguage(context: Context, langCode: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
