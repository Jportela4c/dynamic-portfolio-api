#!/bin/bash
set -e

CERTS_DIR="src/main/resources/certs"
mkdir -p "$CERTS_DIR"

# Clean old keystores
rm -f "$CERTS_DIR"/*.p12 "$CERTS_DIR"/*.cer

echo "Generating mTLS certificates for OFB Mock Server..."

# Generate server certificate
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 \
  -validity 365 -keystore "$CERTS_DIR/server.p12" \
  -storepass changeit -keypass changeit \
  -dname "CN=localhost,OU=OFB Mock,O=Portfolio API,L=Sao Paulo,ST=SP,C=BR" \
  -ext "SAN=dns:localhost,dns:ofb-mock-server,ip:127.0.0.1" -storetype PKCS12

# Export server certificate
keytool -exportcert -alias server -keystore "$CERTS_DIR/server.p12" \
  -storepass changeit -file "$CERTS_DIR/server.cer"

# Generate client certificate
keytool -genkeypair -alias client -keyalg RSA -keysize 2048 \
  -validity 365 -keystore "$CERTS_DIR/client.p12" \
  -storepass changeit -keypass changeit \
  -dname "CN=Portfolio API Client,OU=Client,O=Portfolio API,L=Sao Paulo,ST=SP,C=BR" \
  -storetype PKCS12

# Export client certificate
keytool -exportcert -alias client -keystore "$CERTS_DIR/client.p12" \
  -storepass changeit -file "$CERTS_DIR/client.cer"

# Create truststore with client certificate (for server to trust client)
keytool -importcert -alias client -file "$CERTS_DIR/client.cer" \
  -keystore "$CERTS_DIR/truststore.p12" -storepass changeit \
  -storetype PKCS12 -noprompt

echo "Certificates generated successfully in $CERTS_DIR/"
echo "  - server.p12: Server keystore"
echo "  - client.p12: Client keystore (for main API to use)"
echo "  - truststore.p12: Server truststore (trusts client)"
