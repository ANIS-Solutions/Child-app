package com.anis.child.ui.screen.location

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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

@Composable
fun LocationHistoryScreen(
    viewModel: LocationHistoryViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showMap by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.surface50)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.primary01)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.darkTextPrimary)
                }
                Text(
                    text = "Location History",
                    color = AppColors.darkTextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showMap = !showMap }) {
                    Icon(Icons.Default.Timeline, if (showMap) "Show List" else "Show Map", tint = AppColors.darkTextPrimary)
                }
            }
        }

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
            .height(300.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    Configuration.getInstance().apply {
                        userAgentValue = ctx.packageName
                    }
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(8.0)

                        if (locations.isNotEmpty()) {
                            val first = locations.first()
                            controller.setCenter(GeoPoint(first.latitude, first.longitude))
                        } else if (currentLat != null && currentLng != null) {
                            controller.setCenter(GeoPoint(currentLat, currentLng))
                        }

                        val points = locations.map { GeoPoint(it.latitude, it.longitude) }
                        if (points.size >= 2) {
                            val line = Polyline().apply { setPoints(points) }
                            overlays.add(line)
                        }

                        locations.forEach { loc ->
                            val marker = Marker(this).apply {
                                position = GeoPoint(loc.latitude, loc.longitude)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                val df = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                                title = df.format(Date(loc.timestamp))
                                snippet = "${String.format("%.6f", loc.latitude)}, ${String.format("%.6f", loc.longitude)}"
                            }
                            overlays.add(marker)
                        }

                        if (currentLat != null && currentLng != null) {
                            val currentMarker = Marker(this).apply {
                                position = GeoPoint(currentLat, currentLng)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Current Location"
                                setIcon(androidx.core.content.ContextCompat.getDrawable(ctx, org.osmdroid.library.R.drawable.marker_default))
                            }
                            overlays.add(currentMarker)
                        }

                        invalidate()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onGetCurrentLocation,
                    enabled = !isGettingLocation,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary01.copy(alpha = 0.9f))
                ) {
                    if (isGettingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AppColors.darkTextPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(16.dp), tint = AppColors.darkTextPrimary)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Current", color = AppColors.darkTextPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${locations.size} location(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textSecondary
                )
                if (totalSharedCount > 0) {
                    TextButton(onClick = onClearSent) {
                        Icon(Icons.Default.ClearAll, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear Sent ($totalSharedCount)")
                    }
                }
            }
        }

        items(locations) { location ->
            LocationItem(
                location = location,
                onDelete = { onDelete(location.id) }
            )
        }
    }
}

@Composable
private fun LocationItem(
    location: LocationTelemetryEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface.copy(alpha = 0.05f))
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
                tint = if (location.isSent) AppColors.success500 else AppColors.warning500,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(location.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
                Text(
                    text = if (location.isSent) "Synced" else "Pending",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (location.isSent) AppColors.success500 else AppColors.warning500
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = AppColors.error500, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun TextButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.TextButton(onClick = onClick) { content() }
}
