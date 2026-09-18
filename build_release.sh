#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
if [ ! -f ./gradlew ]; then
  echo "Gradle wrapper is not present. Open this project in Android Studio or run with Gradle 9.6 installed."
  exit 2
fi
./gradlew :app:assembleRelease --no-daemon
APK="$ROOT/app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK" ]; then
  echo "APK was not produced: $APK" >&2
  exit 3
fi
mkdir -p "$ROOT/delivery"
cp "$APK" "$ROOT/delivery/93_Services_Offline_App.apk"
echo "Created $ROOT/delivery/93_Services_Offline_App.apk"
