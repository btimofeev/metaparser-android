package org.emunix.metaparser.helper

import android.content.Context
import android.os.Environment
import androidx.core.os.EnvironmentCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.emunix.metaparser.BuildConfig
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashMap

@Singleton
class StorageHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appVersionHelper: AppVersionHelper
) {

    fun getAppFilesDirectory(): File {
        val storage: Array<File> = context.getExternalFilesDirs(null)
        for (file in storage) {
            if (file != null) {
                val state = EnvironmentCompat.getStorageState(file)
                if (Environment.MEDIA_MOUNTED == state) {
                    return file
                }
            }
        }
        // if external not presented use internal memory // todo check this
        return getDataDirectory()
    }

    fun getDataDirectory(): File = context.filesDir

    fun getSteadDirectory(): File = File(getAppFilesDirectory(), "stead")

    fun getGameDirectory(): File = File(getAppFilesDirectory(), "game")

    fun copyAsset(name: String, toPath: File) {
        val assetManager = context.assets
        try {
            val assets = assetManager.list(name) ?: throw IOException("Assets not found")

            val dir = File(toPath, name)
            if (assets.isEmpty()) {
                FileUtils.copyInputStreamToFile(assetManager.open(name), dir)
            } else {
                FileUtils.forceMkdir(dir)
                for (i in 0 until assets.size) {
                    copyAsset(name + "/" + assets[i], toPath)
                }
            }
        } catch (e: IOException) {
            context.showToast("Copy error: ${e.localizedMessage}")
        }
    }

    suspend fun copyResources() {
        withContext(Dispatchers.IO) {
            if (appVersionHelper.isNewAppVersion() || BuildConfig.DEBUG) {
                getSteadDirectory().deleteRecursively()
                copyAsset("stead", getAppFilesDirectory())

                getGameDirectory().deleteRecursively()
                copyAsset("game", getAppFilesDirectory())

                copyAsset("metaparser.lua", getSteadDirectory())

                appVersionHelper.saveCurrentAppVersion()
            }
        }
    }

    suspend fun getSaveStateInfo(): HashMap<Int, String?> {
        val saves = HashMap<Int, String?>()
        withContext(Dispatchers.IO) {
            for (i in 1..3) {
                val file = File(getAppFilesDirectory(), "$i.sav")
                if (file.exists()) {
                    val timestamp = file.lastModified()
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
                    saves[i] = dateFormat.format(Date(timestamp))
                } else {
                    saves[i] = null
                }
            }
        }
        return saves
    }
}