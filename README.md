# Pulse

Low-power Android inactivity-monitoring prototype.

## Build
Requires JDK 17, Android SDK 35, Gradle 8.11.1 (CI provisions Gradle; no wrapper is committed yet).

```sh
gradle :app:assembleDebug
```

CI builds a debug APK artifact on relevant pushes and pull requests.

## Current behavior
Launch Pulse, grant usage access and location permission, and enable hourly scans. Hold HELP for three seconds to prepare a manual help message with a location attempt (up to 30 seconds). The Android share chooser requires you to select and send it yourself.

**This is not yet an unattended emergency alert system.** Automatic alerts, contacts, and additional evidence sources are not implemented. See [project.md](project.md).
