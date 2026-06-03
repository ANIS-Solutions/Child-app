# ANIS Child Application Documentation Plan

## Objective

Create a comprehensive technical documentation chapter for the ANIS Child Application. The chapter should focus on the Android child-side application, its architecture, implementation, core features, security mechanisms, and its role within the ANIS ecosystem. The writing style should be a hybrid of academic project documentation and software design documentation, suitable for a graduation project, technical report, or software design document.

---

# Chapter: ANIS Child Application

## 1. Introduction

### 1.1 Overview

- Introduce the ANIS Child Application.
- Explain its role within the ANIS ecosystem.
- Describe how it works alongside the Parent Application, Backend Services, and AI Services.
- Explain that it is installed on the child's Android device and serves as the enforcement and protection layer of the platform.

### 1.2 Purpose

- Protect children from harmful digital content.
- Enforce parental control policies.
- Promote healthy digital habits.
- Encourage educational engagement through quizzes and rewards.
- Balance child safety with privacy.

### 1.3 Scope

- Define the responsibilities of the child application.
- Explain what functions are performed locally on the device and what functions rely on backend services.

### 1.4 Requirements Analysis

#### Functional Requirements

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-01 | Device pairing via QR code scanning | High | Implemented |
| FR-02 | Periodic location tracking and reporting | High | Implemented |
| FR-03 | Installed applications discovery and reporting | High | Implemented |
| FR-04 | Real-time screen content capture | High | Implemented |
| FR-05 | OCR text detection on captured frames | High | Implemented |
| FR-06 | Banned word detection with early exit | High | Implemented |
| FR-07 | ONNX-based image content classification | High | Implemented |
| FR-08 | Blur overlay for blocked content | High | Implemented |
| FR-09 | FCM push notification handling | High | Implemented |
| FR-10 | Child profile sync from backend | Medium | Implemented |
| FR-11 | Dark mode theme toggle | Low | Implemented |
| FR-12 | In-app debug logging | Low | Implemented |
| FR-13 | Session lifecycle management with device stats | High | Implemented |
| FR-14 | Session export (XLSX + ZIP) | Medium | Implemented |
| FR-15 | Quiz assignment and completion | High | Planned |
| FR-16 | Reward accumulation and redemption | High | Planned |
| FR-17 | Accessibility Service-based app usage tracking | High | Planned |
| FR-18 | Screen time limits and scheduling | High | Planned |
| FR-19 | Application blocking and time limits | High | Planned |
| FR-20 | Parent policy synchronization | High | Planned |

#### Non-Functional Requirements

| ID | Requirement | Target |
|---|---|---|
| NFR-01 | AI analysis latency per frame | < 1000 ms |
| NFR-02 | Memory footprint (sustained) | < 250 MB PSS |
| NFR-03 | Battery drain rate | < 20% per hour |
| NFR-04 | Offline operation capability | 24+ hours cached policies |
| NFR-05 | Monitoring service uptime | 99.9% (excluding device reboot) |
| NFR-06 | Secure token storage | AES-256-GCM encrypted |
| NFR-07 | API communication | TLS 1.3 |
| NFR-08 | Banned word detection accuracy | > 95% |
| NFR-09 | Threat classification false positive rate | < 5% |
| NFR-10 | Data retention | Auto-cleanup after 7 days |

---

## 2. Technology Selection and Justification

### 2.1 Development Technology Stack

- Kotlin
- Android SDK
- Jetpack Compose (UI)
- Coroutines & Flow
- Hilt (Dependency Injection)
- WorkManager
- Foreground Services
- Accessibility Service
- MediaProjection API
- Retrofit + OkHttp
- Room Database
- DataStore / EncryptedSharedPreferences
- Firebase Cloud Messaging
- ONNX Runtime (AI inference)
- Google ML Kit (OCR)

### 2.2 Why Native Android Instead of Flutter

The decision to implement the ANIS Child Application as a native Android application rather than a cross-platform Flutter alternative was informed by empirical performance data collected from a controlled experiment comparing both implementations of the ANIS AI content moderation pipeline. The experiment, documented in the ANIS research paper [1], tested both implementations under identical conditions (15.5-minute sessions, ~900 frames each, same ONNX model and ML Kit pipeline) and provided the following evidence.

---

#### Deep Android System Integration

ANIS requires direct interaction with Android system services that are inaccessible or restricted in cross-platform frameworks:

- **Accessibility Service**: Critical for monitoring app usage, detecting foreground app changes, and intercepting navigation for restriction enforcement. Requires direct Android Service registration and lifecycle management.
- **MediaProjection API**: Screen capture for content analysis requires `MediaProjectionManager` and `VirtualDisplay` — both platform-specific APIs that would require custom plugin development in Flutter.
- **UsageStatsManager**: Application usage tracking requires system-level permission and direct querying of system usage statistics.
- **KeyStore / EncryptedSharedPreferences**: Security-sensitive token storage relies on Android hardware-backed keystore, accessible only through Android-specific crypto APIs.
- **System Overlays**: The blur overlay for blocked content uses `TYPE_APPLICATION_OVERLAY` window type, requiring direct `WindowManager` interaction.

In a Flutter implementation, all of the above would require custom native plugins communicating through MethodChannel — introducing serialization overhead of ~114 µs per call compared to ~0.5 µs for direct native invocations [2], a difference of over two orders of magnitude.

---

#### Memory Consumption (Critical Differentiator)

The most significant empirical difference between the two implementations is memory usage:

| Metric | Native Android | Flutter | Ratio |
|---|---|---|---|
| Average PSS Memory | ~180 MB | 955.3 MB | ~5.3× |
| Peak Memory | ~200 MB (est.) | 1,160 MB | ~5.8× |
| Rendering Engine | Shared system | Skia/Impeller (private) | N/A |
| Runtime Overhead | ART (shared) | Dart VM (private, 20-40 MB) | N/A |

**Flutter's memory overhead (~800 MB baseline)** is driven by three architectural components [1]:
1. **Bundled rendering engine**: Flutter ships its own Skia/Impeller graphics engine (~30-50 MB) rather than using the system's shared graphics libraries.
2. **Dart VM**: The managed runtime environment adds ~20-40 MB of persistent heap overhead and maintains garbage collection buffers.
3. **Double layer architecture**: Flutter applications run a separate UI layer atop the native platform, effectively duplicating rendering infrastructure.

For the ANIS Child Application — a continuous monitoring service expected to run persistently in the background — this ~800 MB overhead is prohibitive. It would consume a significant portion of the device's available RAM, degrading multitasking performance and increasing the likelihood of the app being killed by the Android Low Memory Killer.

**Memory variance** is also a concern: Flutter memory ranges from 181.6 MB (idle) to 1.16 GB (under load) [1], indicating highly dynamic allocation patterns that make memory planning unpredictable. Native Android maintains a stable, predictable memory profile.

---

#### Processing Latency and Tail Performance

The AI analysis pipeline shows measurable performance differences:

| Phase | Native Android | Flutter | Difference |
|---|---|---|---|
| OCR (ML Kit) | 176.0 ms | 122.5 ms | Flutter faster |
| Image Preprocessing | 97.0 ms | 109.0 ms | +12% |
| ONNX Inference | **372.0 ms** | **532.5 ms** | **+43% (1.43×)** |
| **Total Analysis** | **645.0 ms** | **764.0 ms** | **+18% (1.18×)** |

**The 1.43× ONNX inference slowdown in Flutter** is attributed to Dart FFI marshalling overhead [1]: Flutter's `onnxruntime` package invokes native C++ functions through `dart:ffi`, requiring tensor data conversion across the Dart heap boundary. Native Kotlin invokes ONNX Runtime through direct JNI, operating entirely within native memory space.

**Tail latency is significantly worse in Flutter** [1]:
- Flutter: 12.8% of frames exceeded 1000 ms, 0.3% exceeded 2000 ms, maximum 7829 ms
- Android: no frames exceeded 1000 ms (maximum 1047 ms)
- Flutter's worst outlier (7829 ms) was caused by an OCR pipeline stall coinciding with Dart garbage collection — a failure mode specific to Flutter's managed memory model.

For a real-time content moderation system, predictable latency is more important than average latency. A blocked content frame that arrives 8 seconds late could expose a child to harmful content during the delay.

---

#### Platform Channel Overhead

Flutter communicates with native platform APIs through Platform Channels (MethodChannel/EventChannel), which introduce:

- **Serialization cost**: Messages must be serialized/deserialized between Dart and native code
- **Thread scheduling**: Cross-thread dispatch adds timerfd-based scheduling overhead
- **JNI bridging**: Every platform call traverses the Java Native Interface
- **Binary payload overhead**: Screen capture frames transfer multi-megabyte RGBA buffers; serialization of binary data accounts for 14-48% of total processing time in data-intensive AI pipelines [3]

Empirical benchmarks show Platform Channel round-trip latency on Android is approximately 114 µs per call, compared to 0.5 µs for direct FFI calls — over 200× slower [2]. For an application that captures and analyzes frames every 1 second, the accumulated channel overhead is non-trivial.

---

#### Battery Efficiency

Both implementations achieved comparable battery discharge rates [1]:

| Metric | Native Android | Flutter |
|---|---|---|
| Total Discharge (15.5 min) | 197 mAh | 192 mAh |
| App Power Contribution | 31.6 mAh | ~74 mAh |
| Estimated Hourly Drain | ~750 mAh | ~770 mAh |
| % Battery per Hour (4471 mAh) | ~17% | ~17% |

While total battery drain is similar, the **app-level power contribution** — power attributed specifically to the application versus shared system processes — is **2.3× higher in Flutter** (74 mAh vs 31.6 mAh). This indicates the Flutter implementation consumes more of the device's power budget for its own operations.

---

#### Data Transfer Overhead

The Flutter implementation transferred **5.8× more network data** than the native Android implementation during testing [1]. While the root cause requires further investigation, this suggests that cross-platform abstractions introduce additional telemetry or inefficient serialization that inflates data usage — a concern for users with limited mobile data plans.

---

#### Security and Stability

- Native Android provides direct implementation of security-sensitive features without intermediary abstraction layers.
- Hardware-backed keystore integration, encrypted shared preferences, and certificate pinning are all accessible through documented Android APIs without relying on third-party plugin maintenance.
- Reduced attack surface: fewer abstraction layers means fewer potential vulnerability points.
- No dependency on plugin authors to update security-critical components in response to Android platform changes.

---

#### Reduced Dependency on Custom Plugins

Most ANIS functionality would require custom Flutter plugins [1]:

| Feature | Native Approach | Flutter Alternative |
|---|---|---|
| Screen Capture | MediaProjection (built-in) | Custom MethodChannel plugin |
| AI Inference | ONNX Runtime Android SDK | flutter_onnxruntime (third-party) |
| OCR | ML Kit (built-in) | google_mlkit_text_recognition (third-party) |
| Blur Overlay | WindowManager (built-in) | Custom platform channel |
| Accessibility | AccessibilityService (built-in) | Custom plugin |
| Usage Stats | UsageStatsManager (built-in) | Custom plugin |
| Secure Storage | EncryptedSharedPreferences | flutter_secure_storage (third-party) |

Each third-party Flutter plugin represents a maintenance risk: delayed Android API updates, breaking changes, unpatched vulnerabilities, or plugin abandonment.

---

#### Performance Efficiency Summary

| Metric | Native Android | Flutter | Impact |
|---|---|---|---|
| CPU Utilization | 19.42% | 18.91% | Negligible difference |
| **Memory (PSS)** | **~180 MB** | **955 MB** | **5.3× more** |
| ONNX Inference | 372 ms | 532 ms | 1.43× slower |
| **Tail Latency (>2s)** | **0%** | **0.3%** | **Unpredictable** |
| App Power | 31.6 mAh | 74 mAh | 2.3× higher |
| Network Data | Baseline | 5.8× higher | Data usage concern |

**Conclusion**: Native Android development was selected because ANIS is a system-level parental control application — not a conventional mobile application. The application requires deep OS integration, persistent background execution, predictable latency for real-time content moderation, and memory efficiency for continuous operation on resource-constrained devices. While Flutter offers development velocity advantages for standard applications, the empirical evidence demonstrates that the native Android implementation is superior across memory efficiency, latency predictability, and system integration depth — all critical requirements for a child safety platform.

---

*References:*
[1] Ahmed Ibrahim et al., "Performance Evaluation of Cross-Platform AI-Powered Content Moderation Systems: Flutter vs Native Android," ANIS Research Paper, 2026.
[2] Pocatilu, P., Vetrici, M., & Despa, M. L., "Performance analysis of cross-platform mobile frameworks for compute-intensive applications," IEEE, 2020.
[3] Lin, "Comparative benchmarks of Platform Channel serialization latency," 2024.

---

## 3. Application Architecture

### 3.1 Architectural Overview

- Present the high-level architecture.
- Explain interaction between UI layer, service layer, local storage, backend APIs, and AI services.

### 3.2 Architectural Pattern

- MVVM Architecture.
- Separation of concerns.
- Lifecycle-aware components.
- Hilt dependency injection graph.

### 3.3 Major Layers

#### Presentation Layer

- Jetpack Compose screens
- ViewModels with StateFlow
- Navigation state management
- Material 3 theming (light/dark)

#### Domain Layer

- Business rules and policies
- Monitoring logic
- Restriction enforcement logic
- Quiz and reward management
- Content analysis orchestration

#### Data Layer

- Room database (local persistence)
- DataStore / EncryptedSharedPreferences
- Repository pattern
- Retrofit API communication

#### Service Layer

- Accessibility Service (system-wide monitoring)
- Foreground Monitoring Service (screen capture + AI analysis)
- MediaProjection Service (screen recording for content moderation)
- Background Workers (location telemetry, sync)
- FCM Service (push notifications)
- Notification Management

---

## 4. Core Functionalities

### 4.1 Device Monitoring

- Device activity tracking via Accessibility Service.
- Application usage monitoring via UsageStatsManager.
- Screen content monitoring via periodic capture + AI pipeline.
- Location tracking via FusedLocationProviderClient + WorkManager.
- Session monitoring with device resource metrics (battery, CPU, RAM).
- Installed applications discovery and reporting.
- Offline-capable telemetry with local queue and retry mechanism.

### 4.2 Application Management

- Installed application discovery.
- App categorization.
- Application blocking via overlay + Accessibility Service.
- Application time limits enforcement.

### 4.3 Screen Time Management

- Daily limits.
- Scheduled access periods.
- Temporary restrictions.
- Automatic enforcement via Accessibility Service.
- Usage recovery through quiz completion.

### 4.4 Content Protection System

The Content Protection System is the core AI-powered feature of the ANIS Child Application. It performs real-time on-device content moderation using a CLIP-based vision model running via ONNX Runtime, combined with Google ML Kit for optical character recognition. Every frame is analyzed through a multi-stage pipeline that balances accuracy, latency, and battery efficiency.

---

#### 4.4.1 Architecture Overview

The system is composed of five coordinated components:

| Component | Responsibility |
|---|---|
| AiAnalyzer | Core inference engine: ONNX model loading, image preprocessing, embedding comparison |
| SessionManager | Session lifecycle orchestration, configuration, device stats monitoring |
| SessionCaptureService | Foreground service: MediaProjection capture loop, VirtualDisplay management |
| BlurOverlayManager | BroadcastReceiver-based show/hide of frosted-glass overlay |
| BlurNotificationManager | High-priority notifications for blocked content alerts |

These components are wired together via Hilt dependency injection, with the SessionManager acting as the central coordinator.

---

#### 4.4.2 AI Analysis Pipeline (AiAnalyzer)

The analysis of each captured frame follows a strict sequential pipeline with early-exit optimization:

```
Screen Capture
    │
    ▼
┌──────────────────────────────┐
│ 1. OCR (Google ML Kit)       │ ◄── Text extraction from image
│    - Detects all text blocks  │
│    - Concatenates into string │
└──────────┬───────────────────┘
           ▼
┌──────────────────────────────┐
│ 2. Banned Word Detection     │ ◄── Early exit optimization
│    - 100+ words across       │
│      7 categories            │
│    - Case-insensitive match  │
│    - If found → BLOCKED      │
│      (skips ONNX inference)  │
└──────────┬───────────────────┘
           ▼ (if no banned words)
┌──────────────────────────────┐
│ 3. Image Preprocessing       │ ◄── CLIP standard pipeline
│    - Resize to 224×224       │
│    - Normalize (mean, std)   │
│    - Convert to FP16 tensor  │
└──────────┬───────────────────┘
           ▼
┌──────────────────────────────┐
│ 4. ONNX Runtime Inference    │ ◄── CLIP vision model
│    - vision_model_fp16.onnx  │
│    - Single forward pass     │
│    - Output: image embedding  │
└──────────┬───────────────────┘
           ▼
┌──────────────────────────────┐
│ 5. Embedding Comparison      │ ◄── Threat classification
│    - L2 normalize embedding  │
│    - Cosine similarity vs    │
│      pre-computed threats    │
│    - Logit scaling           │
│    - Threshold comparison    │
└──────────┬───────────────────┘
           ▼
    ┌─────────────┐
    │  Decision   │
    │ Safe/Blocked│
    └─────────────┘
```

##### 4.4.2.1 OCR & Banned Word Detection

Google ML Kit Text Recognition scans each frame for text content. The extracted text is checked against a banned word list organized into 7 categories:

| Category | Examples |
|---|---|
| Profanity | Common swear words and obscenities |
| Violence | Words related to fighting, weapons, harm |
| Adult | Sexually explicit or suggestive terms |
| Drugs | Substance-related terminology |
| Weapons | Firearm, knife, explosive references |
| Hate | Discriminatory or derogatory language |
| Gambling | Betting, casino-related terms |

Detection is case-insensitive. If any banned word is found, the pipeline exits early with a **BLOCKED** decision, avoiding the computationally expensive ONNX inference step entirely.

##### 4.4.2.2 Image Preprocessing

Before ONNX inference, the captured bitmap undergoes CLIP-standard preprocessing:

1. **Resize** to 224×224 pixels (CLIP vision encoder input size)
2. **Normalize** using CLIP mean and standard deviation
3. **Convert** to FP16 normalized tensor via buffer allocation and memory mapping

##### 4.4.2.3 ONNX Runtime Inference

The preprocessed tensor is fed into the CLIP-based vision model:

- **Model**: `vision_model_fp16.onnx` — CLIP ViT variant, FP16 quantized
- **Runtime**: ONNX Runtime Android (`OrtSession`)
- **Output**: A fixed-dimensional embedding vector representing the image content
- **Performance**: ~372ms average inference time on modern Android devices

##### 4.4.2.4 Embedding Comparison & Threat Classification

The extracted embedding is compared against pre-computed threat embeddings loaded from `saved_embeddings.json`:

1. **L2 Normalization** — Both the image embedding and threat embeddings are L2-normalized
2. **Cosine Similarity** — Dot product of normalized vectors
3. **Logit Scaling** — Apply temperature scaling factor
4. **Threshold Comparison**:
   - **Adult category**: threshold = 0.35
   - **Violence category**: threshold = 0.40
   - **Other categories**: threshold = 0.40

If any threat score exceeds its threshold, the content is classified as **BLOCKED**. Otherwise, it is **SAFE**.

##### 4.4.2.5 Performance Instrumentation

Every phase of the pipeline is instrumented with timing measurements:

| Phase | Timing | Typical Duration |
|---|---|---|
| OCR | `ocrTimeMs` | ~176ms |
| Preprocessing | `preprocessTimeMs` | ~97ms |
| ONNX Inference | `onnxTimeMs` | ~372ms |
| **Total** | | **~645ms** |

These timings are logged and stored per-capture for analytics and performance monitoring.

---

#### 4.4.3 Blur Overlay Finite State Machine

The blur overlay is not toggled on every single BLOCKED frame. Instead, a finite state machine prevents flickering from transient content:

```
          ┌──────────────────────────────────────────────┐
          │                                              │
          ▼                                              │
    ┌──────────┐   BLOCKED frame   ┌────────────────┐   │
    │   SAFE   │ ────────────────► │ PENDING_BLUR   │   │
    │          │                   │ (counter = 1)   │   │
    └────┬─────┘                   └────────┬───────┘   │
         ▲                                  │          │
         │                           BLOCKED (counter   │
         │   ┌───────────────────    reaches threshold) │
         │   │                                  │       │
         │   │    SAFE (counter                  ▼       │
         │   │    reaches release)   ┌────────────────┐ │
         │   │                       │   BLURRED      │ │
         │   │                       │ (overlay shown)│ │
         │   │                       └────────┬───────┘ │
         │   │                                │         │
         │   │ ┌──────────────────────────────┘         │
         │   │ │    BLOCKED (reset counter)             │
         │   │ ▼                                        │
         │   ┌──────────────────────────────────────────┘
         │   │
         │   │    SAFE frame
         └───┴──────────────────────────────────────────┘
                    PENDING_RELEASE
```

- **SAFE** → **PENDING_BLUR**: First BLOCKED frame detected; start counting
- **PENDING_BLUR** → **BLURRED**: Configurable threshold reached (default: 3 consecutive BLOCKED frames); show overlay
- **BLURRED** → **PENDING_RELEASE**: First SAFE frame while blurred
- **PENDING_RELEASE** → **SAFE**: Configurable consecutive SAFE frames (default: 3); hide overlay
- **PENDING_BLUR/PENDING_RELEASE** → reset on opposite decision

**Default thresholds:**
- `blurTriggerThreshold`: 3 consecutive unsafe frames
- `blurReleaseThreshold`: 3 consecutive safe frames

---

#### 4.4.4 Session Lifecycle

Sessions organize continuous monitoring periods with full lifecycle tracking.

##### 4.4.4.1 Session States

```
Idle ──► Active ──► Completed
           │
           ├──► PermissionRequired
           └──► Error
```

##### 4.4.4.2 Session Configuration

| Parameter | Default | Range |
|---|---|---|
| `sessionIntervalMs` | 1000ms | 500–5000ms |
| `blurTriggerThreshold` | 3 frames | 1–10 |

##### 4.4.4.3 Device Resource Monitoring

Each session tracks device-level metrics at start and end:

| Metric | Source |
|---|---|
| Battery level | `BatteryManager` |
| Battery charging status | `BatteryManager` |
| CPU time | `/proc/self/stat` |
| RAM PSS | `Debug.MemoryInfo` |

##### 4.4.4.4 Capture Loop

```
while (session is Active) {
    1. Capture frame via MediaProjection + ImageReader
    2. Extract bitmap from ImageReader
    3. Run AiAnalyzer.analyzeImage(bitmap)
    4. Apply blur FSM decision
    5. Store analysis result (batched for SAFE, immediate for BLOCKED)
    6. Wait for configured interval
}
```

##### 4.4.4.5 Batched vs Immediate Writes

To minimize database I/O:
- **SAFE results**: Batched — written to Room DB every 10 results or every 5 seconds, whichever comes first
- **BLOCKED results**: Written immediately to ensure no data loss on crash

##### 4.4.4.6 Fault Tolerance

- **Auto-restart**: Up to 5 consecutive restart attempts on capture failure
- **Exponential backoff**: Increasing delay between restart attempts
- **Max retry cap**: After 5 failures, session enters Error state and stops

---

#### 4.4.5 Database Schema

Two Room entities persist session data:

##### sessions

| Column | Type | Description |
|---|---|---|
| `id` | Long (PK, auto) | Session identifier |
| `startTime` | Long | Epoch millis of session start |
| `endTime` | Long (nullable) | Epoch millis of session end |
| `status` | String | Idle/Active/Completed/Error |
| `intervalMs` | Long | Capture interval in ms |
| `totalCaptures` | Int | Total frames processed |
| `blockedCount` | Int | Number of BLOCKED decisions |
| `safeCount` | Int | Number of SAFE decisions |
| `batteryStart` | Int | Battery % at session start |
| `batteryEnd` | Int (nullable) | Battery % at session end |
| `batteryCharging` | Boolean | Charging state |
| `cpuTimeMs` | Long | Total CPU time |
| `cpuUsagePercent` | Double | CPU usage percentage |
| `ramPssMb` | Double | RAM PSS in MB |

##### analysis_results

| Column | Type | Description |
|---|---|---|
| `id` | Long (PK, auto) | Result identifier |
| `sessionId` | Long (FK → sessions) | Parent session |
| `timestamp` | Long | Capture timestamp |
| `analysisResult` | String | Safe / Blocked |
| `decision` | String | BLOCKED / SAFE |
| `ocrTimeMs` | Long | OCR duration |
| `onnxTimeMs` | Long | ONNX inference duration |
| `threatDetails` | String (JSON) | Per-category threat scores |
| `imagePath` | String (nullable) | Path to captured image file |

---

#### 4.4.6 Export System

Individual session data can be exported for analysis:

- **XLSX export**: Session summary sheet + detailed analysis results sheet
- **ZIP export**: XLSX file bundled with all captured images from the session
- **All sessions export**: Aggregated XLSX across all sessions

Export uses FastExcel for XLSX generation and Android's ZipOutputStream for archive creation.

---

#### 4.4.7 Protected Content Overlay

When blocked content is detected and the FSM triggers the BLURRED state:

1. **Frosted glass overlay**: White background with 94% opacity, system overlay (`TYPE_APPLICATION_OVERLAY`)
2. **BroadcastReceiver mechanism**: `BlurOverlayManager` sends/receives broadcasts to show/hide
3. **High-priority notification**: `BlurNotificationManager` displays a heads-up notification on blocked content with vibration alert
4. **Auto-recovery**: After `blurReleaseThreshold` consecutive safe frames, overlay is dismissed

---

#### 4.4.8 Permission Requirements

The content protection system requires three Android runtime permissions:

| Permission | Purpose |
|---|---|
| `MediaProjection` | Screen capture for analysis |
| `SYSTEM_ALERT_WINDOW` | Blur overlay display |
| `POST_NOTIFICATIONS` | Blocked content alerts (Android 13+) |

Permission flow:
1. App checks if all permissions are granted
2. If missing, session transitions to `PermissionRequired` state
3. User is navigated to Permissions Screen
4. After granting, session resumes automatically

### 4.5 Device Pairing

- QR code-based device pairing.
- Secure token exchange.
- Child-parent device association via backend.

### 4.6 Restriction Enforcement

- Real-time rule enforcement via foreground service.
- Immediate response to violations.
- System overlay blocking mechanism.
- Accessibility Service-based navigation interception.

### 4.7 Notification System

- Alerts and warnings for blocked content.
- Parent instructions via FCM push notifications.
- Child guidance messages.
- Foreground service notifications for persistent monitoring.

### 4.8 Error Handling and Logging

#### 4.8.1 API Error Handling

All API calls are wrapped in a `SafeApiCall` inline function that returns a sealed `ApiResult` type:

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val details: String? = null
    ) : ApiResult<Nothing>()
}
```

The wrapper catches three exception types:
- **HttpException**: Server errors (4xx/5xx) with status code extraction
- **IOException**: Network failures (timeout, DNS, connectivity loss)
- **Generic Exception**: Unexpected errors with message capture

#### 4.8.2 HTTP Logging Interceptor

A custom `AppLoggingInterceptor` logs every API request and response:
- Request: HTTP method, URL path, request body
- Response: Status code, elapsed time in milliseconds, response body
- All logs routed through LogManager for in-app viewing

#### 4.8.3 Retry and Backoff

Failed API operations implement exponential backoff:
- **Base delay**: 30 seconds
- **Maximum delay**: 60 seconds
- **Maximum retries**: 5
- Applied to: location telemetry upload, FCM token registration, apps sync

#### 4.8.4 In-App Logging (LogManager)

`LogManager` provides 5 log types stored in SharedPreferences (up to 100 entries):

| Log Type | Color | Use Case |
|---|---|---|
| INFO | White | General events |
| SUCCESS | Green | Successful API calls, operations |
| ERROR | Red | Failures, exceptions |
| LOCATION | Blue | GPS acquisition, telemetry |
| HTTP | Yellow | Request/response details |

Logs are viewable in the Settings screen's collapsible `LogSection` with monospace formatting and color-coded rows.

#### 4.8.5 Crash Recovery

- Foreground services auto-restart on crash (up to 5 consecutive attempts)
- Exponential backoff between restart attempts
- BootReceiver restarts services after device reboot
- Permission re-verification on every service start

### 4.9 Offline Parent Gate (PIN Protection)

- Settings screen gated by a 4-6 digit PIN to prevent child from altering monitoring settings.
- PIN set during initial device pairing; stored hashed (SHA-256 + salt) in EncryptedSharedPreferences.
- Quiz, Task, and Reward screens accessible freely without PIN — promoting engagement without friction.
- Failed attempt cooldown: 30-second lockout after 5 consecutive failures.
- PIN recovery requires re-pairing via parent app QR code.

---

## 5. Quiz and Educational Challenge System

### 5.1 Purpose

- Encourage productive device usage.
- Promote learning opportunities.
- Provide usage recovery mechanism through positive engagement.

### 5.2 Quiz Workflow

- Quiz assignment from parent via backend.
- Quiz delivery and display on child device.
- Quiz completion tracking.
- Result evaluation and submission.
- Reward calculation based on performance.

### 5.3 Adaptive Learning Support

- Difficulty adjustment based on performance history.
- Performance tracking across subjects.
- Educational engagement analytics.
- Varied question types and categories.

### 5.4 Usage Recovery Mechanism

- Child earns additional screen/usage time by completing quizzes.
- Configurable reward-to-effort ratio controlled by parent.
- Immediate activation of earned time upon successful completion.

---

## 6. Reward Management System

### 6.1 Objectives

- Reinforce positive behavior.
- Encourage healthy digital habits.
- Create a positive feedback loop for desired device usage patterns.

### 6.2 Reward Types

- Additional screen time.
- Temporary privilege unlocks.
- Achievement badges and milestones.
- Custom rewards configured by parent.

### 6.3 Dynamic Reward Adjustment

- Parent-controlled modifications via parent app.
- Reward reduction for policy violations.
- Reward enhancement for sustained good behavior.
- Surprise reward mechanism for positive reinforcement.

### 6.4 Reward Lifecycle

- Earned rewards (pending activation).
- Active rewards (currently in effect).
- Consumed rewards (fully utilized).
- Expired rewards (time-limited offers).
- Revoked rewards (policy violation).

### 6.5 Integration with Content Protection

- Positive behavior (no blocked content) contributes to reward accumulation.
- Violations can reduce or revoke accumulated rewards.
- Transparency: child sees correlation between behavior and rewards.

---

## 7. Android Components

### 7.1 Activities

- MainActivity (single activity, Compose host).
- Permission request handling.
- Navigation host for Compose screens.

### 7.2 Jetpack Compose Screens

- PairingScreen (QR scanning via CameraX + ML Kit).
- HomeScreen (child welcome, status overview, monitoring status, reward summary).
- PinScreen (parent gate: PIN entry to unlock Settings in offline mode).
- SettingsScreen (dark mode, monitoring toggle, permissions, log viewer — PIN-protected).
- QuizScreen (quiz delivery and completion — no PIN required).
- TaskScreen (task list and completion tracking — no PIN required).
- RewardScreen (reward status and history — no PIN required).
- BlockedContentScreen (notification of blocked content).

### 7.3 ViewModels

- PairingViewModel (QR parsing, device pairing).
- HomeViewModel (location send, apps sync, child profile, reward summary).
- PinViewModel (PIN entry, validation, attempt tracking, lockout).
- SettingsViewModel (preferences, permissions, monitoring state).
- QuizViewModel (quiz state, answers, submission).
- TaskViewModel (task list, completion tracking).
- RewardViewModel (reward balance, history).

### 7.4 Foreground Services

- MonitoringService (screen capture + AI analysis loop).
- LocationService (periodic location collection).

### 7.5 Accessibility Service

- System-wide monitoring of child's device interactions.
- App usage tracking (current foreground app, time per app).
- Navigation interception for restriction enforcement.
- Screen reading capabilities for quiz accessibility.
- Detects app launches for real-time policy checks.
- Persistent operation even when app is in background.

### 7.6 MediaProjection Service

- Screen capture for content analysis.
- VirtualDisplay + ImageReader pipeline.
- Configurable capture interval.

### 7.7 Broadcast Receivers

- BootReceiver (restart services after device reboot).
- ConnectivityReceiver (sync trigger on network available).
- ScreenStateReceiver (pause/resume monitoring on screen off/on).

### 7.8 WorkManager Tasks

- LocationTelemetryWorker (hourly location upload).
- AppsSyncWorker (periodic installed apps reporting).
- PolicySyncWorker (fetch latest restrictions from backend).
- RewardSyncWorker (sync reward state).

### 7.9 Notifications

- Foreground service persistent notification.
- Blocked content alert with vibration.
- Quiz available notification.
- Reward earned notification.
- Monitoring status notification.

### 7.10 Local Storage Components

- Room Database: location_telemetry, sessions, analysis_results, quiz_results, rewards.
- EncryptedSharedPreferences: authentication tokens, child profile.
- DataStore: user preferences (theme, monitoring state).
- Internal storage: captured images for analysis.

### 7.11 UI/UX Design

#### Screen Navigation Flow

```
App Launch
    │
    ▼
┌──────────────────┐     First time?     ┌──────────────────┐
│   Splash Screen   │ ──────────────────► │   Onboarding     │
│   (logo + anim)   │                     │  (4-page intro)  │
└────────┬─────────┘                      └────────┬─────────┘
         │ Already paired                           │
         ▼                                          ▼
┌──────────────────┐                     ┌──────────────────┐
│   Home Screen    │                     │  Pairing Screen  │
│ (Welcome, child, │                     │  (QR scanner)    │
│  reward summary) │                     └────────┬─────────┘
└──┬──────┬───────┘                              │ Success
   │      │                                       ▼
   │      │                              ┌──────────────────┐
   │      └──────────┐                   │   Home Screen    │
   │                 │                   └──────────────────┘
   ▼                 ▼
┌──────────┐  ┌──────────┐
│ Quiz     │  │ Reward   │
│ Screen   │  │ Screen   │
│ (no PIN) │  │ (no PIN) │
└──────────┘  └──────────┘

   ▼ (Settings gear icon — PIN required)
┌──────────────────┐
│    Pin Screen     │ ◄── 4-6 digit PIN entry
│  (parent gate)    │
└────────┬─────────┘
         │ Valid PIN
         ▼
┌──────────────────┐
│  Settings Screen  │
│ (monitoring, logs,│
│  permissions)     │
└──────────────────┘
```

#### Design Principles

- **Material 3 Design**: Full Material You theming with dynamic color support on Android 12+
- **Dark/Light Mode**: System-aware with manual toggle in Settings
- **Single Activity Architecture**: One `MainActivity` hosts all Compose screens; navigation controlled by state
- **Permission-Centric Flow**: App guides user through permission grants (Camera, Location, MediaProjection, Overlay, Notifications) before enabling core features
- **Monitoring Status Indicator**: Persistent notification + Settings toggle show current monitoring state
- **Accessibility**: Content descriptions on all UI elements; large touch targets

---

## 8. Communication and Synchronization

### 8.1 Backend Communication

- REST APIs via Retrofit + OkHttp.
- JWT-based authentication with Bearer token.
- Kotlinx.serialization for JSON.
- Automatic token injection via OkHttp interceptor.
- Request/response logging via custom interceptor.

### 8.2 Real-Time Synchronization

- Firebase Cloud Messaging for push notifications.
- FCM-triggered immediate location sync.
- Policy update propagation via silent push.
- Reward and quiz update notifications.

### 8.3 API Endpoints

- POST children/pair — Device pairing.
- POST children/fcm-token — FCM token registration.
- GET children/me — Fetch child profile.
- POST locations/telemetry/{childId} — Send location data.
- POST apps/add-bulk — Send installed apps list.
- POST quiz/submit — Submit quiz results.
- GET quiz/assignments — Fetch assigned quizzes.
- POST rewards/claim — Claim earned rewards.
- GET rewards/balance — Fetch current reward state.
- GET policies — Fetch active restrictions.

### 8.4 Offline Operation

- Local enforcement of cached policies.
- Offline queue for location/app telemetry with retry.
- Exponential backoff for failed API calls.
- Synchronization recovery on connectivity restore.

---

## 9. Dependency Injection with Hilt

### 9.1 DI Architecture

- Hilt application-level component.
- Per-activity and per-service subcomponents.
- Singleton-scoped dependencies (database, repositories, AI analyzer, network).

### 9.2 Provided Dependencies

- Room Database + DAOs.
- Retrofit + OkHttpClient.
- EncryptedSharedPreferences / DataStore.
- AiAnalyzer (ONNX Runtime wrapper).
- SessionManager.
- Repositories (Auth, Location, Apps, FCM, Quiz, Reward, Policy).

### 9.3 Module Organization

- AppModule: core singletons (database, network, preferences).
- AiModule: AI-related dependencies (analyzer, session manager).
- ServiceModule: foreground service dependencies.
- ViewModelModule: Hilt ViewModel bindings.

---

## 10. Security and Reliability

### 10.1 Authentication

- JWT tokens with secure storage.
- Token refresh mechanism.
- Session validation on each API call.

### 10.2 Secure Communication

- TLS 1.3 encryption.
- Certificate pinning via OkHttp.
- API request/response validation.

### 10.3 Secure Local Storage

- EncryptedSharedPreferences (AES256-GCM) for tokens.
- Android KeyStore for cryptographic keys.
- Minimal sensitive data retention.

### 10.4 Offline PIN Protection

To prevent unauthorized access to application settings when the device is offline (and parent authentication via backend is unavailable), a local PIN system gates access to the Settings screen.

#### PIN Flow

- **PIN creation**: Set during initial app setup after QR pairing
- **PIN storage**: Locally hashed (SHA-256 + salt) and stored in EncryptedSharedPreferences
- **PIN entry**: 4-6 digit numeric PIN screen presented when accessing Settings
- **PIN validation**: Hash comparison against stored hash; no backend call required
- **Failed attempts**: Optional cooldown after 5 consecutive failures (30-second lockout)
- **PIN recovery**: Requires re-pairing with parent app (generates new QR code)

#### Screens NOT Protected by PIN

- Quiz Screen — accessible freely to encourage educational engagement
- Task Screen — accessible freely so child can track responsibilities
- Reward Screen — accessible freely so child can view progress and motivation

#### Screens Protected by PIN

- Settings Screen — changing monitoring preferences, viewing logs, adjusting permissions

#### Implementation

- `PinManager`: Singleton managing PIN hash generation, verification, and attempt tracking
- `PinScreen`: Compose screen with numeric keypad, visual feedback, error state, lockout timer
- `EncryptedSharedPreferences`: Stores `pinHash`, `pinSalt`, `failedAttempts`, `lockoutUntil`

- Foreground service auto-restart on kill.
- Service restart after device reboot (BootReceiver).
- Permission re-verification on service start.
- Integrity checks for monitoring continuity.

### 10.5 Reliability Measures

- Crash recovery with automatic service restart.
- Exponential backoff retry for network failures.
- Batched database writes for performance.
- Consecutive failure threshold with max retry cap.
- Background service persistence via high-priority foreground notification.

---

## 11. Privacy and Ethical Considerations

### 11.1 Privacy by Design

- Data minimization: collect only what is necessary.
- On-device processing for sensitive content analysis.
- Controlled data collection with parent transparency.

### 11.2 Child Privacy Protection

- Secure handling of collected information.
- Limited data retention with automatic cleanup.
- No unauthorized data sharing.
- Age-appropriate privacy controls.

### 11.3 Parent Transparency

- Clear monitoring policies communicated to both parent and child.
- User consent mechanisms during initial setup.
- Visible monitoring indicators on child's device.
- Parent dashboard for full visibility.

### 11.4 Regulatory Compliance

- Child safety principles (e.g., COPPA, GDPR-K).
- Data protection requirements.
- Age-appropriate design considerations.

---

## 12. Data Flow

### 12.1 Device to Backend Flow

- Location telemetry: periodic + event-driven (FCM trigger).
- Installed apps: periodic sync with change detection.
- Usage statistics: batched hourly reports.
- Analysis results: immediate for BLOCKED, batched for SAFE.
- Quiz results: submitted on completion.
- Reward state: synced on change.

### 12.2 Backend to Parent Application Flow

- Policy updates propagated to parent dashboard.
- Violation alerts pushed to parent device.
- Usage reports generated for parent review.
- Reward and quiz management from parent app.

### 12.3 Content Analysis Flow

- Screen capture -> OCR (ML Kit) -> banned word check -> image preprocessing -> ONNX inference -> embedding comparison -> decision (Safe/Blocked) -> blur overlay if blocked -> persistence.

### 12.4 Alert Generation Flow

- Blocked content event -> immediate local alert -> FCM notification to parent -> database persistence -> parent dashboard update.

### 12.5 Reward and Quiz Flow

- Parent assigns quiz -> backend -> child device notification -> child completes -> backend evaluation -> reward calculation -> reward credited -> child notified -> parent notified.

Include architecture and sequence diagrams where appropriate.

---

## 13. Benefits and Impact

### 13.1 Child Benefits

- Safer online experience with real-time content protection.
- Better digital habits through screen time management.
- Educational engagement through quiz system.
- Positive reinforcement via reward system.
- Transparency about monitoring.

### 13.2 Parent Benefits

- Increased visibility into child's digital activity.
- Better control with configurable policies.
- Reduced supervision effort with automated enforcement.
- Peace of mind with real-time alerts.
- Ability to encourage positive behavior through rewards.

### 13.3 Platform Benefits

- Balanced safety and privacy.
- Scalable architecture with offline capability.
- AI-assisted protection with on-device processing.
- Native performance for system-level controls.
- Comprehensive accessibility integration for complete device oversight.

---

## 14. Testing and Validation

### 14.1 Testing Strategy

| Level | Scope | Tools / Framework |
|---|---|---|
| Unit Testing | ViewModels, Repositories, Utility classes | JUnit 5, MockK |
| Integration Testing | Repository + Database, API + Network | JUnit 5, Room testing, MockWebServer |
| UI Testing | Compose screens, Navigation flows | Compose UI Test, Espresso |
| Instrumentation Testing | Foreground services, Workers, Permissions | AndroidX Test, WorkManagerTestHelper |
| AI Pipeline Testing | ONNX inference accuracy, OCR accuracy | Custom test dataset, AssertJ |

### 14.2 Unit Testing

#### ViewModel Tests

- **PairingViewModel**: QR code parsing, pairing request construction, UI state transitions (Scanning → Loading → Success/Error)
- **HomeViewModel**: Location sending (success/error), apps sync, child profile fetch
- **SettingsViewModel**: Preference toggle persistence, permission state mapping
- **QuizViewModel** (planned): Quiz state machine, answer validation, submission flow
- **RewardViewModel** (planned): Balance calculation, reward lifecycle transitions

#### Repository Tests

Each repository tested with mocked API service and in-memory database:
- **AuthRepository**: Pairing success, network failure, invalid token
- **LocationRepository**: Telemetry upload, offline queue fallback, retry logic
- **AppsRepository**: Package manager query, bulk upload
- **FCMRepository**: Token registration, re-registration on token change

#### Utility Tests

- **SafeApiCall**: Success path, HttpException mapping, IOException mapping, generic exception
- **LogManager**: Log insertion, 100-entry cap, clear, formatTimestamp
- **PreferenceManager**: Read/write each stored field, missing key defaults
- **ApiConfig**: URL construction, endpoint path correctness

### 14.3 AI Pipeline Validation

#### 14.3.1 Model Accuracy Testing

- **Test dataset**: Labeled image set across Safe, Adult, Violence categories
- **Metrics**: Precision, Recall, F1-Score per threat category
- **Threshold tuning**: ROC curve analysis for Adult (current: 0.35) and Violence (current: 0.40)
- **Baseline drift monitoring**: Compare embedding similarity drift over model versions

#### 14.3.2 OCR Accuracy Testing

- **Test set**: Images containing banned words, safe text, and mixed content
- **Metrics**: Text detection rate, banned word recall, false positive rate
- **Early exit validation**: Verify banned word detection correctly skips ONNX inference

#### 14.3.3 Performance Benchmarking

Continuous integration benchmarks (run on each PR):
| Metric | Target | Measurement |
|---|---|---|
| OCR latency | < 250 ms | `ocrTimeMs` instrumentation |
| ONNX inference latency | < 500 ms | `onnxTimeMs` instrumentation |
| Total analysis latency | < 1000 ms | Pipeline timing |
| Memory during session | < 250 MB PSS | `dumpsys meminfo` |
| Frame drop rate | < 1% | Capture success / total attempts |

### 14.4 Integration Testing

#### Database Integration

- **Room DAO tests**: Insert, read, update, delete for all entities
- **Migration tests**: Verify schema from version 1 → 2 with data preservation
- **Batched write tests**: Verify batch flush behavior (10 results or 5 seconds)

#### Network Integration

- **MockWebServer**: Simulate API endpoints with success, error, and timeout responses
- **AuthInterceptor test**: Verify Bearer token injection, missing token handling
- **AppLoggingInterceptor test**: Verify request/response logging to LogManager

### 14.5 Instrumentation Testing

#### Service Tests

- **Foreground service lifecycle**: Start, running state, stop, restart after crash
- **Capture loop**: Frame acquisition rate, bitmap extraction, error recovery
- **Blur FSM**: SAFE → PENDING_BLUR → BLURRED → PENDING_RELEASE → SAFE transitions

#### Permission Tests

- **Camera permission**: Grant, deny, permanently deny flows for QR pairing
- **Location permission**: Fine, coarse, background permission states
- **MediaProjection**: Intent result handling, consent dialog
- **Overlay permission**: Settings intent navigation, grant detection

### 14.6 End-to-End Testing

- **Pairing flow**: Launch app → QR scan → API call → token storage → Home screen
- **Monitoring session**: Start monitoring → capture frames → AI analysis → results stored → stop
- **Offline resilience**: Enable airplane mode → capture continues → data queued → reconnect → sync
- **FCM trigger**: Send push → app receives → immediate location sync triggered

### 14.7 Testing Tools and Dependencies

| Tool | Purpose |
|---|---|
| JUnit 5 | Test runner and assertions |
| MockK | Kotlin mocking library |
| Turbine | Flow testing (ViewModel StateFlow) |
| MockWebServer (OkHttp) | HTTP endpoint simulation |
| Room Testing | In-memory database for DAO tests |
| Compose UI Test | Compose screen interaction testing |
| AndroidX Test | Instrumentation test orchestration |
| WorkManagerTestHelper | Worker unit testing |
| adb + dumpsys | Performance benchmarking |
| custom timing instrumentation | AI pipeline per-phase measurement |

---
