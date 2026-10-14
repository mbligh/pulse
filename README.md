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

## Activity diagnostics

After granting Usage Access, tap **Scan now** to inspect the most recent usage events. The app records up to 100 qualifying unlock and foreground-app events, with timestamps and package names, plus the last scan and last detected activity. Hourly background scans remain enabled only after pressing **Start hourly activity checks**. No unattended alerts are sent.

## Google Play internal testing

The separate `.github/workflows/play-bundle.yml` workflow builds a signed Android App Bundle and uploads successful builds to the Google Play **internal** track, matching Trail's release pattern. It is inactive until Pulse-specific signing secrets are configured:

- `PULSE_UPLOAD_KEYSTORE_BASE64`: base64-encoded Pulse upload PKCS12 keystore.
- `PULSE_UPLOAD_PASSWORD`: password for the keystore and its private key.
- `PULSE_PLAY_SERVICE_ACCOUNT_JSON`: Play Developer API service-account JSON authorized for the Pulse app.

To generate a new, dedicated upload key (do not reuse Trail's), run:

```sh
keytool -genkeypair -v -storetype PKCS12 -keystore pulse-upload.p12 -alias pulse-upload -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 pulse-upload.p12
```

On macOS use `base64 < pulse-upload.p12 | tr -d '\\n'` if `-w0` is unavailable. Store the key securely; keep it out of Git. Create the `com.mbligh.pulse` app in Play Console, enable Play App Signing, enroll the upload certificate, and grant the service account permission to view the app and publish to testing tracks. Initial Play Console requirements may need manual completion before API uploads work.

The workflow assigns a new versionCode on each run. Missing signing secrets skip the bundle job; missing Play credentials still allow a signed bundle artifact but skip publishing. The debug APK and GitHub Releases workflow remain separate.
