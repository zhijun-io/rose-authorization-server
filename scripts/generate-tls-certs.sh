#!/usr/bin/env bash
# Generate local-only self-signed PEMs for TLS / mTLS samples.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CERT_DIR="${ROOT}/configs/tls/certs"
mkdir -p "${CERT_DIR}"
cd "${CERT_DIR}"

DAYS="${TLS_DAYS:-825}"
SUBJ_SERVER="${TLS_SERVER_SUBJ:-/CN=localhost}"
SUBJ_CLIENT_CA="${TLS_CLIENT_CA_SUBJ:-/CN=rose-as-client-ca}"
SUBJ_CLIENT="${TLS_CLIENT_SUBJ:-/CN=rose-as-client}"

echo "==> server certificate (HTTPS)"
openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout server.key -out server.crt \
  -days "${DAYS}" -subj "${SUBJ_SERVER}" \
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"

echo "==> client CA"
openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout client-ca.key -out client-ca.crt \
  -days "${DAYS}" -subj "${SUBJ_CLIENT_CA}"

echo "==> client certificate (mTLS caller)"
openssl req -newkey rsa:2048 -nodes \
  -keyout client.key -out client.csr \
  -subj "${SUBJ_CLIENT}"
openssl x509 -req -in client.csr \
  -CA client-ca.crt -CAkey client-ca.key -CAcreateserial \
  -out client.crt -days "${DAYS}"
rm -f client.csr client-ca.srl

chmod 600 server.key client.key client-ca.key
echo "==> done: ${CERT_DIR}"
ls -la "${CERT_DIR}"
