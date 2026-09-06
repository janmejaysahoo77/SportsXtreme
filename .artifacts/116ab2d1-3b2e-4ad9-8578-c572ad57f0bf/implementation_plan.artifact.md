# Fix RuntimeException in Firebase SMS Retrieval

The app is experiencing a `java.lang.RuntimeException: Error receiving broadcast Intent` in Firebase Auth's internal `BroadcastReceiver` (`zzagp.onReceive`) during SMS retrieval. This is likely caused by a conflict between manual `SmsRetriever`/`SmsUserConsent` initialization and Firebase Auth's internal automatic SMS retrieval, especially when targeting high API levels (API 34+). Specifically, manual `SmsUserConsent` triggers a broadcast with the same action (`SMS_RETRIEVED_ACTION`) but different extras, which Firebase Auth's internal receiver fails to handle safely.

## User Review Required

> [!IMPORTANT]
> This fix removes the manual SMS User Consent flow (the bottom sheet that appears for SMS without a hash). The app will now rely entirely on Firebase Auth's automatic SMS retrieval, which requires the SMS to contain the correct App Hash. I have verified that the code already prints the App Hash in the logs for testing.

## Proposed Changes

### [Presentation Layer]

#### [MODIFY] [OtpVerificationScreenView.kt](file:///D:/AndroidProject/SportsXtreme/app/src/main/java/com/example/sportsxtreme/presentation/auth/OtpVerificationScreenView.kt)

- Remove manual `SmsRetriever` and `SmsUserConsent` initialization methods (`startSmsRetriever`, `startSmsUserConsent`).
- Remove the manual `smsReceiver` and its registration/unregistration logic in `onAttachedToWindow` and `onDetachedFromWindow`.
- Rely on Firebase Auth's built-in automatic SMS retrieval, which is already handled via `onVerificationCompleted` in `FirebasePhoneAuthManager` and collected in `OtpVerificationScreenView`.

### [Build Configuration]

#### [MODIFY] [app/build.gradle.kts](file:///D:/AndroidProject/SportsXtreme/app/build.gradle.kts)

- Adjust `compileSdk` and `targetSdk` to 35 (Android 15) instead of 36, as 36 is not a standard release and might trigger untested strict behaviors in libraries.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.
- `gradlew assembleDebug`

### Manual Verification
1. Open the app and navigate to the Phone Authentication screen.
2. Enter a phone number and wait for the SMS.
3. Check the logs (`Logcat`) for the App Hash.
4. Send a test SMS to the device with the format: `<#> Your SportsXtreme OTP is 123456. [APP_HASH]`
5. Verify that the OTP is automatically filled and no crash occurs.
