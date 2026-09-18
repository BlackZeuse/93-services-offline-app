@echo off
where gradle >nul 2>nul
if errorlevel 1 (
  echo Gradle is not installed. Open this project in Android Studio or install Gradle 9.6.0+ and rerun.
  exit /b 1
)
gradle :app:assembleRelease
echo.
echo APK: %CD%\app\build\outputs\apk\release\app-release.apk
