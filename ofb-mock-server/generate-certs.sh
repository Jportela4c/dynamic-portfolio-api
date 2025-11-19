#!/bin/bash
set -e

CERTS_DIR="src/main/resources/certs"
mkdir -p "$CERTS_DIR"

# Clean old certificates
rm -f "$CERTS_DIR"/*.p12 "$CERTS_DIR"/*.cer "$CERTS_DIR"/*.jks "$CERTS_DIR"/*.key "$CERTS_DIR"/*.crt "$CERTS_DIR"/*.csr "$CERTS_DIR"/*.srl

echo "Generating mTLS certificates with CA for OFB Mock Server..."

# Step 1: Generate Certificate Authority (CA)
openssl genpkey -algorithm RSA -out "$CERTS_DIR/ca.key"
openssl req -new -x509 -days 365 -key "$CERTS_DIR/ca.key" -out "$CERTS_DIR/ca.crt" \
  -subj "/C=BR/ST=SP/L=Sao Paulo/O=Portfolio API/OU=IT/CN=portfolio-ca.local"

# Step 2: Generate server certificate signed by CA
openssl genpkey -algorithm RSA -out "$CERTS_DIR/server.key"
openssl req -new -key "$CERTS_DIR/server.key" -out "$CERTS_DIR/server.csr" \
  -subj "/C=BR/ST=SP/L=Sao Paulo/O=Portfolio API/OU=OFB Mock/CN=ofb-mock-server"

# Create server certificate with Subject Alternative Names
cat > "$CERTS_DIR/server.ext" <<EOF
subjectAltName = DNS:localhost,DNS:ofb-mock-server,IP:127.0.0.1
EOF

openssl x509 -req -days 365 -in "$CERTS_DIR/server.csr" \
  -CA "$CERTS_DIR/ca.crt" -CAkey "$CERTS_DIR/ca.key" -CAcreateserial \
  -out "$CERTS_DIR/server.crt" -extfile "$CERTS_DIR/server.ext"

# Step 3: Generate client certificate signed by CA
openssl genpkey -algorithm RSA -out "$CERTS_DIR/client.key"
openssl req -new -key "$CERTS_DIR/client.key" -out "$CERTS_DIR/client.csr" \
  -subj "/C=BR/ST=SP/L=Sao Paulo/O=Portfolio API/OU=Client/CN=portfolio-api-client"

openssl x509 -req -days 365 -in "$CERTS_DIR/client.csr" \
  -CA "$CERTS_DIR/ca.crt" -CAkey "$CERTS_DIR/ca.key" -CAcreateserial \
  -out "$CERTS_DIR/client.crt"

# Step 4: Create server keystore with CA chain
openssl pkcs12 -export -in "$CERTS_DIR/server.crt" -inkey "$CERTS_DIR/server.key" \
  -out "$CERTS_DIR/server.p12" -name server \
  -CAfile "$CERTS_DIR/ca.crt" -caname root -chain \
  -passout pass:changeit

# Step 5: Create client keystore with CA chain
openssl pkcs12 -export -in "$CERTS_DIR/client.crt" -inkey "$CERTS_DIR/client.key" \
  -out "$CERTS_DIR/client.p12" -name client \
  -CAfile "$CERTS_DIR/ca.crt" -caname root -chain \
  -passout pass:changeit

# Step 6: Create server truststore (trusts CA, therefore trusts any client cert signed by CA)
keytool -import -file "$CERTS_DIR/ca.crt" -alias ca \
  -keystore "$CERTS_DIR/truststore.p12" -storepass changeit \
  -storetype PKCS12 -noprompt

# Step 7: Export server certificate for API to trust
cp "$CERTS_DIR/ca.crt" "$CERTS_DIR/server.cer"

echo "Certificates generated successfully in $CERTS_DIR/"
echo "  - ca.crt: Certificate Authority"
echo "  - server.p12: Server keystore (with CA chain)"
echo "  - client.p12: Client keystore (with CA chain, for main API to use)"
echo "  - truststore.p12: Server truststore (trusts CA)"
echo "  - server.cer: CA certificate (for API truststore)"
echo ""
echo "All certificates are signed by CA and include the certificate chain."
