package org.emunix.metaparser

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.emunix.metaparser.helper.StorageHelper
import org.emunix.metaparser.helper.showToast
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

    suspend fun init() = withContext(Dispatchers.IO) {
        if (registerExtension() != 0)
            context.showToast("Can't register tiny extension")

        val dir = StorageHelper(context).getAppFilesDirectory().absolutePath
        val gameDir = "$dir/game"
        if (insteadInit(dir, gameDir) != 0) {
            context.showToast("Can not init game")
            return@withContext
        }

        if (insteadLoad() != 0) {
            context.showToast("Can not load game")
            return@withContext
        }
    }

    suspend fun send(text: String): String = withContext(Dispatchers.IO) {
        return@withContext command("@metaparser \"$text\"")
    }

    suspend fun save(): String = withContext(Dispatchers.IO) {
        return@withContext command("save ../autosave")
    }

    suspend fun save(name: String): String = withContext(Dispatchers.IO) {
        return@withContext command("save ../$name")
    }

    suspend fun load(): String = withContext(Dispatchers.IO) {
        val autosave = File(StorageHelper(context).getAppFilesDirectory(), "autosave")
        return@withContext if (autosave.exists()) {
            command("load ../autosave")
        } else {
            command("look")
        }
    }

    suspend fun load(name: String): String = withContext(Dispatchers.IO) {
        return@withContext command("load ../$name")
    }

    suspend fun done() = withContext(Dispatchers.IO) {
        insteadDone()
    }

    suspend fun isRestartFromGame(): Boolean = withContext(Dispatchers.IO) {
        return@withContext isRestart() != 0
    }
}