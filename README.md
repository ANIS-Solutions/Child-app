# ANIS Child App

A child monitoring Android application for tracking location and communicating with parent devices.

## Features

- **Device Pairing**: Scan QR code from parent app to pair devices
- **Location Tracking**: Automatic hourly location updates when enabled
- **Manual Location**: Send location on-demand with button
- **Offline Support**: Location data stored locally when offline
- **Dark Mode**: Toggle between light and dark themes
- **Secure Storage**: Tokens stored in encrypted preferences

## Setup

1. Install on Android device (API 24+)
2. Grant camera and location permissions
3. Scan QR code from parent app
4. Enable location monitoring in settings

## Tech Stack

- Kotlin + Jetpack Compose
- CameraX + ML Kit (QR scanning)
- WorkManager (background tasks)
- Room (local database)
- Retrofit (networking)
- EncryptedSharedPreferences (security)