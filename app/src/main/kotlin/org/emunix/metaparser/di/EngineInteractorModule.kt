package org.emunix.metaparser.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.emunix.metaparser.interactor.engine.EngineInteractor
import org.emunix.metaparser.interactor.engine.MetaparserInteractor

@Module
@InstallIn(SingletonComponent::class)
abstract class EngineInteractorModule {

    @Binds
    abstract fun bindEngine(impl: MetaparserInteractor): EngineInteractor
}