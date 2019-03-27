package org.emunix.metaparser

import android.content.Context
import org.emunix.metaparser.helper.showToast
import org.emunix.metaparser.helper.StorageHelper
import java.io.File

class Game(val context: Context) {

    companion object {
        init {
            System.loadLibrary("lua")
            System.loadLibrary("metaparser")
        }
    }

    external fun registerExtension(): Int
    external fun insteadInit(directory: String, gameDir: String): Int
    external fun insteadLoad(): Int
    external fun insteadCommand(cmd: String): String
    external fun insteadDone()
    external fun isRestart(): Int

    private fun command(text: String): String {
        return insteadCommand(text)
    }

    fun init() {
        if (registerExtension() != 0)
            context.showToast("Can't register tiny extension")

        val dir = StorageHelper(context).getAppFilesDirectory().absolutePath
        val gameDir = "$dir/game"
        if (insteadInit(dir, gameDir) != 0) {
            context.showToast("Can not init game")
            return
        }

        if (insteadLoad() != 0) {
            context.showToast("Can not load game")
            return
        }
    }

    fun send(text: String): String {
        return command("@metaparser \"$text\"")
    }

    fun save(): String {
        return command("save ../autosave")
    }

    fun load(): String {
        val autosave = File(StorageHelper(context).getAppFilesDirectory(), "autosave")
        return if (autosave.exists()) {
            command("load ../autosave")
        } else {
            command("look")
        }
    }

    fun done() {
        insteadDone()
    }

    fun isRestartFromGame(): Boolean {
        return isRestart() != 0
    }
}