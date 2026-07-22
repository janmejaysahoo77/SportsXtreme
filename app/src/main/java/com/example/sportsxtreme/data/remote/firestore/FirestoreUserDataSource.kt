package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.User

interface FirestoreUserDataSource {
    suspend fun createOrUpdateUser(profile: PendingUserProfile): Result<User>
}
