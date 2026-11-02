# OXIDE-NECK

Standalone Android screen-space calibration utility. This repository is developed independently and does not import code from the Oxide companion app.

## Implemented baseline
- Android application (Java 17, min SDK 26, target SDK 34).
- Manual two-point reference measurement with live pixel distance, signed displacement, angle and viewport-normalized coordinates.
- Median coordinate stabilizer and calibration-error math in the geometry library.
- Resolution scaling and explicit unknown-FOV representation in calibration profiles.
- Bounded gesture measurement model (describes gestures; does not inject touch events).
- Local bounded JSON measurement history.
- JUnit tests and GitHub Actions build/test/APK artifact workflow.

## Measurement model and limitations
All distances are screen-space pixels or normalized viewport units. They are not anatomical distances or world-space metres. Real-world scale requires a known physical reference and camera calibration; FOV estimation requires camera intrinsics or a known angular reference. The app does not infer head/neck anatomy, select player hitboxes, track opponents, automate aiming, inject touch events, or evade game protections.

The current UI is an in-app manual calibration workspace. A system-wide floating overlay and live game-frame capture are not part of this baseline and are not claimed as implemented.

## Build
Requires JDK 17 and Android SDK platform 35:
```sh
gradle clean testDebugUnitTest assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`
