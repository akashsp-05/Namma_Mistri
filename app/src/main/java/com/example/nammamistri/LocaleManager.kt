package com.example.nammamistri

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {
    const val ENGLISH = "en"
    const val KANNADA = "kn"

    private const val PREF_NAME = "nammamistri_preferences"
    private const val KEY_LANGUAGE = "selected_language"

    fun applySavedLocale(context: Context): Context {
        return updateResources(context, getSavedLanguage(context))
    }

    fun setLocale(context: Context, languageCode: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
    }

    fun getSavedLanguage(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, ENGLISH) ?: ENGLISH
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }
}
