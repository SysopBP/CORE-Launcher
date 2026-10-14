# CORE: D2 lock screen and automatic folders

Install the matching CORE and D2 bridge builds. Set CORE as Android’s default Home app. Enable D2 and configure its PIN as usual. CORE’s Settings → Gestures → Use D2 lock screen is enabled by default.

The normal double-tap Sleep action first asks D2 to lock. D2 displays its own lock surface and turns the screen off through its existing Root, Shizuku or Device Admin path. On wake, CORE asks D2 to resume only while D2 reports an active lock. Successful D2 authentication clears that state, preventing a relock loop when returning Home. Calls and trusted authentication defer the handoff. Samsung’s first-boot credentials remain authoritative.

The D2 provider checks the caller UID against the current default Home app. It exposes only readiness, lock state, and immutable lock-only PendingIntents. It exposes no PIN, notification data, unlock operation, or preview control. The receiving Activity rechecks armed/configured state, calls, and authentication before launching the private lock Activity. Older D2 builds use CORE’s existing Sleep fallback.

## Automatic folders

CORE Settings → App drawer → Automatic app folders groups installed apps into Google, Finance, Games, and the other bundled category rules. New CORE installations enable this layout by default; existing layouts are preserved until the switch is enabled. App install/remove updates regroup the drawer. A category remains a folder even when it currently has one app. Hidden apps and separate work/private profiles retain the existing filtering rules.

Google packages are grouped first. Functional categories take precedence over the System Apps fallback, so a preinstalled game or finance app can appear in its useful category. Unknown apps go into Other; uncategorized system apps go into System Apps. Finance coverage includes common US banks, payment apps, investment services and credit apps. Category matching is local and rule based, so obscure apps may appear in Other. Manual folders remain available with automatic folders disabled.

## Phone validation

1. Set CORE as default Home and enable D2 with its PIN configured.
2. Double-tap an empty Home area with the Sleep gesture selected: D2 should appear before the display sleeps.
3. Wake with the power button: D2 should cover Home. Authenticate once: Home should remain visible without a relock loop.
4. Repeat with an incoming call and with fingerprint authentication in progress: CORE must not interrupt those trusted surfaces.
5. Toggle D2 off: CORE must not force its lock surface.
6. Enable Automatic app folders and inspect Google, Finance and Games. Install/remove a categorized app and verify the folder updates. Turn the layout off to restore the regular drawer/manual folders.

CI compiles CORE and builds/lints/tests D2. Samsung Android 17 behavior requires the phone validation above.
