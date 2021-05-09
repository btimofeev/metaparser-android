/*
 * Copyright (c) 2019, 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */


package org.emunix.metaparser.ui.game

import android.text.Spanned
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.emunix.metaparser.*
import org.emunix.metaparser.helper.*
import org.emunix.metaparser.interactor.engine.EngineException
import org.emunix.metaparser.interactor.engine.EngineInteractor
import org.emunix.metaparser.preferences.ApplicationPreferences
import org.emunix.metaparser.storage.Storage
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val game: EngineInteractor,
    private val tagParser: TagParser,
    private val storage: Storage,
    private val accessibilityHelper: AccessibilityHelper,
    private val themeHelper: ThemeHelper,
    private val preferences: ApplicationPreferences,
    private val resources: ResourceProvider
) : ViewModel() {

    private val history = arrayListOf<Paragraph>()
    private val historyLiveData = MutableLiveData<ArrayList<Paragraph>>()
    private val showProgressState = MutableLiveData<Boolean>()
    private val showCriticalError = MutableLiveData<String>()
    private val showSaveMenu = MutableLiveData<Boolean>()
    private val showLoadMenu = MutableLiveData<Boolean>()
    private val pinToolbar = MutableLiveData<Boolean>()

    private val _message = MutableLiveData<ConsumableEvent<String>>()
    val message: LiveData<ConsumableEvent<String>>
        get() = _message

    private var isInit = false

    fun init() = viewModelScope.launch {
        if (accessibilityHelper.isTouchExplorationEnabled())
            pinToolbar.value = true

        if (!isInit) {
            showProgressState.value = true
            try {
                storage.copyResourcesFromApk()
            } catch (e: IOException) {
                showCriticalError.value = "Copy error: ${e.localizedMessage}"
                return@launch
            } finally {
                showProgressState.value = false
            }

            try {
                game.init()
                loadGame()
                isInit = true
            } catch (e: EngineException) {
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
        } catch (e: EngineException) {
            showCriticalError.value = e.message
        }
    }

    fun saveState(name: String? = null) = viewModelScope.launch {
        try {
            if (name.isNullOrBlank())
                game.save()
            else {
                game.save(name)
                _message.value = ConsumableEvent(resources.getString(R.string.game_saved))
            }
        } catch (e: EngineException) {
            showCriticalError.value = e.message
        }
    }

    fun loadState(name: String) = viewModelScope.launch {
        history.clear()
        game.done()
        try {
            game.init()
            val response = game.load(name)
            _message.value = ConsumableEvent(resources.getString(R.string.game_loaded))
            showTextBlock("", response)
        } catch (e: EngineException) {
            showCriticalError.value = e.message
        }
    }

    fun getSaveStates(): Map<Int, String?> = runBlocking {
        return@runBlocking storage.getSaveStateInfo()
    }

    fun restartGame() = viewModelScope.launch {
        val autosave = File(storage.getAppFilesDirectory(), "autosave")
        if (autosave.exists() && !FileUtils.deleteQuietly(autosave))
            _message.value =
                ConsumableEvent(resources.getString(R.string.error_delete_autosave_failed))
        history.clear()
        historyLiveData.value = history
        game.done()
        try {
            game.init()
            loadGame()
        } catch (e: EngineException) {
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
        } catch (e: EngineException) {
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