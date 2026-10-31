package com.example.sportsxtreme.data.di

import com.example.sportsxtreme.data.local.dao.NotificationDao
import com.example.sportsxtreme.data.repository.NotificationRepositoryImpl
import com.example.sportsxtreme.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationRepositoryModule {
    @Binds
    abstract fun bindNotificationRepository(
        repository: NotificationRepositoryImpl
    ): NotificationRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationEntryPoint {
    fun notificationDao(): NotificationDao
    fun notificationRepository(): NotificationRepository
}
