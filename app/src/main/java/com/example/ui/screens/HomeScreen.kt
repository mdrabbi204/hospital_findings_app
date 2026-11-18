package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.BangladeshHealthcareCatalog
import com.example.data.local.HospitalDiagnosticPriceEntity
import com.example.data.model.AppNotification
import com.example.data.model.DiagnosticTest
import com.example.data.model.Doctor
import com.example.data.model.Hospital
import com.example.data.model.UserProfile
import com.example.data.repository.AreaHospitalRemoteDataSource
import com.example.ui.components.RatingPill
import com.example.ui.components.VerifiedChip
import com.example.ui.state.LocalizedText
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MonoNumericStyle
import com.example.ui.theme.WarningAmber
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    allHospitals: List<Hospital>,
    allDoctors: List<Doctor> = BangladeshHealthcareCatalog.doctors,
    allDiagnosticTests: List<DiagnosticTest> = BangladeshHealthcareCatalog.diagnosticTests,
    profile: UserProfile?,
    isSignedIn: Boolean,
    language: String,
    strings: LocalizedText,
    globalSearch: String,
    isSearchingLive: Boolean,
    onSearchChange: (String) -> Unit,
    onSearchLive: (String) -> Unit,
    onOpenHospitalsWithQuery: (String) -> Unit,
    distanceKmProvider: (Hospital) -> Double,
    recentHospitalIds: List<String>,
    recentDoctorIds: List<String>,
    notifications: List<AppNotification>,
    onMarkNotificationRead: (String) -> Unit,
    onToggleLanguage: () -> Unit,
    onRequestSignIn: () -> Unit,
    onOpenEmergency: () -> Unit,
    onOpenAppointments: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenDiagnosticTab: () -> Unit,
    onOpenHospitalsTab: (String?) -> Unit,
    onOpenDoctorsWithSpecialization: (String) -> Unit,
    onOpenHospitalDetail: (Hospital) -> Unit,
    onOpenDoctorDetail: (Doctor) -> Unit,
    onOpenTestDetail: (DiagnosticTest) -> Unit,
    diagnosticPrices: List<HospitalDiagnosticPriceEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    var showNotificationsSheet by remember { mutableStateOf(false) }
    val unreadCount = notifications.count { !it.read }
    val isBn = language == "bn"

    val q = globalSearch.trim()
    LaunchedEffect(q) {
        if (q.length >= 3) {
            onSearchLive(q)
        }
    }

    val filteredHospitals = remember(q, allHospitals) {
        if (q.isBlank()) allHospitals
        else allHospitals.filter { AreaHospitalRemoteDataSource.matchesQuery(it, q) }
    }
    val nearbyHospitals = remember(filteredHospitals, profile?.area) {
        filteredHospitals.sortedBy { distanceKmProvider(it) }.take(8)
    }
    val topDoctors = remember(q, allDoctors) {
        val base = if (q.isBlank()) allDoctors
        else allDoctors.filter {
            it.name.contains(q, true) || it.specialization.contains(q, true) ||
                it.chambers.any { c -> c.hospitalName.contains(q, true) || c.area.contains(q, true) }
        }
        base.sortedByDescending { it.rating }.take(6)
    }
    val featuredTests = remember(q, allDiagnosticTests) {
        if (q.isBlank()) allDiagnosticTests.take(6)
        else allDiagnosticTests.filter {
            it.name.contains(q, true) || it.shortName.contains(q, true) ||
                it.category.contains(q, true) || it.subcategory.contains(q, true) ||
                it.whatItDetects.contains(q, true) ||
                it.relatedAliases.any { alias -> alias.contains(q, true) }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header + Location + Language + Notifications
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenHospitalsTab(null) }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Selected area",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MedCare BD · ${profile?.division ?: "Dhaka"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${profile?.area ?: "Dhanmondi"}, ${profile?.district ?: "Dhaka"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.testTag("language_toggle_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = "Switch language",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "EN" else "বাংলা",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    IconButton(
                        onClick = { showNotificationsSheet = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text("$unreadCount") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                }
            }
        }

        // Hero Card with Search
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onOpenHospitalsTab(null) }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_healthcare_1794986471297),
                        contentDescription = "Healthcare discovery banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xDC003732), Color(0x66005048), Color.Transparent)
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(18.dp)
                            .fillMaxWidth(0.75f)
                    ) {
                        Text(
                            text = if (isBn) "সুস্থতার জন্য সঠিক সেবা" else "Trusted Care Across Bangladesh",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isBn) "হাসপাতাল, বিশেষজ্ঞ ডাক্তার ও ডায়াগনস্টিক তুলনা" else "Compare hospitals, chamber slots & diagnostic tests in BDT (৳)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = globalSearch,
                    onValueChange = onSearchChange,
                    placeholder = { Text(strings.searchPlaceholder) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (globalSearch.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchChange("") },
                                modifier = Modifier.testTag("home_search_clear")
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (q.isNotBlank()) onOpenHospitalsWithQuery(q)
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )

                if (q.length >= 2) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        onClick = { onOpenHospitalsWithQuery(q) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_all_maps_button")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Public,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isSearchingLive) {
                                            "Searching Google Maps & directory for \"$q\"..."
                                        } else {
                                            "Search \"$q\" in All Hospitals & Google Maps (${filteredHospitals.size} found)"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open full search",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (isSearchingLive) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
                            }
                        }
                    }
                }
            }
        }

        // 24/7 Emergency Banner
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenEmergency)
                    .testTag("emergency_banner_card")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_emergency_care_1794986472465),
                    contentDescription = "Emergency services banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    EmergencyRed.copy(alpha = 0.94f),
                                    EmergencyRed.copy(alpha = 0.72f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Emergency,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.emergencyTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.emergencySubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.92f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open emergency services",
                        tint = Color.White
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    icon = Icons.Filled.CalendarMonth,
                    label = strings.quickAppointments,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = onOpenAppointments,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_appointments_button")
                )
                QuickActionTile(
                    icon = Icons.Filled.CompareArrows,
                    label = strings.quickCompare,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onOpenCompare,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_compare_button")
                )
                QuickActionTile(
                    icon = Icons.Filled.Science,
                    label = strings.quickTests,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = onOpenDiagnosticTab,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_tests_button")
                )
                QuickActionTile(
                    icon = Icons.Filled.FolderShared,
                    label = strings.quickRecords,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onOpenRecords,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_records_button")
                )
            }
        }

        // Popular Specializations
        item {
            Column {
                SectionHeader(
                    title = if (isBn) "জনপ্রিয় বিশেষজ্ঞ বিভাগ" else "Popular Specializations",
                    actionText = "All Doctors",
                    onAction = { onOpenDoctorsWithSpecialization("All") }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BangladeshHealthcareCatalog.specializations) { spec ->
                        FilterChip(
                            selected = false,
                            onClick = { onOpenDoctorsWithSpecialization(spec) },
                            label = { Text(spec) }
                        )
                    }
                }
            }
        }

        // Nearby & Popular Hospitals
        item {
            Column {
                SectionHeader(
                    title = strings.nearbyHospitals,
                    actionText = "Map & All",
                    onAction = {
                        if (q.isNotBlank()) onOpenHospitalsWithQuery(q) else onOpenHospitalsTab(null)
                    }
                )
                if (nearbyHospitals.isEmpty() && q.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clickable { onOpenHospitalsWithQuery(q) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isSearchingLive) "Searching Live Maps for \"$q\"..." else "Tap to search \"$q\" across Google Maps & Bangladesh",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Opens full live map and directory search across all Dhaka areas and districts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(nearbyHospitals, key = { it.id }) { hospital ->
                        val km = distanceKmProvider(hospital)
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .width(272.dp)
                                .clickable { onOpenHospitalDetail(hospital) }
                                .testTag("home_hospital_${hospital.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = hospital.type.labelEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    RatingPill(rating = hospital.rating)
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = if (isBn) hospital.nameBn else hospital.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${hospital.area}, ${hospital.district} · ${String.format(Locale.US, "%.1f", km)} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (hospital.verified) VerifiedChip(strings.verifiedBadge)
                                    if (hospital.hasIcu) FacilityTag("ICU")
                                    if (hospital.hasEmergency247) FacilityTag("24/7 ER", isUrgent = true)
                                }
                            }
                        }
                    }
                    }
                }
            }
        }

        // Top-Rated Doctors
        item {
            Column {
                SectionHeader(
                    title = strings.topDoctors,
                    actionText = "Directory",
                    onAction = { onOpenDoctorsWithSpecialization("All") }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(topDoctors, key = { it.id }) { doctor ->
                        val primaryChamber = doctor.chambers.firstOrNull()
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .width(260.dp)
                                .clickable { onOpenDoctorDetail(doctor) }
                                .testTag("home_doctor_${doctor.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = doctor.specialization,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    RatingPill(rating = doctor.rating)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = if (isBn) doctor.nameBn else doctor.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = doctor.qualification,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(8.dp))
                                if (primaryChamber != null) {
                                    Text(
                                        text = primaryChamber.hospitalName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Fee ৳${primaryChamber.consultationFeeBdt}",
                                            style = MonoNumericStyle,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${doctor.experienceYears} yrs exp",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diagnostic Banner + Services
        item {
            Column {
                SectionHeader(
                    title = strings.diagnosticServices,
                    actionText = "All Tests",
                    onAction = onOpenDiagnosticTab
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(106.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenDiagnosticTab)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_banner_diagnostic_1794986472392),
                        contentDescription = "Diagnostic tests banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xDE0E3C54), Color(0x55124E66))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (isBn) "ল্যাব টেস্টের খরচ তুলনা ও হোম কালেকশন" else "Compare Lab Prices & Home Sample Collection",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Live official hospital prices · Prep guide · Home collection",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(featuredTests, key = { it.id }) { test ->
                        val minVerifiedPrice = diagnosticPrices
                            .filter {
                                it.canonicalTestId == test.id &&
                                    it.priceAvailable &&
                                    it.currentPriceBdt != null &&
                                    it.verificationStatus == "VERIFIED"
                            }
                            .mapNotNull { it.currentPriceBdt }
                            .minOrNull()
                        val minUnverifiedPrice = diagnosticPrices
                            .filter {
                                it.canonicalTestId == test.id &&
                                    it.priceAvailable &&
                                    it.currentPriceBdt != null &&
                                    it.verificationStatus == "UNVERIFIED"
                            }
                            .mapNotNull { it.currentPriceBdt }
                            .minOrNull()
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .width(215.dp)
                                .clickable { onOpenTestDetail(test) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (test.shortName.isNotBlank()) "${test.category} · ${test.shortName}" else test.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (isBn) test.nameBn else test.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(8.dp))
                                when {
                                    minVerifiedPrice != null -> Text(
                                        text = "Verified: From ৳$minVerifiedPrice",
                                        style = MonoNumericStyle,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    minUnverifiedPrice != null -> Text(
                                        text = "Unverified: From ৳$minUnverifiedPrice",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = WarningAmber
                                    )
                                    else -> Text(
                                        text = "Price Not Available",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recently Viewed (if any)
        val recentHospitals = allHospitals.filter { it.id in recentHospitalIds }
        val recentDoctors = allDoctors.filter { it.id in recentDoctorIds }
        if (recentHospitals.isNotEmpty() || recentDoctors.isNotEmpty()) {
            item {
                Column {
                    SectionHeader(title = strings.recentlyViewed, actionText = null, onAction = {})
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(recentHospitals, key = { "rec_h_${it.id}" }) { h ->
                            Surface(
                                onClick = { onOpenHospitalDetail(h) },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp
                            ) {
                                Column(modifier = Modifier.padding(12.dp).width(170.dp)) {
                                    Text("Hospital", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(h.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(h.area, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        items(recentDoctors, key = { "rec_d_${it.id}" }) { d ->
                            Surface(
                                onClick = { onOpenDoctorDetail(d) },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp
                            ) {
                                Column(modifier = Modifier.padding(12.dp).width(170.dp)) {
                                    Text(d.specialization, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                    Text(d.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(d.qualification, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNotificationsSheet) {
        ModalBottomSheet(onDismissRequest = { showNotificationsSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Notifications", style = MaterialTheme.typography.titleLarge)
                if (!isSignedIn) {
                    Text(
                        "Sign in with Google to receive appointment confirmations and lab test notifications.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    androidx.compose.material3.Button(
                        onClick = {
                            showNotificationsSheet = false
                            onRequestSignIn()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign in with Google")
                    }
                } else if (notifications.isEmpty()) {
                    Text(
                        "No notifications yet. Appointment & diagnostic booking updates will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    notifications.take(8).forEach { item ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.read) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMarkNotificationRead(item.id) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(item.title, style = MaterialTheme.typography.titleSmall)
                                Text(item.body, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = modifier.height(82.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null) {
            TextButton(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun FacilityTag(text: String, isUrgent: Boolean = false) {
    Surface(
        color = if (isUrgent) EmergencyRed.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUrgent) EmergencyRed else MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}
