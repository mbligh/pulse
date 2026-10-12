# Pulse — project design decisions

## Purpose
Low-battery, privacy-oriented Android dead man's handle. Detect meaningful user activity, and eventually alert configurable contacts after a prolonged absence. iOS may follow. This is a safety-related prototype, **not** a certified emergency system.

## Agreed architecture
- Native Kotlin Android initially; native Swift on iOS later if appropriate. Keep evidence schema and alert payload platform-neutral.
- No custom backend/heartbeat server. Phone failure is not evidence of user incapacitation.
- Periodic background evaluation, configurable interval (default one hour), not continuous GPS tracking. WorkManager is inexact and Doze can delay execution.
- Collect activity evidence with timestamps and source; do not interpret missing permission or stale sensor readings as proof of inactivity.
- Signals intended: keyguard unlock; meaningful foreground app use (exclude Pulse); GPS movement; thresholded step count; external power plug/unplug; outgoing or manually answered calls.
- Do not count battery becoming fully charged as an unplug event. Observe plugged-in power source, not charge status. Debounce transitions.
- Avoid Wi-Fi changes as evidence. Fall/impact sensing and Pixel Watch integration are deferred.
- GPS: try cached fix first; if younger than half the check interval use it, otherwise request a fresh fix. Compare to last *accepted* reference using both accuracy radii and a conservative displacement threshold. Reject unreasonably inaccurate fixes.
- Manual HELP: three-second press-and-hold; try a high-accuracy fix for up to 30 seconds; include location, accuracy, fix age, and direct map link, or clearly state unavailable.
- Both automatic and manual alerts eventually go to configurable email/SMS contacts. Explore SMTP and Android SMS/Play policy restrictions. Never embed credentials in repository.
- Future: warning/grace period, delivery retry queue, explicit acknowledgement/all-clear, and possible continuous location sharing via an existing service.
- No automatic alerts should be enabled until delivery and state-machine tests are complete.

## Initial prototype scope (0.1.0)
- Native activity with usage-access and location permission entry points.
- Hourly WorkManager usage-history scan; locally stores last scan and latest observed unlock/foreground event.
- Manual three-second HELP action; 30-second location attempt and Android share chooser. **Requires user to choose/send; not automatic delivery.**
- GitHub Actions builds an unsigned debug APK on pushes to main and PRs affecting build inputs.
- Not yet implemented: GPS displacement detector; step/call/charging detectors; contact configuration; automatic inactivity alert; SMTP/SMS; durable delivery queue; location fallback quality improvements; notification/grace workflow; comprehensive tests.

## Safety/reliability constraints
- A phone that is off, out of power, or without connectivity cannot issue a direct alert.
- WorkManager cannot guarantee hourly execution or precise alert deadlines.
- Android permissions and Play SMS/call-log restrictions may prevent certain signals or automatic SMS.
- A foreground-app resume does not prove human interaction; improve filtering before treating it as strong evidence.
- Never report an alert as delivered merely because a chooser was opened.
- Validate behavior on a Pixel 8, including Doze, reboot, permission revocation, and GPS uncertainty.
