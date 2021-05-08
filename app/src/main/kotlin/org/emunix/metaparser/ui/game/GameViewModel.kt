package org.emunix.metaparser.ui.game

import android.app.Application
import android.text.Spanned
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.emunix.metaparser.*
import org.emunix.metaparser.helper.*
import org.emunix.metaparser.preferences.ApplicationPreferences
import java.io.File
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    val game: Game,
    val tagParser: TagParser,
    val storage: StorageHelper,
    val accessibilityHelper: AccessibilityHelper,
    val themeHelper: ThemeHelper,
    val preferences: ApplicationPreferences,
    application: Application
) : AndroidViewModel(application) {

    private val history = arrayListOf<Paragraph>()
    private val historyLiveData = MutableLiveData<ArrayList<Paragraph>>()
    private val showProgressState = MutableLiveData<Boolean>()
    private val showCriticalError = MutableLiveData<String>()
    private val showSaveMenu = MutableLiveData<Boolean>()
    private val showLoadMenu = MutableLiveData<Boolean>()
    private val pinToolbar = MutableLiveData<Boolean>()

    private var isInit = false

    fun init() = viewModelScope.launch {
        if (accessibilityHelper.isTouchExplorationEnabled())
            pinToolbar.value = true

        if (!isInit) {
            showProgressState.value = true
            storage.copyResources()
            showProgressState.value = false

            try {
                game.init()
                loadGame()
                isInit = true
            } catch (e: MetaparserException) {
                showCriticalError.value = e.message
            }
        }
    }

    private fun showTextBlock(command: String, text: String) = viewModelScope.launch {
        val spannedCommand: Spanned =
            accessibilityHelper.getSpannedCommand(command)
        val spannedText = tagParser.parse(text)
        val paragraph = Paragraph(spannedCommand, spannedText)
        history.add(paragraph)
        historyLiveData.value = history
    }

    private fun loadGame() = viewModelScope.launch {
        try {
            val response = game.load()
            showTextBlock("", response)
        } catch (e: MetaparserException) {
            showCriticalError.value = e.message
        }
    }

    fun saveState(name: String? = null) = viewModelScope.launch {
        try {
            if (name.isNullOrBlank())
                game.save()
            else {
                game.save(name)
                getApplication<Metaparser>().showToast(R.string.game_saved, Toast.LENGTH_SHORT)
            }
        } catch (e: MetaparserException) {
            showCriticalError.value = e.message
        }
    }

    fun loadState(name: String) = viewModelScope.launch {
        history.clear()
        game.done()
        try {
            game.init()
            val response = game.load(name)
            getApplication<Metaparser>().showToast(R.string.game_loaded, Toast.LENGTH_SHORT)
            showTextBlock("", response)
        } catch (e: MetaparserException) {
            showCriticalError.value = e.message
        }
    }

    fun getSaveStates(): HashMap<Int, String?> = runBlocking {
        return@runBlocking storage.getSaveStateInfo()
    }

    fun restartGame() = viewModelScope.launch {
        val autosave = File(storage.getAppFilesDirectory(), "autosave")
        if (autosave.exists() && !FileUtils.deleteQuietly(autosave))
            getApplication<Metaparser>().showToast(R.string.error_delete_autosave_failed)
        history.clear()
        historyLiveData.value = history
        game.done()
        try {
            game.init()
            loadGame()
        } catch (e: MetaparserException) {
            showCriticalError.value = e.message
        }
    }

    fun sendTextToGame(s: String) = viewModelScope.launch {
        val text = s.replace("\"", "")

        try {
            val response = game.send(text)
            showTextBlock("> $text", response)
            if (game.isRestartFromGame()) {
                restartGame()
                return@launch
            }
            if (game.isSaveFromGame()) {
                showSaveMenu.value = true
                showSaveMenu.value = false
                return@launch
            }
            if (game.isLoadFromGame()) {
                showLoadMenu.value = true
                showLoadMenu.value = false
                return@launch
            }
        } catch (e: MetaparserException) {
            showCriticalError.value = e.message
        }
    }

    fun getHistory(): LiveData<ArrayList<Paragraph>> = historyLiveData

    fun getShowProgressState(): LiveData<Boolean> = showProgressState

    fun getShowCriticalError(): LiveData<String> = showCriticalError

    fun getShowSaveMenu(): LiveData<Boolean> = showSaveMenu

    fun getShowLoadMenu(): LiveData<Boolean> = showLoadMenu

    fun getPinToolbar(): LiveData<Boolean> = pinToolbar

    fun getAppTheme(): String = preferences.theme

    fun setAppTheme(theme: String) {
        themeHelper.applyTheme(theme)
        preferences.theme = theme
    }

    override fun onCleared() {
        viewModelScope.launch {
            super.onCleared()
            game.save()
            game.done()
        }
    }
}