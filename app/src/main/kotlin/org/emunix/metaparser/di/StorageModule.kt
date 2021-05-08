package org.emunix.metaparser.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.emunix.metaparser.storage.Storage
import org.emunix.metaparser.storage.StorageImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    abstract fun bindStorage(impl: StorageImpl): Storage
}