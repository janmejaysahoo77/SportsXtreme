package com.sportsxtreme.app

import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/** Uses Play Integrity for release builds distributed to users. */
internal object FirebaseAppCheckInstaller {
    fun install() {
        FirebaseAppCheck.getInstance().apply {
            installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
            // Refresh in the background so a sign-in request is less likely
            // to wait for a Play Integrity token refresh.
            setTokenAutoRefreshEnabled(true)
        }
    }
}
