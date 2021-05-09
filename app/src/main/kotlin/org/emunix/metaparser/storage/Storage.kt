/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.metaparser.storage

import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

interface Storage {

    /**
     * Returns the directory where the application stores resources.
     *
     * Contains directories: <b>game</b>, <b>stead</b>.
     * Contains files: <b>autosave</b>, <b>1..n.sav</b>.
     *
     * @return [File] object of directory
     */
    fun getAppFilesDirectory(): File

    /**
     * Copy resources (game and stead) from APK to filesystem.
     *
     * Really copied only if the application version code has increased or if we have a DEBUG build.
     * Files are copied in the IO thread.
     *
     * @throws [IOException] when IO error occurs
     */
    @Throws(IOException::class)
    suspend fun copyResourcesFromApk()

    /**
     * Returns information about the game save slots.
     *
     * @return [Map] whose key is the number of the save slot,
     * and the value is a string with the creation date of the save,
     * or null if nothing is saved to this slot.
     */
    suspend fun getSaveStateInfo(): Map<Int, String?>
}