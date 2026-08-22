#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
EXTENSION_ID="${1:-}"
SERVER_BINARY="$ROOT_DIR/SERVER/target/release/anagrafe-server"
HOST_DIR="$HOME/Library/Application Support/Google/Chrome/NativeMessagingHosts"
HOST_MANIFEST="$HOST_DIR/anagrafe.server.json"

if [[ "$(uname -s)" != "Darwin" ]]; then
  printf '%s\n' "Questo script deve essere eseguito su macOS, non nel container Linux."
  exit 1
fi

if ! command -v brew >/dev/null 2>&1; then
  printf '%s\n' "Homebrew non trovato. Installalo da https://brew.sh e rilancia lo script."
  exit 1
fi

for command_name in java javac node npm cargo; do
  if ! command -v "$command_name" >/dev/null 2>&1; then
    printf 'Comando mancante: %s\n' "$command_name"
    exit 1
  fi
done

JAVA_MAJOR="$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p')"
if [[ -z "$JAVA_MAJOR" || "$JAVA_MAJOR" -lt 17 ]]; then
  printf '%s\n' "Serve JDK 17 o superiore."
  exit 1
fi

printf '%s\n' "[1/4] Compilo Java..."
mkdir -p "$ROOT_DIR/build/classes"
javac -d "$ROOT_DIR/build/classes" "$ROOT_DIR"/src/*.java

printf '%s\n' "[2/4] Compilo il server Rust..."
(cd "$ROOT_DIR/SERVER" && cargo build --release)
chmod +x "$SERVER_BINARY"

printf '%s\n' "[3/4] Compilo l'estensione TypeScript..."
npm install --prefix "$ROOT_DIR/Anagr@Fe_Extension"
npm run --prefix "$ROOT_DIR/Anagr@Fe_Extension" build

if [[ -z "$EXTENSION_ID" ]]; then
  printf '%s\n' "[4/4] Compilazione completata."
  printf '%s\n' "Importa Anagr@Fe_Extension/chrome-edge-opera in chrome://extensions."
  printf '%s\n' "Poi rilancia questo script passando l'ID mostrato da Chrome:"
  printf '  %s %s\n' "$0" "ID_ESTENSIONE"
  exit 0
fi

if [[ ! "$EXTENSION_ID" =~ ^[a-z]{32}$ ]]; then
  printf '%s\n' "ID estensione non valido: Chrome usa 32 lettere minuscole."
  exit 1
fi

printf '%s\n' "[4/4] Installo il Native Messaging host..."
mkdir -p "$HOST_DIR"
printf '%s\n' \
  '{' \
  '  "name": "anagrafe.server",' \
  '  "description": "Anagr@Fe local integration host",' \
  "  \"path\": \"$SERVER_BINARY\"," \
  '  "type": "stdio",' \
  '  "allowed_origins": [' \
  "    \"chrome-extension://$EXTENSION_ID/\"" \
  '  ]' \
  '}' > "$HOST_MANIFEST"

printf '%s\n' "Installazione completata."
printf '%s\n' "Avvia Java con: java -cp build/classes Main"
printf '%s\n' "Poi ricarica l'estensione in chrome://extensions e apri il popup."
