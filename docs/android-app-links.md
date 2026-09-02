# Android App Link association

The Android manifest handles team invitations at:

`https://sportsxtreme-95fbb.web.app/team-invite?token=<token>`

For Android to open this URL directly in SportsXtreme, publish the following
file at `https://sportsxtreme-95fbb.web.app/.well-known/assetlinks.json` using
the existing Firebase Hosting deployment. Do not replace the current Hosting
configuration merely to add this file.

```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.example.sportsxtreme",
      "sha256_cert_fingerprints": [
        "99:4B:3B:AC:90:21:6E:D9:C6:DF:72:3B:FC:93:E0:1F:84:7A:54:DC:93:A4:DF:AE:B3:E5:F8:BD:A4:CF:B4:14"
      ]
    }
  }
]
```

That fingerprint is for this workspace's debug build. Add the SHA-256
fingerprint of the production signing certificate (or Google Play App Signing
certificate) before releasing the app.
