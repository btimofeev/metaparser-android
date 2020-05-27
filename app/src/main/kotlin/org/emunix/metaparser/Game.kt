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
    external fun insteadErr(): String
    external fun insteadLoad(): Int
    external fun insteadCommand(cmd: String): String?
    external fun insteadDone()
    external fun isRestart(): Int
    external fun isSave(): Int
    external fun isLoad(): Int

    private fun command(text: String): String {
        val ret = insteadCommand(text)
        if (ret == null) {
            val err = insteadErr()
            Timber.e(err)
            throw MetaparserException("instead_cmd(): $err")
        }
        return ret
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

        if (insteadLoad() != 0) {
            val err = insteadErr()
            Timber.e(err)
            throw MetaparserException("instead_load(): $err")
        }

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

    suspend fun isSaveFromGame(): Boolean = withContext(Dispatchers.IO) {
        return@withContext isSave() != 0
    }

    suspend fun isLoadFromGame(): Boolean = withContext(Dispatchers.IO) {
        return@withContext isLoad() != 0
    }
}