package org.emunix.metaparser.helper

import android.content.Context
import android.os.Environment
import androidx.core.os.EnvironmentCompat
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class StorageHelper(val context: Context) {

    fun getAppFilesDirectory() : File {
        val storage : Array<File> = context.getExternalFilesDirs(null)
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
            val assets = assetManager.list(name)

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
}