# TLS / mTLS sample materials (local / test only)

Generate self-signed PEMs (required before running TLS / Compose TLS examples):

```bash
./scripts/generate-tls-certs.sh
```

Outputs under `configs/tls/certs/` (gitignored except `.gitkeep`):

| File | Purpose |
|---|---|
| `server.crt` / `server.key` | Authorization Server HTTPS identity |
| `client-ca.crt` / `client-ca.key` | CA used to verify client certificates (mTLS) |
| `client.crt` / `client.key` | Sample client certificate for mTLS callers |

## Host YAML vs Compose YAML

| File                                                           | Paths                                        | Use with                      |
|----------------------------------------------------------------|----------------------------------------------|-------------------------------|
| `application-tls.yml` / `application-mtls.yml`                 | `configs/tls/certs/...` (repo-root relative) | `java -jar` from repo root    |
| `application-tls.compose.yml` / `application-mtls.compose.yml` | `/certs/...` (container paths)               | `docker/docker-compose.*.yml` |

## HTTPS only

```bash
./mvnw -ntp -B clean package -DskipTests
java -jar target/rose-authorization-server.jar \
  --config=configs/config.yml \
  --spring.config.additional-location=optional:file:configs/tls/application-tls.yml
```

Issuer becomes `https://localhost:9000`. Clients must trust `server.crt` (or disable verification only in local tests).

## mTLS

```bash
java -jar target/rose-authorization-server.jar \
  --config=configs/config.yml \
  --spring.config.additional-location=optional:file:configs/tls/application-mtls.yml
```

Callers must present `client.crt` / `client.key` signed by `client-ca.crt`.

## Testcontainers

```java
@Container
static RoseAuthorizationServerContainer authServer = new RoseAuthorizationServerContainer()
        .withConfig(Path.of("configs/config.yml"))
        .withTls(Path.of("configs/tls/certs/server.crt"), Path.of("configs/tls/certs/server.key"));
```

For mTLS: `.withMutualTls(serverCrt, serverKey, Path.of("configs/tls/certs/client-ca.crt"))`.

mTLS readiness uses a startup-log probe (not HTTPS `/actuator/health`), because health checks without a client certificate fail when `client-auth=NEED`.

> Do not commit generated materials under `configs/tls/certs/` (ignored by git). Regenerate with
> `./scripts/generate-tls-certs.sh`.
