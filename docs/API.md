# On Vpn Control API v0.1

GET /v1/servers
POST /v1/devices/enroll
POST /v1/devices/{id}/configs
GET /v1/health

The client generates its WireGuard private/public keypair locally. The API receives only the public key and device metadata needed for enrollment. Configuration responses are short-lived and versioned.
