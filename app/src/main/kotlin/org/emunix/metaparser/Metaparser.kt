package org.emunix.metaparser

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log


class Metaparser: Application() {

    override fun onCreate() {
        super.onCreate()
    }

    fun getVersionCode(context: Context): Long {
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

    fun getAppVersion(context: Context): String {
        var versionName = "N/A"
        try {
            val pinfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = pinfo.versionName
        } catch (e: Exception) {
            Log.e("Metaparser", "App version is not available")
        }

        return versionName
    }
}