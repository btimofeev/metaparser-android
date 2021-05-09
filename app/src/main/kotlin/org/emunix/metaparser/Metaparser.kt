/*
 * Copyright (c) 2019, 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.metaparser

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.emunix.metaparser.preferences.ApplicationPreferences
import org.emunix.metaparser.helper.ThemeHelper
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

@HiltAndroidApp
class Metaparser : Application() {

    @Inject lateinit var themeHelper: ThemeHelper
    @Inject lateinit var preferences: ApplicationPreferences

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        themeHelper.applyTheme(preferences.theme)
    }
}