/*
 * Copyright (c) 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.metaparser.preferences

interface ApplicationPreferences {

    /**
     * Theme of the Application (light, dark or default)
     */
    var theme: String

    /**
     * Application version (to check if the app has been updated)
     */
    var appVersion: Long
}