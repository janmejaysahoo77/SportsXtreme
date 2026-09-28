# HomeScreen Compose Migration Notes

## Current State

`HomeScreenView` is still a custom Android `FrameLayout`, not a Compose screen. `MainActivity` currently renders it inside Compose with `AndroidView`, then keeps a `homeScreenView` reference so `onResume()` can call `refreshAfterResume()`.

The migration goal is to replace the internals phase by phase while preserving the current visuals, colors, spacing, navigation, animations, and activity launches.

## App Flow Map

- `MainActivity` owns the top-level in-memory screen state for splash, onboarding, signup, login, sport selection, and home.
- `SportsSplashView`, `OnboardingScreenView`, `SignupScreenView`, `LoginScreenView`, and `SportSelectionView` are custom Android views rendered through `AndroidView`.
- `SportSelectionView` enters the home screen through `MainActivity.showHomeScreen()`.
- `XtremeMediaActivity` and `ShoppingActivity` are separate activities used by the top mode buttons.
- Most newer match/tournament/player/profile screens are already Compose activities.
- `ActivityWindowStyle.kt` applies common status and navigation bar colors.

## HomeScreenView Responsibilities

- Creates a `DrawerLayout` root.
- Adds the main content shell and a 300dp left nav drawer.
- Draws its own background with custom `View` subclasses.
- Owns the bottom nav state with `selectedIndex` and `cachedTabs`.
- Builds bottom nav tabs: Home, Stats, Host, Community, Leaderboard.
- Builds a raised Host FAB in the center of the bottom nav.
- Opens the nav drawer from top bar menu buttons.
- Opens `MyProfileViewOfSportsXtreme` when the drawer profile icon or name is tapped.
- Launches `TournamentRegistrationActivity` and `StartMatchActivity` from drawer actions.
- Launches `ViewAllScoreCardActivity` from "View All".
- Launches `ScorecardActivity` from match cards and host slider CTA.
- Calls `MainActivity.showHomeScreen()`, `showXtremeMediaScreen()`, and `showXtremeCartScreen()` from the top mode selector.

## Home Tab

The Home tab builds a custom vertical Android view tree:

- top mode selector and brand/action top bar
- location row
- "Matches Near You" horizontal score cards
- Pro pass horizontal cards with custom drawing effects
- personalized gear section
- sports feed card
- next match card
- multiple custom icon/background/glow views

Key colors in this screen:

- background: `Color.rgb(1, 5, 9)`
- panel: `Color.rgb(7, 14, 18)`
- primary accent: `Color.rgb(193, 255, 0)`
- cyan accent: `Color.rgb(0, 210, 255)`
- muted text: `Color.rgb(130, 145, 142)`

## Host Tab

The Host tab is also custom Android views:

- dark blue-black background
- custom top strip with menu/logo/pro/bell
- auto-flipping hero carousel using `ViewFlipper`
- tournament registration card
- start match card
- security note

Important interactions:

- Tournament card opens `TournamentRegistrationActivity`.
- Start match card opens `StartMatchActivity`.
- Slider CTAs open tournament registration, start match, or scorecard depending on slide.

## Drawer

The drawer is a custom vertical Android layout:

- profile row, name, id, pro member chip, completion bar
- pro banner
- menu rows
- expanded "More" sub-items
- social icons
- footer links

Important interactions:

- profile row and name open `MyProfileViewOfSportsXtreme`
- "Add a Tournament/Series" opens `TournamentRegistrationActivity`
- "Start A Match" opens `StartMatchActivity`

## Already Compose Screens Near Home

- `CommunityScreen` and `LeaderboardScreen` are Compose functions, but `HomeScreenView` currently routes those bottom tabs to `createComingSoon()` to keep the build stable.
- `XtremeSectionScreen` is a reusable Compose section shell for Sports/Media/Cart style pages.
- `MyProfileViewOfSportsXtreme` is a Compose activity.

## Migration Rules

- Keep the same colors, text sizes, spacing, image assets, activity launches, and animation behavior unless a later phase explicitly changes them.
- Convert one visible area at a time.
- Keep `HomeScreenView` available until the replacement Compose screen reaches parity.
- After each phase, run `assembleDebug`.
- Do not delete custom drawing/icon classes until no active UI path uses them.

## Phase Plan

1. Add a Compose route wrapper for the existing `HomeScreenView`. This changes the entry point only and should not change UI.
2. Move home screen state (`selectedIndex`, tab model, refresh callback) to Compose while still rendering tab bodies with Android views.
3. Rebuild the bottom navigation in Compose, preserving dimensions and colors.
4. Rebuild the drawer in Compose, preserving width, profile tap behavior, and drawer actions.
5. Rebuild the top bar and mode selector in Compose.
6. Migrate Home tab content section by section.
7. Migrate Host tab content section by section, including carousel timing.
8. Decide whether to restore Compose `CommunityScreen` and `LeaderboardScreen` tabs.
9. Remove unused custom View code after parity is verified.

## Completed Steps

- Introduced `HomeScreenRoute`.
- Moved Home bottom navigation selection state to Compose.
- Rebuilt the Home top mode selector in Compose.
- Rebuilt the Home brand/action top bar in Compose for the `HomeScreenRoute` path.
- Rebuilt the Home location row in Compose for the `HomeScreenRoute` path.
- Rebuilt the Home "Matches Near You" score-card section in Compose for the `HomeScreenRoute` path.
- Rebuilt the Home Pro pass carousel in Compose for the `HomeScreenRoute` path.
- Rebuilt the Home bottom navigation in Compose.
- Kept the existing `HomeScreenView` as the tab body host and drawer owner.
- Migrated the Community and Leaderboard tab bodies to their existing Compose screens through `ComposeView`.
- Wired Community and Leaderboard menu buttons back to the existing drawer.

`MainActivity` still keeps `homeScreenView` updated for `refreshAfterResume()`.

This gives us a Compose-owned Home entry point with effectively zero visual or behavior risk.
