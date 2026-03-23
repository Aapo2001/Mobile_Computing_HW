# Mobile Computing Homework App

This repository contains a multi-feature Android application built for a Mobile Computing course. The app is implemented in Kotlin with Jetpack Compose and groups several platform features into one project so they can be demonstrated from a single navigation shell.

The current implementation includes:

- A local chat screen with Room-backed message history and optional Gemma responses
- A profile flow for saving a username and profile image in local storage
- Accelerometer monitoring with a foreground service and notifications
- Google Maps integration with current-location support and map markers
- Camera preview, photo capture, and real-time face detection overlay
- Audio recording, playback, speech transcription, and optional Gemma-based processing
- Video playback for local files and sample remote media

`docs/` is the main documentation set for this repository. It reflects the current code structure more closely than `docs_2/`, which appears to be an older or alternate draft.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room
- Google Maps Compose
- CameraX
- Media3 / ExoPlayer
- Android SpeechRecognizer
- MediaPipe GenAI and Vision

## Project Layout

```text
Mobile_Computing_HW/
|- app/
|  |- src/main/java/com/example/myapplication/
|  |  |- database/       Room entities, DAOs, and database singleton
|  |  |- helper/         AI/ML and speech-recognition helpers
|  |  |- navigation/     Route definitions and app bars
|  |  |- notification/   Notification channel and builders
|  |  |- repository/     Repository wrappers over Room and file storage
|  |  |- service/        Foreground accelerometer service
|  |  |- ui/             Compose screens grouped by feature
|  |  |- MainActivity.kt Activity entry point and app composition root
|  |  `- SampleData.kt   Fallback sample chat messages used in previews/demo state
|  `- src/main/res/      Android resources, themes, strings, drawables, launcher assets
|- docs/                 Main hand-written project documentation
|- gradle/               Gradle wrapper files
|- build.gradle.kts      Root Gradle configuration
`- app/build.gradle.kts  Android app module build configuration
```

## Feature Summary

### Chat

The Home screen shows a conversation UI backed by the local Room database. User messages are stored immediately. If the Gemma helper initializes successfully, the same screen also requests a generated response and stores it as another message.

### Profile

The profile flow stores a single `UserProfile` row in Room and copies the selected profile image into internal app storage. That same stored image path is then reused by the chat UI for the user avatar.

### Sensors and Notifications

The sensor feature binds the UI to a foreground `SensorService` that listens to the accelerometer, detects shake events, and posts a notification every third shake. Notification taps deep-link the user back into the sensor screen.

### Map

The map screen requests location permission when needed, reads the device's last known location through the fused location provider, and lets the user add markers by tapping the map.

### Camera

The camera screen combines CameraX preview, still capture, and `ImageAnalysis` with MediaPipe face detection. Detected faces are rendered on top of the preview with a Compose canvas overlay.

### Audio

The audio screen supports local recording and playback, live transcription through Android speech recognition, and optional Gemma-assisted processing for either recorded audio or transcribed text.

### Video

The video screen uses Media3 / ExoPlayer to play content selected from device storage or a set of sample online MP4 URLs.

## Data and Storage

The app is currently local-first:

- Messages are stored in the Room `messages` table
- The user profile is stored in the Room `user_profile` table
- Profile images are copied into `filesDir/profile_picture.jpg`
- Captured photos are stored in `filesDir/photos/`
- Audio recordings are stored in `filesDir/recordings/`

There is no backend API or cloud persistence layer in the repository.

## Permissions

The manifest declares permissions for:

- Notifications
- Foreground service execution
- Camera
- Microphone
- Fine and coarse location
- Media reads for images, audio, and video
- Internet access

Most permissions are requested from the relevant feature screen instead of at application startup.

## AI / ML Assets

Several features depend on local model files or additional configuration:

- Chat Gemma helper expects `gemma3-1B-it-int4.task`
- Face detection expects `face_detection_short_range.tflite`
- Audio Gemma helper references `gemma-3n-E2B-it-int4.litertlm`

The repository contains code for these integrations, but first-run setup still depends on having the correct model files available in the expected locations.

## Build Requirements

From the current Gradle configuration:

- `minSdk = 24`
- `targetSdk = 35`
- `compileSdk = 35`
- Java 17 / JVM target 17
- Android Gradle Plugin `8.13.2`
- Kotlin `1.9.23`

## Build and Run

From the repository root on Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

You can also open the project in Android Studio and run the `app` configuration on an emulator or physical device.

## Documentation Map

- [`docs/README.md`](docs/README.md): documentation index
- [`docs/overview.md`](docs/overview.md): application overview and features
- [`docs/architecture.md`](docs/architecture.md): structure, data flow, and integrations
- [`docs/setup-and-running.md`](docs/setup-and-running.md): setup requirements and launch steps

## Current Limitations

- AI-dependent features require model assets that are not fully documented in-repo
- Some screens are better suited to physical devices than emulators
- The test suite currently contains only placeholder examples
- Room is configured with `allowMainThreadQueries()` and destructive fallback migrations, which is acceptable for coursework but not ideal for production
