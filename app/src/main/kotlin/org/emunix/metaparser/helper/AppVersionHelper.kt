package org.emunix.metaparser.helper

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import org.emunix.metaparser.preferences.ApplicationPreferences
import javax.inject.Inject

class AppVersionHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: ApplicationPreferences
) {

    fun isNewAppVersion(): Boolean = getVersionCode() != preferences.appVersion

    fun saveCurrentAppVersion() {
        preferences.appVersion = getVersionCode()
    }

    private fun getVersionCode(): Long {
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
}