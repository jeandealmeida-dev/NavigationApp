package com.jeandealmeida_dev.billortest.location.di

import android.content.Context
import com.jeandealmeida_dev.billortest.location.ui.handler.LocationHandler
import com.jeandealmeida_dev.billortest.location.data.repository.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideLocationRepository(): LocationRepository {
        return LocationRepository()
    }


    @Provides
    @Singleton
    fun provideLocationHandler(
        @ApplicationContext context: Context,
        locationRepository: LocationRepository
    ): LocationHandler {
        return LocationHandler(
            context = context,
            locationRepository = locationRepository
        )
    }
}
