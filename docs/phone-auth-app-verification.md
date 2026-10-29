# Phone authentication app verification

The Android phone-auth implementation uses Firebase Authentication with `PhoneAuthOptions` and a live `Activity`. The project uses Firebase Auth 24.2.0, which supports Play Integrity app verification. Firebase chooses Play Integrity when it can attest the installed app; otherwise it can open the browser reCAPTCHA fallback. The app cannot force Firebase to use Play Integrity for every device/install.

## Firebase setup

1. In Firebase Console, open **Authentication > Sign-in method** and enable **Phone**.
2. In **Authentication > Settings**, allow the SMS regions used by the app, such as India (+91).
3. In **Project settings > General > Your apps**, register the Android app package as exactly `com.sportsxtreme.app`, then add the SHA-1 and SHA-256 fingerprints for:
   - the local debug keystore;
   - the release signing keystore;
   - the Google Play App Signing certificate, if the app is distributed through Google Play.
4. Confirm the Firebase Auth API key is unrestricted or permits the Firebase Auth handler domain `<PROJECT_ID>.firebaseapp.com`. The configured project ID is `sportsxtreme-95fbb`, so the handler domain is `sportsxtreme-95fbb.firebaseapp.com`.
5. Download the refreshed `google-services.json` after changing the registered fingerprints and rebuild the app. Fingerprint registration itself is managed in Firebase Console; it is not encoded in this file.
6. Install a release build from Google Play when validating Play Integrity in production-like conditions. Sideloaded/debug builds and devices without Google Play services may use Firebase's reCAPTCHA fallback, so some browser checks can remain even after setup is correct.

The app's Gradle `applicationId` is `com.sportsxtreme.app`. The checked-in `app/google-services.json` contains a matching Firebase Android client, as well as a legacy client for `com.example.sportsxtreme`; Google Services selects the matching client for the build. Do not change the Gradle `applicationId` to the legacy package to work around verification. The JSON file alone cannot establish that the signing certificate on a particular installed APK/AAB is registered in Firebase. If Firebase reports that the request is missing a valid app identifier, compare the installed build's signing certificate with the SHA-1 and SHA-256 fingerprints on the matching Firebase Android app, then check the API key restriction for the reCAPTCHA handler domain.

## Google sign-in setup

Google sign-in and sign-up both use Firebase Authentication. In Firebase Console, enable **Google** under **Authentication > Sign-in method** and choose a support email. Register the SHA-1 fingerprints for the debug certificate, the release certificate used for local APKs, and the Google Play App Signing certificate if applicable. The current matching client in `app/google-services.json` includes one Android OAuth client for `com.sportsxtreme.app` with SHA-1 `3b:29:2d:8e:53:d1:9c:83:1b:57:4a:33:50:c1:9b:97:86:42:de:1b`; a build signed with a different certificate needs its SHA-1 added in Firebase. After registering fingerprints or changing Google provider settings, download the updated `google-services.json` and rebuild. The web OAuth client ID is generated from this file at build time; reference it through `R.string.default_web_client_id` so resource shrinking preserves it, and do not hard-code it in app resources.

For a Play-distributed build, use the **App signing key certificate** from Play Console, not only the upload key certificate. For a locally installed build, register the certificate that signed that exact build. After changing fingerprints in Firebase, download a fresh `google-services.json` and rebuild/reinstall.

Get the local debug fingerprints with (requires the Gradle wrapper distribution to be available):

```text
./gradlew.bat signingReport
```

## Development testing

For emulator or devices without Google Play services, configure Firebase fictional phone numbers under **Authentication > Sign-in method > Phone numbers for testing**. Do not disable app verification or hard-code fictional numbers in a production build. `setAppVerificationDisabledForTesting()` only applies to fictional test numbers; it is not a supported way to skip checks for real users.

There is intentionally no call to `forceRecaptchaFlowForTesting()` or `setAppVerificationDisabledForTesting()` in the app. Those APIs are for testing only and would either force the browser flow or weaken verification.

Firebase documentation: https://firebase.google.com/docs/auth/android/phone-auth
