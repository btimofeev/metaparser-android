package org.emunix.metaparser.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ApplicationPreferencesImpl @Inject constructor(
    @ApplicationContext context: Context
) : ApplicationPreferences {

    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    override var theme: String
        get() = preferences.getString(PREF_APP_THEME, PREF_APP_THEME_VALUE) ?: PREF_APP_THEME_VALUE
        set(value) = preferences.edit {
            putString(PREF_APP_THEME, value)
        }

    override var appVersion: Long
        get() = preferences.getLong(PREF_APP_VERSION, -1)
        set(value) = preferences.edit {
            putLong(PREF_APP_VERSION, value)
        }

    companion object {

        /** the name of the application theme setting **/
        private const val PREF_APP_THEME = "app_theme"

        /**
         * default value of application theme setting
         * @see [org.emunix.metaparser.helper.ThemeHelper.DEFAULT_MODE]
         */
        private const val PREF_APP_THEME_VALUE = "default"

        /**
         * setting containing the version of the application
         * in which the game resources were last copied from APK to filesystem
         */
        private const val PREF_APP_VERSION = "app_version"
    }
}