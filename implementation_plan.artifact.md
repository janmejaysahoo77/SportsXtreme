# Implementation Plan - Pixel Perfect Members Screen

Implement a professional, pixel-perfect "Members" screen based on the provided design, using specific assets and brand colors.

## User Review Required

> [!IMPORTANT]
> The navigation will be integrated into the existing `MainActivity` state-based navigation system. A new `Screen.Members` enum will be added.

## Proposed Changes

### UI Components

#### [NEW] [MembersScreen.kt](file:///D:/AndroidProject/SportsXtreme/app/src/main/java/com/example/sportsxtreme/presentation/clubs/MembersScreen.kt)
- Create a professional Compose-based screen implementing the exact UI from the screenshot.
- Use `BlueBackground` for screen background and `BlueCardBackGround` for cards.
- Apply `XtremeLime` for icons and accents.
- Use requested assets: `@drawable/whitecall`, `@drawable/whiteuser`, `@drawable/whitevictory`, `@drawable/whitevitorystar`, `@drawable/victory`.
- Ensure icons have the correct Lime tint (except `victory`).

### Navigation & Integration

#### [MODIFY] [MainActivity.kt](file:///D:/AndroidProject/SportsXtreme/app/src/main/java/com/example/sportsxtreme/presentation/auth/MainActivity.kt)
- Add `Members` to the `Screen` enum.
- Add `showMembersScreen()` method to switch the current screen.
- Update `SportsXtremeApp()` composable to handle the `Screen.Members` state.

#### [MODIFY] [VictoryClubActivity.kt](file:///D:/AndroidProject/SportsXtreme/app/src/main/java/com/example/sportsxtreme/presentation/clubs/VictoryClubActivity.kt)
- Update the click listener for the "Members" section to trigger navigation to the new screen. Since `VictoryClubActivity` is a separate activity, I will provide a way to navigate back or finish the activity to show the screen in `MainActivity` if that's what's intended, OR I'll update `VictoryClubActivity` to host the new `MembersScreen` directly for a better UX.
- *Decision*: I will update `VictoryClubActivity` to show the new `MembersScreen` directly when the "Members" section is clicked, as it's a more seamless experience within that context.

## Verification Plan

### Manual Verification
- Deploy the app to a device/emulator.
- Navigate to the Victory Club screen.
- Click on the "Members" section.
- Verify the UI matches the provided screenshot exactly:
    - Background color, card colors, and lime accents.
    - Assets (`whitecall`, `whiteuser`, etc.) are present and tinted correctly.
    - Layout matches the pixel-perfect design.
