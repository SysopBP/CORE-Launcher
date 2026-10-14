# CORE Launcher

Independent Android launcher prototype based on Lawnchair and AOSP Launcher3.

## Prototype baseline

CORE app IDs and labels, an original adaptive icon, selected English onboarding text, an upstream updater guard and a preview build workflow are included. Launcher layout and behavior remain upstream. Floating dock, expanded search, folder panels and D2/TokenX integration are planned. Remaining About links and translations need a branding audit.

## Build

Clone this repository recursively, install Java 21 and the Android SDK, then run:

```bash
./gradlew assembleLawnWithQuickstepGithubDebug --stacktrace
```

Or run **CORE Preview APK** in Actions. It also runs on pushes to `core-dev`. Successful builds upload APK artifacts; no automatic release publishing. A stable signing key must be configured for upgradeable public builds; debug runner keys may vary.

## Compatibility and validation

Android 17 / One UI 9 remains unverified. Use CORE as ordinary Home first; do not replace Samsung Recents. Upstream release Quickstep supports Android 15–16; debug relaxes the gate without proving compatibility.

Patch whitespace and modified XML checks passed. Local compilation stopped before compilation because Gradle download access was blocked. No successful build or device test is claimed. See docs/CORE-DEVICE-TEST.md.

## Foundation

Pinned Lawnchair commit: `85335d904a1c8b9790e40c581226684c59166297`. SystemUI submodule: `7d9e92bda50359dc66a530248e15f9748343524e`. Inherited toolchain: Java 21, Gradle 9.8.1, AGP 9.4.1, Kotlin 2.4.21, compile SDK 37 minor API 2, Build Tools 37.0.0.

Original notices and LICENSE.txt are retained. Individual files and dependencies may carry additional terms. Upstream README is preserved at docs/UPSTREAM-README.md; upstream automation is archived under .github/upstream-workflows. CORE is independent of Lawnchair and Samsung.
