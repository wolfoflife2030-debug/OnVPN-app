# On Vpn — engineering workspace

On Vpn is a cross-platform VPN project intended to use WireGuard rather than a custom cryptographic protocol.

## Current milestone
- `android/`: Android MVP source using the official WireGuard tunnel library, with config parsing, Android VPN permission, and tunnel up/down controls.
- `docs/`: architecture and initial API notes.
- `server/`: **development-only stub**, not a production API. It currently uses plain HTTP and returns a placeholder endpoint; do not expose it to the Internet.
- `windows/`: reserved for the Windows client milestone.

## Build Android
Open the `android/` directory in Android Studio with JDK 17 and Android SDK 35. See `android/README.md` for requirements and setup.

### GitHub Actions
The repository includes CI under `.github/workflows/android.yml`. Every push/PR touching the Android project runs lint and produces a debug APK as a GitHub Actions artifact. The workflow uses JDK 17 and Gradle 8.11.1, matching the AGP 8.10.x toolchain.

A manual release workflow is available under `.github/workflows/release.yml`. It currently produces an unsigned release artifact; production signing must be configured with protected GitHub secrets before publishing.

GitHub's Gradle guidance recommends using the Gradle Wrapper; this repository temporarily uses the pinned Gradle version in CI because the current archive does not include a wrapper yet. The wrapper will be added before the first public release.

## Security posture
No VPN server is bundled. A functioning VPN requires a reachable, properly configured WireGuard server. Never commit real WireGuard private keys, tokens, or production endpoints to this repository. Do not describe the current milestone as production-ready until security and network-leak tests pass.

## Build verification
This workspace has been checked for project structure and source/API compatibility against the current WireGuard Android tunnel API. The execution environment used to prepare this archive does not contain the Android SDK/Gradle distribution, so an APK build could not be executed here. Build with Android Studio or the Gradle wrapper on a machine with Android SDK 35 and JDK 17.

## Release gate

Do not publish a production release until the GitHub Android, Windows, and Server CI workflows are green and a real WireGuard server has been tested from both an Android device and Windows 10/11.
