#!/bin/sh
set -e

CERT_DIR=/tmp/kafka-certs
mkdir -p "$CERT_DIR"

# -------------------------------
# Validate required env vars
# -------------------------------
required_vars="
KAFKA_CA_PEM_BASE64
KAFKA_CLIENT_CERT_BASE64
KAFKA_CLIENT_KEY_BASE64
KAFKA_KEYSTORE_PASSWORD
KAFKA_TRUSTSTORE_PASSWORD
"

for var in $required_vars; do
  if [ -z "$(eval echo \$$var)" ]; then
    echo "ERROR: Environment variable $var is not set"
    exit 1
  fi
done

# -------------------------------
# Decode Base64 certs (portable)
# -------------------------------
decode() {
  echo "$1" | tr -d '\n' | base64 -d
}

decode "$KAFKA_CA_PEM_BASE64" > "$CERT_DIR/ca.pem"
decode "$KAFKA_CLIENT_CERT_BASE64" > "$CERT_DIR/client.cert"
decode "$KAFKA_CLIENT_KEY_BASE64" > "$CERT_DIR/client.key"

chmod 600 "$CERT_DIR"/*

# -------------------------------
# Create truststore (overwrite safe)
# -------------------------------
rm -f "$CERT_DIR/truststore.jks"

keytool -import \
  -file "$CERT_DIR/ca.pem" \
  -alias kafka-ca \
  -keystore "$CERT_DIR/truststore.jks" \
  -storepass "$KAFKA_TRUSTSTORE_PASSWORD" \
  -noprompt

# -------------------------------
# Create keystore (PKCS12)
# -------------------------------
rm -f "$CERT_DIR/keystore.p12"

openssl pkcs12 -export \
  -in "$CERT_DIR/client.cert" \
  -inkey "$CERT_DIR/client.key" \
  -out "$CERT_DIR/keystore.p12" \
  -name kafka-client \
  -passout pass:"$KAFKA_KEYSTORE_PASSWORD"

chmod 600 "$CERT_DIR"/*


echo "Kafka certs generated:"
ls -l /tmp/kafka-certs

