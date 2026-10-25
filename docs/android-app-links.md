# Android App Links

## Hosts and routes

The app declares HTTPS intent filters for these hosts:

| Host | Route | Purpose and Android destination |
| --- | --- | --- |
| `sportsxtreme-95fbb.web.app` | `/join?invite=<token>` | Match invitation; opens `MainActivity`, which resolves the match invitation. |
| `sportsxtreme-95fbb.web.app` | `/team-invite?token=<token>` | Team invitation; opens `MainActivity`, which claims the team invitation. |
| `sportsxtreme-95fbb.web.app` | `/tournament-invite?token=<token>` | Tournament invitation; opens `MainActivity` and the tournament invitation screen. |
| `sportsxtreme-95fbb.web.app` | `/scorecard?matchId=<id>` | Public scorecard; opens `ScorecardActivity`, which reads `matchId` from the URL. |
| `sportsxtreme-95fbb.web.app` | `/tournament` | Tournament registration; opens `RegisterTournamentFinalPageActivity`. |
| `sportsxtreme-95fbb.web.app` | `/post/<postId>` | Shared community post; opens `MainActivity`, which validates and opens the post. |
| `sportsxtreme-95fbb.firebaseapp.com` | `/auth...` | Firebase email authentication links; opens `MainActivity`. |

Firebase Hosting serves the Android association document from
`public/.well-known/assetlinks.json`. The Hosting configuration sets its
content type to `application/json; charset=utf-8`. Invitation and scorecard
URLs also rewrite to `public/app-link-fallback.html` for visitors without the
app. The page directs them to Google Play and tells them to reopen the original
URL after installation.

## Signing certificate verification required

The association JSON names the application ID `com.example.sportsxtreme`.
It currently contains two SHA-256 fingerprints:

- `99:4B:3B:AC:90:21:6E:D9:C6:DF:72:3B:FC:93:E0:1F:84:7A:54:DC:93:A4:DF:AE:B3:E5:F8:BD:A4:CF:B4:14` is documented as this workspace's debug certificate.
- `53:2B:7C:27:97:AE:DD:5D:31:19:96:37:10:0F:16:49:58:37:FF:4F:B2:FE:B6:D3:F1:71:72:C6:F5:DE:3B:5A` is present in the association file, but this repository does not establish which certificate it belongs to.

Do not treat the second fingerprint as the Play App Signing certificate until
it has been compared with Play Console. In Play Console, select the app and go
to **Test and release → Setup → App signing** (sometimes shown under **App
integrity**), then copy the SHA-256 fingerprint under **App signing key
certificate**. Use the app signing key fingerprint, not the upload key
fingerprint. Replace or retain the fingerprints in `assetlinks.json` only
after confirming their certificate roles. [Google Play's App Signing help](https://support.google.com/googleplay/android-developer/answer/9842756?hl=en)

The app ID in Play Console must be exactly `com.example.sportsxtreme` for this
association to apply. The Google Play install link in the fallback page uses
that same ID; it will work once a listing for this package is available.

## Verify before release

1. Confirm the Play App Signing key SHA-256 as described above and verify the
   JSON entry against it.
2. Deploy Firebase Hosting only after an authorized release deployment.
3. Check `https://sportsxtreme-95fbb.web.app/.well-known/assetlinks.json` for
   HTTP 200, `Content-Type: application/json`, and the expected production
   fingerprint.
4. On a device with the release-signed app installed, verify the four
   invitation and scorecard URLs above open their intended screens. On a
   device without the app, verify those URLs display the install fallback and
   preserve the original URL for reopening after installation.
