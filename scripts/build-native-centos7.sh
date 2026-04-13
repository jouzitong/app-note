#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

echo "[env] glibc:"
rpm -q glibc || true

echo "[env] java:"
java -version

echo "[env] native-image:"
native-image --version

echo "[build] mvn native:compile (boot module)"
echo "[build] mvn package -Pnative (boot module)"
mvn -pl boot -am -Pnative -DskipTests package

echo "[ok] output:"
ls -la boot/target | sed -n '1,120p'
