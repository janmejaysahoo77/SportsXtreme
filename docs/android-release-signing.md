# Android release signing for Google Play

SportsXtreme uses a dedicated **upload key** to sign Play upload bundles. Google
Play App Signing uses its app signing key for APKs delivered to users; the
upload key is used only to authenticate uploads. Keep a secure backup of the
keystore and its credentials. Do not commit them or share them in source
control.

## Create the upload keystore

Create the key once on a trusted machine. Choose a path outside this repository
and allow `keytool` to prompt for passwords; do not put passwords in the
command line:

```powershell
New-Item -ItemType Directory -Force "$env:USERPROFILE\keys"
keytool -genkeypair -v `
  -keystore "$env:USERPROFILE\keys\sportsxtreme-upload.jks" `
  -keyalg RSA -keysize 2048 -validity 10000 `
  -alias sportsxtreme-upload
```

Keep the keystore, alias, store password, and key password backed up securely.
Do not regenerate or replace this key for later uploads. If an upload key is
lost, use Play Console's upload-key reset process.

## Provide local signing credentials

Set these four values in the **user-level** Gradle properties file
`%USERPROFILE%\.gradle\gradle.properties`, or provide them as environment
variables. Gradle properties take precedence over environment variables.
Protect any file that contains passwords with the operating system account's
normal access controls. Do not put credentials in the project-level
`gradle.properties`.

```properties
SPORTSXTREME_UPLOAD_STORE_FILE=C:/Users/<you>/keys/sportsxtreme-upload.jks
SPORTSXTREME_UPLOAD_STORE_PASSWORD=<store password>
SPORTSXTREME_UPLOAD_KEY_ALIAS=sportsxtreme-upload
SPORTSXTREME_UPLOAD_KEY_PASSWORD=<key password>
```

The equivalent PowerShell environment variable names are the same. Set them
for the current session before building. The keystore path should point to the
existing upload keystore; Gradle does not create keys.

## Build a signed release bundle

From the repository root, run:

```powershell
.\gradlew.bat :app:bundleRelease
```

The signed bundle is written to
`app\build\outputs\bundle\release\app-release.aab`. Release build tasks fail
before building if any required credential is missing or the keystore path is
unreadable. Debug signing is not changed.

To check the upload certificate without exposing passwords, use `keytool` and
enter the keystore password at its prompt:

```powershell
keytool -list -v -keystore "$env:USERPROFILE\keys\sportsxtreme-upload.jks" -alias sportsxtreme-upload
```

When enrolling in Play App Signing, record Google's **app signing key** SHA-256
separately from this upload certificate. Android App Links for Play-installed
builds must use the app signing certificate fingerprint.
