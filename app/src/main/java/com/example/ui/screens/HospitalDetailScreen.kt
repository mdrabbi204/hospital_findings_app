package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.catalog.BangladeshHealthcareCatalog
import com.example.data.catalog.MedicalTestMasterCatalog
import com.example.data.local.DiagnosticPriceHistoryEntity
import com.example.data.local.HospitalDiagnosticPriceEntity
import com.example.data.local.HospitalPriceSyncMetaEntity
import com.example.data.local.toFallbackDiagnosticTest
import com.example.data.model.DiagnosticTest
import com.example.data.model.Doctor
import com.example.data.model.Hospital
import com.example.data.model.Review
import com.example.data.repository.LocationHelper
import com.example.data.sync.DiagnosticSyncScheduler
import com.example.ui.components.FavoriteIconButton
import com.example.ui.components.RatingPill
import com.example.ui.components.ReviewCardItem
import com.example.ui.components.SubmitReviewDialog
import com.example.ui.components.VerifiedChip
import com.example.ui.state.UiState
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MonoNumericStyle
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.WarningAmber
import java.util.Locale

private data class HospitalDiagnosticRowItem(
    val testId: String,
    val testName: String,
    val shortName: String,
    val category: String,
    val subcategory: String,
    val reportDeliveryLabel: String,
    val priceEntity: HospitalDiagnosticPriceEntity?,
    val openableTest: DiagnosticTest?
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HospitalDetailScreen(
    hospital: Hospital,
    isSignedIn: Boolean,
    distanceKm: Double,
    isFavorite: Boolean,
    isCompared: Boolean,
    reviewsState: UiState<List<Review>>,
    onObserveReviews: (String, String) -> Unit,
    onRequestSignIn: () -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleCompare: () -> Unit,
    onOpenDoctor: (Doctor) -> Unit,
    onOpenTest: (DiagnosticTest) -> Unit,
    onSubmitReview: (Int, Int, Int, Int, Int, String) -> Unit,
    onReportReview: (String, String) -> Unit,
    allDoctors: List<Doctor> = BangladeshHealthcareCatalog.doctors,
    allTests: List<DiagnosticTest> = BangladeshHealthcareCatalog.diagnosticTests,
    diagnosticPrices: List<HospitalDiagnosticPriceEntity> = emptyList(),
    priceHistory: List<DiagnosticPriceHistoryEntity> = emptyList(),
    syncMeta: HospitalPriceSyncMetaEntity? = null,
    isSyncingPrices: Boolean = false,
    onSyncHospitalPrices: () -> Unit = {}
) {
    val context = LocalContext.current
    var showReviewDialog by remember { mutableStateOf(false) }
    var showPriceHistoryDialog by remember { mutableStateOf(false) }
    var reportingReviewId by remember { mutableStateOf<String?>(null) }
    var reportReason by remember { mutableStateOf("") }

    LaunchedEffect(hospital.id) {
        onObserveReviews(hospital.id, "hospital")
    }

    val doctorsHere = remember(hospital.id, allDoctors) {
        allDoctors.filter { doc ->
            doc.chambers.any { it.hospitalId == hospital.id }
        }
    }

    val hospitalDiagnosticRows = remember(hospital, allTests, diagnosticPrices) {
        val priceByTestId = diagnosticPrices.associateBy { it.canonicalTestId }
        val catalogTestById = allTests.associateBy { it.id }

        val rows = mutableListOf<HospitalDiagnosticRowItem>()
        val seenIds = mutableSetOf<String>()

        // 1. Include synced test records (skip invalidated unavailable rows for tests not offered at this facility)
        diagnosticPrices.forEach { entity ->
            val catalogTest = catalogTestById[entity.canonicalTestId]
            val hasPrice = entity.priceAvailable && entity.currentPriceBdt != null
            if (catalogTest != null && !hasPrice &&
                !MedicalTestMasterCatalog.isTestOfferedAtHospital(catalogTest, hospital, false)
            ) {
                return@forEach
            }
            seenIds += entity.canonicalTestId
            val linkedTest = catalogTest ?: entity.toFallbackDiagnosticTest()
            rows += HospitalDiagnosticRowItem(
                testId = entity.canonicalTestId,
                testName = linkedTest.name,
                shortName = linkedTest.shortName,
                category = linkedTest.category,
                subcategory = linkedTest.subcategory,
                reportDeliveryLabel = "Report in ${linkedTest.estimatedDelivery}",
                priceEntity = entity,
                openableTest = linkedTest
            )
        }

        // 2. Include master catalog tests realistically offered by this hospital's specialties/capabilities
        allTests.filter { test ->
            MedicalTestMasterCatalog.isTestOfferedAtHospital(
                test = test,
                hospital = hospital,
                hasLiveSyncedRecord = false
            )
        }.forEach { test ->
            if (seenIds.add(test.id)) {
                rows += HospitalDiagnosticRowItem(
                    testId = test.id,
                    testName = test.name,
                    shortName = test.shortName,
                    category = test.category,
                    subcategory = test.subcategory,
                    reportDeliveryLabel = "Report: ${test.estimatedDelivery}",
                    priceEntity = priceByTestId[test.id],
                    openableTest = test
                )
            }
        }

        rows.sortedWith(
            compareByDescending<HospitalDiagnosticRowItem> {
                it.priceEntity?.priceAvailable == true && it.priceEntity.verificationStatus != "UNVERIFIED"
            }
                .thenByDescending { it.priceEntity?.priceAvailable == true }
                .thenBy { it.priceEntity?.currentPriceBdt ?: Int.MAX_VALUE }
                .thenBy { it.testName }
        )
    }

    val hospitalHistory = remember(hospital.id, priceHistory) {
        priceHistory.filter { it.hospitalId == hospital.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hospital Profile", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("hospital_detail_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FavoriteIconButton(
                        isFavorite = isFavorite,
                        onClick = onToggleFavorite,
                        testTag = "hospital_detail_favorite"
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = hospital.type.labelEn,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                if (hospital.verified) VerifiedChip()
                                if (hospital.dataSource != "Directory") {
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = hospital.dataSource,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            RatingPill(hospital.rating, hospital.reviewCount)
                        }

                        Spacer(Modifier.height(10.dp))
                        Text(hospital.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (hospital.nameBn != hospital.name) {
                            Text(hospital.nameBn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(hospital.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${hospital.address} (${String.format(Locale.US, "%.1f", distanceKm)} km away)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(hospital.operatingHours, style = MaterialTheme.typography.bodySmall)
                        }
                        val contactWeb = listOf(hospital.website, hospital.email)
                            .filter { it.isNotBlank() }
                            .joinToString(" · ")
                        if (contactWeb.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(contactWeb, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { LocationHelper.openDialer(context, hospital.phone) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("hospital_call_button")
                            ) {
                                Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Call")
                            }
                            Button(
                                onClick = {
                                    LocationHelper.openGoogleMapsDirections(
                                        context, hospital.latitude, hospital.longitude, hospital.name
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("hospital_directions_button")
                            ) {
                                Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Maps")
                            }
                            OutlinedButton(
                                onClick = onToggleCompare,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("hospital_detail_compare")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = "Compare", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isCompared) "Added" else "Compare")
                            }
                        }
                    }
                }
            }

            // Facilities & Departments
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Facilities & Critical Care", style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (hospital.hasEmergency247) FacilityTag("24/7 Emergency", isUrgent = true)
                            if (hospital.hasIcu) FacilityTag("ICU Available")
                            if (hospital.hasCcu) FacilityTag("CCU Available")
                            if (hospital.hasNicu) FacilityTag("NICU Available")
                            if (hospital.hasAmbulance) FacilityTag("Ambulance")
                            if (hospital.hasBloodBank) FacilityTag("Blood Bank")
                            hospital.amenities.forEach { FacilityTag(it) }
                        }
                        Text("Departments", style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            hospital.departments.forEach { dept ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = dept,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Doctors at this hospital
            item {
                Text("Available Doctors (${doctorsHere.size})", style = MaterialTheme.typography.titleLarge)
            }
            if (doctorsHere.isEmpty()) {
                item {
                    Text(
                        "Contact hospital desk for visiting specialist schedules.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(doctorsHere, key = { it.id }) { doc ->
                    val chamber = doc.chambers.firstOrNull { it.hospitalId == hospital.id }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDoctor(doc) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(doc.name, style = MaterialTheme.typography.titleMedium)
                                Text("${doc.specialization} · ${doc.qualification}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (chamber != null) {
                                    Text(chamber.availableDaysSummary, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (chamber != null) {
                                    Text("৳${chamber.consultationFeeBdt}", style = MonoNumericStyle, color = MaterialTheme.colorScheme.primary)
                                }
                                Text("Book →", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }

            // Real-Time Diagnostic & Testing Services Section
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Diagnostic Test Prices (${hospitalDiagnosticRows.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (syncMeta != null) {
                                        "Last checked: ${DiagnosticSyncScheduler.formatTimestamp(syncMeta.lastSyncedMillis)}"
                                    } else {
                                        "Auto-sync checks official portal every 6h"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (hospitalHistory.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { showPriceHistoryDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("hospital_price_history_button")
                                    ) {
                                        Icon(Icons.Filled.History, contentDescription = "Price History", modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("History (${hospitalHistory.size})")
                                    }
                                }
                                Button(
                                    onClick = onSyncHospitalPrices,
                                    enabled = !isSyncingPrices,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("hospital_sync_prices_button")
                                ) {
                                    if (isSyncingPrices) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(Icons.Filled.Sync, contentDescription = "Sync Prices", modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isSyncingPrices) "Syncing" else "Sync")
                                }
                            }
                        }

                        if (syncMeta != null) {
                            Text(
                                text = syncMeta.statusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (syncMeta.syncState == "VERIFIED") VerifiedGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Tap Sync to fetch latest verified prices directly from the hospital's official portal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (hospitalDiagnosticRows.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Price Not Available", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "No verified online diagnostic test price list was found for ${hospital.name}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(hospitalDiagnosticRows, key = { it.testId }) { row ->
                    val entity = row.priceEntity
                    val livePrice = if (entity != null && entity.priceAvailable) entity.currentPriceBdt else null
                    val isVerified = livePrice != null && entity != null && entity.verificationStatus != "UNVERIFIED"
                    val isUnverified = livePrice != null && entity != null && entity.verificationStatus == "UNVERIFIED"
                    val badgeText = when {
                        isVerified -> "Verified Official"
                        isUnverified -> "Unverified Source"
                        else -> "Price Not Available"
                    }
                    val badgeColor = when {
                        isVerified -> VerifiedGreen
                        isUnverified -> WarningAmber
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = row.openableTest != null) {
                                row.openableTest?.let(onOpenTest)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (row.shortName.isNotBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = row.shortName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Surface(
                                        color = badgeColor.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = badgeColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(row.testName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                val catLine = if (row.subcategory.isNotBlank()) {
                                    "${row.category} · ${row.subcategory} · ${row.reportDeliveryLabel}"
                                } else {
                                    "${row.category} · ${row.reportDeliveryLabel}"
                                }
                                Text(
                                    text = catLine,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (livePrice != null && entity != null) {
                                    Text(
                                        text = "Updated ${DiagnosticSyncScheduler.formatTimestamp(entity.lastUpdatedMillis)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColor
                                    )
                                    val prev = entity.previousPriceBdt
                                    if (prev != null && prev != livePrice) {
                                        val delta = livePrice - prev
                                        val arrow = if (delta > 0) "▲ +৳$delta" else "▼ -৳${-delta}"
                                        Text(
                                            text = "Previous: ৳$prev ($arrow)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (delta > 0) EmergencyRed else VerifiedGreen
                                        )
                                    }
                                    if (entity.sourceUrl.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                runCatching {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(entity.sourceUrl)))
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = "Open source URL",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = entity.sourceUrl,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                textDecoration = TextDecoration.Underline,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = entity?.statusNote?.takeIf { it.isNotBlank() }
                                            ?: syncMeta?.let { "Checked ${DiagnosticSyncScheduler.formatRelativeTime(it.lastSyncedMillis)}" }
                                            ?: "Awaiting official online source",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                if (livePrice != null) {
                                    Text(
                                        text = "৳$livePrice",
                                        style = MonoNumericStyle,
                                        color = if (isVerified) MaterialTheme.colorScheme.primary else WarningAmber
                                    )
                                    Text(
                                        text = "Details →",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                } else {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Price Not Available",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Ratings & Reviews
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Patient Reviews", style = MaterialTheme.typography.titleLarge)
                    Button(
                        onClick = {
                            if (isSignedIn) showReviewDialog = true else onRequestSignIn()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("write_hospital_review_button")
                    ) {
                        Icon(Icons.Filled.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Write Review")
                    }
                }
            }

            if (!isSignedIn) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Sign in with Google to read community reviews and publish your own rating for ${hospital.name}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(onClick = onRequestSignIn, shape = RoundedCornerShape(12.dp)) {
                                Text("Sign in with Google")
                            }
                        }
                    }
                }
            } else {
                when (reviewsState) {
                    is UiState.Loading -> item {
                        Text("Loading community reviews...", style = MaterialTheme.typography.bodyMedium)
                    }
                    is UiState.Error -> item {
                        Text(reviewsState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    is UiState.Success -> {
                        val reviews = reviewsState.data
                        if (reviews.isEmpty()) {
                            item {
                                Text(
                                    "No patient reviews submitted yet. Be the first to review this hospital!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(reviews, key = { it.id }) { rev ->
                                ReviewCardItem(
                                    review = rev,
                                    onReportClick = { reportingReviewId = rev.id }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReviewDialog) {
        SubmitReviewDialog(
            targetName = hospital.name,
            onDismiss = { showReviewDialog = false },
            onSubmit = { overall, doc, staff, wait, clean, comment ->
                onSubmitReview(overall, doc, staff, wait, clean, comment)
                showReviewDialog = false
            }
        )
    }

    if (reportingReviewId != null) {
        AlertDialog(
            onDismissRequest = { reportingReviewId = null },
            title = { Text("Report Inappropriate Review") },
            text = {
                OutlinedTextField(
                    value = reportReason,
                    onValueChange = { reportReason = it },
                    label = { Text("Reason") },
                    placeholder = { Text("e.g. Spam, abusive language, off-topic") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReportReview(reportingReviewId!!, reportReason.ifBlank { "Inappropriate content" })
                        reportingReviewId = null
                        reportReason = ""
                    }
                ) { Text("Report") }
            },
            dismissButton = {
                TextButton(onClick = { reportingReviewId = null }) { Text("Cancel") }
            }
        )
    }

    if (showPriceHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showPriceHistoryDialog = false },
            title = { Text("Price History · ${hospital.name}") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (hospitalHistory.isEmpty()) {
                        Text(
                            "No price change history recorded yet for this hospital.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        hospitalHistory.forEach { entry ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(entry.testName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                val prevStr = entry.previousPriceBdt?.let { "৳$it" } ?: "None"
                                val currStr = entry.currentPriceBdt?.let { "৳$it" } ?: "Price Not Available"
                                Text(
                                    text = "$prevStr → $currStr (${entry.changeReason.replace('_', ' ')})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = DiagnosticSyncScheduler.formatTimestamp(entry.changedAtMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                HorizontalDivider()
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPriceHistoryDialog = false }) { Text("Close") }
            }
        )
    }
}

