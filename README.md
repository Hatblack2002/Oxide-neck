# Oxide-neck

Standalone Java 17 aim-calibration and visual-assistance engine for Android.

This repository is intentionally independent from the Oxide companion repository. It contains no copied source files or runtime dependencies from that project.

## Scope

- Manual camera and crosshair calibration
- Screen-space geometry and target-independent visual distance estimation
- Configurable sensitivity profiles
- Safe, bounded touch-gesture planning for user-directed input
- Resource-interaction timing primitives (user initiated; no unattended multiplayer bot)
- JUnit 4 tests and GitHub Actions verification

This project does not implement automatic player targeting, head/neck hitbox selection, recoil scripts, anti-cheat evasion, or unattended multiplayer farming.

## Build

Requires JDK 17.

```sh
./gradlew clean test jar
```

The build produces a standalone Java library JAR. Android integration is through the documented Java API; this baseline does not claim a device-tested APK.
