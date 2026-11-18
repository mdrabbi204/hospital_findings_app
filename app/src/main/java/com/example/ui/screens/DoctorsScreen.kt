package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.catalog.BangladeshHealthcareCatalog
import com.example.data.model.Doctor
import com.example.ui.components.FavoriteIconButton
import com.example.ui.components.RatingPill
import com.example.ui.components.VerifiedChip
import com.example.ui.state.DoctorFilterState
import com.example.ui.theme.MonoNumericStyle

private val DOCTOR_QUICK_AREAS = listOf(
    "All",
    "Mirpur",
    "Dhanmondi",
    "Panthapath",
    "Uttara",
    "Gulshan",
    "Banani",
    "Shahbagh",
    "Shyamoli",
    "Mohakhali",
    "Mohammadpur",
    "Badda",
    "Malibagh",
    "Kakrail",
    "Moghbazar",
    "Motijheel",
    "Lalmatia",
    "Old Dhaka",
    "Savar"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DoctorsScreen(
    filter: DoctorFilterState,
    onFilterChange: ((DoctorFilterState) -> DoctorFilterState) -> Unit,
    favoriteDoctorIds: Set<String>,
    onToggleFavorite: (Doctor, Boolean) -> Unit,
    onOpenDoctorDetail: (Doctor) -> Unit,
    allDoctors: List<Doctor> = BangladeshHealthcareCatalog.doctors,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    val filteredDoctors = remember(filter, allDoctors) {
        val q = filter.query.trim()
        allDoctors.filter { d ->
            val minFee = d.chambers.minOfOrNull { it.consultationFeeBdt } ?: 0
            (q.isBlank() || d.name.contains(q, true) || d.specialization.contains(q, true) ||
                d.qualification.contains(q, true) || d.designation.contains(q, true) ||
                d.chambers.any {
                    it.hospitalName.contains(q, true) || it.area.contains(q, true) ||
                        it.address.contains(q, true)
                }) &&
                (filter.specialization == "All" || d.specialization.equals(filter.specialization, true)) &&
                (filter.area == "All" || d.chambers.any {
                    it.area.equals(filter.area, true) || it.address.contains(filter.area, true)
                }) &&
                (d.experienceYears >= filter.minExperienceYears) &&
                (minFee <= filter.maxFeeBdt) &&
                (d.rating >= filter.minRating) &&
                (filter.weekdayShort == "All" || d.chambers.any { c ->
                    c.weeklySchedule.any { day -> day.weekdayShort == filter.weekdayShort && day.isOpen }
                })
        }.sortedByDescending { it.rating }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = filter.query,
                    onValueChange = { q -> onFilterChange { it.copy(query = q) } },
                    placeholder = { Text("Search doctor, specialty, hospital...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("doctors_search_input")
                )
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("doctors_filter_button")
                ) {
                    Icon(Icons.Filled.FilterAlt, contentDescription = "Filter doctors")
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All") + BangladeshHealthcareCatalog.specializations) { spec ->
                    FilterChip(
                        selected = filter.specialization == spec,
                        onClick = { onFilterChange { it.copy(specialization = spec) } },
                        label = { Text(spec) }
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DOCTOR_QUICK_AREAS) { area ->
                    FilterChip(
                        selected = filter.area == area,
                        onClick = { onFilterChange { it.copy(area = area) } },
                        label = { Text(if (area == "All") "All Areas (${filteredDoctors.size})" else area) }
                    )
                }
            }
        }

        if (filteredDoctors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No doctors match your filters", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { onFilterChange { DoctorFilterState() } }) {
                        Text("Clear Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDoctors, key = { it.id }) { doctor ->
                    val isFav = doctor.id in favoriteDoctorIds
                    val primaryChamber = doctor.chambers.firstOrNull()
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDoctorDetail(doctor) }
                            .testTag("doctor_card_${doctor.id}")
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
                                            text = doctor.specialization,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (doctor.verified) VerifiedChip(doctor.bmdcRegNo)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = doctor.name,
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
                                    Text(
                                        text = "${doctor.designation} · ${doctor.experienceYears} yrs exp",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    FavoriteIconButton(
                                        isFavorite = isFav,
                                        onClick = { onToggleFavorite(doctor, isFav) },
                                        testTag = "fav_doctor_${doctor.id}"
                                    )
                                    RatingPill(doctor.rating, doctor.reviewCount)
                                }
                            }

                            if (primaryChamber != null) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "Chamber: ${primaryChamber.hospitalName} (${primaryChamber.area})",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = primaryChamber.availableDaysSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(10.dp))
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
                                    Button(
                                        onClick = { onOpenDoctorDetail(doctor) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("book_doctor_${doctor.id}")
                                    ) {
                                        Text("View Schedule & Book")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
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
                    Text("Doctor Filters", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { onFilterChange { DoctorFilterState() } }) { Text("Reset") }
                }

                Text("Dhaka / BD Chamber Area", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DOCTOR_QUICK_AREAS.forEach { area ->
                        FilterChip(
                            selected = filter.area == area,
                            onClick = { onFilterChange { it.copy(area = area) } },
                            label = { Text(area) }
                        )
                    }
                }

                Text("Available Weekday", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri").forEach { day ->
                        FilterChip(
                            selected = filter.weekdayShort == day,
                            onClick = { onFilterChange { it.copy(weekdayShort = day) } },
                            label = { Text(day) }
                        )
                    }
                }

                Text("Max Consultation Fee: ৳${filter.maxFeeBdt}", style = MaterialTheme.typography.titleSmall)
                Slider(
                    value = filter.maxFeeBdt.toFloat(),
                    onValueChange = { v -> onFilterChange { it.copy(maxFeeBdt = (v / 100).toInt() * 100) } },
                    valueRange = 500f..3000f
                )

                Text("Minimum Experience", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Any", 10 to "10+ yrs", 15 to "15+ yrs").forEach { (yrs, label) ->
                        FilterChip(
                            selected = filter.minExperienceYears == yrs,
                            onClick = { onFilterChange { it.copy(minExperienceYears = yrs) } },
                            label = { Text(label) }
                        )
                    }
                }

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Show ${filteredDoctors.size} Doctors")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
