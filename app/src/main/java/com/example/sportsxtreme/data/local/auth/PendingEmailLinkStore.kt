package com.example.sportsxtreme.data.local.auth

import android.content.Context

class PendingEmailLinkStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveEmail(email: String) {
        preferences.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(): String? {
        return preferences.getString(KEY_EMAIL, null)?.takeIf { it.isNotBlank() }
    }

    fun saveLink(link: String) {
        preferences.edit().putString(KEY_LINK, link).apply()
    }

    fun getLink(): String? {
        return preferences.getString(KEY_LINK, null)?.takeIf { it.isNotBlank() }
    }

    fun clearLink() {
        preferences.edit().remove(KEY_LINK).apply()
    }

    fun clearAll() {
        preferences.edit().remove(KEY_EMAIL).remove(KEY_LINK).apply()
    }

    private companion object {
        const val PREFS_NAME = "sportsxtreme_email_link_auth"
        const val KEY_EMAIL = "pending_email"
        const val KEY_LINK = "pending_link"
    }
}
