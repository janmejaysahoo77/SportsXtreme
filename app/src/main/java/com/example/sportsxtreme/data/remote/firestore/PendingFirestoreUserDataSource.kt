package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.model.UserAchievement
import com.example.sportsxtreme.domain.model.UserProfile
import com.example.sportsxtreme.domain.model.UserProfileSettings
import com.example.sportsxtreme.domain.model.UserProfileStats

class PendingFirestoreUserDataSource : FirestoreUserDataSource {
    override suspend fun createOrUpdateUser(profile: PendingUserProfile): Result<User> {
        return Result.success(profile.toUser())
    }

    override suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return Result.success(
            UserProfile(
                id = userId,
                name = "Siddheshwar Sahoo",
                email = "siddhesh900s7@gmail.com",
                phoneNumber = "+91 7428 5145 4312"
            )
        )
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<UserProfile> {
        return Result.success(profile)
    }

    override suspend fun getUserProfileStats(userId: String): Result<UserProfileStats> {
        return Result.success(UserProfileStats(userId = userId))
    }

    override suspend fun updateUserProfileStats(stats: UserProfileStats): Result<UserProfileStats> {
        return Result.success(stats)
    }

    override suspend fun getUserProfileSettings(userId: String): Result<UserProfileSettings> {
        return Result.success(UserProfileSettings(userId = userId))
    }

    override suspend fun updateUserProfileSettings(settings: UserProfileSettings): Result<UserProfileSettings> {
        return Result.success(settings)
    }

    override suspend fun getUserAchievements(userId: String): Result<List<UserAchievement>> {
        return Result.success(
            listOf(
                UserAchievement(
                    id = "season_form",
                    userId = userId,
                    title = "Season Form",
                    description = "Top performer streak is active.",
                    type = "form",
                    iconName = "xp"
                )
            )
        )
    }
}
