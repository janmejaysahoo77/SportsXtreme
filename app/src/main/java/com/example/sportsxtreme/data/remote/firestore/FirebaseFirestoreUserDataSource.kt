package com.example.sportsxtreme.data.remote.firestore

import android.net.Uri
import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.model.UserAchievement
import com.example.sportsxtreme.domain.model.UserProfile
import com.example.sportsxtreme.domain.model.UserProfileSettings
import com.example.sportsxtreme.domain.model.UserProfileStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebaseFirestoreUserDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : FirestoreUserDataSource {
    override suspend fun createOrUpdateUser(profile: PendingUserProfile): Result<User> {
        return suspendCancellableCoroutine { continuation ->
            val document = firestore.collection(USERS_COLLECTION).document(profile.id)
            val publicPayload = mutableMapOf<String, Any?>(
                "id" to profile.id,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (profile.name.isNotBlank()) {
                publicPayload["name"] = profile.name
            }

            document.get()
                .addOnSuccessListener { existingProfile ->
                    if (!existingProfile.exists()) {
                        publicPayload["createdAt"] = FieldValue.serverTimestamp()
                    }
                    val publicProfile = publicProfilePayload(
                        id = profile.id,
                        name = profile.name.ifBlank { existingProfile.getString("name").orEmpty() },
                        profilePhotoUrl = profile.profilePhotoUrl ?: existingProfile.getString("profilePhotoUrl"),
                        location = existingProfile.getString("location").orEmpty(),
                        joinedLabel = existingProfile.getString("joinedLabel").orEmpty(),
                        gender = existingProfile.getString("gender").orEmpty(),
                        role = existingProfile.getString("role").orEmpty(),
                        battingStyle = existingProfile.getString("battingStyle").orEmpty(),
                        bowlingStyle = existingProfile.getString("bowlingStyle").orEmpty()
                    ) + mapOf(
                        "createdAt" to (existingProfile.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    firestore.runBatch { batch ->
                        batch.set(document, publicPayload, SetOptions.merge())
                        batch.set(firestore.collection(PUBLIC_PROFILES_COLLECTION).document(profile.id), publicProfile, SetOptions.merge())
                    }
                        .addOnSuccessListener {
                            FirebaseFunctions.getInstance()
                                .getHttpsCallable("syncPrivateUserAccount")
                                .call(emptyMap<String, Any>())
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
            val isOwner = auth.currentUser?.uid == userId
            val profileRef = firestore.collection(if (isOwner) USERS_COLLECTION else PUBLIC_PROFILES_COLLECTION)
                .document(userId)
            profileRef.get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val baseProfile = snapshot.toUserProfile(userId)
                        if (isOwner) enrichWithPrivateAccountIfOwner(userId, baseProfile, continuation)
                        else continuation.resume(Result.success(baseProfile.copy(email = "", phoneNumber = "")))
                    } else {
                        continuation.resume(Result.failure(NoSuchElementException("User profile not found")))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    private fun enrichWithPrivateAccountIfOwner(
        userId: String,
        baseProfile: UserProfile,
        continuation: kotlinx.coroutines.CancellableContinuation<Result<UserProfile>>
    ) {
        val currentUid = auth.currentUser?.uid
        if (currentUid != null && currentUid == userId) {
            val authUser = auth.currentUser
            userDocument(userId).collection(PRIVATE_COLLECTION).document(ACCOUNT_DOCUMENT).get()
                .addOnSuccessListener { privateDoc ->
                    val email = privateDoc.getString("email")
                        ?.ifBlank { null }
                        ?: baseProfile.email.ifBlank { null }
                        ?: authUser?.email.orEmpty()
                    val phone = privateDoc.getString("phoneNumber")
                        ?.ifBlank { null }
                        ?: baseProfile.phoneNumber.ifBlank { null }
                        ?: authUser?.phoneNumber.orEmpty()
                    val authProviderStr = privateDoc.getString("authProvider")
                    val authProvider = if (!authProviderStr.isNullOrBlank()) {
                        runCatching { com.example.sportsxtreme.domain.model.AuthProvider.valueOf(authProviderStr) }
                            .getOrDefault(baseProfile.authProvider)
                    } else baseProfile.authProvider

                    val isEmailVerified = privateDoc.getBoolean("isEmailVerified")
                        ?: authUser?.isEmailVerified
                        ?: baseProfile.isEmailVerified
                    val isPhoneVerified = privateDoc.getBoolean("isPhoneVerified")
                        ?: baseProfile.isPhoneVerified

                    val enriched = baseProfile.copy(
                        email = email,
                        phoneNumber = phone,
                        authProvider = authProvider,
                        isEmailVerified = isEmailVerified,
                        isPhoneVerified = isPhoneVerified
                    )
                    continuation.resume(Result.success(enriched))
                }
                .addOnFailureListener {
                    val enriched = baseProfile.copy(
                        email = authUser?.email.orEmpty().ifBlank { baseProfile.email },
                        phoneNumber = authUser?.phoneNumber.orEmpty().ifBlank { baseProfile.phoneNumber },
                        isEmailVerified = authUser?.isEmailVerified ?: baseProfile.isEmailVerified
                    )
                    continuation.resume(Result.success(enriched))
                }
        } else {
            continuation.resume(Result.success(baseProfile.copy(email = "", phoneNumber = "")))
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<UserProfile> {
        return suspendCancellableCoroutine { continuation ->
            val publicPayload = profile.toFirestorePayload() + mapOf(
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val userRef = firestore.collection(USERS_COLLECTION).document(profile.id)
            val projection = profile.publicProjectionPayload() + mapOf(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.runBatch { batch ->
                batch.set(userRef, publicPayload, SetOptions.merge())
                batch.set(firestore.collection(PUBLIC_PROFILES_COLLECTION).document(profile.id), projection, SetOptions.merge())
            }
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
            "profilePhotoUrl" to profilePhotoUrl,
            "location" to location,
            "joinedLabel" to joinedLabel,
            "gender" to gender,
            "role" to role,
            "battingStyle" to battingStyle,
            "bowlingStyle" to bowlingStyle
        )
    }

    private fun publicProfilePayload(
        id: String, name: String, profilePhotoUrl: String?, location: String,
        joinedLabel: String, gender: String, role: String,
        battingStyle: String, bowlingStyle: String
    ): Map<String, Any?> = mapOf(
        "id" to id, "name" to name, "profilePhotoUrl" to profilePhotoUrl,
        "location" to location, "joinedLabel" to joinedLabel, "gender" to gender,
        "role" to role, "battingStyle" to battingStyle, "bowlingStyle" to bowlingStyle
    )

    private fun UserProfile.publicProjectionPayload(): Map<String, Any?> = publicProfilePayload(
        id, name, profilePhotoUrl, location, joinedLabel, gender, role, battingStyle, bowlingStyle
    )

    private fun DocumentSnapshot.toUserProfile(fallbackId: String): UserProfile {
        return UserProfile(
            id = getString("id").orEmpty().ifBlank { fallbackId },
            name = getString("name").orEmpty(),
            email = getString("email").orEmpty(),
            phoneNumber = getString("phoneNumber").orEmpty(),
            profilePhotoUrl = getString("profilePhotoUrl").takeUnless { it.isGoogleAccountPhotoUrl() },
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
            "bestBowling" to bestBowling,
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
            bestBowling = getString("bestBowling").orEmpty(),
            trophies = getLong("trophies")?.toInt() ?: 0,
            topPerformerStreak = getBoolean("topPerformerStreak") ?: false,
            battingAverage = getDouble("battingAverage") ?: 0.0,
            bowlingAverage = getDouble("bowlingAverage") ?: 0.0,
            strikeRate = getDouble("strikeRate") ?: 0.0,
            mvpCount = getLong("mvpCount")?.toInt() ?: 0
        )
    }

    private fun String?.isGoogleAccountPhotoUrl(): Boolean {
        val host = this?.let { value -> runCatching { Uri.parse(value).host.orEmpty() }.getOrDefault("") }
            ?.lowercase()
            .orEmpty()
        return host == "googleusercontent.com" || host.endsWith(".googleusercontent.com")
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
        const val PUBLIC_PROFILES_COLLECTION = "publicProfiles"
        const val PRIVATE_COLLECTION = "private"
        const val ACCOUNT_DOCUMENT = "account"
        const val STATS_COLLECTION = "stats"
        const val SUMMARY_DOCUMENT = "summary"
        const val SETTINGS_COLLECTION = "settings"
        const val PREFERENCES_DOCUMENT = "preferences"
        const val ACHIEVEMENTS_COLLECTION = "achievements"
    }
}
