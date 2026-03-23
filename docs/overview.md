# App Overview

## What this app is

This repository contains an Android application built with Jetpack Compose and organized as a single `app` module. The app bundles several mobile-computing demonstrations into one project: messaging, profile management, sensors, maps, camera, audio, and video.

Based on the root README and the implementation, the app appears to be coursework for a mobile computing class rather than a production application with a narrowly defined business domain.

## Primary purpose

The app showcases how one Android project can combine:

- Compose-based UI and navigation
- Local persistence with Room
- Foreground services and notifications
- Google Maps and device location
- CameraX and on-device vision processing
- Audio recording and speech recognition
- Media playback with Media3
- On-device AI or ML helpers via MediaPipe

## Intended audience

The most likely audience, based on repository evidence, is:

- A student building a mobile computing assignment
- An instructor or teaching assistant reviewing Android feature work
- A developer exploring how several Android platform APIs can be combined in one sample app

## Main screens

### Home / Chat

The Home screen is the default entry point. It:

- Displays a conversation view
- Loads persisted messages from the local Room database
- Reads the saved user profile so the user's avatar can appear in chat
- Lets the user send new messages
- Attempts to generate a Gemma response when the Gemma helper is initialized successfully

If there are no saved messages, the UI falls back to bundled sample conversation data.

### Profile

The Profile area has two screens:

- A profile display screen showing username and profile image
- An edit/setup screen where the user can choose a photo and save a username

The selected profile image is copied into internal app storage and the profile record is stored in Room.

### Sensors

The Sensors screen demonstrates accelerometer monitoring and notifications. It:

- Shows notification permission state
- Displays live accelerometer values
- Tracks shake count
- Starts and stops a foreground service for background monitoring
- Resets the shake counter

The foreground service posts a notification every third detected shake and can deep-link back into the sensor screen.

### Map

The Map screen demonstrates location and map rendering. It:

- Requests fine/coarse location permissions
- Reads the last known location through the fused location provider
- Displays the user's current position when available
- Lets the user tap the map to add markers
- Includes a floating action button to move the camera to the current location

### Camera

The Camera screen demonstrates capture and on-device face detection. It:

- Requests camera permission
- Shows a live camera preview using CameraX
- Supports front/back camera switching
- Captures still photos into internal app storage
- Shows a live face overlay and face count from `FaceDetectorHelper`
- Lets the user browse, select, preview, and delete saved photos

### Audio

The Audio screen demonstrates recording, playback, transcription, and AI-assisted processing. It:

- Requests microphone permission
- Records audio clips to internal app storage
- Lists existing recordings
- Plays and deletes recordings
- Uses Android speech recognition for live transcription
- Uses `AudioGemmaHelper` for Gemma-3n-based audio transcription and text processing when available

### Video

The Video screen demonstrates media playback. It:

- Lets the user choose a local video file through the content picker
- Plays selected media using ExoPlayer / Media3
- Supports play, pause, stop, and seek interactions
- Includes sample remote video URLs for quick testing

## Navigation model

The application uses Compose Navigation. The bottom navigation bar includes:

- Chat
- Sensors
- Map
- Video
- Camera
- Audio

The Profile screen is also reachable from the Home screen's top-bar action, and the edit-profile screen is reachable from Profile.

## Permissions used by the app

The manifest declares support for:

- `POST_NOTIFICATIONS`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_SPECIAL_USE`
- `CAMERA`
- `RECORD_AUDIO`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `READ_EXTERNAL_STORAGE` (up to API 32)
- `READ_MEDIA_VIDEO`
- `READ_MEDIA_IMAGES`
- `READ_MEDIA_AUDIO`
- `INTERNET`

Not every permission is needed at startup; most are requested in the feature screens when needed.

## Local-only vs external dependencies

### Clearly local or on-device

- Room database for profile and messages
- Internal file storage for photos and recordings
- Accelerometer monitoring
- Notification delivery
- Camera preview and capture
- Speech recognition integration
- Media playback

### Requires additional services or configuration

- Google Maps requires a valid Maps API key
- Gemma and face detection features require model files referenced by code

## Known documentation gaps from the repo

The following information is not fully documented in the repository:

- How the AI model files should be obtained
- Where all required model assets should be placed for a clean first-time setup
- Whether the intended runtime target is emulator, physical device, or both for every feature
- Any release or distribution strategy beyond local development builds
