# On Vpn pre-GitHub validation

## What was checked locally

- Kotlin source structure: balanced delimiters and no accidental debug secrets.
- Android XML and Windows XAML: parsed successfully.
- C# source structure: balanced delimiters.
- Go server: `go test ./...` passes under Go 1.23 after a temporary local-only go.mod compatibility copy.
- No private signing keys, `.env`, JKS/keystore files, or Gradle build outputs are included.

## What cannot be truthfully claimed from this environment

- A real Android APK was not built here because Android SDK/Gradle dependencies are not available locally.
- A real Windows WPF executable was not built here because the environment is Linux and does not provide the Windows Desktop SDK.
- A real VPN tunnel was not tested against a live WireGuard server in this environment.

## GitHub verification required

After pushing, require all of these checks to pass before merging:

1. Android CI: lint + debug APK.
2. Windows CI: restore + build + win-x64 publish.
3. Server CI: test + build.

Only after these pass should a release tag be created.

## Security boundary

The current server endpoint is a demo API and does not provision peers or authenticate users. It must not be exposed as a production VPN control plane yet.
