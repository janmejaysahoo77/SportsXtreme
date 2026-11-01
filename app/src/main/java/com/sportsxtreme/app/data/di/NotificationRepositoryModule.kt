package com.sportsxtreme.app.data.di

import com.sportsxtreme.app.data.local.dao.NotificationDao
import com.sportsxtreme.app.data.repository.NotificationRepositoryImpl
import com.sportsxtreme.app.domain.repository.NotificationRepository
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
