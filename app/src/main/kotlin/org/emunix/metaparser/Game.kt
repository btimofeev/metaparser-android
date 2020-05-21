package org.emunix.metaparser

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.emunix.metaparser.helper.StorageHelper
import timber.log.Timber
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
        val extRet = registerExtension()
        if (extRet != 0)
            throw MetaparserException("instead_extension() return code: $extRet")

        val dir = StorageHelper(context).getAppFilesDirectory().absolutePath
        val gameDir = "$dir/game"
        val initRet = insteadInit(dir, gameDir)
        if (initRet != 0)
            throw MetaparserException("instead_init() return code: $initRet")

        val loadRet = insteadLoad()
        if (loadRet != 0)
            throw MetaparserException("instead_load() return code: $loadRet")

        Timber.d("instead init successful")
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