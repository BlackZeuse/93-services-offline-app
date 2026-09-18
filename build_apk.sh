#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if command -v gradle >/dev/null 2>&1; then
  gradle :app:assembleDebug --no-daemon
  echo "APK: $(pwd)/app/build/outputs/apk/debug/app-debug.apk"
else
  echo "Gradle is not installed. Open the project in Android Studio or install Gradle 9.6.0, then run: gradle :app:assembleDebug"
  exit 1
fi
