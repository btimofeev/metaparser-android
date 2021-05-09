/*
 * Copyright (c) 2019, 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */


package org.emunix.metaparser.interactor.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.emunix.metaparser.storage.Storage
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.jvm.Throws

class MetaparserInteractor @Inject constructor(
    val storage: Storage
) : EngineInteractor {

    companion object {
        init {
            System.loadLibrary("lua")
            System.loadLibrary("metaparser")
        }
    }

    private external fun registerExtension(): Int
    external fun insteadInit(directory: String, gameDir: String): Int
    external fun insteadErr(): String
    external fun insteadLoad(): Int
    external fun insteadCommand(cmd: String): String?
    external fun insteadDone()
    external fun isRestart(): Int
    external fun isSave(): Int
    external fun isLoad(): Int

    @Throws(EngineException::class)
    private fun command(text: String): String {
        val ret = insteadCommand(text)
        if (ret == null) {
            val err = insteadErr()
            Timber.e(err)
            throw EngineException("instead_cmd(): $err")
        }
        return ret
    }

    @Throws(EngineException::class)
    override suspend fun init() = withContext(Dispatchers.IO) {
        val extRet = registerExtension()
        if (extRet != 0)
            throw EngineException("instead_extension() return code: $extRet")

        val dir = storage.getAppFilesDirectory().absolutePath
        val gameDir = "$dir/game"
        val initRet = insteadInit(dir, gameDir)
        if (initRet != 0)
            throw EngineException("instead_init() return code: $initRet")

        if (insteadLoad() != 0) {
            val err = insteadErr()
            Timber.e(err)
            throw EngineException("instead_load(): $err")
        }

        Timber.d("instead init successful")
    }

    @Throws(EngineException::class)
    override suspend fun send(text: String): String = withContext(Dispatchers.IO) {
        return@withContext command("@metaparser \"$text\"")
    }

    @Throws(EngineException::class)
    override suspend fun save(): String = withContext(Dispatchers.IO) {
        return@withContext command("save ../autosave")
    }

    @Throws(EngineException::class)
    override suspend fun save(name: String): String = withContext(Dispatchers.IO) {
        return@withContext command("save ../$name")
    }

    @Throws(EngineException::class)
    override suspend fun load(): String = withContext(Dispatchers.IO) {
        val autosave = File(storage.getAppFilesDirectory(), "autosave")
        return@withContext if (autosave.exists()) {
            command("load ../autosave")
        } else {
            command("look")
        }
    }

    @Throws(EngineException::class)
    override suspend fun load(name: String): String = withContext(Dispatchers.IO) {
        return@withContext command("load ../$name")
    }

    override suspend fun done() = withContext(Dispatchers.IO) {
        insteadDone()
    }

    override suspend fun isRestartFromGame(): Boolean = withContext(Dispatchers.IO) {
        return@withContext isRestart() != 0
    }

    override suspend fun isSaveFromGame(): Boolean = withContext(Dispatchers.IO) {
        return@withContext isSave() != 0
    }

    override suspend fun isLoadFromGame(): Boolean = withContext(Dispatchers.IO) {
        return@withContext isLoad() != 0
    }
}