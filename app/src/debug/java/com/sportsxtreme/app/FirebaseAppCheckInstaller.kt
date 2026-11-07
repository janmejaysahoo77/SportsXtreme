package com.sportsxtreme.app

import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/** Uses Firebase's debug provider for local development and emulator runs. */
internal object FirebaseAppCheckInstaller {
    fun install() {
        FirebaseAppCheck.getInstance().apply {
            installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
            setTokenAutoRefreshEnabled(true)
        }
    }
}
