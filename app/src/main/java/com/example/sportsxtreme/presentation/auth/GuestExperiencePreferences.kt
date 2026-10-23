package com.example.sportsxtreme.presentation.auth

import android.content.Context

internal object GuestExperiencePreferences {
    private const val PREFS = "guest_experience"
    private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    private const val KEY_SELECTED_SPORT = "selected_sport"

    fun hasCompletedOnboarding(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun markOnboardingComplete(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    fun selectedSport(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_SPORT, null)

    fun saveSelectedSport(context: Context, sport: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_SELECTED_SPORT, sport).apply()
    }
}
