# CORE Launcher

CORE Launcher is SysopBP's independent Android launcher fork of [Lawnchair](https://github.com/LawnchairLauncher/lawnchair), based on AOSP Launcher3. It is not an official Lawnchair, Google or Samsung release.

## Builds and installation

Use this repository's [Actions](https://github.com/SysopBP/CORE-Launcher/actions) to find a successful build for the intended branch and commit. Download its APK artifact while signed in to GitHub. [Releases](https://github.com/SysopBP/CORE-Launcher/releases) is the destination for published CORE releases when available.

Upstream Lawnchair's Play Store, F-Droid, nightly and Obtainium links install upstream builds, not CORE. The archived [upstream README](docs/UPSTREAM-README.md) is retained for historical documentation and attribution.

The source keeps Lawnchair/Launcher3 namespaces and Gradle variant names where needed for inherited code. The current build configuration still contains upstream application IDs and some upstream labels; confirm the APK variant, package, signer and label before installing. Installation over another signer may require a separate package or uninstall; do not assume settings migrate. A future package migration needs separate device validation.

## Build from source

The default development branch is 16-dev. Initialize submodules and use the Java/Android SDK versions specified in [CI](.github/workflows/ci.yml).

```sh
git clone --recurse-submodules --branch 16-dev https://github.com/SysopBP/CORE-Launcher.git
cd CORE-Launcher
./gradlew assembleLawnWithQuickstepGithubDebug
```

Inherited task/namespace names describe build internals, not upstream endorsement. Tests and Samsung/D2 integration must be evaluated for the exact build; a passing compilation does not establish device behavior.

## Support and security

Report CORE bugs through [this repository's issues](https://github.com/SysopBP/CORE-Launcher/issues). Use [SECURITY.md](SECURITY.md) for security reporting. Upstream maintainers do not support CORE modifications.

## License and attribution

The project retains [Apache 2.0](LICENSE.txt), original AOSP/Lawnchair copyright notices, and third-party component licenses. See [NOTICE](NOTICE) and bundled library licenses. UI branding changes must preserve legal notices and attribution in source and license displays.

CORE modifications are maintained by SysopBP. Credit belongs to the AOSP Launcher3 authors, Lawnchair developers, and contributors to the libraries and submodules used here. Mentioning those projects describes origin; it does not imply affiliation or endorsement.
