# Phone authentication app verification

The Android phone-auth implementation uses Firebase Authentication with `PhoneAuthOptions` and a live `Activity`. The project uses Firebase Auth 24.2.0, which supports Play Integrity app verification. Firebase chooses Play Integrity when it can attest the installed app; otherwise it can open the browser reCAPTCHA fallback. The app cannot force Firebase to use Play Integrity for every device/install.

## Firebase setup

1. In Firebase Console, open **Authentication > Sign-in method** and enable **Phone**.
2. In **Authentication > Settings**, allow the SMS regions used by the app, such as India (+91).
3. In **Project settings > General > Your apps**, confirm the Android app package is exactly `com.example.sportsxtreme`, then add the SHA-1 and SHA-256 fingerprints for:
   - the local debug keystore;
   - the release signing keystore;
   - the Google Play App Signing certificate, if the app is distributed through Google Play.
4. Confirm the Firebase Auth API key is unrestricted or permits the Firebase Auth handler domain `<PROJECT_ID>.firebaseapp.com`. The configured project ID is `sportsxtreme-95fbb`, so the handler domain is `sportsxtreme-95fbb.firebaseapp.com`.
5. Download the refreshed `google-services.json` after changing the registered fingerprints and rebuild the app. Fingerprint registration itself is managed in Firebase Console; it is not encoded in this file.
6. Install a release build from Google Play when validating Play Integrity in production-like conditions. Sideloaded/debug builds and devices without Google Play services may use Firebase's reCAPTCHA fallback, so some browser checks can remain even after setup is correct.

Get the local debug fingerprints with:

```text
./gradlew.bat signingReport
```

## Development testing

For emulator or devices without Google Play services, configure Firebase fictional phone numbers under **Authentication > Sign-in method > Phone numbers for testing**. Do not disable app verification or hard-code fictional numbers in a production build. `setAppVerificationDisabledForTesting()` only applies to fictional test numbers; it is not a supported way to skip checks for real users.

There is intentionally no call to `forceRecaptchaFlowForTesting()` or `setAppVerificationDisabledForTesting()` in the app. Those APIs are for testing only and would either force the browser flow or weaken verification.

Firebase documentation: https://firebase.google.com/docs/auth/android/phone-auth
