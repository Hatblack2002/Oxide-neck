# OXIDE-NECK

Independent Android screen-measurement and calibration utility. Java 17; min SDK 26; target SDK 34; version 0.2.0.

## Implemented
- Explicit Android MediaProjection consent and a foreground capture session.
- User-triggered still-frame archiving to app-private storage, with a 30-frame retention limit and in-app preview. No continuous recording or remote upload.
- Manual two-point measurement with pixel distance, angle, deltas and normalized viewport coordinates.
- Bounded local measurement history (500 records), first/latest sample comparison and JSON text export.
- Persistent calibration profile create/read/update/delete, active-profile selection, resolution scaling and profile-to-profile coordinate comparison.
- Floating, non-touchable overlay that uses the active profile's two normalized reference points. It cannot intercept game touches.
- Geometry and calibration-profile unit tests, plus a repeatable CPU microbenchmark.

## Deliberate boundary
Automatic player or anatomical landmark recognition, opponent tracking, automatic aim selection and automated game input are not implemented. The app is a manual measurement and calibration instrument.

## Measurement limits
Measurements are screen-space pixels and normalized coordinates. They are not physical anatomical distances or world-space metres. Physical scale requires a known reference. FOV remains unknown unless independently calibrated. The CPU microbenchmark is not a thermal, GPU, FPS or sustained-load test.

## Permissions
- Screen capture: Android MediaProjection consent for every session.
- Floating guide: user grants Display over other apps.
- The guide is non-touchable and passes touch input through to the foreground app.

## Build
Requires JDK 17 and Android SDK platform 35 / build-tools 35.0.0:
```sh
gradle --no-daemon clean testDebugUnitTest assembleDebug
```
GitHub Actions runs unit tests, assembles the debug APK and publishes `oxide-neck-debug-apk`.
