package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.User

class PendingFirestoreUserDataSource : FirestoreUserDataSource {
    override suspend fun createOrUpdateUser(profile: PendingUserProfile): Result<User> {
        return Result.success(profile.toUser())
    }
}
