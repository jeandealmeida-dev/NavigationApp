package com.jeandealmeida_dev.billortest.di

import com.jeandealmeida_dev.billortest.chat.di.ChatModule
import com.jeandealmeida_dev.billortest.location.di.LocationModule
import com.jeandealmeida_dev.billortest.map.di.MapModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(includes = [ChatModule::class, LocationModule::class, MapModule::class])
@InstallIn(SingletonComponent::class)
object AppModule
