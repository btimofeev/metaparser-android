package org.emunix.metaparser.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.emunix.metaparser.Game
import org.emunix.metaparser.Metaparser
import org.emunix.metaparser.Paragraph
import org.emunix.metaparser.R
import org.emunix.metaparser.helper.StorageHelper
import org.emunix.metaparser.helper.TagParser
import org.emunix.metaparser.helper.ThemeHelper
import org.emunix.metaparser.helper.showToast
import java.io.File


class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val history = arrayListOf<Paragraph>()
    private val historyLiveData = MutableLiveData<ArrayList<Paragraph>>()
    private val showProgressState = MutableLiveData<Boolean>()

    private var game: Game = Game(getApplication())
    private var isInit = false

    fun init() = viewModelScope.launch {
        if (!isInit) {
            showProgressState.value = true
            StorageHelper(getApplication()).copyResources()
            showProgressState.value = false

            game.init()
            loadGame()

            isInit = true
        }
    }

    private fun showTextBlock(command: String, text: String) {
        val spanned = TagParser.parse(text)
        val paragraph = Paragraph(command, spanned)
        history.add(paragraph)
        historyLiveData.value = history
    }

    private fun loadGame() = viewModelScope.launch {
        val response = game.load()
        showTextBlock("", response)
    }

    fun saveState(name: String? = null) = viewModelScope.launch {
        if (name.isNullOrBlank())
            game.save()
        else
            game.save(name)
    }

    fun loadState(name: String) = viewModelScope.launch {
        history.clear()
        game.done()
        game.init()
        val response = game.load(name)
        showTextBlock("", response)
    }

    fun getSaveStates(): HashMap<Int, String?> = runBlocking {
        return@runBlocking StorageHelper(getApplication()).getSaveStateInfo()
    }

    fun restartGame() = viewModelScope.launch {
        val autosave = File(StorageHelper(getApplication()).getAppFilesDirectory(), "autosave")
        if (autosave.exists() && !FileUtils.deleteQuietly(autosave))
            getApplication<Metaparser>().showToast(getApplication<Metaparser>().getString(R.string.error_delete_autosave_failed))
        history.clear()
        game.done()
        game.init()
        loadGame()
    }

    fun sendTextToGame(s: String) = viewModelScope.launch {
        val text = s.replace("\"", "")
        val response = game.send(text)
        showTextBlock("> $text", response)

        if (game.isRestartFromGame())
            restartGame()
    }

    fun getHistory(): LiveData<ArrayList<Paragraph>> = historyLiveData

    fun getShowProgressState(): LiveData<Boolean> = showProgressState

    fun getAppTheme(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        return sharedPreferences.getString("app_theme", ThemeHelper.DEFAULT_MODE) ?: ThemeHelper.DEFAULT_MODE
    }

    fun setAppTheme(theme: String) {
        ThemeHelper.applyTheme(theme)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        sharedPreferences.edit().putString("app_theme", theme).apply()
    }

    override fun onCleared() {
        viewModelScope.launch {
            super.onCleared()
            game.save()
            game.done()
        }
    }
}