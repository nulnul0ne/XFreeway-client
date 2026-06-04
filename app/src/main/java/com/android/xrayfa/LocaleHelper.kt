package com.android.xrayfa

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import com.android.xrayfa.common.repository.LanguageMode
import java.util.Locale

object LocaleHelper {
    private const val PREFS = "myfreeway_locale"
    private const val KEY_LANGUAGE_MODE = "language_mode"

    fun saveMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_LANGUAGE_MODE, mode)
            .apply()
    }

    fun getMode(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_LANGUAGE_MODE, LanguageMode.AUTO)
    }

    fun resolveLanguageTag(mode: Int): String {
        return when (mode) {
            LanguageMode.RU -> "ru"
            LanguageMode.EN -> "en"
            else -> {
                val systemLanguage = Resources.getSystem().configuration.locales[0].language
                if (systemLanguage.equals("ru", ignoreCase = true)) "ru" else "en"
            }
        }
    }

    fun wrap(base: Context): ContextWrapper {
        val locale = Locale.forLanguageTag(resolveLanguageTag(getMode(base)))
        Locale.setDefault(locale)

        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return ContextWrapper(base.createConfigurationContext(config))
    }
}
