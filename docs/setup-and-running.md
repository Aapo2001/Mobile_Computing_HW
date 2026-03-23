# Setup and Running

## Environment requirements

The repository itself shows these build targets and toolchain settings:

- Android Gradle Plugin: `8.13.2`
- Kotlin Android plugin: `1.9.23`
- KSP plugin: `1.9.23-1.0.19`
- `compileSdk = 35`
- `targetSdk = 35`
- `minSdk = 24`
- Java target: `17`

In practical terms, you need:

- A working Android development environment
- JDK 17
- Android SDK 35
- An Android emulator or physical device running API 24 or higher

## Project layout

This is a standard Gradle Android project rooted at `Mobile_Computing_HW` with a single `app` module.

Useful top-level files:

- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `gradlew`
- `gradlew.bat`

## Build the app

From the repository root on Windows:

```powershell
.\gradlew.bat assembleDebug
```

To install directly on a connected device or running emulator:

```powershell
.\gradlew.bat installDebug
```

## Run from Android Studio

1. Open the repository root in Android Studio.
2. Let Gradle sync the project.
3. Select an emulator or connected Android device.
4. Run the `app` configuration.

## Runtime permissions

Different screens request permissions only when needed.

### Notifications

Required for:

- Sensor notifications on Android 13+

### Camera

Required for:

- Live camera preview
- Photo capture
- Face detection while preview is active

### Microphone

Required for:

- Audio recording
- Live speech transcription

### Location

Required for:

- Showing current location on the map
- Enabling my-location map behavior

## Maps configuration

The app configures a Maps API key through the `MAPS_API_KEY` Gradle property.

The build script sets:

- `manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY") ?: "YOUR_API_KEY_HERE"`

The repository's `gradle.properties` currently contains a `MAPS_API_KEY` entry. If you need to replace it for your environment, update that property or provide it as a Gradle property at build time.

## AI and ML model prerequisites

Several features depend on model files, but the repository does not include complete setup instructions for them.

### Chat Gemma model

`GemmaHelper` expects:

- `gemma3-1B-it-int4.task`

The helper attempts to copy this file from app assets into internal app storage when first used.

### Audio Gemma model

`AudioGemmaHelper` references:

- `gemma-3n-E2B-it-int4.litertlm`

The helper currently initializes from:

- `/data/local/tmp/llm/gemma-3n-E2B-it-int4.litertlm`

The repo does not document how that file should be provisioned.

### Face detection model

`FaceDetectorHelper` expects:

- `face_detection_short_range.tflite`

The repository does not include this asset.

## What works without extra model assets

Based on the code, these features should still be broadly usable without the missing ML assets:

- Profile create/edit
- Local chat persistence
- Sensor monitoring and notifications
- Maps and marker placement, assuming Maps setup is valid
- Camera preview and photo capture
- Audio recording and playback
- Live speech transcription through Android speech recognition, when supported by the device
- Video selection and playback

The AI-assisted chat/audio/face-detection parts may fail gracefully or show initialization errors if model files are not available.

## Storage locations used by the app

The code stores generated media in internal app storage:

- Profile image: `filesDir/profile_picture.jpg`
- Photos: `filesDir/photos/`
- Recordings: `filesDir/recordings/`

Room data is stored in the app database:

- Database name: `app_database`

## Network use

The app requests internet access. The repository shows network dependence for at least:

- Google Maps
- Sample online videos used by the Video screen

No general backend API client or remote application server is defined in the repository.

## Validation and testing

The repository contains only minimal example test stubs:

- `ExampleUnitTest.kt`
- `ExampleInstrumentedTest.kt`

There is no larger automated test suite or documented QA workflow in the repo.

## Known repo gaps and limitations

### Missing setup documentation

Not found in repo:

- Full instructions for Gemma model installation
- Full instructions for face detection model setup
- A comprehensive getting-started guide for first-time contributors

### Current implementation caveats visible in code

- Room is configured with `allowMainThreadQueries()`
- Room is configured with `fallbackToDestructiveMigration()`
- The root README does not describe the app beyond a one-line course reference
- Some advanced features may depend on physical hardware or device support

## Suggested first-run path

For the fastest functional smoke test:

1. Build and launch the app.
2. Open Profile and save a username.
3. Open Sensors and verify live accelerometer updates.
4. Open Map and grant location permission.
5. Open Camera and verify preview and photo capture.
6. Open Audio and verify recording and playback.
7. Open Video and play one of the sample URLs.

Then test AI-dependent features only after the required model files are provisioned.
