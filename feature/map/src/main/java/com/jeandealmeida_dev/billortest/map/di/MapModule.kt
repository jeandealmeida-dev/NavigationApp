package com.jeandealmeida_dev.billortest.map.di

import android.content.Context
import com.jeandealmeida_dev.billortest.location.data.repository.LocationRepository
import com.jeandealmeida_dev.billortest.map.ui.navigation.NavigationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapModule {

    @Provides
    @Singleton
    fun provideNavigationManager(
        @ApplicationContext context: Context,
        locationRepository: LocationRepository
    ): NavigationManager {
        return NavigationManager(
            context,
            locationRepository
        )
    }
}