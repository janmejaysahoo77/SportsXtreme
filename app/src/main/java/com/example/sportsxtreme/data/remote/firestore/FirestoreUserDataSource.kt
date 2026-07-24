package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.model.UserAchievement
import com.example.sportsxtreme.domain.model.UserProfile
import com.example.sportsxtreme.domain.model.UserProfileSettings
import com.example.sportsxtreme.domain.model.UserProfileStats

interface FirestoreUserDataSource {
    suspend fun createOrUpdateUser(profile: PendingUserProfile): Result<User>
    suspend fun getUserProfile(userId: String): Result<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile): Result<UserProfile>
    suspend fun getUserProfileStats(userId: String): Result<UserProfileStats>
    suspend fun updateUserProfileStats(stats: UserProfileStats): Result<UserProfileStats>
    suspend fun getUserProfileSettings(userId: String): Result<UserProfileSettings>
    suspend fun updateUserProfileSettings(settings: UserProfileSettings): Result<UserProfileSettings>
    suspend fun getUserAchievements(userId: String): Result<List<UserAchievement>>
}
