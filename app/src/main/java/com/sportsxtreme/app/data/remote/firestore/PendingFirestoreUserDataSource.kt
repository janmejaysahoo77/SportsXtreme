package com.sportsxtreme.app.data.remote.firestore

import com.sportsxtreme.app.domain.model.PendingUserProfile
import com.sportsxtreme.app.domain.model.User
import com.sportsxtreme.app.domain.model.UserAchievement
import com.sportsxtreme.app.domain.model.UserProfile
import com.sportsxtreme.app.domain.model.UserProfileSettings
import com.sportsxtreme.app.domain.model.UserProfileStats

class PendingFirestoreUserDataSource : FirestoreUserDataSource {
    override suspend fun createOrUpdateUser(profile: PendingUserProfile): Result<User> {
        return Result.success(profile.toUser())
    }

    override suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return Result.success(
            UserProfile(
                id = userId,
                name = "",
                email = "",
                phoneNumber = ""
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
