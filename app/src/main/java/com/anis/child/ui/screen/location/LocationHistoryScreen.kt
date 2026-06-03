package com.anis.child.ui.screen.location

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.anis.child.data.local.LocationTelemetryEntity
import com.anis.child.ui.theme.AppColors
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationHistoryScreen(
    viewModel: LocationHistoryViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showMap by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Location History", color = AppColors.darkTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.darkTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showMap = !showMap }) {
                        Icon(Icons.Default.Timeline, if (showMap) "Show List" else "Show Map", tint = AppColors.darkTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.primary01)
            )
        },
        containerColor = AppColors.surface50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MonitoringToggle(
                isEnabled = uiState.isMonitoringEnabled,
                onToggle = { viewModel.toggleMonitoring(it) }
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.primary01)
                }
            } else {
                if (showMap) {
                    LocationMap(
                        locations = uiState.locations,
                        currentLat = uiState.currentLatitude,
                        currentLng = uiState.currentLongitude,
                        onGetCurrentLocation = { viewModel.getCurrentLocation() },
                        isGettingLocation = uiState.isGettingCurrentLocation
                    )
                }

                if (uiState.locations.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, null, tint = AppColors.textDisabled, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No location data yet", color = AppColors.textSecondary)
                            Text("Enable location monitoring or tap 'Get Current Location'", style = MaterialTheme.typography.bodySmall, color = AppColors.textDisabled, textAlign = TextAlign.Center)
                        }
                    }
                } else if (!showMap) {
                    LocationList(
                        locations = uiState.locations,
                        onDelete = { viewModel.deleteLocation(it) },
                        onClearSent = { viewModel.clearSentLocations() },
                        totalSharedCount = uiState.totalSharedCount
                    )
                }
            }
        }
    }
}

@Composable
private fun MonitoringToggle(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, null, tint = if (isEnabled) AppColors.success500 else AppColors.textDisabled, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Location Monitoring", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = AppColors.textPrimary)
                Text(if (isEnabled) "Sharing location every hour" else "Not sharing location", style = MaterialTheme.typography.bodySmall, color = AppColors.textSecondary)
            }
            Switch(checked = isEnabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun LocationMap(
    locations: List<LocationTelemetryEntity>,
    currentLat: Double?,
    currentLng: Double?,
    onGetCurrentLocation: () -> Unit,
    isGettingLocation: Boolean
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(300.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    createMapView(ctx, locations, currentLat, currentLng)
                },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    updateMapView(mapView, locations, currentLat, currentLng)
                }
            )

            Button(
                onClick = onGetCurrentLocation,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary01),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                if (isGettingLocation) {
                    CircularProgressIndicator(
                        color = AppColors.darkTextPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.MyLocation, "Get current location", tint = AppColors.darkTextPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

private fun createMapView(
    context: Context,
    locations: List<LocationTelemetryEntity>,
    currentLat: Double?,
    currentLng: Double?
): MapView {
    Configuration.getInstance().apply {
        userAgentValue = context.packageName
        osmdroidTileCache = context.cacheDir
    }

    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(15.0)

        val hasHistoryData = locations.any { !it.isSent }
        val zoomTarget = when {
            currentLat != null && currentLng != null -> GeoPoint(currentLat, currentLng)
            hasHistoryData -> {
                val latest = locations.first()
                GeoPoint(latest.latitude, latest.longitude)
            }
            else -> GeoPoint(0.0, 0.0)
        }
        controller.setCenter(zoomTarget)

        locations.forEach { loc ->
            addMarker(
                context,
                GeoPoint(loc.latitude, loc.longitude),
                "Location: ${String.format("%.6f", loc.latitude)}, ${String.format("%.6f", loc.longitude)}",
                loc.timestamp,
                isSent = loc.isSent
            )
        }

        if (currentLat != null && currentLng != null) {
            addMarker(
                context,
                GeoPoint(currentLat, currentLng),
                "Current Location",
                System.currentTimeMillis(),
                isCurrent = true
            )
        }

        val trackPoints = locations
            .filter { !it.isSent }
            .map { GeoPoint(it.latitude, it.longitude) }
        if (trackPoints.size >= 2) {
            overlays.add(Polyline().apply {
                setPoints(trackPoints)
                outlinePaint.color = 0xFF2196F3.toInt()
                outlinePaint.strokeWidth = 4f
            })
        }
    }
}

private fun updateMapView(
    mapView: MapView,
    locations: List<LocationTelemetryEntity>,
    currentLat: Double?,
    currentLng: Double?
) {
    mapView.overlays.clear()

    locations.forEach { loc ->
        mapView.addMarker(
            mapView.context,
            GeoPoint(loc.latitude, loc.longitude),
            "Location: ${String.format("%.6f", loc.latitude)}, ${String.format("%.6f", loc.longitude)}",
            loc.timestamp,
            isSent = loc.isSent
        )
    }

    if (currentLat != null && currentLng != null) {
        mapView.addMarker(
            mapView.context,
            GeoPoint(currentLat, currentLng),
            "Current Location",
            System.currentTimeMillis(),
            isCurrent = true
        )
    }

    val trackPoints = locations
        .filter { !it.isSent }
        .map { GeoPoint(it.latitude, it.longitude) }
    if (trackPoints.size >= 2) {
        mapView.overlays.add(Polyline().apply {
            setPoints(trackPoints)
            outlinePaint.color = 0xFF2196F3.toInt()
            outlinePaint.strokeWidth = 4f
        })
    }

    mapView.invalidate()
}

private fun MapView.addMarker(
    context: Context,
    position: GeoPoint,
    title: String,
    timestamp: Long,
    isSent: Boolean = false,
    isCurrent: Boolean = false
) {
    val marker = Marker(this)
    marker.position = position
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    marker.title = title
    marker.snippet = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    marker.icon = if (isCurrent) {
        MarkerIcons.createCurrentLocationIcon(context)
    } else if (isSent) {
        MarkerIcons.createSentIcon(context)
    } else {
        MarkerIcons.createDefaultIcon(context)
    }
    overlays.add(marker)
}

private object MarkerIcons {
    private var defaultIcon: Drawable? = null
    private var sentIcon: Drawable? = null
    private var currentIcon: Drawable? = null

    fun createDefaultIcon(context: Context): Drawable {
        if (defaultIcon == null) {
            defaultIcon = context.getDrawable(org.osmdroid.library.R.drawable.marker_default)
        }
        return defaultIcon!!
    }

    fun createSentIcon(context: Context): Drawable {
        if (sentIcon == null) {
            sentIcon = context.getDrawable(org.osmdroid.library.R.drawable.marker_default)
                ?.apply { alpha = 100 }
        }
        return sentIcon!!
    }

    fun createCurrentLocationIcon(context: Context): Drawable {
        if (currentIcon == null) {
            currentIcon = context.getDrawable(android.R.drawable.ic_menu_mylocation)
        }
        return currentIcon ?: createDefaultIcon(context)
    }
}

@Composable
private fun LocationList(
    locations: List<LocationTelemetryEntity>,
    onDelete: (Long) -> Unit,
    onClearSent: () -> Unit,
    totalSharedCount: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${locations.size} locations · $totalSharedCount shared",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
                if (totalSharedCount > 0) {
                    IconButton(onClick = onClearSent) {
                        Icon(Icons.Default.ClearAll, "Clear sent", tint = AppColors.textSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        items(locations, key = { it.id }) { location ->
            LocationCard(
                location = location,
                onDelete = { onDelete(location.id) }
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun LocationCard(
    location: LocationTelemetryEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (location.isSent) AppColors.darkSurface.copy(alpha = 0.03f)
            else AppColors.surface50
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                null,
                tint = if (location.isSent) AppColors.textDisabled else AppColors.primary01,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textPrimary
                )
                Text(
                    dateFormat.format(Date(location.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (location.isSent) "Shared" else "Pending",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (location.isSent) AppColors.success500 else AppColors.warning500
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = AppColors.error500, modifier = Modifier.size(16.dp))
            }
        }
    }
}
