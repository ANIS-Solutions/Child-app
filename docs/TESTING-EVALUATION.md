# Testing and Evaluation

## 1. Testing Strategy

The testing strategy follows the Android testing pyramid with coverage across unit, integration, instrumentation, and end-to-end levels:

| Level | Scope | Tools |
|---|---|---|
| Unit Testing | ViewModels, Repositories, Utility classes | JUnit 5, MockK, Turbine |
| Integration Testing | Repository + Database, API + Network | Room testing, MockWebServer |
| AI Pipeline Testing | ONNX inference accuracy, OCR accuracy | Custom test dataset, AssertJ |
| Instrumentation Testing | Foreground services, Workers, Permissions | AndroidX Test, WorkManagerTestHelper |
| End-to-End Testing | Full user flows, offline resilience | adb, custom scripts |

## 2. Unit Testing

### ViewModel Tests

Each ViewModel is tested for state transitions, error handling, and edge cases:

- **PairingViewModel**: QR code parsing with valid and invalid JSON, successful and failed pairing requests, UI state transitions (Scanning -> Loading -> Success/Error).
- **HomeViewModel**: Location sending with success and network failure, installed apps sync, child profile fetch with cached fallback.
- **PinViewModel**: Valid PIN acceptance, invalid PIN rejection, failed attempt increment, lockout after 5 failures, lockout expiry.
- **SettingsViewModel**: Preference toggle persistence with DataStore, permission state mapping for all runtime permission states (granted, denied, permanently denied).

### Repository Tests

Repositories are tested with mocked API service and in-memory Room database:

- **AuthRepository**: Successful pairing, network failure with cached fallback, invalid token rejection.
- **LocationRepository**: Successful telemetry upload, offline queue fallback with isSent flag verification, retry logic after failure.
- **AppsRepository**: PackageManager query with mocked packages, bulk upload with partial failure handling.
- **FCMRepository**: Initial token registration, re-registration on token change, server error handling.

### Utility Tests

- **SafeApiCall**: Success path returning ApiResult.Success, HttpException mapping with status codes, IOException mapping, generic exception catching.
- **LogManager**: Log insertion capacity (100 entries), overflow eviction (FIFO), clear operation, formatTimestamp correctness.
- **PreferenceManager**: Read/write for each stored field type (String, Boolean, Int, Long), default value fallback, missing key handling.
- **ApiConfig**: URL construction correctness, endpoint path derivation.

## 3. AI Pipeline Validation

### OCR Accuracy Testing

- **Test set**: Images containing banned words, safe text, and mixed content across all 7 banned categories.
- **Metrics**: Text detection rate, banned word recall, false positive rate.
- **Early exit validation**: Verification that banned word detection correctly skips ONNX inference, with timing measurements to confirm the performance benefit.

- **Test set**: Images containing banned words, safe text, and mixed content across all 7 banned categories.
- **Metrics**: Text detection rate, banned word recall, false positive rate.
- **Early exit validation**: Verification that banned word detection correctly skips ONNX inference, with timing measurements to confirm the performance benefit.

### Performance Benchmarks

Continuous integration benchmarks enforced on each pull request:

| Metric | Target | Measurement Method |
|---|---|---|
| OCR latency | < 250 ms | ocrTimeMs instrumentation |
| ONNX inference latency | < 500 ms | onnxTimeMs instrumentation |
| Total analysis latency | < 1000 ms | Pipeline timing instrumentation |
| Memory during session | < 250 MB PSS | dumpsys meminfo |
| Frame capture rate | >= 95% of configured interval | Capture success / total attempts |
| Blur FSM response time | < 100 ms | FSM transition instrumentation |

## 4. Integration Testing

### Database Integration

- **Room DAO tests**: Insert, read (by ID, by session, all), update, and delete operations for each entity using in-memory Room database.
- **Migration tests**: Schema migration from version 1 to version 2 verified with data preservation assertions.
- **Batched write tests**: Verification that batched writes correctly flush after 10 results or 5 seconds, and that mixed SAFE/BLOCKED writes do not interleave incorrectly.

### Network Integration

- **MockWebServer**: Simulate API endpoints with success responses, error responses (4xx, 5xx), and timeout scenarios.
- **AuthInterceptor test**: Verify Bearer token injection for authenticated requests and missing-token handling for unauthenticated requests.
- **AppLoggingInterceptor test**: Verify that request and response data is correctly logged to LogManager with proper log type attribution.

## 5. Instrumentation Testing

### Service Tests

- **Foreground service lifecycle**: Start service, verify active state, stop service, verify completed state, restart after simulated crash.
- **Capture loop**: Frame acquisition rate at configured intervals, bitmap extraction correctness, error recovery when ImageReader returns null.
- **Blur FSM**: Full state transition verification for SAFE -> PENDING_BLUR -> BLURRED -> PENDING_RELEASE -> SAFE cycle with configurable thresholds.
- **Session persistence**: Verify that session data survives service restart and process kill.

### Permission Tests

- **Camera permission**: Grant, deny, and permanently deny flows for QR pairing screen.
- **Location permission**: Fine location, coarse location, and background location permission states.
- **MediaProjection**: Intent result handling when user accepts or declines screen capture consent dialog.
- **Overlay permission**: Settings intent navigation, grant detection, and re-check flows.

## 6. End-to-End Testing

- **Pairing flow**: Launch application -> QR scanning screen -> scan valid QR -> API call -> token storage -> navigate to Home Screen.
- **Monitoring session**: Start monitoring -> capture frames -> run AI analysis -> store results -> stop monitoring -> verify session data integrity.
- **Offline resilience**: Enable airplane mode -> capture continues in offline mode -> data queued in Room -> reconnect network -> verify data synchronizes.
- **FCM trigger**: Send silent push notification from test server -> application receives in FCMService -> verify immediate location sync triggered.
- **PIN gate flow**: Navigate to Settings -> PIN screen displayed -> enter wrong PIN -> increment counter -> enter correct PIN -> access Settings.

## 7. Testing Tools and Dependencies

| Tool | Purpose |
|---|---|
| JUnit 5 | Test runner and assertion framework |
| MockK | Kotlin-specific mocking library |
| Turbine | Flow and StateFlow testing |
| MockWebServer (OkHttp) | HTTP endpoint simulation |
| Room Testing | In-memory database for DAO tests |
| Compose UI Test | Jetpack Compose screen interaction testing |
| AndroidX Test | Instrumentation test orchestration |
| WorkManagerTestHelper | Worker unit testing |
| adb + dumpsys | Performance benchmarking |
| Custom timing instrumentation | AI pipeline per-phase measurement |

## 8. Android Performance Benchmarks

### Full Pipeline Latency (Android, 856 frames)

| Operation | Average | Min | Max | Std Dev |
|---|---|---|---|---|
| Capture (ImageReader) | 0.31 ms | 0.14 ms | 2.37 ms | 0.28 ms |
| Bitmap Conversion | 6.55 ms | 3.63 ms | 50.18 ms | 3.12 ms |
| Analysis (ONNX + OCR) | 609.98 ms | 195.35 ms | 1631.46 ms | 312.45 ms |

OCR early exit: When banned words are detected, analysis exits early reducing processing time to ~5-50 ms.

### Memory Consumption

| Metric | Value |
|---|---|
| Average PSS Memory | 184.6 MB |
| Peak PSS Memory | ~200 MB |

### Battery Consumption

| Metric | Value |
|---|---|
| Test Duration | 15m 35s |
| Battery Change | 100% to 96% (-4%) |
| Total Discharge | 197 mAh |
| App Power Contribution | 31.6 mAh |

### Test Environment

| Parameter | Specification |
|---|---|
| Device | Google Pixel 6 |
| Android Version | Android 15 (API 35) |
| Memory | 8 GB LPDDR5 |
| Processor | Google Tensor (5 nm) |
| Test Duration per Session | 15.5 minutes |
| Frames per Session | ~900 |
| Session Interval | 1000 ms |
| Network | Wi-Fi (stable) |

### Edge Case Validation

- **Empty capture frames**: When MediaProjection returns a null image, the system retries up to 3 times before marking the frame as an error capture.
- **Permission revocation during session**: If the user revokes MediaProjection or SYSTEM_ALERT_WINDOW permission mid-session, the FSM transitions to PermissionRequired state and pauses capture.
- **Rapid app switching**: The Accessibility Service correctly tracks foreground app changes even under rapid switching conditions (tested at 10 switches per second).
- **Concurrent notification storms**: The notification system handles up to 50 simultaneous blocked content alerts without ANR or crash.
- **Database corruption recovery**: If the Room database file is corrupted, the application falls back to a clean state with data loss limited to the current session.
