# Phone authentication app verification

The Android phone-auth implementation uses Firebase Authentication with `PhoneAuthOptions` and a live `Activity`. Firebase uses Play Integrity for app verification when the installation can be attested, so no browser CAPTCHA is shown in the normal supported path.

## Firebase setup

1. In Firebase Console, open **Authentication > Sign-in method** and enable **Phone**.
2. In **Authentication > Settings**, allow the SMS regions used by the app, such as India (+91).
3. In **Project settings > General > Your apps**, add the SHA-1 and SHA-256 fingerprints for:
   - the local debug keystore;
   - the release signing keystore;
   - the Google Play App Signing certificate, if the app is distributed through Google Play.
4. Download the refreshed `google-services.json` after changing the registered fingerprints and rebuild the app.
5. Install a release build from Google Play when validating Play Integrity in production-like conditions. Sideloaded, unsigned, or non-Google-Play devices may use Firebase's reCAPTCHA fallback.

Get the local debug fingerprints with:

```text
./gradlew.bat signingReport
```

## Development testing

For emulator or devices without Google Play services, configure Firebase fictional phone numbers under **Authentication > Sign-in method > Phone numbers for testing**. Do not disable app verification or hard-code fictional numbers in a production build.

There is intentionally no call to `forceRecaptchaFlowForTesting()` or `setAppVerificationDisabledForTesting()` in the app. Those APIs are for testing only and would either force the browser flow or weaken verification.

Firebase documentation: https://firebase.google.com/docs/auth/android/phone-auth
