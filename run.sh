#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
mkdir -p build/classes
javac -d build/classes src/anagrafe/*.java
java -cp build/classes Main "$@"
