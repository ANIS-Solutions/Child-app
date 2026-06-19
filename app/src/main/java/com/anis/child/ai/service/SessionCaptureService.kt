package com.anis.child.ai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import com.anis.child.MainActivity
import com.anis.child.ai.AiAnalyzer
import com.anis.child.ai.SessionManager
import com.anis.child.ai.util.BlurNotificationManager
import com.anis.child.ai.util.BlurOverlayManager
import com.anis.child.ai.util.ImageStorageManager
import com.anis.child.data.local.AnalysisResultEntity
import com.anis.child.data.repository.SessionRepository
import com.anis.child.util.registerReceiverSafe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SessionCaptureService : Service() {

    private val TAG = "SessionCaptureService"

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var aiAnalyzer: AiAnalyzer

    @Inject
    lateinit var sessionManager: SessionManager

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var captureJob: Job? = null

    private var sessionId: Long = -1
    private var intervalMs: Int = 1000

    private var blurTriggerThreshold = 3
    private val SAFE_FRAMES_TO_HIDE = 3
    @Volatile
    private var autoRotateMaxCaptures = 0
    @Volatile
    private var captureCount = 0

    @Volatile
    private var screenOff = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    screenOff = true
                    Log.d(TAG, "Screen OFF — pausing captures")
                }
                Intent.ACTION_SCREEN_ON -> {
                    screenOff = false
                    Log.d(TAG, "Screen ON — resuming captures")
                }
            }
        }
    }

    private enum class UiState {
        SAFE,
        PENDING_BLUR,
        BLURRED,
        PENDING_RELEASE
    }

    private var uiState = UiState.SAFE
    private var unsafeCounter = 0
    private var safeCounter = 0

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var isAnalyzing = false
    @Volatile
    private var captureInProgress = false
    private var consecutiveAnalysisFailures = 0
    private val MAX_ANALYSIS_FAILURES = 5

    private val safeResultsBuffer: MutableList<AnalysisResultEntity> = mutableListOf()
    private val SAFE_BATCH_SIZE = 10
    private var lastSafeBatchWrite = System.currentTimeMillis()
    private val SAFE_BATCH_INTERVAL_MS = 5000L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiverSafe(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1)
                intervalMs = intent.getIntExtra(EXTRA_INTERVAL_MS, 1000)
                blurTriggerThreshold = intent.getIntExtra(EXTRA_BLUR_TRIGGER_THRESHOLD, 3)
                autoRotateMaxCaptures = intent.getIntExtra(EXTRA_AUTO_ROTATE_MAX_CAPTURES, 0)
                captureCount = 0
                screenOff = false
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, -1)

                val data: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_DATA)
                }

                if (data != null) {
                    startCapture(resultCode, data)
                } else {
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopCapture()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startCapture(resultCode: Int, data: Intent) {
        uiState = UiState.SAFE
        unsafeCounter = 0
        safeCounter = 0

        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        }

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val density = displayMetrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "SessionCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        startCaptureLoop()
    }

    private fun startCaptureLoop() {
        captureJob?.cancel()
        captureJob = serviceScope.launch {
            // Initialization guard: if the first capture doesn't succeed in 10s, stop
            val initDeadline = System.currentTimeMillis() + 10_000
            var initialized = false

            while (isActive) {
                if (!initialized && System.currentTimeMillis() > initDeadline) {
                    Log.w(TAG, "Initialization timeout — no successful capture in 10s")
                    stopCapture()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@launch
                }

                if (isAnalyzing || captureInProgress) {
                    delay(intervalMs.toLong())
                    continue
                }

                if (screenOff) {
                    delay(intervalMs.toLong())
                    continue
                }

                isAnalyzing = true
                captureInProgress = true
                val totalStart = System.nanoTime()

                val captureStart = System.nanoTime()
                val image = withContext(Dispatchers.IO) { imageReader?.acquireLatestImage() }
                val captureTime = System.nanoTime() - captureStart

                if (image == null) {
                    Log.w(TAG, "No image available - capture took ${captureTime / 1_000_000.0}ms")
                    isAnalyzing = false
                    captureInProgress = false
                    delay(intervalMs.toLong())
                    continue
                }

                captureCount++
                initialized = true
                val timestamp = System.currentTimeMillis()

                val isLimitReached = autoRotateMaxCaptures > 0 && captureCount >= autoRotateMaxCaptures
                if (isLimitReached) {
                    Log.d(TAG, "Capture limit reached ($captureCount/$autoRotateMaxCaptures), signaling auto-rotate")
                }

                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        val bmp = android.graphics.Bitmap.createBitmap(
                            image.width,
                            image.height,
                            android.graphics.Bitmap.Config.ARGB_8888
                        )
                        image.planes[0].buffer.rewind()
                        bmp.copyPixelsFromBuffer(image.planes[0].buffer)
                        bmp
                    }

                    val analysisStart = System.nanoTime()
                    val result = aiAnalyzer.analyzeImage(bitmap)
                    val analysisTime = System.nanoTime() - analysisStart
                    val decision = if (result.contains("BLOCKED")) "BLOCKED" else "SAFE"

                    val ocrTime = extractTimeValue(result, "OCR Time:")
                    val onnxTime = extractTimeValue(result, "ONNX Time:")

                    val isUnsafe = decision == "BLOCKED"

                    when (uiState) {
                        UiState.SAFE -> {
                            if (isUnsafe) {
                                unsafeCounter = 1
                                uiState = UiState.PENDING_BLUR
                                Log.d(TAG, "FSM: SAFE -> PENDING_BLUR (counter=1)")
                            }
                        }

                        UiState.PENDING_BLUR -> {
                            if (isUnsafe) {
                                unsafeCounter++
                                Log.d(TAG, "FSM: PENDING_BLUR unsafe (counter=$unsafeCounter/threshold=$blurTriggerThreshold)")
                                if (unsafeCounter >= blurTriggerThreshold) {
                                    Log.d(TAG, "FSM: PENDING_BLUR -> BLURRED (threshold reached)")
                                    BlurOverlayManager.showFrostedGlassOverlay(this@SessionCaptureService)
                                    BlurNotificationManager.showBlockedContentNotification(this@SessionCaptureService)
                                    uiState = UiState.BLURRED
                                    unsafeCounter = 0
                                }
                            } else {
                                Log.d(TAG, "FSM: PENDING_BLUR -> SAFE (false alarm)")
                                unsafeCounter = 0
                                uiState = UiState.SAFE
                            }
                        }

                        UiState.BLURRED -> {
                            if (!isUnsafe) {
                                safeCounter = 1
                                uiState = UiState.PENDING_RELEASE
                                Log.d(TAG, "FSM: BLURRED -> PENDING_RELEASE (counter=1)")
                            }
                        }

                        UiState.PENDING_RELEASE -> {
                            if (!isUnsafe) {
                                safeCounter++
                                Log.d(TAG, "FSM: PENDING_RELEASE safe (counter=$safeCounter/threshold=$SAFE_FRAMES_TO_HIDE)")
                                if (safeCounter >= SAFE_FRAMES_TO_HIDE) {
                                    Log.d(TAG, "FSM: PENDING_RELEASE -> SAFE (threshold reached)")
                                    BlurOverlayManager.hideBlurOverlay(this@SessionCaptureService)
                                    BlurNotificationManager.cancelBlockedNotification(this@SessionCaptureService)
                                    uiState = UiState.SAFE
                                    safeCounter = 0
                                }
                            } else {
                                Log.d(TAG, "FSM: PENDING_RELEASE -> BLURRED (unsafe again)")
                                safeCounter = 0
                                uiState = UiState.BLURRED
                            }
                        }
                    }

                    val showBlurOverlay = uiState == UiState.BLURRED || uiState == UiState.PENDING_RELEASE
                    val imagePath = if (!showBlurOverlay) {
                        val path = ImageStorageManager.getCacheImagePath(
                            this@SessionCaptureService, sessionId, timestamp
                        )
                        launch {
                            ImageStorageManager.saveImageToCache(
                                this@SessionCaptureService,
                                bitmap,
                                sessionId,
                                timestamp
                            )
                        }
                        path
                    } else {
                        null
                    }

                    val embeddingJson = aiAnalyzer.lastEmbedding?.let { emb ->
                        org.json.JSONArray(emb.map { it.toDouble() }).toString()
                    }

                    val totalTime = System.nanoTime() - totalStart
                    Log.d(TAG, "TIMING: capture=${captureTime / 1_000_000.0}ms, analysis=${analysisTime / 1_000_000.0}ms, total=${totalTime / 1_000_000.0}ms")

                    consecutiveAnalysisFailures = 0

                    val resultEntity = AnalysisResultEntity(
                        sessionId = sessionId,
                        timestamp = timestamp,
                        analysisResult = result,
                        decision = decision,
                        ocrTimeMs = ocrTime,
                        onnxTimeMs = onnxTime,
                        threatDetails = "",
                        imagePath = imagePath,
                        embedding = embeddingJson
                    )

                    if (decision == "BLOCKED") {
                        try {
                            sessionRepository.addAnalysisResultEntity(resultEntity)
                        } catch (e: Exception) {
                            Log.e(TAG, "FAILED to save BLOCKED result", e)
                        }
                    } else {
                        safeResultsBuffer.add(resultEntity)
                        val now = System.currentTimeMillis()
                        if (safeResultsBuffer.size >= SAFE_BATCH_SIZE ||
                            (now - lastSafeBatchWrite) > SAFE_BATCH_INTERVAL_MS) {
                            flushSafeResults()
                        }
                    }

                    if (isLimitReached) {
                        if (safeResultsBuffer.isNotEmpty()) {
                            try {
                                sessionRepository.addAnalysisResultsBatch(safeResultsBuffer.toList())
                                safeResultsBuffer.clear()
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to flush safe results before auto-rotate", e)
                            }
                        }
                        val newId = sessionManager.handleAutoRotate(sessionId)
                        Log.d(TAG, "Auto-rotated: session $sessionId -> $newId")
                        sessionId = newId
                        captureCount = 0
                        uiState = UiState.SAFE
                        unsafeCounter = 0
                        safeCounter = 0
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in captureAndAnalyze", e)
                    consecutiveAnalysisFailures++

                    if (consecutiveAnalysisFailures >= MAX_ANALYSIS_FAILURES) {
                        Log.e(TAG, "Too many analysis failures ($consecutiveAnalysisFailures), restarting capture")
                        restartCapture()
                        return@launch
                    }
                } finally {
                    image.close()
                    isAnalyzing = false
                    captureInProgress = false
                    delay(intervalMs.toLong())
                }
            }
        }
    }

    private fun extractTimeValue(result: String, key: String): Double {
        val regex = "$key\\s*([\\d.]+)\\s*ms".toRegex()
        val match = regex.find(result)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }

    private fun stopCapture() {
        BlurOverlayManager.hideBlurOverlay(this)
        captureJob?.cancel()
        captureJob = null
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        isAnalyzing = false
        captureInProgress = false
        consecutiveAnalysisFailures = 0
    }

    private fun restartCapture() {
        Log.d(TAG, "Restarting capture due to failures or stall")
        stopCapture()

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        }

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val density = displayMetrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "SessionCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        consecutiveAnalysisFailures = 0
        isAnalyzing = false
        captureInProgress = false

        startCaptureLoop()
    }

    private fun flushSafeResults() {
        if (safeResultsBuffer.isEmpty()) return

        serviceScope.launch {
            try {
                val resultsToSave = safeResultsBuffer.toList()
                safeResultsBuffer.clear()
                sessionRepository.addAnalysisResultsBatch(resultsToSave)
                lastSafeBatchWrite = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to flush safe results", e)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Session Monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when session monitoring is active"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Session Monitoring Active")
            .setContentText("Capturing and analyzing screen content")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        try { unregisterReceiver(screenReceiver) } catch (_: Exception) {}
        if (safeResultsBuffer.isNotEmpty()) {
            serviceScope.launch {
                try {
                    sessionRepository.addAnalysisResultsBatch(safeResultsBuffer.toList())
                    safeResultsBuffer.clear()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to flush safe results on destroy", e)
                }
            }
        }
        stopCapture()
        serviceScope.cancel()
        super.onDestroy()
    }

}
