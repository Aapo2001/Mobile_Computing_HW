# Mobile Computing App Documentation

This folder contains detailed documentation for the Android application in this repository.

## Document map

- [Overview](./overview.md): product summary, supported features, user-facing behavior, and permissions.
- [Architecture](./architecture.md): application structure, navigation, data flow, persistence, and integration points.
- [Setup and Running](./setup-and-running.md): prerequisites, build/run steps, configuration, and known repo gaps.

## App at a glance

This project is an Android application built with Jetpack Compose. It combines several mobile-computing features inside one app:

- Chat with local message persistence and optional Gemma-powered replies
- User profile editing with a locally stored profile photo
- Accelerometer monitoring with a foreground service and notifications
- Google Maps with current-location support and user-added markers
- Camera preview, photo capture, and face detection overlay
- Audio recording, playback, speech transcription, and Gemma-based audio processing
- Video playback for both local and sample remote media

## Repo-backed summary

From the codebase, this appears to be a course project or homework app used to demonstrate multiple Android platform capabilities inside a single codebase. The root README, screen labels, dependencies, and implementation structure all support that interpretation.

## Important repo gaps

Some features depend on assets or setup instructions that are referenced in code but not present in the repository:

- Gemma model file for chat: `gemma3-1B-it-int4.task`
- Gemma model file for audio: `gemma-3n-E2B-it-int4.litertlm`
- Face detection model asset: `face_detection_short_range.tflite`
- End-user setup guidance for those model files

Where relevant, those gaps are called out in the linked documents.
