package org.emunix.metaparser.storage

import android.content.Context
import android.os.Environment
import androidx.core.os.EnvironmentCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.emunix.metaparser.BuildConfig
import org.emunix.metaparser.helper.AppVersionHelper
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashMap
import kotlin.jvm.Throws

@Singleton
class StorageImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appVersionHelper: AppVersionHelper
) : Storage {

    override fun getAppFilesDirectory(): File {
        val storage: Array<File> = context.getExternalFilesDirs(null)
        for (file in storage) {
            if (file != null) {
                val state = EnvironmentCompat.getStorageState(file)
                if (Environment.MEDIA_MOUNTED == state) {
                    return file
                }
            }
        }
        // if external not presented use internal memory
        return getDataDirectory()
    }

    @Throws(IOException::class)
    override suspend fun copyResourcesFromApk() {
        withContext(Dispatchers.IO) {
            if (appVersionHelper.isNewAppVersion() || BuildConfig.DEBUG) {
                getSteadDirectory().deleteRecursively()
                copyAsset(STEAD_DIR_NAME, getAppFilesDirectory())

                getGameDirectory().deleteRecursively()
                copyAsset(GAME_DIR_NAME, getAppFilesDirectory())

                copyAsset("metaparser.lua", getSteadDirectory())

                appVersionHelper.saveCurrentAppVersion()
            }
        }
    }

    override suspend fun getSaveStateInfo(): Map<Int, String?> {
        val saves = HashMap<Int, String?>()
        withContext(Dispatchers.IO) {
            for (i in 1..NUMBER_OF_SAVE_STATES) {
                val file = File(getAppFilesDirectory(), "$i.sav")
                if (file.exists()) {
                    val timestamp = file.lastModified()
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
                    saves[i] = dateFormat.format(Date(timestamp))
                } else {
                    saves[i] = null
                }
            }
        }
        return saves
    }

    private fun getDataDirectory(): File = context.filesDir

    private fun getSteadDirectory(): File = File(getAppFilesDirectory(), STEAD_DIR_NAME)

    private fun getGameDirectory(): File = File(getAppFilesDirectory(), GAME_DIR_NAME)

    @Throws(IOException::class)
    private fun copyAsset(name: String, toPath: File) {
        val assetManager = context.assets
        val assets = assetManager.list(name) ?: throw IOException("Assets not found")

        val dir = File(toPath, name)
        if (assets.isEmpty()) {
            FileUtils.copyInputStreamToFile(assetManager.open(name), dir)
        } else {
            FileUtils.forceMkdir(dir)
            for (element in assets) {
                copyAsset("$name/$element", toPath)
            }
        }
    }

    companion object {
        private const val NUMBER_OF_SAVE_STATES = 3
        private const val GAME_DIR_NAME = "game"
        private const val STEAD_DIR_NAME = "stead"
    }
}