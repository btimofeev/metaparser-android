package org.emunix.metaparser

import android.app.Application
import androidx.preference.PreferenceManager
import org.emunix.metaparser.helper.ThemeHelper


class Metaparser: Application() {

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePref = sharedPreferences.getString("app_theme", ThemeHelper.DEFAULT_MODE)
        ThemeHelper.applyTheme(themePref!!)
    }
}