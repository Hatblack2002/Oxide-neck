# OXIDE-NECK

Independent Android screen-measurement and calibration utility. Java 17; min SDK 26; target SDK 34. No source is imported from the Oxide companion repository.

## Added in 0.2.0
- Android MediaProjection consent flow and foreground capture session. Capture remains local; frames are not uploaded or interpreted for player recognition.
- System-wide, non-touchable reference guide. It is explicitly pass-through and cannot intercept the game's touch input.
- Persistent named calibration profiles in private SharedPreferences, including saved resolution and normalized reference coordinates.
- Profile comparison against the current display resolution.
- Repeatable geometry CPU microbenchmark.
- Existing manual two-point screen-space measurements and local measurement history.

## Deliberate boundary
Automatic character/head/neck recognition, anatomical landmark tracking, opponent tracking, automatic aim selection, and automated game input are not implemented. The guide is manually positioned and does not follow characters. This avoids turning a measurement utility into an automated targeting system.

## Measurement validity
Measurements are pixels and normalized viewport coordinates, not physical anatomical distances or world-space metres. Physical scale requires a known reference; camera/FOV calibration requires camera intrinsics or a known angular reference. The included CPU benchmark is a repeatable microbenchmark, not a device thermal or sustained-frame-rate test.

## Android permissions
- Screen capture: Android's MediaProjection consent prompt is shown each session.
- Floating guide: user must grant "Display over other apps" in Android settings.
- The guide is non-touchable so touches pass through to the foreground app.

## Build
JDK 17 and Android SDK platform 35:
```sh
gradle --no-daemon clean testDebugUnitTest assembleDebug
```
GitHub Actions builds the debug APK and publishes it as `oxide-neck-debug-apk`.
