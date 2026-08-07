# Implementation Plan - Fix Build Errors (KSP and JDK/jlink)

The project is failing to build due to two main issues:
1. **JDK/jlink Error**: Gradle is attempting to use a JRE from a VS Code extension directory that is missing the `jlink` executable.
2. **KSP Version Mismatch**: The KSP plugin version (`2.3.9`) does not match the Kotlin version (`2.2.10`), leading to `PROCESSING_ERROR`.

## Proposed Changes

### Build Configuration

#### [MODIFY] [gradle.properties](file:///C:/Users/janme/AndroidStudioProjects/SportsXtreme/gradle.properties)
- Explicitly set `org.gradle.java.home` to a valid JDK 21 already present in the Gradle cache (`C:/Users/janme/.gradle/jdks/jetbrains_s_r_o_-21-amd64-windows.2`). This will bypass the broken VS Code JRE path.

#### [MODIFY] [libs.versions.toml](file:///C:/Users/janme/AndroidStudioProjects/SportsXtreme/gradle/libs.versions.toml)
- Update the `ksp` version from `2.3.9` to a version compatible with Kotlin `2.2.10` (e.g., `2.2.10-1.0.31`).

## Verification Plan

### Automated Tests
- Run `./gradlew clean assembleDebug` to verify the build process completes successfully.
- Run `:app:kspDebugKotlin` specifically to ensure KSP processing finishes without `PROCESSING_ERROR`.
