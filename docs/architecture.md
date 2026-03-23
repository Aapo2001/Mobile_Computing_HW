# Architecture

## High-level structure

The application is a single Android app module built around Jetpack Compose. The major layers visible in the repository are:

- Activity and app shell
- Navigation
- Feature screens and ViewModels
- Repositories and local persistence
- Device/service/helper integrations

## App shell

`MainActivity` is the only activity defined in the manifest. It is responsible for:

- Installing the Android splash screen
- Reading intent extras used for sensor-notification deep linking
- Creating the `UserProfileRepository` and `MessageRepository`
- Launching the Compose app entry point

The activity forwards startup routing information into `MyAppNavHost`, so a notification tap can send the user directly to the sensor screen.

## Navigation

Navigation is implemented with Compose Navigation in `MyAppNavHost`.

### Registered destinations

- `home`
- `profile`
- `edit_profile`
- `sensor`
- `map`
- `video`
- `camera`
- `audio`

### Navigation patterns

- Bottom navigation provides quick access to the main feature tabs
- The Home screen opens Profile from its top bar
- The Profile screen navigates to the edit-profile flow
- Notification taps start the app with `SensorDest.route`

## UI layer

Each screen is implemented as a Compose UI function, usually backed by a feature-specific ViewModel. The ViewModels own UI state and mediate calls into repositories, services, or helper classes.

### Feature ViewModels

- `HomeViewModel`
- `ProfileViewModel`
- `ProfileEditViewModel`
- `SensorViewModel`
- `MapViewModel`
- `VideoViewModel`
- `CameraViewModel`
- `AudioViewModel`

### Common pattern

The general implementation pattern is:

1. Compose screen obtains a ViewModel with a custom factory.
2. ViewModel exposes state through `StateFlow` or derived flow combinations.
3. UI collects state with `collectAsState()`.
4. User actions call ViewModel methods.
5. ViewModel updates repositories, helpers, or services.
6. Updated state flows back into Compose for recomposition.

## Persistence layer

The app uses Room for its structured local data.

### Database

`AppDatabase` defines two entities:

- `UserProfile`
- `MessageEntity`

It also exposes:

- `UserProfileDao`
- `MessageDao`

### Repository layer

Two repositories wrap the DAOs:

- `UserProfileRepository`
- `MessageRepository`

#### `UserProfileRepository`

Responsibilities:

- Expose the current user profile as a `Flow`
- Save the chosen profile image into internal app storage
- Insert or update the single saved profile row

#### `MessageRepository`

Responsibilities:

- Read persisted chat messages
- Insert new user or model-generated messages
- Delete one message or all messages
- Return message counts and one-shot query results

### Storage behavior

- Profile images are copied into `context.filesDir`
- Camera photos are stored in `filesDir/photos`
- Audio recordings are stored in `filesDir/recordings`

This means media is private to the app unless exported elsewhere.

## Services and helper classes

The app delegates platform-specific or ML-specific behavior into separate helper/service classes.

### `SensorService`

Purpose:

- Register an accelerometer listener
- Detect shake events
- Maintain shake count while service is running
- Promote itself to a foreground service
- Trigger notifications through `NotificationHelper`

The service exposes a binder so `SensorViewModel` can observe values and update the screen.

### `NotificationHelper`

Purpose:

- Create the notification channel
- Show shake-event notifications
- Build the persistent foreground-service notification

Notification taps reopen `MainActivity` with extras that identify the sensor destination and current shake count.

### `GemmaHelper`

Purpose:

- Initialize a MediaPipe LLM inference engine for the chat flow
- Copy a Gemma model from assets into internal storage if needed
- Generate model responses for chat messages

Repo note:

- The code expects `gemma3-1B-it-int4.task`
- The setup instructions for that asset are not documented in the repo

### `AudioGemmaHelper`

Purpose:

- Initialize a separate Gemma-3n helper for audio workflows
- Attempt text and audio-based inference
- Process speech-derived text or audio bytes

Repo note:

- The code references `gemma-3n-E2B-it-int4.litertlm`
- The helper currently points at `/data/local/tmp/llm/...`
- End-to-end setup documentation for that model is not included in the repo

### `AudioTranscriptionHelper`

Purpose:

- Wrap Android `SpeechRecognizer`
- Expose transcription state as flows
- Start and stop live recognition
- Emit partial and final transcription results

### `FaceDetectorHelper`

Purpose:

- Initialize a MediaPipe face detector
- Process live camera frames
- Return face detection results and errors to the camera UI

Repo note:

- The code expects a `face_detection_short_range.tflite` model asset
- That asset is not present in the repository

## Maps and location

`MapViewModel` uses the fused location provider from Google Play Services. It:

- Checks location permission state
- Updates map properties when permission changes
- Fetches the last known location
- Stores user-added marker positions in UI state

The map UI itself is rendered with `maps-compose`.

## Media stack

### Camera

The camera flow combines:

- `ProcessCameraProvider`
- `Preview`
- `ImageCapture`
- `ImageAnalysis`
- `PreviewView`
- `FaceDetectorHelper`

This lets the app show a live preview, run per-frame face detection, and save still captures.

### Audio

The audio flow combines:

- `MediaRecorder` for capture
- `MediaPlayer` for playback
- `SpeechRecognizer` for live transcription
- `AudioGemmaHelper` for optional AI processing

### Video

The video flow uses `ExoPlayer` from Media3 and supports:

- Local content chosen through `GetContent`
- Sample remote MP4 URLs
- Play/pause/stop
- Slider-based seeking

## Data flow examples

### Chat flow

1. User submits text in `HomeScreen`.
2. `HomeViewModel` writes the user message through `MessageRepository`.
3. If `GemmaHelper` initialized successfully, the ViewModel requests a generated reply.
4. The generated reply is also inserted through `MessageRepository`.
5. The Room-backed message flow updates the UI.

### Sensor flow

1. User starts the sensor service from `SensorScreen`.
2. `SensorViewModel` starts and binds to `SensorService`.
3. The service registers the accelerometer listener.
4. Incoming sensor events update shake count and live values.
5. Every third shake triggers a notification.
6. The bound callback updates screen state through the ViewModel.

### Profile flow

1. User selects an image and enters a username.
2. `ProfileEditViewModel` calls `UserProfileRepository`.
3. The repository copies the image into internal storage and updates Room.
4. `ProfileViewModel` and `HomeViewModel` observe the new profile through flows.
5. Profile UI and chat avatar rendering update automatically.

## Current implementation notes

These are useful architectural notes visible in code:

- `AppDatabase` uses `allowMainThreadQueries()`
- `AppDatabase` uses `fallbackToDestructiveMigration()`
- Feature state is managed locally inside ViewModels rather than through a shared dependency-injection container
- There is no backend API client or server integration visible in the repository
- Instrumentation and unit tests exist only as minimal example test files

## Project structure

Key directories:

- `app/src/main/java/com/example/myapplication`: app code
- `app/src/main/res`: resources, themes, strings, drawables, launcher assets
- `app/src/test`: local unit test stub
- `app/src/androidTest`: instrumentation test stub

Within the main Java/Kotlin package:

- `ui/`: screens, feature UIs, ViewModels, theming
- `navigation/`: destinations and navigation scaffolding
- `database/`: Room entities, DAOs, database
- `repository/`: repository wrappers
- `service/`: foreground sensor service
- `notification/`: notification helper
- `helper/`: AI, camera, and transcription helpers
