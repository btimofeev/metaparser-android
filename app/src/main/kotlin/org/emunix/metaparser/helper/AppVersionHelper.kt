package org.emunix.metaparser.helper

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppVersionHelper @Inject constructor(@ApplicationContext val context: Context) {

    fun getVersionCode(): Long {
        var version: Long = 0
        try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            version = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                info.versionCode.toLong()
            } else {
                info.longVersionCode
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return version
    }

    fun isNewAppVersion(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val lastUpdate = prefs.getLong(PREF_RESOURCES_LAST_UPDATE, -1)
        if (lastUpdate != getVersionCode()) {
            return true
        }
        return false
    }

    fun saveCurrentAppVersion(value: Long) {
        val prefs = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putLong(PREF_RESOURCES_LAST_UPDATE, value)
        editor.apply()
    }

    companion object {
        private const val PREFS_FILENAME = "version_prefs"
        private const val PREF_RESOURCES_LAST_UPDATE = "resources_last_update"
    }
}