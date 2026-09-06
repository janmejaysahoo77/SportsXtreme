# Fix NullPointerException in Firebase SMS Retrieval

The app is experiencing a `java.lang.NullPointerException` in Firebase Auth's internal `BroadcastReceiver` (`zzagp.onReceive`) during SMS retrieval. This occurs because `OtpVerificationScreenView` manually starts both `SmsRetriever` and `SmsUserConsent`, leading to conflicting `SMS_RETRIEVED` broadcasts. Specifically, `SmsUserConsent` triggers a broadcast with a `SUCCESS` status but without an `EXTRA_SMS_MESSAGE` (containing an `EXTRA_CONSENT_INTENT` instead), which Firebase Auth's internal parser fails to handle.

## Proposed Changes

### [Presentation Layer]

#### [MODIFY] [OtpVerificationScreenView.kt](file:///D:/AndroidProject/SportsXtreme/app/src/main/java/com/example/sportsxtreme/presentation/auth/OtpVerificationScreenView.kt)

- Remove manual `SmsRetriever` and `SmsUserConsent` initialization.
- Remove the manual `smsReceiver` and its registration/unregistration logic.
- Rely entirely on Firebase Auth's built-in automatic SMS retrieval, which is already correctly configured in `FirebasePhoneAuthManager`.

## Verification Plan

### Automated Tests
- I will verify that the project builds successfully after the changes.

### Manual Verification
1. Trigger the Phone Auth flow in the app.
2. Verify that the SMS is still automatically retrieved (if the SMS contains the correct app hash).
3. Verify that the app no longer crashes when an SMS arrives.
