package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Appointment
import com.example.data.model.Doctor
import com.example.data.model.Review
import com.example.data.model.SlotStatus
import com.example.data.model.UserProfile
import com.example.data.repository.LocationHelper
import com.example.ui.components.FavoriteIconButton
import com.example.ui.components.RatingPill
import com.example.ui.components.ReviewCardItem
import com.example.ui.components.SubmitReviewDialog
import com.example.ui.components.VerifiedChip
import com.example.ui.state.UiState
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MonoNumericStyle
import com.example.ui.theme.SlotAvailableBg
import com.example.ui.theme.SlotBookedBg
import com.example.ui.theme.SlotBreakBg
import com.example.ui.theme.VerifiedGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DoctorDetailScreen(
    doctor: Doctor,
    isSignedIn: Boolean,
    profile: UserProfile?,
    activeAppointments: List<Appointment>,
    isFavorite: Boolean,
    reviewsState: UiState<List<Review>>,
    onObserveReviews: (String, String) -> Unit,
    onRequestSignIn: () -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onBookSlot: (
        hospitalId: String,
        hospitalName: String,
        chamberName: String,
        appointmentDate: String,
        slotTime: String,
        patientName: String,
        patientPhone: String,
        feeBdt: Int,
        notes: String,
        onDone: () -> Unit
    ) -> Unit,
    onSubmitReview: (Int, Int, Int, Int, Int, String) -> Unit,
    onReportReview: (String, String) -> Unit
) {
    LaunchedEffect(doctor.id) {
        onObserveReviews(doctor.id, "doctor")
    }

    var selectedChamberIndex by remember { mutableIntStateOf(0) }
    val chamber = doctor.chambers.getOrNull(selectedChamberIndex) ?: doctor.chambers.firstOrNull()
    var selectedDayIndex by remember(chamber?.chamberId) {
        mutableIntStateOf(chamber?.weeklySchedule?.indexOfFirst { it.isOpen }?.coerceAtLeast(0) ?: 0)
    }
    val daySchedule = chamber?.weeklySchedule?.getOrNull(selectedDayIndex) ?: chamber?.weeklySchedule?.firstOrNull()

    val upcomingDate = remember(daySchedule?.weekdayShort) {
        daySchedule?.let { LocationHelper.nextIsoDateForWeekday(it.weekdayShort) }
            ?: LocationHelper.todayIsoDate()
    }

    val use24Hour = profile?.use24HourTime ?: false
    var pendingSlot by remember { mutableStateOf<String?>(null) }
    var patientName by remember(profile?.displayName) { mutableStateOf(profile?.displayName ?: "") }
    var patientPhone by remember(profile?.phone) { mutableStateOf(profile?.phone.orEmpty()) }
    var bookingNotes by remember { mutableStateOf("") }
    var showReviewDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor & Chamber Schedule", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("doctor_detail_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FavoriteIconButton(
                        isFavorite = isFavorite,
                        onClick = onToggleFavorite,
                        testTag = "doctor_detail_favorite"
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
            // Doctor Profile Header
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
                                Text(
                                    doctor.specialization,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                if (doctor.verified) VerifiedChip(doctor.bmdcRegNo)
                            }
                            RatingPill(doctor.rating, doctor.reviewCount)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(doctor.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(doctor.qualification, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Text(doctor.designation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(doctor.bio, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FacilityTag("${doctor.experienceYears} yrs experience")
                            FacilityTag(doctor.languages.joinToString(" · "))
                        }
                    }
                }
            }

            // Chamber Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Chamber Location (${doctor.chambers.size})", style = MaterialTheme.typography.titleLarge)
                    doctor.chambers.forEachIndexed { idx, ch ->
                        val selected = idx == selectedChamberIndex
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (selected) 1.5.dp else 0.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedChamberIndex = idx }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ch.hospitalName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(ch.chamberName, style = MaterialTheme.typography.bodySmall)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(4.dp))
                                        Text("${ch.address} (${ch.area})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text("৳${ch.consultationFeeBdt}", style = MonoNumericStyle, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Weekly Chamber Schedule Calendar & Slots
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Weekly Chamber Schedule", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(upcomingDate, style = MonoNumericStyle, color = MaterialTheme.colorScheme.secondary)
                        }

                        if (chamber == null || chamber.weeklySchedule.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Chamber schedule details are currently unavailable. Please contact hospital desk directly.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(chamber.weeklySchedule) { idx, day ->
                                    FilterChip(
                                        selected = idx == selectedDayIndex,
                                        onClick = { selectedDayIndex = idx },
                                        label = {
                                            Text(if (day.isOpen) day.weekdayShort else "${day.weekdayShort} (Off)")
                                        }
                                    )
                                }
                            }

                            if (daySchedule == null || !daySchedule.isOpen) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${daySchedule?.weekdayShort ?: "Schedule"}: ${daySchedule?.holidayNote ?: "Chamber Closed"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Hours: ${LocationHelper.formatTime(daySchedule.startTime24h, use24Hour)} - ${LocationHelper.formatTime(daySchedule.endTime24h, use24Hour)} · Break: ${daySchedule.breakWindow}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Slot Legend
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    SlotLegendDot("Available", VerifiedGreen)
                                    SlotLegendDot("Booked", EmergencyRed)
                                    SlotLegendDot("Break", Color(0xFFB36B00))
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    daySchedule.slots.forEach { slot ->
                                        val userBooked = activeAppointments.any {
                                            it.doctorId == doctor.id &&
                                                it.chamberName == chamber.chamberName &&
                                                it.appointmentDate == upcomingDate &&
                                                it.slotTime == slot.time24h &&
                                                it.status != "cancelled"
                                        }
                                        val effectiveStatus = if (userBooked) SlotStatus.BOOKED else slot.status
                                        val bgColor = when (effectiveStatus) {
                                            SlotStatus.AVAILABLE -> SlotAvailableBg
                                            SlotStatus.BOOKED -> SlotBookedBg
                                            SlotStatus.BREAK -> SlotBreakBg
                                            SlotStatus.CLOSED -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                        val textColor = when (effectiveStatus) {
                                            SlotStatus.AVAILABLE -> VerifiedGreen
                                            SlotStatus.BOOKED -> EmergencyRed
                                            SlotStatus.BREAK -> Color(0xFF9A5B00)
                                            SlotStatus.CLOSED -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }

                                        Surface(
                                            color = bgColor,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable(enabled = effectiveStatus == SlotStatus.AVAILABLE) {
                                                    pendingSlot = slot.time24h
                                                }
                                                .testTag("slot_${daySchedule.weekdayShort}_${slot.time24h}")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = LocationHelper.formatTime(slot.time24h, use24Hour),
                                                    style = MonoNumericStyle.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize),
                                                    color = textColor
                                                )
                                                Text(
                                                    text = when (effectiveStatus) {
                                                        SlotStatus.AVAILABLE -> "Open"
                                                        SlotStatus.BOOKED -> if (userBooked) "Yours" else "Booked"
                                                        SlotStatus.BREAK -> "Break"
                                                        SlotStatus.CLOSED -> "Closed"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = textColor
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

            // Reviews
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
                        shape = RoundedCornerShape(12.dp)
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
                                "Sign in with Google to view patient reviews and publish feedback for ${doctor.name}.",
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
                    is UiState.Loading -> item { Text("Loading reviews...") }
                    is UiState.Error -> item { Text(reviewsState.message, color = MaterialTheme.colorScheme.error) }
                    is UiState.Success -> {
                        if (reviewsState.data.isEmpty()) {
                            item {
                                Text(
                                    "No reviews yet for ${doctor.name}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(reviewsState.data, key = { it.id }) { rev ->
                                ReviewCardItem(review = rev, onReportClick = { onReportReview(rev.id, "Reported by user") })
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingSlot != null && chamber != null) {
        val activeChamber = chamber
        val slotToBook = pendingSlot!!
        AlertDialog(
            onDismissRequest = { pendingSlot = null },
            title = { Text("Confirm Appointment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "${doctor.name}\n${activeChamber.hospitalName} · ${activeChamber.chamberName}\nDate: $upcomingDate at ${LocationHelper.formatTime(slotToBook, use24Hour)}\nFee: ৳${activeChamber.consultationFeeBdt}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        label = { Text("Patient Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("booking_patient_name")
                    )
                    OutlinedTextField(
                        value = patientPhone,
                        onValueChange = { patientPhone = it },
                        label = { Text("Bangladesh Mobile (01XXXXXXXXX)") },
                        placeholder = { Text("e.g. 017XXXXXXXX") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("booking_patient_phone")
                    )
                    OutlinedTextField(
                        value = bookingNotes,
                        onValueChange = { bookingNotes = it },
                        label = { Text("Symptoms / Visit Note (optional)") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBookSlot(
                            activeChamber.hospitalId,
                            activeChamber.hospitalName,
                            activeChamber.chamberName,
                            upcomingDate,
                            slotToBook,
                            patientName,
                            patientPhone,
                            activeChamber.consultationFeeBdt,
                            bookingNotes
                        ) {
                            pendingSlot = null
                            bookingNotes = ""
                        }
                    },
                    enabled = patientName.isNotBlank() && patientPhone.isNotBlank(),
                    modifier = Modifier.testTag("confirm_appointment_button")
                ) {
                    Text("Confirm (৳${activeChamber.consultationFeeBdt})")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingSlot = null }) { Text("Cancel") }
            }
        )
    }

    if (showReviewDialog) {
        SubmitReviewDialog(
            targetName = doctor.name,
            onDismiss = { showReviewDialog = false },
            onSubmit = { overall, doc, staff, wait, clean, comment ->
                onSubmitReview(overall, doc, staff, wait, clean, comment)
                showReviewDialog = false
            }
        )
    }
}

@Composable
private fun SlotLegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
