package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.catalog.BangladeshHealthcareCatalog
import com.example.data.model.FacilityType
import com.example.data.model.Hospital
import com.example.data.repository.AreaHospitalRemoteDataSource
import com.example.data.repository.LocationHelper
import com.example.ui.components.FacilityMapPanel
import com.example.ui.components.FavoriteIconButton
import com.example.ui.components.RatingPill
import com.example.ui.components.VerifiedChip
import com.example.ui.state.HospitalFilterState
import java.util.Locale

private val QUICK_AREA_PRESETS = listOf(
    "Mirpur",
    "Dhanmondi",
    "Panthapath",
    "Uttara",
    "Gulshan",
    "Banani",
    "Mohammadpur",
    "Shyamoli",
    "Shahbagh",
    "Mohakhali",
    "Badda",
    "Malibagh",
    "Kakrail",
    "Moghbazar",
    "Motijheel",
    "Lalmatia",
    "Old Dhaka",
    "Jatrabari",
    "Savar"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HospitalsScreen(
    hospitals: List<Hospital>,
    isFetchingArea: Boolean,
    areaSyncStatus: String?,
    filter: HospitalFilterState,
    onFilterChange: ((HospitalFilterState) -> HospitalFilterState) -> Unit,
    onSyncArea: (force: Boolean, query: String) -> Unit,
    onSearchLive: (query: String) -> Unit,
    recentSearches: List<String> = emptyList(),
    onDeleteRecentSearch: (String) -> Unit = {},
    favoriteHospitalIds: Set<String>,
    compareHospitalIds: List<String>,
    userCoordinates: Pair<Double, Double>,
    hasLocationPermission: Boolean,
    distanceKmProvider: (Hospital) -> Double,
    onLocationPermissionResult: (Boolean) -> Unit,
    onToggleFavorite: (Hospital, Boolean) -> Unit,
    onToggleCompare: (Hospital) -> Unit,
    onOpenCompare: () -> Unit,
    onOpenHospitalDetail: (Hospital) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLocationFilterSheet by remember { mutableStateOf(false) }
    var selectedMapHospital by remember { mutableStateOf<Hospital?>(null) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> onLocationPermissionResult(granted) }
    )

    LaunchedEffect(filter.query) {
        if (filter.query.trim().length >= 3) {
            onSearchLive(filter.query)
        }
    }

    val filteredHospitals = remember(hospitals, filter, userCoordinates) {
        val q = filter.query.trim()
        hospitals.filter { h ->
            AreaHospitalRemoteDataSource.matchesQuery(h, q) &&
                (filter.division == "All" || h.division.equals(filter.division, true) ||
                    h.address.contains(filter.division, true)) &&
                (filter.district == "All" || h.district.equals(filter.district, true) ||
                    h.address.contains(filter.district, true)) &&
                (filter.area == "All" || h.area.equals(filter.area, true) ||
                    h.address.contains(filter.area, true)) &&
                (filter.facilityType == null || h.type == filter.facilityType) &&
                (!filter.onlyEmergency247 || h.hasEmergency247) &&
                (!filter.onlyIcu || h.hasIcu) &&
                (h.rating >= filter.minRating)
        }.sortedBy { distanceKmProvider(it) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search & List/Map View Switcher
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = filter.query,
                        onValueChange = { q -> onFilterChange { it.copy(query = q) } },
                        placeholder = { Text("Search hospital, area, ICU...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (filter.query.isNotEmpty()) {
                                IconButton(
                                    onClick = { onFilterChange { it.copy(query = "") } },
                                    modifier = Modifier.testTag("hospitals_search_clear")
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { onSyncArea(true, filter.query) }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hospitals_search_input")
                    )
                    IconButton(
                        onClick = { showLocationFilterSheet = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("hospitals_area_filter_button")
                    ) {
                        Icon(Icons.Filled.FilterAlt, contentDescription = "Filter by division, district, area")
                    }
                    IconButton(
                        onClick = { locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("hospitals_gps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "Use GPS location",
                            tint = if (hasLocationPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Segmented List vs Google Map Toggle
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !filter.mapViewEnabled,
                        onClick = { onFilterChange { it.copy(mapViewEnabled = false) } },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = { Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("hospitals_list_tab")
                    ) {
                        Text("List (${filteredHospitals.size})")
                    }
                    SegmentedButton(
                        selected = filter.mapViewEnabled,
                        onClick = { onFilterChange { it.copy(mapViewEnabled = true) } },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = { Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("hospitals_map_tab")
                    ) {
                        Text("Map View")
                    }
                }

                // Live Area-Wise Google / Map Discovery Status Bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
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
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isFetchingArea) {
                                        val target = if (filter.area != "All") filter.area else if (filter.district != "All") filter.district else "Dhaka"
                                        "Searching Google / Live Map for $target..."
                                    } else {
                                        areaSyncStatus ?: "SQLite Room DB (${hospitals.size} facilities) + Live Map"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (filter.query.isNotBlank()) {
                                TextButton(
                                    onClick = { onSyncArea(true, filter.query) },
                                    modifier = Modifier.testTag("search_google_area_button")
                                ) {
                                    Text("Search Maps", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            IconButton(
                                onClick = { onSyncArea(true, filter.query) },
                                enabled = !isFetchingArea,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("refresh_area_hospitals_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh area hospitals from Google Maps",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (isFetchingArea) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
                        }
                    }
                }

                if (recentSearches.isNotEmpty() && filter.query.isBlank()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Text(
                                text = "Recent:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(recentSearches, key = { "rec_q_$it" }) { pastQuery ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    onFilterChange {
                                        it.copy(query = pastQuery, division = "All", district = "All", area = "All")
                                    }
                                    onSyncArea(true, pastQuery)
                                },
                                label = { Text(pastQuery, maxLines = 1) },
                                trailingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable { onDeleteRecentSearch(pastQuery) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Remove $pastQuery",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Quick Area Selection Chips (1-tap Mirpur, Dhanmondi, Uttara, Gulshan, etc.)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filter.division != "All" || filter.area != "All",
                            onClick = { showLocationFilterSheet = true },
                            label = {
                                val locLabel = when {
                                    filter.area != "All" -> filter.area
                                    filter.district != "All" -> filter.district
                                    filter.division != "All" -> filter.division
                                    else -> "All Areas"
                                }
                                Text(locLabel)
                            }
                        )
                    }
                    items(QUICK_AREA_PRESETS) { areaPreset ->
                        val selected = filter.area.equals(areaPreset, ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                onFilterChange {
                                    if (selected) {
                                        it.copy(area = "All")
                                    } else {
                                        it.copy(division = "Dhaka", district = "Dhaka", area = areaPreset)
                                    }
                                }
                            },
                            label = { Text(areaPreset) },
                            modifier = Modifier.testTag("quick_area_chip_${areaPreset.lowercase()}")
                        )
                    }
                }

                // Facility Type & Attribute Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filter.onlyEmergency247,
                            onClick = { onFilterChange { it.copy(onlyEmergency247 = !it.onlyEmergency247) } },
                            label = { Text("24/7 ER") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filter.onlyIcu,
                            onClick = { onFilterChange { it.copy(onlyIcu = !it.onlyIcu) } },
                            label = { Text("ICU / CCU") }
                        )
                    }
                    items(FacilityType.entries) { type ->
                        FilterChip(
                            selected = filter.facilityType == type,
                            onClick = {
                                onFilterChange {
                                    it.copy(facilityType = if (it.facilityType == type) null else type)
                                }
                            },
                            label = { Text(type.labelEn) }
                        )
                    }
                }
            }

            if (filter.mapViewEnabled) {
                FacilityMapPanel(
                    hospitals = filteredHospitals,
                    userCoordinates = userCoordinates,
                    hasLocationPermission = hasLocationPermission,
                    selectedHospital = selectedMapHospital,
                    onSelectHospital = { selectedMapHospital = it },
                    onOpenHospitalDetail = onOpenHospitalDetail,
                    onDirectionsClick = { h ->
                        LocationHelper.openGoogleMapsDirections(context, h.latitude, h.longitude, h.name)
                    },
                    onRequestLocation = {
                        locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                if (filteredHospitals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No matching facilities found", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Fetch live Google Maps / OpenStreetMap hospitals for this area or clear your filters.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { onSyncArea(true, filter.query) }) {
                                    Text("Search Live Map")
                                }
                                if (filter.area != "All" || filter.district != "All" || filter.division != "All") {
                                    OutlinedButton(onClick = {
                                        onFilterChange {
                                            it.copy(division = "All", district = "All", area = "All")
                                        }
                                        onSyncArea(true, filter.query)
                                    }) {
                                        Text("All Areas")
                                    }
                                }
                                OutlinedButton(onClick = { onFilterChange { HospitalFilterState() } }) {
                                    Text("Reset Filters")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 84.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredHospitals, key = { it.id }) { hospital ->
                            val isFav = hospital.id in favoriteHospitalIds
                            val isCompared = hospital.id in compareHospitalIds
                            val km = distanceKmProvider(hospital)
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenHospitalDetail(hospital) }
                                    .testTag("hospital_card_${hospital.id}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = hospital.type.labelEn,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (hospital.verified) VerifiedChip()
                                                if (hospital.dataSource != "Directory") {
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = hospital.dataSource,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = hospital.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${hospital.area}, ${hospital.district} · ${String.format(Locale.US, "%.1f", km)} km",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            FavoriteIconButton(
                                                isFavorite = isFav,
                                                onClick = { onToggleFavorite(hospital, isFav) },
                                                testTag = "fav_hospital_${hospital.id}"
                                            )
                                            RatingPill(hospital.rating, hospital.reviewCount)
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (hospital.hasEmergency247) FacilityTag("24/7 ER", isUrgent = true)
                                        if (hospital.hasIcu) FacilityTag("ICU")
                                        if (hospital.hasCcu) FacilityTag("CCU")
                                        if (hospital.hasNicu) FacilityTag("NICU")
                                        if (hospital.hasBloodBank) FacilityTag("Blood Bank")
                                    }

                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { LocationHelper.openDialer(context, hospital.phone) },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Call")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                LocationHelper.openGoogleMapsDirections(
                                                    context, hospital.latitude, hospital.longitude, hospital.name
                                                )
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Map")
                                        }
                                        Button(
                                            onClick = { onToggleCompare(hospital) },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("compare_toggle_${hospital.id}")
                                        ) {
                                            Text(if (isCompared) "Added ✓" else "Compare", maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (compareHospitalIds.isNotEmpty() && !filter.mapViewEnabled) {
            ExtendedFloatingActionButton(
                onClick = onOpenCompare,
                icon = { Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null) },
                text = { Text("Compare (${compareHospitalIds.size})") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("floating_compare_button")
            )
        }
    }

    if (showLocationFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showLocationFilterSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bangladesh Area & Rating Filters", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { onFilterChange { HospitalFilterState() } }) {
                        Text("Reset")
                    }
                }

                Text("Division", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    (listOf("All") + BangladeshHealthcareCatalog.divisions).forEach { div ->
                        FilterChip(
                            selected = filter.division == div,
                            onClick = {
                                onFilterChange { it.copy(division = div, district = "All", area = "All") }
                            },
                            label = { Text(div) }
                        )
                    }
                }

                Text("District", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    (listOf("All") + BangladeshHealthcareCatalog.districtsFor(filter.division)).forEach { dist ->
                        FilterChip(
                            selected = filter.district == dist,
                            onClick = { onFilterChange { it.copy(district = dist, area = "All") } },
                            label = { Text(dist) }
                        )
                    }
                }

                Text("Area / Thana", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    (listOf("All") + BangladeshHealthcareCatalog.areasFor(filter.district)).forEach { area ->
                        FilterChip(
                            selected = filter.area == area,
                            onClick = { onFilterChange { it.copy(area = area) } },
                            label = { Text(area) }
                        )
                    }
                }

                Text("Minimum Rating", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.0 to "Any", 4.3 to "4.3+", 4.6 to "4.6+").forEach { (r, label) ->
                        FilterChip(
                            selected = filter.minRating == r,
                            onClick = { onFilterChange { it.copy(minRating = r) } },
                            label = { Text(label) }
                        )
                    }
                }

                Button(
                    onClick = { showLocationFilterSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Apply Filters (${filteredHospitals.size})")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
