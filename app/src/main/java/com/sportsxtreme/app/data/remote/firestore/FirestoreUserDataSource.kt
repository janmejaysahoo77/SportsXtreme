package com.sportsxtreme.app.data.remote.firestore

import com.sportsxtreme.app.domain.model.PendingUserProfile
import com.sportsxtreme.app.domain.model.User
import com.sportsxtreme.app.domain.model.UserAchievement
import com.sportsxtreme.app.domain.model.UserProfile
import com.sportsxtreme.app.domain.model.UserProfileSettings
import com.sportsxtreme.app.domain.model.UserProfileStats

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
