# On Vpn for Windows

Windows MVP using the official WireGuard for Windows tunnel service. On Vpn does not implement its own cryptography or driver.

## Requirements
- Windows 10/11 x64
- .NET 8 Desktop Runtime/SDK for development
- Official WireGuard for Windows installed

The app invokes the official WireGuard tunnel-service interface to install/start and uninstall the selected `.conf` tunnel. The official WireGuard Windows project documents this service architecture and commands. See: https://github.com/WireGuard/wireguard-windows

## Build
```powershell
dotnet restore
dotnet build OnVpn.Client/OnVpn.Client.csproj -c Release -p:Platform=x64
```

The current MVP intentionally requires the official WireGuard installation. Later releases can package a signed installer that deploys the required WireGuard components.
