# On Vpn — Architecture v0.1

## Goal
A lightweight VPN client for Android and Windows using WireGuard, with a small control plane for server selection and configuration delivery.

## Components
- Android: Kotlin + Android VpnService integration, foreground service, per-app routing, persistent tunnel state.
- Windows: C#/.NET desktop UI + service boundary; WireGuardNT/WireGuard official components for tunnel implementation.
- Control API: Go service, HTTPS only, device registration, server catalog, short-lived configuration delivery.
- VPN servers: Linux + WireGuard; no application traffic logging by default.

## Security principles
1. Never transmit private WireGuard keys to the control server after device generation.
2. Device generates its own keypair locally.
3. API authenticates devices using short-lived enrollment tokens.
4. Config responses are signed/versioned and expire.
5. No custom cryptography.
6. Kill switch is fail-closed when explicitly enabled.
7. DNS routing is explicit; avoid silent DNS leakage.

## Data economy
- WireGuard UDP transport.
- Persistent tunnel and sensible keepalive only when required by NAT conditions.
- Optional split tunneling.
- Minimal control-plane polling; exponential backoff.
- Server selection based on latency, loss and load rather than frequent switching.

## MVP acceptance tests
- Connect/disconnect reliably.
- IPv4 + IPv6 routing policy is explicit.
- DNS leak test passes when full tunnel is enabled.
- Kill switch blocks traffic when tunnel is down.
- Split tunnel routes only selected apps/domains according to documented policy.
- No private key appears in logs.

## Android MVP implementation
The Android client uses Kotlin and the embeddable `com.wireguard.android:tunnel` library. The UI imports a WireGuard client profile through Android's document picker, parses it using the WireGuard config parser, and invokes the backend to bring the tunnel up/down. The app does not create server credentials or ship a public endpoint. Android's VPN consent dialog is required before activation.
