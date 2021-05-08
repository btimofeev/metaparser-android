package org.emunix.metaparser.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.emunix.metaparser.preferences.ApplicationPreferences
import org.emunix.metaparser.preferences.ApplicationPreferencesImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferenceModule {

    @Binds
    abstract fun bindApplicationPreferences(preferences: ApplicationPreferencesImpl): ApplicationPreferences
}