package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Appointment
import com.example.data.model.MedicalRecord
import com.example.data.model.TestBooking
import com.example.data.repository.LocationHelper
import com.example.ui.state.UiState
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MonoNumericStyle
import com.example.ui.theme.VerifiedGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    isSignedIn: Boolean,
    appointmentsState: UiState<List<Appointment>>,
    testBookingsState: UiState<List<TestBooking>>,
    use24HourTime: Boolean,
    onRequestSignIn: () -> Unit,
    onRescheduleAppointment: (Appointment, String, String, String) -> Unit,
    onCancelAppointment: (Appointment) -> Unit,
    onCancelTestBooking: (TestBooking) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var reschedulingTarget by remember { mutableStateOf<Appointment?>(null) }
    var newDate by remember { mutableStateOf(LocationHelper.todayIsoDate()) }
    var newSlot by remember { mutableStateOf("18:30") }
    var newNotes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings & History") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("appointments_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) {
                    Text("Doctor Appointments")
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) {
                    Text("Lab Tests")
                }
            }

            if (!isSignedIn) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Sign in to view your bookings", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Your doctor appointments and diagnostic test bookings are stored privately under your Google account.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onRequestSignIn, shape = RoundedCornerShape(14.dp)) {
                            Text("Sign in with Google")
                        }
                    }
                }
            } else if (selectedTab == 0) {
                when (appointmentsState) {
                    is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(appointmentsState.message) }
                    is UiState.Success -> {
                        val list = appointmentsState.data
                        if (list.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No doctor appointments booked yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(list, key = { it.id }) { appt ->
                                    val isTerminal = appt.status in listOf("completed", "cancelled")
                                    Card(
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                StatusPill(appt.status)
                                                Text("৳${appt.consultationFeeBdt}", style = MonoNumericStyle, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Text(appt.doctorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text("${appt.specialization} · ${appt.hospitalName}", style = MaterialTheme.typography.bodySmall)
                                            Text("Chamber: ${appt.chamberName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                "Slot: ${appt.appointmentDate} · ${LocationHelper.formatTime(appt.slotTime, use24HourTime)}",
                                                style = MonoNumericStyle.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize),
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            if (!isTerminal) {
                                                Spacer(Modifier.height(4.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            reschedulingTarget = appt
                                                            newDate = appt.appointmentDate
                                                            newSlot = appt.slotTime
                                                            newNotes = appt.notes
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Reschedule")
                                                    }
                                                    OutlinedButton(
                                                        onClick = { onCancelAppointment(appt) },
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Cancel", color = EmergencyRed)
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
            } else {
                when (testBookingsState) {
                    is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(testBookingsState.message) }
                    is UiState.Success -> {
                        val list = testBookingsState.data
                        if (list.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No diagnostic tests booked yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(list, key = { it.id }) { tb ->
                                    val isTerminal = tb.status in listOf("completed", "cancelled")
                                    Card(
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                StatusPill(tb.status)
                                                Text("৳${tb.priceBdt}", style = MonoNumericStyle, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Text(tb.testName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(tb.centerName, style = MaterialTheme.typography.bodySmall)
                                            Text(
                                                "Date: ${tb.bookingDate} (${tb.timeWindow})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            if (tb.homeCollection) {
                                                Text("Home Sample Collection: ${tb.collectionAddress}", style = MaterialTheme.typography.labelSmall, color = VerifiedGreen)
                                            }
                                            if (!isTerminal) {
                                                OutlinedButton(
                                                    onClick = { onCancelTestBooking(tb) },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Cancel Test Booking", color = EmergencyRed)
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
        }
    }

    if (reschedulingTarget != null) {
        AlertDialog(
            onDismissRequest = { reschedulingTarget = null },
            title = { Text("Reschedule Appointment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newDate,
                        onValueChange = { newDate = it },
                        label = { Text("New Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSlot,
                        onValueChange = { newSlot = it },
                        label = { Text("New Slot (24h, e.g. 18:30)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRescheduleAppointment(reschedulingTarget!!, newDate, newSlot, newNotes)
                        reschedulingTarget = null
                    },
                    enabled = newDate.isNotBlank() && newSlot.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { reschedulingTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatusPill(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "confirmed", "completed" -> VerifiedGreen.copy(alpha = 0.14f) to VerifiedGreen
        "cancelled" -> EmergencyRed.copy(alpha = 0.14f) to EmergencyRed
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(color = bg, shape = RoundedCornerShape(50.dp)) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordsScreen(
    isSignedIn: Boolean,
    recordsState: UiState<List<MedicalRecord>>,
    onRequestSignIn: () -> Unit,
    onAddRecord: (String, String, String, String, String, String?) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var recordType by remember { mutableStateOf("Prescription") }
    var facility by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocationHelper.todayIsoDate()) }
    var notes by remember { mutableStateOf("") }
    var pickedUri by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> pickedUri = uri?.toString() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical History & Records") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("records_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isSignedIn) showAddDialog = true else onRequestSignIn()
                },
                modifier = Modifier.testTag("add_record_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add medical record")
            }
        }
    ) { padding ->
        if (!isSignedIn) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.FolderShared, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                    Text("Sign in to access your Health Vault", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Save prescriptions, lab reports, and imaging notes privately to your Google account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onRequestSignIn, shape = RoundedCornerShape(14.dp)) {
                        Text("Sign in with Google")
                    }
                }
            }
        } else when (recordsState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(recordsState.message) }
            is UiState.Success -> {
                val records = recordsState.data
                if (records.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.FolderShared, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("Your private health vault is empty", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Save prescriptions, lab reports, and discharge summaries privately to your account.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(records, key = { it.id }) { rec ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        FacilityTag(rec.recordType)
                                        Text(rec.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        if (rec.facilityName.isNotBlank()) {
                                            Text("${rec.facilityName} · ${rec.recordDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (rec.notes.isNotBlank()) {
                                            Text(rec.notes, style = MaterialTheme.typography.bodySmall)
                                        }
                                        if (!rec.attachmentUri.isNullOrBlank()) {
                                            Text("Photo attached ✓", style = MaterialTheme.typography.labelSmall, color = VerifiedGreen)
                                        }
                                    }
                                    IconButton(onClick = { onDeleteRecord(rec.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete record", tint = EmergencyRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Medical Record") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title (e.g. ECG & Blood Panel)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Prescription", "Lab Report", "Imaging").forEach { t ->
                            FilterChip(
                                selected = recordType == t,
                                onClick = { recordType = t },
                                label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = facility,
                        onValueChange = { facility = it },
                        label = { Text("Hospital / Diagnostic Center") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Summary") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (pickedUri == null) "Attach Prescription Photo" else "Photo Attached ✓")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onAddRecord(title, recordType, facility, date, notes, pickedUri)
                            showAddDialog = false
                            title = ""
                            facility = ""
                            notes = ""
                            pickedUri = null
                        }
                    },
                    enabled = title.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
