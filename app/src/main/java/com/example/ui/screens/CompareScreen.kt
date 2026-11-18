package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.catalog.BangladeshHealthcareCatalog
import com.example.data.catalog.MedicalTestMasterCatalog
import com.example.data.local.HospitalDiagnosticPriceEntity
import com.example.data.model.DiagnosticTest
import com.example.data.model.Doctor
import com.example.data.model.Hospital
import com.example.data.sync.DiagnosticSyncScheduler
import com.example.ui.theme.MonoNumericStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    allHospitals: List<Hospital>,
    selectedHospitalIds: List<String>,
    distanceKmProvider: (Hospital) -> Double,
    onRemoveHospital: (String) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit,
    onOpenHospitalDetail: (Hospital) -> Unit,
    allDoctors: List<Doctor> = BangladeshHealthcareCatalog.doctors,
    allTests: List<DiagnosticTest> = BangladeshHealthcareCatalog.diagnosticTests,
    diagnosticPrices: List<HospitalDiagnosticPriceEntity> = emptyList()
) {
    val facilities = allHospitals.filter { it.id in selectedHospitalIds }

    val availableTestsToCompare = remember(allTests, diagnosticPrices) {
        val known = allTests.map {
            Triple(it.id, it.shortName.ifBlank { it.name }, it.name)
        }.toMutableList()
        val knownIds = allTests.map { it.id }.toHashSet()
        diagnosticPrices
            .filter { it.canonicalTestId !in knownIds }
            .distinctBy { it.canonicalTestId }
            .forEach { known += Triple(it.canonicalTestId, it.testName, it.testName) }
        known
    }
    var selectedTestId by remember(availableTestsToCompare) {
        mutableStateOf(availableTestsToCompare.firstOrNull()?.first ?: "test_cbc")
    }
    val selectedTestObj = allTests.firstOrNull { it.id == selectedTestId }
    val selectedTestName = availableTestsToCompare.firstOrNull { it.first == selectedTestId }?.third ?: "Diagnostic Test"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compare Facilities (${facilities.size}/3)") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("compare_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (facilities.isNotEmpty()) {
                        TextButton(onClick = onClearAll) { Text("Clear") }
                    }
                }
            )
        }
    ) { padding ->
        if (facilities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CompareArrows,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No facilities selected to compare", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tap 'Compare' on 2 or 3 hospitals or diagnostic centers in the Hospitals tab to compare ICU, ratings, doctors, and live test prices side-by-side.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Browse Hospitals") }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (availableTestsToCompare.isNotEmpty()) {
                    Text(
                        text = "Select Test Price to Compare",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableTestsToCompare, key = { it.first }) { (id, chipLabel, _) ->
                            FilterChip(
                                selected = selectedTestId == id,
                                onClick = { selectedTestId = id },
                                label = { Text(chipLabel) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    facilities.forEach { h ->
                        val docCount = allDoctors.count { d ->
                            d.chambers.any { it.hospitalId == h.id }
                        }
                        val priceEntry = diagnosticPrices.firstOrNull {
                            it.hospitalId == h.id && it.canonicalTestId == selectedTestId &&
                                it.priceAvailable && it.currentPriceBdt != null
                        }
                        val isOffered = selectedTestObj == null || MedicalTestMasterCatalog.isTestOfferedAtHospital(
                            test = selectedTestObj,
                            hospital = h,
                            hasLiveSyncedRecord = priceEntry != null
                        )
                        val priceLabel = when {
                            priceEntry?.currentPriceBdt != null -> buildString {
                                val badge = if (priceEntry.verificationStatus == "UNVERIFIED") "Unverified" else "Verified"
                                append("৳${priceEntry.currentPriceBdt} · $badge")
                                append("\n${DiagnosticSyncScheduler.formatRelativeTime(priceEntry.lastUpdatedMillis)}")
                                if (priceEntry.previousPriceBdt != null && priceEntry.previousPriceBdt != priceEntry.currentPriceBdt) {
                                    append("\nPrev: ৳${priceEntry.previousPriceBdt}")
                                }
                            }
                            !isOffered -> "Not Offered at Facility"
                            else -> "Price Not Available"
                        }

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.width(240.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = h.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { onRemoveHospital(h.id) }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Remove")
                                    }
                                }
                                CompareRow("Type", h.type.labelEn)
                                CompareRow("Location", "${h.area}, ${h.district}")
                                CompareRow("Distance", "${String.format(Locale.US, "%.1f", distanceKmProvider(h))} km")
                                CompareRow("Rating", "${h.rating} ★ (${h.reviewCount})")
                                CompareRow("24/7 Emergency", if (h.hasEmergency247) "Yes" else "No")
                                CompareRow("ICU / CCU / NICU", listOfNotNull(
                                    if (h.hasIcu) "ICU" else null,
                                    if (h.hasCcu) "CCU" else null,
                                    if (h.hasNicu) "NICU" else null
                                ).ifEmpty { listOf("Standard") }.joinToString(", "))
                                CompareRow("Listed Doctors", "$docCount specialists")
                                CompareRow("Departments", "${h.departments.size} departments")
                                CompareRow(selectedTestName, priceLabel)
                                CompareRow("Amenities", h.amenities.take(3).joinToString(", "))

                                Button(
                                    onClick = { onOpenHospitalDetail(h) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Open Profile")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MonoNumericStyle.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
