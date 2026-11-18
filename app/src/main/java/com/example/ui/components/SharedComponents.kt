package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.model.FacilityType
import com.example.data.model.Hospital
import com.example.data.model.Review
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MonoNumericStyle
import com.example.ui.theme.RatingGold
import com.example.ui.theme.VerifiedGreen
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale
import kotlin.math.hypot

@Composable
fun VerifiedChip(label: String = "Verified") {
    Surface(
        color = VerifiedGreen.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = label,
                tint = VerifiedGreen,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = VerifiedGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RatingPill(rating: Double, reviewCount: Int? = null) {
    Surface(
        color = RatingGold.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = RatingGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = String.format(Locale.US, "%.1f", rating) + (reviewCount?.let { " ($it)" } ?: ""),
                style = MonoNumericStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FavoriteIconButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = if (isFavorite) "Remove from saved" else "Save",
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FacilityMapPanel(
    hospitals: List<Hospital>,
    userCoordinates: Pair<Double, Double>,
    hasLocationPermission: Boolean,
    selectedHospital: Hospital?,
    onSelectHospital: (Hospital) -> Unit,
    onOpenHospitalDetail: (Hospital) -> Unit,
    onDirectionsClick: (Hospital) -> Unit,
    onRequestLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mapsKey = BuildConfig.MAPS_API_KEY
    val hasValidMapsKey = mapsKey.isNotBlank() && mapsKey != "MY_MAPS_API_KEY"

    Box(modifier = modifier.fillMaxSize()) {
        if (hasValidMapsKey) {
            val centerLatLng = selectedHospital?.let { LatLng(it.latitude, it.longitude) }
                ?: hospitals.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
                ?: LatLng(userCoordinates.first, userCoordinates.second)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(centerLatLng, 12.2f)
            }
            LaunchedEffect(centerLatLng) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(centerLatLng, 12.6f)
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false)
            ) {
                hospitals.forEach { hospital ->
                    val hue = when (hospital.type) {
                        FacilityType.DIAGNOSTIC_CENTER -> BitmapDescriptorFactory.HUE_AZURE
                        FacilityType.GOVERNMENT, FacilityType.MEDICAL_COLLEGE -> BitmapDescriptorFactory.HUE_GREEN
                        else -> BitmapDescriptorFactory.HUE_RED
                    }
                    Marker(
                        state = MarkerState(position = LatLng(hospital.latitude, hospital.longitude)),
                        title = hospital.name,
                        snippet = "${hospital.area}, ${hospital.district}",
                        icon = BitmapDescriptorFactory.defaultMarker(hue),
                        onClick = {
                            onSelectHospital(hospital)
                            true
                        }
                    )
                }
            }
        } else {
            InteractiveVectorFacilityMap(
                hospitals = hospitals,
                userCoordinates = userCoordinates,
                selectedHospital = selectedHospital,
                onSelectHospital = onSelectHospital,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top overlay info & GPS button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(50.dp),
                shadowElevation = 3.dp
            ) {
                Text(
                    text = if (hasValidMapsKey) {
                        "Google Maps · ${hospitals.size} facilities"
                    } else {
                        "Interactive Map (${hospitals.size} pins) · Set MAPS_API_KEY in Secrets for tiles"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
            Surface(
                onClick = onRequestLocation,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("map_my_location_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Use my GPS location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Selected Hospital Bottom Preview Card
        val active = selectedHospital ?: hospitals.firstOrNull()
        if (active != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = active.type.labelEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (active.hasEmergency247) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "24/7 ER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmergencyRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = active.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${active.area}, ${active.district}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RatingPill(rating = active.rating)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onDirectionsClick(active) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Navigate")
                        }
                        Button(
                            onClick = { onOpenHospitalDetail(active) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("View Details")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveVectorFacilityMap(
    hospitals: List<Hospital>,
    userCoordinates: Pair<Double, Double>,
    selectedHospital: Hospital?,
    onSelectHospital: (Hospital) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val roadColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    val bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val userDotColor = MaterialTheme.colorScheme.secondary
    val selectedColor = MaterialTheme.colorScheme.primary

    val allLats = hospitals.map { it.latitude } + userCoordinates.first
    val allLngs = hospitals.map { it.longitude } + userCoordinates.second
    val minLat = (allLats.minOrNull() ?: 23.70) - 0.02
    val maxLat = (allLats.maxOrNull() ?: 23.90) + 0.02
    val minLng = (allLngs.minOrNull() ?: 90.35) - 0.02
    val maxLng = (allLngs.maxOrNull() ?: 90.45) + 0.02

    Box(
        modifier = modifier
            .background(bgColor)
            .pointerInput(hospitals, userCoordinates) {
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat().coerceAtLeast(1f)
                    val h = size.height.toFloat().coerceAtLeast(1f)
                    val pad = 64f
                    val usableW = (w - pad * 2).coerceAtLeast(1f)
                    val usableH = (h - pad * 3).coerceAtLeast(1f)
                    val hit = hospitals.minByOrNull { hospital ->
                        val nx = ((hospital.longitude - minLng) / (maxLng - minLng).coerceAtLeast(0.001)).toFloat()
                        val ny = (1.0 - (hospital.latitude - minLat) / (maxLat - minLat).coerceAtLeast(0.001)).toFloat()
                        val px = pad + nx * usableW
                        val py = pad + ny * usableH
                        hypot(tapOffset.x - px, tapOffset.y - py)
                    }
                    if (hit != null) onSelectHospital(hit)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val step = 64f
            var x = 0f
            while (x < w) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < h) {
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                y += step
            }

            // Stylized Dhaka arterial roads
            drawLine(roadColor, Offset(w * 0.1f, h * 0.2f), Offset(w * 0.9f, h * 0.78f), strokeWidth = 10f)
            drawLine(roadColor, Offset(w * 0.25f, h * 0.85f), Offset(w * 0.8f, h * 0.15f), strokeWidth = 8f)

            val pad = 64f
            val usableW = (w - pad * 2).coerceAtLeast(1f)
            val usableH = (h - pad * 3).coerceAtLeast(1f)

            fun project(lat: Double, lng: Double): Offset {
                val nx = ((lng - minLng) / (maxLng - minLng).coerceAtLeast(0.001)).toFloat()
                val ny = (1.0 - (lat - minLat) / (maxLat - minLat).coerceAtLeast(0.001)).toFloat()
                return Offset(pad + nx * usableW, pad + ny * usableH)
            }

            // User location radar ring
            val userPt = project(userCoordinates.first, userCoordinates.second)
            drawCircle(
                color = userDotColor.copy(alpha = 0.16f),
                radius = 52f,
                center = userPt
            )
            drawCircle(
                color = userDotColor,
                radius = 12f,
                center = userPt
            )

            // Facility pins
            hospitals.forEach { hospital ->
                val pt = project(hospital.latitude, hospital.longitude)
                val isSelected = selectedHospital?.id == hospital.id
                val pinColor = when {
                    isSelected -> selectedColor
                    hospital.type == FacilityType.DIAGNOSTIC_CENTER -> Color(0xFF1E5C8A)
                    hospital.hasEmergency247 -> EmergencyRed
                    else -> selectedColor
                }
                if (isSelected) {
                    drawLine(
                        color = selectedColor.copy(alpha = 0.5f),
                        start = userPt,
                        end = pt,
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                    drawCircle(color = pinColor.copy(alpha = 0.24f), radius = 34f, center = pt)
                }
                drawCircle(color = Color.White, radius = if (isSelected) 20f else 15f, center = pt)
                drawCircle(color = pinColor, radius = if (isSelected) 15f else 11f, center = pt)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewCardItem(
    review: Review,
    onReportClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.authorName,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = if (review.verifiedBooking) {
                                VerifiedGreen.copy(alpha = 0.14f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = if (review.verifiedBooking) "Verified Visit" else "Unverified Review",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (review.verifiedBooking) VerifiedGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingPill(rating = review.overallRating.toDouble())
                    IconButton(
                        onClick = onReportClick,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = "Report inappropriate review",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniCriteriaChip("Doctor ${review.doctorBehaviorRating}/5")
                MiniCriteriaChip("Staff ${review.staffBehaviorRating}/5")
                MiniCriteriaChip("Wait ${review.waitingTimeRating}/5")
                MiniCriteriaChip("Clean ${review.cleanlinessRating}/5")
            }
        }
    }
}

@Composable
private fun MiniCriteriaChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SubmitReviewDialog(
    targetName: String,
    onDismiss: () -> Unit,
    onSubmit: (overall: Int, doctor: Int, staff: Int, waiting: Int, cleanliness: Int, comment: String) -> Unit
) {
    var overall by remember { mutableIntStateOf(5) }
    var doctor by remember { mutableIntStateOf(5) }
    var staff by remember { mutableIntStateOf(4) }
    var waiting by remember { mutableIntStateOf(4) }
    var cleanliness by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review $targetName", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RatingSliderRow("Overall Experience", overall) { overall = it }
                RatingSliderRow("Doctor Behavior", doctor) { doctor = it }
                RatingSliderRow("Staff Behavior", staff) { staff = it }
                RatingSliderRow("Waiting Time", waiting) { waiting = it }
                RatingSliderRow("Cleanliness", cleanliness) { cleanliness = it }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Write your review") },
                    placeholder = { Text("Share helpful details for patients...") },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (comment.trim().length >= 3) {
                        onSubmit(overall, doctor, staff, waiting, cleanliness, comment)
                    }
                },
                enabled = comment.trim().length >= 3
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RatingSliderRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("$value / 5", style = MonoNumericStyle.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize))
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt().coerceIn(1, 5)) },
            valueRange = 1f..5f,
            steps = 3
        )
    }
}
