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