package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.model.UserAchievement
import com.example.sportsxtreme.domain.model.UserProfile
import com.example.sportsxtreme.domain.model.UserProfileSettings
import com.example.sportsxtreme.domain.model.UserProfileStats
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebaseFirestoreUserDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FirestoreUserDataSource {
    override suspend fun createOrUpdateUser(profile: PendingUserProfile): Result<User> {
        return suspendCancellableCoroutine { continuation ->
            val document = firestore.collection(USERS_COLLECTION).document(profile.id)
            val payload = mutableMapOf<String, Any?>(
                "id" to profile.id,
                "email" to profile.email,
                "authProvider" to profile.authProvider.name,
                "isEmailVerified" to profile.isEmailVerified,
                "isPhoneVerified" to profile.isPhoneVerified,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (profile.name.isNotBlank()) {
                payload["name"] = profile.name
            }
            if (profile.phoneNumber.isNotBlank()) {
                payload["phoneNumber"] = profile.phoneNumber
            }
            profile.profilePhotoUrl?.let { payload["profilePhotoUrl"] = it }

            document
                .set(payload, SetOptions.merge())
                .addOnSuccessListener {
                    seedProfileSubcollections(profile.id)
                        .addOnSuccessListener {
                            continuation.resume(Result.success(profile.toUser()))
                        }
                        .addOnFailureListener { exception ->
                            continuation.resume(Result.failure(exception))
                        }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    override suspend fun getUserProfileStats(userId: String): Result<UserProfileStats> {
        return suspendCancellableCoroutine { continuation ->
            userDocument(userId)
                .collection(STATS_COLLECTION)
                .document(SUMMARY_DOCUMENT)
                .get()
                .addOnSuccessListener { snapshot ->
                    continuation.resume(Result.success(snapshot.toUserProfileStats(userId)))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    override suspend fun updateUserProfileStats(stats: UserProfileStats): Result<UserProfileStats> {
        return suspendCancellableCoroutine { continuation ->
            userDocument(stats.userId)
                .collection(STATS_COLLECTION)
                .document(SUMMARY_DOCUMENT)
                .set(stats.toFirestorePayload() + mapOf("updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
                .addOnSuccessListener {
                    continuation.resume(Result.success(stats))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    override suspend fun getUserProfileSettings(userId: String): Result<UserProfileSettings> {
        return suspendCancellableCoroutine { continuation ->
            userDocument(userId)
                .collection(SETTINGS_COLLECTION)
                .document(PREFERENCES_DOCUMENT)
                .get()
                .addOnSuccessListener { snapshot ->
                    continuation.resume(Result.success(snapshot.toUserProfileSettings(userId)))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    override suspend fun updateUserProfileSettings(settings: UserProfileSettings): Result<UserProfileSettings> {
        return suspendCancellableCoroutine { continuation ->
            userDocument(settings.userId)
                .collection(SETTINGS_COLLECTION)
                .document(PREFERENCES_DOCUMENT)
                .set(settings.toFirestorePayload() + mapOf("updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
                .addOnSuccessListener {
                    continuation.resume(Result.success(settings))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    override suspend fun getUserAchievements(userId: String): Result<List<UserAchievement>> {
        return suspendCancellableCoroutine { continuation ->
            userDocument(userId)
                .collection(ACHIEVEMENTS_COLLECTION)
                .get()
                .addOnSuccessListener { snapshot ->
                    continuation.resume(Result.success(snapshot.documents.map { it.toUserAchievement(userId) }))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    private fun seedProfileSubcollections(userId: String): com.google.android.gms.tasks.Task<Void> {
        val batch = firestore.batch()
        val stats = UserProfileStats(userId = userId)
        val settings = UserProfileSettings(userId = userId)
        val starterAchievement = UserAchievement(
            id = "season_form",
            userId = userId,
            title = "Welcome",
            description = "SportsXtreme profile created.",
            type = "form",
            iconName = "xp"
        )

        batch.set(
            userDocument(userId).collection(STATS_COLLECTION).document(SUMMARY_DOCUMENT),
            stats.toFirestorePayload() + mapOf("updatedAt" to FieldValue.serverTimestamp()),
            SetOptions.merge()
        )
        batch.set(
            userDocument(userId).collection(SETTINGS_COLLECTION).document(PREFERENCES_DOCUMENT),
            settings.toFirestorePayload() + mapOf("updatedAt" to FieldValue.serverTimestamp()),
            SetOptions.merge()
        )
        batch.set(
            userDocument(userId).collection(ACHIEVEMENTS_COLLECTION).document(starterAchievement.id),
            starterAchievement.toFirestorePayload() + mapOf("createdAt" to FieldValue.serverTimestamp()),
            SetOptions.merge()
        )
        return batch.commit()
    }

    private fun userDocument(userId: String) = firestore.collection(USERS_COLLECTION).document(userId)

    override suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        continuation.resume(Result.success(snapshot.toUserProfile(userId)))
                    } else {
                        continuation.resume(Result.failure(NoSuchElementException("User profile not found")))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<UserProfile> {
        return suspendCancellableCoroutine { continuation ->
            val payload = profile.toFirestorePayload() + mapOf(
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection(USERS_COLLECTION)
                .document(profile.id)
                .set(payload, SetOptions.merge())
                .addOnSuccessListener {
                    continuation.resume(Result.success(profile))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    private fun UserProfile.toFirestorePayload(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "profilePhotoUrl" to profilePhotoUrl,
            "location" to location,
            "joinedLabel" to joinedLabel,
            "gender" to gender,
            "role" to role,
            "battingStyle" to battingStyle,
            "bowlingStyle" to bowlingStyle,
            "followers" to followers,
            "following" to following,
            "matchesPlayed" to matchesPlayed,
            "wins" to wins,
            "bestScore" to bestScore,
            "trophies" to trophies,
            "hostedTournaments" to hostedTournaments,
            "hostedSeries" to hostedSeries,
            "hostedMatches" to hostedMatches,
            "topPerformerStreak" to topPerformerStreak,
            "isPro" to isPro,
            "authProvider" to authProvider.name,
            "isEmailVerified" to isEmailVerified,
            "isPhoneVerified" to isPhoneVerified
        )
    }

    private fun DocumentSnapshot.toUserProfile(fallbackId: String): UserProfile {
        return UserProfile(
            id = getString("id").orEmpty().ifBlank { fallbackId },
            name = getString("name").orEmpty(),
            email = getString("email").orEmpty(),
            phoneNumber = getString("phoneNumber").orEmpty(),
            profilePhotoUrl = getString("profilePhotoUrl"),
            location = getString("location").orEmpty(),
            joinedLabel = getString("joinedLabel").orEmpty(),
            gender = getString("gender").orEmpty(),
            role = getString("role").orEmpty(),
            battingStyle = getString("battingStyle").orEmpty(),
            bowlingStyle = getString("bowlingStyle").orEmpty(),
            followers = getLong("followers")?.toInt() ?: 0,
            following = getLong("following")?.toInt() ?: 0,
            matchesPlayed = getLong("matchesPlayed")?.toInt() ?: 0,
            wins = getLong("wins")?.toInt() ?: 0,
            bestScore = getString("bestScore").orEmpty(),
            trophies = getLong("trophies")?.toInt() ?: 0,
            hostedTournaments = getLong("hostedTournaments")?.toInt() ?: 0,
            hostedSeries = getLong("hostedSeries")?.toInt() ?: 0,
            hostedMatches = getLong("hostedMatches")?.toInt() ?: 0,
            topPerformerStreak = getBoolean("topPerformerStreak") ?: false,
            isPro = getBoolean("isPro") ?: false,
            authProvider = runCatching {
                com.example.sportsxtreme.domain.model.AuthProvider.valueOf(
                    getString("authProvider").orEmpty()
                )
            }.getOrDefault(com.example.sportsxtreme.domain.model.AuthProvider.EMAIL_PASSWORD),
            isEmailVerified = getBoolean("isEmailVerified") ?: false,
            isPhoneVerified = getBoolean("isPhoneVerified") ?: false
        )
    }

    private fun UserProfileStats.toFirestorePayload(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "matchesPlayed" to matchesPlayed,
            "wins" to wins,
            "losses" to losses,
            "runs" to runs,
            "wickets" to wickets,
            "bestScore" to bestScore,
            "trophies" to trophies,
            "topPerformerStreak" to topPerformerStreak,
            "battingAverage" to battingAverage,
            "bowlingAverage" to bowlingAverage,
            "strikeRate" to strikeRate,
            "mvpCount" to mvpCount
        )
    }

    private fun DocumentSnapshot.toUserProfileStats(userId: String): UserProfileStats {
        return UserProfileStats(
            userId = getString("userId").orEmpty().ifBlank { userId },
            matchesPlayed = getLong("matchesPlayed")?.toInt() ?: 0,
            wins = getLong("wins")?.toInt() ?: 0,
            losses = getLong("losses")?.toInt() ?: 0,
            runs = getLong("runs")?.toInt() ?: 0,
            wickets = getLong("wickets")?.toInt() ?: 0,
            bestScore = getString("bestScore").orEmpty(),
            trophies = getLong("trophies")?.toInt() ?: 0,
            topPerformerStreak = getBoolean("topPerformerStreak") ?: false,
            battingAverage = getDouble("battingAverage") ?: 0.0,
            bowlingAverage = getDouble("bowlingAverage") ?: 0.0,
            strikeRate = getDouble("strikeRate") ?: 0.0,
            mvpCount = getLong("mvpCount")?.toInt() ?: 0
        )
    }

    private fun UserProfileSettings.toFirestorePayload(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "language" to language,
            "theme" to theme,
            "notificationsEnabled" to notificationsEnabled,
            "profileVisibility" to profileVisibility
        )
    }

    private fun DocumentSnapshot.toUserProfileSettings(userId: String): UserProfileSettings {
        return UserProfileSettings(
            userId = getString("userId").orEmpty().ifBlank { userId },
            language = getString("language").orEmpty().ifBlank { "English (UK)" },
            theme = getString("theme").orEmpty().ifBlank { "system" },
            notificationsEnabled = getBoolean("notificationsEnabled") ?: true,
            profileVisibility = getString("profileVisibility").orEmpty().ifBlank { "public" }
        )
    }

    private fun UserAchievement.toFirestorePayload(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "title" to title,
            "description" to description,
            "type" to type,
            "iconName" to iconName,
            "earnedAtMillis" to earnedAtMillis,
            "matchId" to matchId,
            "tournamentId" to tournamentId
        )
    }

    private fun DocumentSnapshot.toUserAchievement(userId: String): UserAchievement {
        return UserAchievement(
            id = getString("id").orEmpty().ifBlank { id },
            userId = getString("userId").orEmpty().ifBlank { userId },
            title = getString("title").orEmpty(),
            description = getString("description").orEmpty(),
            type = getString("type").orEmpty(),
            iconName = getString("iconName").orEmpty(),
            earnedAtMillis = getLong("earnedAtMillis") ?: 0L,
            matchId = getString("matchId"),
            tournamentId = getString("tournamentId")
        )
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val STATS_COLLECTION = "stats"
        const val SUMMARY_DOCUMENT = "summary"
        const val SETTINGS_COLLECTION = "settings"
        const val PREFERENCES_DOCUMENT = "preferences"
        const val ACHIEVEMENTS_COLLECTION = "achievements"
    }
}
