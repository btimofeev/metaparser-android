package org.emunix.metaparser.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private val scope = CoroutineScope(Dispatchers.Main)
    private var game: Game = Game(getApplication())
    private var isInit = false

    fun init() = scope.launch {
        if (!isInit) {
            showProgressState.value = true
            StorageHelper(getApplication()).copyResources()
            showProgressState.value = false

            game.init()
            loadGame()

            isInit = true
        }
    }

    private fun loadGame() {
        val response = game.load()
        val spanned = TagParser.parse(response)
        val paragraph = Paragraph("", spanned)
        history.add(paragraph)
        historyLiveData.value = history
    }

    fun saveGame() {
        game.save()
    }

    fun restartGame() {
        val autosave = File(StorageHelper(getApplication()).getAppFilesDirectory(), "autosave")
        if (autosave.exists() && !FileUtils.deleteQuietly(autosave))
            getApplication<Metaparser>().showToast(getApplication<Metaparser>().getString(R.string.error_delete_autosave_failed))
        history.clear()
        game.done()
        game.init()
        loadGame()
    }

    fun sendTextToGame(s: String) {
        val text = s.replace("\"", "")
        val response = game.send(text)
        val spanned = TagParser.parse(response)
        val paragraph = Paragraph("> $text", spanned)
        history.add(paragraph)
        historyLiveData.value = history

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
        super.onCleared()
        game.save()
        game.done()
    }
}