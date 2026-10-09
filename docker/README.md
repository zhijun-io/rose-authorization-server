# Docker Compose examples

Prerequisites: Docker, and (for TLS) local PEMs from `./scripts/generate-tls-certs.sh`.

## Plain HTTP

```bash
docker compose -f docker/docker-compose.yml up
```

- Issuer: http://localhost:9000
- Config: `../configs/config.yml`

## HTTPS

```bash
./scripts/generate-tls-certs.sh
docker compose -f docker/docker-compose.tls.yml up
```

- Issuer: https://localhost:9000
- Trust `../configs/tls/certs/server.crt` in the client (or use test-only insecure trust)

## mTLS

```bash
./scripts/generate-tls-certs.sh
docker compose -f docker/docker-compose.mtls.yml up
```

Probe with client certificate:

```bash
curl -fk \
  --cacert configs/tls/certs/server.crt \
  --cert configs/tls/certs/client.crt \
  --key configs/tls/certs/client.key \
  https://localhost:9000/actuator/health
```

> Image must be pullable (`ghcr.io/zhijun-io/rose-authorization-server:latest`). If the package is private,
> `docker login ghcr.io` first, or build locally and retag.
