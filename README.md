![](./_assets/anisChildApp-asset.jpg)

# ANIS Child App

Child-side Android application for ANIS — a parental control and child care monitoring system.
This app provides screen time management, app blocking, location tracking, content filtering, AI-powered monitoring, and gamified tasks & rewards.

## Tech Stack

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-1572B6?logo=hilt&logoColor=white)
![Room](https://img.shields.io/badge/Room-003B57?logo=room&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-48B983?logo=retrofit&logoColor=white)
![OkHttp](https://img.shields.io/badge/OkHttp-006F48?logo=okhttp&logoColor=white)
![Kotlinx Serialization](https://img.shields.io/badge/Kotlinx%20Serialization-7F52FF?logo=kotlin&logoColor=white)
![CameraX](https://img.shields.io/badge/CameraX-FF6D00?logo=camerax&logoColor=white)
![ML Kit](https://img.shields.io/badge/ML%20Kit-FF6F00?logo=mlkit&logoColor=white)
![ONNX Runtime](https://img.shields.io/badge/ONNX%20Runtime-005CED?logo=onnx&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?logo=firebase&logoColor=black)
![WorkManager](https://img.shields.io/badge/WorkManager-4285F4?logo=android&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-7F52FF?logo=kotlin&logoColor=white)

---

## Getting Started

1. Clone the repository:

```bash
git clone https://github.com/ANIS-Solutions/Child-app.git
cd Child-app
```

2. Open the project in **Android Studio** (Ladybug or later recommended).

3. Place your `google-services.json` from Firebase in the `app/` directory.

4. Build the project:

```bash
./gradlew assembleDebug
```

5. Install on your device:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Development

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires keystore env vars)
./gradlew assembleRelease

# Run tests
./gradlew test

# Clean build
./gradlew clean
```

---

## Bug Reports

If you find any issues, please open an Issue in the repository with detailed steps to reproduce the problem.

_Bye! ... Bye!_
