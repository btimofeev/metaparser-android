/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.metaparser.interactor.engine

interface EngineInteractor {

    /**
     * Initialize the engine. Must be called before working with the engine.
     *
     * @throws EngineException when initialization failed
     */
    suspend fun init()

    /**
     * Finish working with the engine.
     */
    suspend fun done()

    /**
     * Send user input to engine.
     *
     * @param text user entered text
     * @return the engine response
     * @throws EngineException when engine returns error
     */
    suspend fun send(text: String): String

    /**
     * Save game to autosave slot
     *
     * @return the engine response
     * @throws EngineException when engine returns error
     */
    suspend fun save(): String

    /**
     * Save the game to the slot with the specified name
     *
     * @param name save state name
     * @return the engine response
     * @throws EngineException when engine returns error
     */
    suspend fun save(name: String): String

    /**
     * Initial loading of the game.
     *
     * If there is an autosave, then it will be loaded,
     * otherwise the game will be started from the beginning.
     *
     * @return the engine response
     * @throws EngineException when engine returns error
     */
    suspend fun load(): String

    /**
     * Load game from save state
     *
     * @param name name of save state
     * @return the engine response
     * @throws EngineException when engine returns error
     */
    suspend fun load(name: String): String

    /**
     * Find out if the user entered 'restart' command
     *
     * @return true if user enter restart command
     */
    suspend fun isRestartFromGame(): Boolean

    /**
     * Find out if the user entered 'save' command
     *
     * @return true if user enter save command
     */
    suspend fun isSaveFromGame(): Boolean

    /**
     * Find out if the user entered 'load' command
     *
     * @return true if user enter load command
     */
    suspend fun isLoadFromGame(): Boolean
}