package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.example.data.model.CenterTestPrice
import com.example.data.model.DiagnosticTest
import com.example.data.model.Hospital
import com.example.data.model.UserProfile
import com.example.data.repository.LocationHelper
import com.example.data.repository.OfficialHospitalPortalRegistry
import com.example.data.sync.DiagnosticSyncScheduler
import com.example.ui.state.DiagnosticFilterState
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MonoNumericStyle
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.WarningAmber
import java.util.Locale
import kotlin.math.roundToInt

private data class HospitalTestComparisonRow(
    val hospitalId: String,
    val hospitalName: String,
    val areaDistrict: String,
    val isOffered: Boolean,
    val homeCollectionAvailable: Boolean,
    val homeCollectionFeeBdt: Int,
    val reportDeliveryHours: Int,
    val currentPriceBdt: Int?,
    val previousPriceBdt: Int?,
    val priceAvailable: Boolean,
    val verificationStatus: String, // "VERIFIED" | "UNVERIFIED" | "UNAVAILABLE"
    val lastUpdatedMillis: Long,
    val sourceName: String,
    val sourceUrl: String,
    val statusNote: String,
    val hospitalModel: Hospital?
)

private enum class HospitalFilterTab(val label: String) {
    OFFERED_ONLY("Offered Here"),
    ALL_HOSPITALS("All Hospitals"),
    VERIFIED_ONLY("Verified Price")
}

@Composable
fun DiagnosticScreen(
    filter: DiagnosticFilterState,
    onFilterChange: ((DiagnosticFilterState) -> DiagnosticFilterState) -> Unit,
    onOpenTestDetail: (DiagnosticTest) -> Unit,
    allTests: List<DiagnosticTest> = BangladeshHealthcareCatalog.diagnosticTests,
    diagnosticPrices: List<HospitalDiagnosticPriceEntity> = emptyList(),
    syncMetaList: List<HospitalPriceSyncMetaEntity> = emptyList(),
    isSyncingPrices: Boolean = false,
    priceSyncStatus: String? = null,
    onSyncAllPrices: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val combinedTests = remember(allTests, diagnosticPrices) {
        val knownIds = allTests.map { it.id }.toHashSet()
        val extraTests = diagnosticPrices
            .filter { it.canonicalTestId !in knownIds }
            .groupBy { it.canonicalTestId }
            .map { (_, entries) ->
                entries.first().toFallbackDiagnosticTest()
            }
        allTests + extraTests
    }

    val categories = remember(combinedTests) {
        listOf("All") + combinedTests.map { it.category }.distinct().sorted()
    }

    val subcategories = remember(combinedTests, filter.category) {
        if (filter.category == "All") emptyList()
        else listOf("All") + combinedTests
            .filter { it.category == filter.category && it.subcategory.isNotBlank() }
            .map { it.subcategory }
            .distinct()
            .sorted()
    }

    val sampleFamilies = remember {
        listOf("All", "Blood", "Urine", "Stool", "Imaging / Scan", "Tissue / Swab")
    }

    val verifiedPricesByTestId = remember(diagnosticPrices) {
        diagnosticPrices
            .filter { it.priceAvailable && it.currentPriceBdt != null && it.verificationStatus == "VERIFIED" }
            .groupBy { it.canonicalTestId }
    }

    val unverifiedPricesByTestId = remember(diagnosticPrices) {
        diagnosticPrices
            .filter { it.priceAvailable && it.currentPriceBdt != null && it.verificationStatus == "UNVERIFIED" }
            .groupBy { it.canonicalTestId }
    }

    val latestSyncMillis = remember(syncMetaList) {
        syncMetaList.maxOfOrNull { it.lastSyncedMillis } ?: 0L
    }

    val filteredTests = remember(
        filter,
        combinedTests,
        verifiedPricesByTestId
    ) {
        val q = filter.query.trim()
        combinedTests.filter { t ->
            val matchesQuery = q.isBlank() ||
                t.name.contains(q, true) ||
                t.shortName.contains(q, true) ||
                t.nameBn.contains(q, true) ||
                t.category.contains(q, true) ||
                t.subcategory.contains(q, true) ||
                t.whatItDetects.contains(q, true) ||
                t.description.contains(q, true) ||
                t.sampleType.contains(q, true) ||
                t.relatedAliases.any { alias -> alias.contains(q, true) }

            val matchesCategory = filter.category == "All" || t.category == filter.category
            val matchesSubcategory = filter.subcategory == "All" || t.subcategory == filter.subcategory
            val matchesSample = when (filter.sampleFamily) {
                "Blood" -> t.sampleType.contains("blood", true) || t.sampleType.contains("serum", true) || t.sampleType.contains("plasma", true)
                "Urine" -> t.sampleType.contains("urine", true)
                "Stool" -> t.sampleType.contains("stool", true)
                "Imaging / Scan" -> t.sampleType.contains("ray", true) || t.sampleType.contains("ct", true) ||
                    t.sampleType.contains("mri", true) || t.sampleType.contains("ultrasound", true) ||
                    t.sampleType.contains("ecg", true) || t.sampleType.contains("echo", true) ||
                    t.sampleType.contains("imaging", true) || t.sampleType.contains("mammography", true)
                "Tissue / Swab" -> t.sampleType.contains("tissue", true) || t.sampleType.contains("swab", true) ||
                    t.sampleType.contains("cytology", true) || t.sampleType.contains("aspirate", true) ||
                    t.sampleType.contains("sputum", true) || t.sampleType.contains("biopsy", true)
                else -> true
            }
            val matchesHome = !filter.onlyHomeCollection || t.supportsHomeCollection
            val matchesVerified = !filter.onlyVerifiedPrice || verifiedPricesByTestId[t.id].orEmpty().isNotEmpty()

            matchesQuery && matchesCategory && matchesSubcategory && matchesSample && matchesHome && matchesVerified
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Medical Test Master Directory (${combinedTests.size} Tests)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = priceSyncStatus ?: if (latestSyncMillis > 0L) {
                                "Last synced: ${DiagnosticSyncScheduler.formatRelativeTime(latestSyncMillis)} · Verified portal sync 6h"
                            } else {
                                "Auto-syncs every 6h from official hospital portals"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSyncAllPrices,
                        enabled = !isSyncingPrices,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("sync_all_diagnostic_prices_button")
                    ) {
                        if (isSyncingPrices) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Filled.Sync, contentDescription = "Sync All Prices", modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(if (isSyncingPrices) "Syncing" else "Sync Now")
                    }
                }
            }

            OutlinedTextField(
                value = filter.query,
                onValueChange = { q -> onFilterChange { it.copy(query = q) } },
                placeholder = { Text("Search CBC, HbA1c, MRI, Thyroid, Biopsy, dengue...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("diagnostic_search_input")
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filter.onlyVerifiedPrice,
                        onClick = { onFilterChange { it.copy(onlyVerifiedPrice = !it.onlyVerifiedPrice) } },
                        leadingIcon = { Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Verified Price") }
                    )
                }
                item {
                    FilterChip(
                        selected = filter.onlyHomeCollection,
                        onClick = { onFilterChange { it.copy(onlyHomeCollection = !it.onlyHomeCollection) } },
                        leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Home Collection") }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = filter.category == cat,
                        onClick = { onFilterChange { it.copy(category = cat) } },
                        label = { Text(cat) }
                    )
                }
            }

            if (subcategories.size > 1) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subcategories) { sub ->
                        FilterChip(
                            selected = filter.subcategory == sub,
                            onClick = { onFilterChange { it.copy(subcategory = sub) } },
                            label = { Text(sub, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(sampleFamilies) { family ->
                    FilterChip(
                        selected = filter.sampleFamily == family,
                        onClick = { onFilterChange { it.copy(sampleFamily = family) } },
                        label = { Text(if (family == "All") "All Samples" else family, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        if (filteredTests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("No diagnostic tests found.", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Try clearing filters or searching by test abbreviation (e.g., CBC, TSH, MRI).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTests, key = { it.id }) { test ->
                    val verifiedList = verifiedPricesByTestId[test.id].orEmpty()
                    val unverifiedList = unverifiedPricesByTestId[test.id].orEmpty()
                    val verifiedNums = verifiedList.mapNotNull { it.currentPriceBdt }
                    val minVerified = verifiedNums.minOrNull()
                    val maxVerified = verifiedNums.maxOrNull()
                    val avgVerified = if (verifiedNums.isNotEmpty()) verifiedNums.average().roundToInt() else null
                    val latestUpdated = verifiedList.maxOfOrNull { it.lastUpdatedMillis }
                        ?: unverifiedList.maxOfOrNull { it.lastUpdatedMillis } ?: 0L
                    val minUnverified = unverifiedList.mapNotNull { it.currentPriceBdt }.minOrNull()

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTestDetail(test) }
                            .testTag("test_card_${test.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (test.subcategory.isNotBlank()) "${test.category} · ${test.subcategory}" else test.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    if (test.shortName.isNotBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = test.shortName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                                if (test.supportsHomeCollection) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = VerifiedGreen.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(50.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Icon(Icons.Filled.Home, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(3.dp))
                                            Text("Home", style = MaterialTheme.typography.labelSmall, color = VerifiedGreen)
                                        }
                                    }
                                }
                            }

                            Text(test.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Detects: ${test.whatItDetects.ifBlank { test.description }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Sample: ${test.sampleType} · Delivery: ${test.estimatedDelivery}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    when {
                                        minVerified != null && maxVerified != null && avgVerified != null -> {
                                            val rangeText = if (minVerified == maxVerified) "৳$minVerified" else "৳$minVerified – ৳$maxVerified"
                                            Text(
                                                text = "Verified: $rangeText",
                                                style = MonoNumericStyle,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Avg ৳$avgVerified · ${verifiedList.size} hospital(s) · ${DiagnosticSyncScheduler.formatRelativeTime(latestUpdated)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = VerifiedGreen,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        minUnverified != null -> {
                                            Text(
                                                text = "Unverified Source: From ৳$minUnverified",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = WarningAmber
                                            )
                                            Text(
                                                text = "${unverifiedList.size} unverified source(s) · Excluded from verified stats",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WarningAmber,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = "Price Not Available",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${test.centerPrices.size} known center(s) · Tap to inspect & sync",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { onOpenTestDetail(test) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Details")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TestDetailScreen(
    test: DiagnosticTest,
    profile: UserProfile?,
    onBack: () -> Unit,
    onBookTest: (
        center: CenterTestPrice,
        bookingDate: String,
        timeWindow: String,
        homeCollection: Boolean,
        address: String,
        patientName: String,
        patientPhone: String,
        totalPriceBdt: Int,
        onDone: () -> Unit
    ) -> Unit,
    allHospitals: List<Hospital> = emptyList(),
    diagnosticPrices: List<HospitalDiagnosticPriceEntity> = emptyList(),
    priceHistory: List<DiagnosticPriceHistoryEntity> = emptyList(),
    syncMetaList: List<HospitalPriceSyncMetaEntity> = emptyList(),
    isSyncingPrices: Boolean = false,
    onSyncPrices: () -> Unit = {},
    onOpenHospitalDetail: (Hospital) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedCenter by remember { mutableStateOf<CenterTestPrice?>(null) }
    var filterTab by remember { mutableStateOf(HospitalFilterTab.OFFERED_ONLY) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var bookingDate by remember { mutableStateOf(LocationHelper.todayIsoDate()) }
    var timeWindow by remember { mutableStateOf("08:00 - 10:00") }
    var homeCollection by remember { mutableStateOf(false) }
    var address by remember(profile?.area) {
        mutableStateOf("${profile?.area ?: "Dhanmondi"}, ${profile?.district ?: "Dhaka"}")
    }
    var patientName by remember(profile?.displayName) { mutableStateOf(profile?.displayName ?: "") }
    var patientPhone by remember(profile?.phone) { mutableStateOf(profile?.phone.orEmpty()) }

    val hospitalById = remember(allHospitals) { allHospitals.associateBy { it.id } }
    val syncMetaByHospitalId = remember(syncMetaList) { syncMetaList.associateBy { it.hospitalId } }

    val testHistory = remember(test.id, priceHistory) {
        priceHistory.filter { it.canonicalTestId == test.id }
    }

    val comparisonRows = remember(test, diagnosticPrices, allHospitals, hospitalById, syncMetaByHospitalId) {
        val dbForTest = diagnosticPrices.filter { it.canonicalTestId == test.id }
        val seenHospitals = mutableSetOf<String>()
        val rows = mutableListOf<HospitalTestComparisonRow>()

        // 1. Live synced hospital price entries
        dbForTest.forEach { entity ->
            seenHospitals += entity.hospitalId
            val hosp = hospitalById[entity.hospitalId]
            val catalogMeta = test.centerPrices.firstOrNull { it.centerId == entity.hospitalId }
            val hasPrice = entity.priceAvailable && entity.currentPriceBdt != null
            val offered = hosp?.let {
                MedicalTestMasterCatalog.isTestOfferedAtHospital(test, it, hasPrice)
            } ?: hasPrice
            val vStatus = if (hasPrice) {
                if (entity.verificationStatus == "UNVERIFIED") "UNVERIFIED" else "VERIFIED"
            } else {
                "UNAVAILABLE"
            }
            val resolvedUrl = entity.sourceUrl.ifBlank {
                hosp?.website?.takeIf { !OfficialHospitalPortalRegistry.isPlaceholderUrl(it) }.orEmpty()
            }
            rows += HospitalTestComparisonRow(
                hospitalId = entity.hospitalId,
                hospitalName = hosp?.name ?: entity.hospitalName,
                areaDistrict = hosp?.let { "${it.area}, ${it.district}" }
                    ?: catalogMeta?.let { "${it.area}, ${it.district}" }
                    ?: "Dhaka",
                isOffered = offered,
                homeCollectionAvailable = entity.homeCollectionAvailable,
                homeCollectionFeeBdt = entity.homeCollectionFeeBdt,
                reportDeliveryHours = entity.reportDeliveryHours,
                currentPriceBdt = entity.currentPriceBdt,
                previousPriceBdt = entity.previousPriceBdt,
                priceAvailable = hasPrice,
                verificationStatus = vStatus,
                lastUpdatedMillis = entity.lastUpdatedMillis,
                sourceName = entity.sourceName,
                sourceUrl = resolvedUrl,
                statusNote = entity.statusNote,
                hospitalModel = hosp
            )
        }

        // 2. Known offering centers in master catalog without live records yet
        test.centerPrices.forEach { cp ->
            if (seenHospitals.add(cp.centerId)) {
                val hosp = hospitalById[cp.centerId]
                val meta = syncMetaByHospitalId[cp.centerId]
                val resolvedUrl = meta?.sourceUrl?.takeIf { it.isNotBlank() }
                    ?: hosp?.website?.takeIf { !OfficialHospitalPortalRegistry.isPlaceholderUrl(it) }
                    ?: ""
                rows += HospitalTestComparisonRow(
                    hospitalId = cp.centerId,
                    hospitalName = hosp?.name ?: cp.centerName,
                    areaDistrict = "${cp.area}, ${cp.district}",
                    isOffered = true,
                    homeCollectionAvailable = cp.homeCollectionAvailable,
                    homeCollectionFeeBdt = cp.homeCollectionFeeBdt,
                    reportDeliveryHours = cp.reportDeliveryHours,
                    currentPriceBdt = null,
                    previousPriceBdt = null,
                    priceAvailable = false,
                    verificationStatus = "UNAVAILABLE",
                    lastUpdatedMillis = meta?.lastSyncedMillis ?: 0L,
                    sourceName = "",
                    sourceUrl = resolvedUrl,
                    statusNote = meta?.statusMessage ?: "Offered at facility — awaiting official online price sync",
                    hospitalModel = hosp
                )
            }
        }

        // 3. Remaining facilities in Room directory evaluated realistically for hospital-wise availability
        allHospitals.forEach { hosp ->
            if (seenHospitals.add(hosp.id)) {
                val offered = MedicalTestMasterCatalog.isTestOfferedAtHospital(
                    test = test,
                    hospital = hosp,
                    hasLiveSyncedRecord = false
                )
                val meta = syncMetaByHospitalId[hosp.id]
                val resolvedUrl = meta?.sourceUrl?.takeIf { it.isNotBlank() }
                    ?: hosp.website.takeIf { !OfficialHospitalPortalRegistry.isPlaceholderUrl(it) }
                    ?: ""
                rows += HospitalTestComparisonRow(
                    hospitalId = hosp.id,
                    hospitalName = hosp.name,
                    areaDistrict = "${hosp.area}, ${hosp.district}",
                    isOffered = offered,
                    homeCollectionAvailable = false,
                    homeCollectionFeeBdt = 0,
                    reportDeliveryHours = 12,
                    currentPriceBdt = null,
                    previousPriceBdt = null,
                    priceAvailable = false,
                    verificationStatus = "UNAVAILABLE",
                    lastUpdatedMillis = meta?.lastSyncedMillis ?: 0L,
                    sourceName = "",
                    sourceUrl = resolvedUrl,
                    statusNote = if (offered) {
                        meta?.statusMessage ?: "Likely offered — no public online price published yet"
                    } else {
                        "Not offered (department/equipment not listed at this facility)"
                    },
                    hospitalModel = hosp
                )
            }
        }

        rows.sortedWith(
            compareByDescending<HospitalTestComparisonRow> { it.verificationStatus == "VERIFIED" }
                .thenByDescending { it.verificationStatus == "UNVERIFIED" }
                .thenByDescending { it.isOffered }
                .thenBy { it.currentPriceBdt ?: Int.MAX_VALUE }
                .thenBy { it.hospitalName }
        )
    }

    // Min, Max, Average strictly from VERIFIED available prices
    val verifiedRows = remember(comparisonRows) {
        comparisonRows.filter { it.priceAvailable && it.verificationStatus == "VERIFIED" && it.currentPriceBdt != null }
    }
    val unverifiedCount = remember(comparisonRows) {
        comparisonRows.count { it.priceAvailable && it.verificationStatus == "UNVERIFIED" }
    }
    val verifiedMin = remember(verifiedRows) { verifiedRows.mapNotNull { it.currentPriceBdt }.minOrNull() }
    val verifiedMax = remember(verifiedRows) { verifiedRows.mapNotNull { it.currentPriceBdt }.maxOrNull() }
    val verifiedAvg = remember(verifiedRows) {
        val nums = verifiedRows.mapNotNull { it.currentPriceBdt }
        if (nums.isEmpty()) null else nums.average().roundToInt()
    }

    val displayedRows = remember(comparisonRows, filterTab) {
        when (filterTab) {
            HospitalFilterTab.OFFERED_ONLY -> comparisonRows.filter { it.isOffered }
            HospitalFilterTab.VERIFIED_ONLY -> comparisonRows.filter { it.verificationStatus == "VERIFIED" && it.priceAvailable }
            HospitalFilterTab.ALL_HOSPITALS -> comparisonRows
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(test.shortName.ifBlank { "Test Details" }, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("test_detail_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (testHistory.isNotEmpty()) {
                        IconButton(onClick = { showHistoryDialog = true }) {
                            Icon(Icons.Filled.History, contentDescription = "Price History")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Complete Medical Test Master Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (test.subcategory.isNotBlank()) "${test.category} · ${test.subcategory}" else test.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (test.shortName.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Abbr: ${test.shortName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Text(test.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (test.nameBn != test.name) {
                            Text(test.nameBn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(test.description, style = MaterialTheme.typography.bodyMedium)

                        if (test.whatItDetects.isNotBlank()) {
                            ClinicalInfoBox(
                                title = "What This Test Detects",
                                body = test.whatItDetects,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Science, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("Sample Type: ${test.sampleType}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("Expected Report Delivery: ${test.estimatedDelivery}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }

                        if (test.normalReferenceRange.isNotBlank()) {
                            ClinicalInfoBox(
                                title = "Normal Reference Range (Age / Sex Variations)",
                                body = test.normalReferenceRange,
                                containerColor = VerifiedGreen.copy(alpha = 0.09f)
                            )
                        }

                        ClinicalInfoBox(
                            title = "Preparation & Fasting Requirements",
                            body = test.preparationInstructions,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        )

                        if (test.procedure.isNotBlank()) {
                            ClinicalInfoBox(
                                title = "Test Procedure",
                                body = test.procedure,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        }

                        if (test.relatedAliases.isNotEmpty()) {
                            Text("Related / Alternative Names", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                test.relatedAliases.forEach { alias ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = alias,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Verified Price Summary Card (Strictly Verified Official Prices Only)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (verifiedRows.isNotEmpty()) VerifiedGreen.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = if (verifiedRows.isNotEmpty()) Icons.Filled.VerifiedUser else Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = if (verifiedRows.isNotEmpty()) VerifiedGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Verified Official Price Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (verifiedMin != null && verifiedMax != null && verifiedAvg != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PriceStatTile("Minimum", "৳$verifiedMin", Modifier.weight(1f))
                                PriceStatTile("Average", "৳$verifiedAvg", Modifier.weight(1f))
                                PriceStatTile("Maximum", "৳$verifiedMax", Modifier.weight(1f))
                            }
                            Text(
                                text = "Calculated strictly from ${verifiedRows.size} verified official portal price(s)." +
                                    if (unverifiedCount > 0) " ($unverifiedCount unverified price(s) excluded from stats.)" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = VerifiedGreen
                            )
                        } else {
                            Text(
                                text = "Price Not Available (No Verified Official Prices Yet)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (unverifiedCount > 0) {
                                    "$unverifiedCount unverified collected price(s) exist below, excluded from verified Min/Avg/Max statistics. Tap Sync to query official portals."
                                } else {
                                    "MedCare BD never fabricates or estimates diagnostic test fees. Tap Sync to fetch live official hospital prices."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. Hospital-Wise Availability & Pricing Header + Sync + Filter Tabs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Hospital-Wise Pricing (${displayedRows.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onSyncPrices,
                            enabled = !isSyncingPrices,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("test_detail_sync_button")
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

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(HospitalFilterTab.entries) { tab ->
                            FilterChip(
                                selected = filterTab == tab,
                                onClick = { filterTab = tab },
                                label = { Text(tab.label) }
                            )
                        }
                        if (testHistory.isNotEmpty()) {
                            item {
                                OutlinedButton(
                                    onClick = { showHistoryDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("History (${testHistory.size})")
                                }
                            }
                        }
                    }
                }
            }

            if (displayedRows.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Price Not Available", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "No matching hospital price records in this filter for ${test.name}. Switch to 'Offered Here' or tap Sync.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(displayedRows, key = { it.hospitalId }) { row ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (row.isOffered) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = row.hospitalModel != null) {
                                row.hospitalModel?.let(onOpenHospitalDetail)
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Top status badges: Availability + Verification Status
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    color = if (row.isOffered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (row.isOffered) "Offered at Facility" else "Not Offered",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (row.isOffered) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }

                                val (badgeLabel, badgeColor) = when {
                                    row.priceAvailable && row.verificationStatus == "VERIFIED" -> "Verified Official" to VerifiedGreen
                                    row.priceAvailable && row.verificationStatus == "UNVERIFIED" -> "Unverified Source" to WarningAmber
                                    else -> "Price Not Available" to MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Surface(
                                    color = badgeColor.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                row.priceAvailable && row.verificationStatus == "VERIFIED" -> Icons.Filled.CheckCircle
                                                row.priceAvailable && row.verificationStatus == "UNVERIFIED" -> Icons.Filled.Warning
                                                else -> Icons.Filled.Info
                                            },
                                            contentDescription = null,
                                            tint = badgeColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = badgeLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = badgeColor
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(row.hospitalName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(row.areaDistrict, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (row.isOffered) {
                                        Text(
                                            text = if (row.homeCollectionAvailable) "Home Collection +৳${row.homeCollectionFeeBdt} · ${row.reportDeliveryHours}h report"
                                            else "Walk-in Lab · ${row.reportDeliveryHours}h report",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (row.homeCollectionAvailable) VerifiedGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (row.priceAvailable && row.currentPriceBdt != null) {
                                        Text(
                                            text = "Last updated: ${DiagnosticSyncScheduler.formatTimestamp(row.lastUpdatedMillis)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (row.verificationStatus == "VERIFIED") VerifiedGreen else WarningAmber
                                        )
                                        if (row.previousPriceBdt != null && row.previousPriceBdt != row.currentPriceBdt) {
                                            val delta = row.currentPriceBdt - row.previousPriceBdt
                                            val arrow = if (delta > 0) "▲ +৳$delta" else "▼ -৳${-delta}"
                                            Text(
                                                text = "Previous: ৳${row.previousPriceBdt} ($arrow)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (delta > 0) EmergencyRed else VerifiedGreen
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = row.statusNote,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (row.lastUpdatedMillis > 0L) {
                                            Text(
                                                text = "Last checked: ${DiagnosticSyncScheduler.formatTimestamp(row.lastUpdatedMillis)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (row.sourceUrl.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable {
                                                    runCatching {
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(row.sourceUrl)))
                                                    }
                                                }
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = "Open source URL",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Source: ${row.sourceUrl}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                textDecoration = TextDecoration.Underline,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.width(10.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    if (row.priceAvailable && row.currentPriceBdt != null) {
                                        Text(
                                            text = "৳${row.currentPriceBdt}",
                                            style = MonoNumericStyle,
                                            color = if (row.verificationStatus == "VERIFIED") MaterialTheme.colorScheme.primary else WarningAmber
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                homeCollection = false
                                                selectedCenter = CenterTestPrice(
                                                    centerId = row.hospitalId,
                                                    centerName = row.hospitalName,
                                                    area = row.areaDistrict.substringBefore(",").trim(),
                                                    district = row.areaDistrict.substringAfter(",", "Dhaka").trim(),
                                                    priceBdt = row.currentPriceBdt,
                                                    homeCollectionFeeBdt = row.homeCollectionFeeBdt,
                                                    homeCollectionAvailable = row.homeCollectionAvailable,
                                                    reportDeliveryHours = row.reportDeliveryHours
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("book_center_${row.hospitalId}")
                                        ) {
                                            Text("Book")
                                        }
                                    } else {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (row.isOffered) "Price Not Available" else "Not Offered",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("Price History · ${test.shortName.ifBlank { test.name }}") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    testHistory.forEach { entry ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(entry.hospitalName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                            if (entry.sourceUrl.isNotBlank()) {
                                Text(
                                    text = entry.sourceUrl,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) { Text("Close") }
            }
        )
    }

    if (selectedCenter != null) {
        val cp = selectedCenter!!
        val total = cp.priceBdt + if (homeCollection) cp.homeCollectionFeeBdt else 0
        AlertDialog(
            onDismissRequest = { selectedCenter = null },
            title = { Text("Book Diagnostic Test") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${test.name}\n${cp.centerName}", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = bookingDate,
                        onValueChange = { bookingDate = it },
                        label = { Text("Preferred Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("08:00 - 10:00", "10:00 - 12:00", "16:00 - 18:00").forEach { win ->
                            FilterChip(
                                selected = timeWindow == win,
                                onClick = { timeWindow = win },
                                label = { Text(win, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    if (cp.homeCollectionAvailable) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Home Sample Collection (+৳${cp.homeCollectionFeeBdt})", style = MaterialTheme.typography.bodySmall)
                            Switch(checked = homeCollection, onCheckedChange = { homeCollection = it })
                        }
                    }
                    if (homeCollection) {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Collection Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        label = { Text("Patient Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = patientPhone,
                        onValueChange = { patientPhone = it },
                        label = { Text("Bangladesh Mobile") },
                        placeholder = { Text("e.g. 017XXXXXXXX") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBookTest(
                            cp, bookingDate, timeWindow, homeCollection,
                            if (homeCollection) address else "",
                            patientName, patientPhone, total
                        ) { selectedCenter = null }
                    },
                    enabled = patientName.isNotBlank() && patientPhone.isNotBlank(),
                    modifier = Modifier.testTag("confirm_test_booking_button")
                ) {
                    Text("Confirm (৳$total)")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCenter = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ClinicalInfoBox(
    title: String,
    body: String,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PriceStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MonoNumericStyle, color = VerifiedGreen, fontWeight = FontWeight.Bold)
        }
    }
}
