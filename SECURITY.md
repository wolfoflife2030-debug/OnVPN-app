# Security Policy

On Vpn is an experimental VPN project and must not be treated as production-secure until its threat model, dependency chain, tunnel behavior, DNS/IPv6 leak resistance, and release process have been independently reviewed.

## Reporting a vulnerability

Do not publish private keys, credentials, tokens, or exploit details in a public issue. Use GitHub's private vulnerability reporting feature when it is enabled for the repository.

## Rules

- Never commit WireGuard private keys.
- Never commit production API tokens.
- Never embed server secrets in the Android client.
- Treat configuration files as sensitive.
- Verify dependencies before production releases.
