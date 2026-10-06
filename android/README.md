# On Vpn Android MVP (v0.1.0)

This is a real Android application shell wired to the official WireGuard Android tunnel library. It parses a WireGuard config, asks Android for VPN permission, and calls the WireGuard backend to raise/lower a tunnel.

## Requirements
- Android Studio with Android SDK Platform 35
- JDK 17
- Internet access on the build machine to resolve Maven/Gradle dependencies
- A working WireGuard server and a valid client `.conf` file

## Build
1. Open the `android/` folder in Android Studio.
2. Allow Gradle sync to complete.
3. Connect an Android device (Android 7.0/API 24 or later) or start an emulator.
4. Run the `app` configuration.

The APK is not prebuilt in this archive. Build it locally so Android Studio can resolve dependencies and sign/debug it for your device.

## Use
1. Provision a WireGuard server you own or trust.
2. Generate a client config on the server. The config needs `[Interface]` and `[Peer]` sections, a valid private key, server public key, endpoint and allowed IPs.
3. Paste the full config into the app and tap **اتصال**.
4. Accept Android's VPN permission prompt.

## Important MVP limits
- No server is bundled or provided. The UI cannot provide Internet access until a reachable WireGuard server and valid configuration exist.
- This version does not yet include account provisioning, server discovery, smart server selection, traffic counters, per-app split tunneling UI, or an audited always-on/kill-switch workflow.
- The config is held in the Activity's input field for this MVP. Do not share screenshots or bug reports containing a real config; the private key is secret. A later iteration should use Android Keystore-backed encrypted storage and a dedicated config import flow.
- Do not publish this build as production-ready. Test DNS, IPv4/IPv6 routing, reconnects, captive portals, sleep/resume, battery usage and failure handling on real devices first.

## Dependency
Uses `com.wireguard.android:tunnel:1.0.20260102`, published by the WireGuard Android project under Apache-2.0. See https://github.com/WireGuard/wireguard-android and https://central.sonatype.com/artifact/com.wireguard.android/tunnel/overview.
