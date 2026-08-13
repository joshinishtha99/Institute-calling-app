package com.institute.calling.di

import com.institute.calling.data.repository.NetworkCallingRepository
import com.institute.calling.domain.repository.CallingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the repository interface to the network-backed implementation.
 * (InMemoryCallingRepository is kept in the codebase as an offline/testing fallback.)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindCallingRepository(
        impl: NetworkCallingRepository,
    ): CallingRepository
}
