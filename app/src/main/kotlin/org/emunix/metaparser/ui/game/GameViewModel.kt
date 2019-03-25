package org.emunix.metaparser.ui.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import org.emunix.metaparser.Game
import org.emunix.metaparser.Metaparser
import org.emunix.metaparser.helper.StorageHelper

private const val PREFS_FILENAME = "version_prefs"
private const val PREF_RESOURCES_LAST_UPDATE = "resources_last_update"

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val history = arrayListOf<String>()
    private val historyLiveData = MutableLiveData<ArrayList<String>>()
    private val showProgressState = MutableLiveData<Boolean>()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var game: Game = Game(getApplication())
    private var isInit = false

    fun init() = scope.launch {
        if (!isInit) {
            showProgressState.value = true
            copyResources()
            showProgressState.value = false

            game.init()

            val res = game.load()
            history.add(res)
            historyLiveData.value = history

            isInit = true
        }
    }

    fun sendTextToGame(text: String) {
        history.add("> $text")
        val response = game.send(text)
        history.add(response)
        historyLiveData.value = history
    }

    fun getHistory(): LiveData<ArrayList<String>> = historyLiveData

    fun getShowProgressState(): LiveData<Boolean> = showProgressState

    fun saveGame() {
        game.save()
    }

    override fun onCleared() {
        super.onCleared()
        game.save()
        game.done()
    }

    private suspend fun copyResources() {
        withContext(Dispatchers.IO) {
            val context = getApplication<Metaparser>()
            if (isNewAppVersion()) {
                StorageHelper(context).getSteadDirectory().deleteRecursively()
                StorageHelper(context).copyAsset("stead", StorageHelper(context).getAppFilesDirectory())

                StorageHelper(context).getGameDirectory().deleteRecursively()
                StorageHelper(context).copyAsset("game", StorageHelper(context).getAppFilesDirectory())

                saveCurrentAppVersion(Metaparser().getVersionCode(context))
            }
        }
    }

    private fun isNewAppVersion(): Boolean {
        val context = getApplication<Metaparser>()
        val prefs = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val lastUpdate = prefs.getLong(PREF_RESOURCES_LAST_UPDATE, -1)
        if (lastUpdate != Metaparser().getVersionCode(context)) {
            return true
        }
        return false
    }

    private fun saveCurrentAppVersion(value: Long) {
        val context = getApplication<Metaparser>()
        val prefs = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putLong(PREF_RESOURCES_LAST_UPDATE, value)
        editor.apply()
    }
}