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
import org.emunix.metaparser.storage.Storage
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val engine: EngineInteractor,
    private val tagParser: TagParser,
    private val storage: Storage,
    private val accessibilityHelper: AccessibilityHelper,
    private val resources: ResourceProvider
) : ViewModel() {

    private val historyBlocks = arrayListOf<Paragraph>()

    /** blocks of text representing user input and response from the engine **/
    private val _history = MutableLiveData<ArrayList<Paragraph>>()
    val history: LiveData<ArrayList<Paragraph>>
        get() = _history

    /** show message to user **/
    private val _message = MutableLiveData<ConsumableEvent<String>>()
    val message: LiveData<ConsumableEvent<String>>
        get() = _message

    /** show fatal error screen **/
    private val _fatalError = MutableLiveData<String>()
    val fatalError: LiveData<String>
        get() = _fatalError

    /** show/hide progress indicator **/
    private val _progress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean>
        get() = _progress

    /** show save menu event **/
    private val _showSaveMenu = MutableLiveData<ConsumableEvent<Unit>>()
    val showSaveMenu: LiveData<ConsumableEvent<Unit>>
        get() = _showSaveMenu

    /** show load menu event **/
    private val _showLoadMenu = MutableLiveData<ConsumableEvent<Unit>>()
    val showLoadMenu: LiveData<ConsumableEvent<Unit>>
        get() = _showLoadMenu

    /** pin/unpin toolbar **/
    private val _pinToolbar = MutableLiveData<Boolean>()
    val pinToolbar: LiveData<Boolean>
        get() = _pinToolbar

    /** prevents reinitialization when re-creating an activity **/
    private var isHasBeenInitialized = false

    fun init() = viewModelScope.launch {
        if (accessibilityHelper.isTouchExplorationEnabled())
            _pinToolbar.value = true

        if (!isHasBeenInitialized) {
            _progress.value = true
            try {
                storage.copyResourcesFromApk()
            } catch (e: IOException) {
                _fatalError.value = "Copy error: ${e.localizedMessage}"
                return@launch
            } finally {
                _progress.value = false
            }

            try {
                engine.init()
                startGame()
                isHasBeenInitialized = true
            } catch (e: EngineException) {
                _fatalError.value = e.message
            }
        }
    }

    private fun showTextBlock(command: String, text: String) = viewModelScope.launch {
        val spannedCommand: Spanned =
            accessibilityHelper.getSpannedCommand(command)
        val spannedText = tagParser.parse(text)
        val paragraph = Paragraph(spannedCommand, spannedText)
        historyBlocks.add(paragraph)
        _history.value = historyBlocks
    }

    private fun startGame() = viewModelScope.launch {
        try {
            val response = engine.startGame()
            showTextBlock("", response)
        } catch (e: EngineException) {
            _fatalError.value = e.message
        }
    }

    fun saveState(name: String? = null) = viewModelScope.launch {
        try {
            if (name.isNullOrBlank())
                engine.saveGame()
            else {
                engine.saveGame(name)
                _message.value = ConsumableEvent(resources.getString(R.string.game_saved))
            }
        } catch (e: EngineException) {
            _fatalError.value = e.message
        }
    }

    fun loadState(name: String) = viewModelScope.launch {
        historyBlocks.clear()
        engine.done()
        try {
            engine.init()
            val response = engine.loadGame(name)
            _message.value = ConsumableEvent(resources.getString(R.string.game_loaded))
            showTextBlock("", response)
        } catch (e: EngineException) {
            _fatalError.value = e.message
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
        historyBlocks.clear()
        _history.value = historyBlocks
        engine.done()
        try {
            engine.init()
            startGame()
        } catch (e: EngineException) {
            _fatalError.value = e.message
        }
    }

    fun sendTextToGame(s: String) = viewModelScope.launch {
        val text = s.replace("\"", "")

        try {
            val response = engine.processUserInput(text)
            showTextBlock("> $text", response)
            if (engine.isRestartFromGame()) {
                restartGame()
                return@launch
            }
            if (engine.isSaveFromGame()) {
                _showSaveMenu.value = ConsumableEvent(Unit)
                return@launch
            }
            if (engine.isLoadFromGame()) {
                _showLoadMenu.value = ConsumableEvent(Unit)
                return@launch
            }
        } catch (e: EngineException) {
            _fatalError.value = e.message
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            engine.saveGame()
            engine.done()
        }
    }
}