package com.example.sportsxtreme.presentation.profile

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.model.UserProfile
import com.example.sportsxtreme.domain.model.UserProfileSettings
import com.example.sportsxtreme.domain.model.UserProfileStats
import com.example.sportsxtreme.domain.usecase.AuthUseCases

/** State and data mapping for the profile screen; intentionally UI-framework free. */
internal data class ProfileUiState(
    val profile: UserProfile = fallbackProfile(),
    val stats: UserProfileStats = UserProfileStats(userId = ""),
    val settings: UserProfileSettings = UserProfileSettings(userId = ""),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

internal suspend fun loadProfileUiState(useCases: AuthUseCases): ProfileUiState {
    val currentUser = useCases.getCurrentUser()
    val userId = currentUser?.id.orEmpty()
    val fallbackProfile = currentUser.toFallbackProfile()
    if (userId.isBlank() || currentUser == null) {
        return ProfileUiState(profile = fallbackProfile, stats = fallbackProfile.toFallbackStats(), settings = UserProfileSettings(userId = ""), isLoading = false, errorMessage = "Could not find logged-in user.")
    }
    useCases.createOrUpdateUserProfile(currentUser.toPendingProfile())
    val profileResult = useCases.getUserProfile(userId)
    val statsResult = useCases.getUserProfileStats(userId)
    val settingsResult = useCases.getUserProfileSettings(userId)
    val profile = profileResult.successData() ?: fallbackProfile
    val stats = statsResult.successData() ?: profile.toFallbackStats()
    val settings = settingsResult.successData() ?: UserProfileSettings(userId = userId)
    val hasError = listOf(profileResult.errorText(), statsResult.errorText(), settingsResult.errorText()).any { it != null }
    return ProfileUiState(profile, stats, settings, isLoading = false, errorMessage = if (hasError) "Some profile data could not refresh." else null)
}

internal fun fallbackProfile() = UserProfile(id = "", name = "SportsXtreme Player", email = "", phoneNumber = "")

private fun User?.toFallbackProfile() = this?.let {
    UserProfile(id = id, name = name.ifBlank { "SportsXtreme Player" }, email = email, phoneNumber = mobileNumber, profilePhotoUrl = profilePhotoUrl, authProvider = authProvider, isEmailVerified = isEmailVerified, isPhoneVerified = isPhoneVerified)
} ?: fallbackProfile()

private fun User.toPendingProfile() = PendingUserProfile(id, name, email, mobileNumber, profilePhotoUrl, authProvider, isEmailVerified, isPhoneVerified)

private fun UserProfile.toFallbackStats() = UserProfileStats(userId = id, matchesPlayed = matchesPlayed, wins = wins, losses = (matchesPlayed - wins).coerceAtLeast(0), bestScore = bestScore, trophies = trophies, topPerformerStreak = topPerformerStreak)
private fun <T> Resource<T>.successData(): T? = (this as? Resource.Success)?.data
private fun <T> Resource<T>.errorText(): String? = (this as? Resource.Error)?.message
