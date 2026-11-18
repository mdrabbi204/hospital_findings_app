package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.catalog.BangladeshHealthcareCatalog
import com.example.data.model.FavoriteItem
import com.example.data.model.UserProfile
import com.example.data.repository.LocationHelper
import com.example.ui.theme.EmergencyRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    isSignedIn: Boolean,
    profile: UserProfile?,
    guestLanguage: String,
    guestDarkMode: String,
    favorites: List<FavoriteItem>,
    appointmentCount: Int,
    recordCount: Int,
    onRequestSignIn: () -> Unit,
    onSaveProfile: (UserProfile) -> Unit,
    onSetGuestLanguage: (String) -> Unit,
    onSetGuestDarkMode: (String) -> Unit,
    onOpenAppointments: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenEmergency: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenFavoriteTarget: (FavoriteItem) -> Unit,
    onSignOut: () -> Unit,
    onDeleteProfileData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null && profile != null) {
                onSaveProfile(profile.copy(photoUri = uri.toString()))
            }
        }
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Profile Header (Authenticated or Guest)
        item {
            if (!isSignedIn) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "Guest user",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Browsing as Guest",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sign in with Google to book appointments, save favorites, and store health records.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = onRequestSignIn,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("profile_sign_in_button")
                        ) {
                            Text("Sign in with Google")
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val photoUri = profile?.photoUri
                                if (!photoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = photoUri,
                                        contentDescription = "Profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = "Select profile photo",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile?.displayName ?: "Patient",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = profile?.email ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val bloodGroup = profile?.bloodGroup
                                    if (!bloodGroup.isNullOrBlank()) {
                                        FacilityTag("Blood: $bloodGroup", isUrgent = true)
                                    }
                                    FacilityTag("${profile?.area ?: "Dhanmondi"}, ${profile?.district ?: "Dhaka"}")
                                }
                            }
                            OutlinedButton(
                                onClick = { showEditDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("edit_profile_button")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit")
                            }
                        }

                        val emergencyContactName = profile?.emergencyContactName
                        if (!emergencyContactName.isNullOrBlank()) {
                            val emergencyContactPhone = profile.emergencyContactPhone
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Emergency Contact: $emergencyContactName ($emergencyContactPhone)",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Hub Links
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Care Hub", style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileHubCard(
                        icon = Icons.Filled.CalendarMonth,
                        title = "Appointments",
                        subtitle = "$appointmentCount active/past",
                        onClick = onOpenAppointments,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileHubCard(
                        icon = Icons.Filled.FolderShared,
                        title = "Health Records",
                        subtitle = "$recordCount saved",
                        onClick = onOpenRecords,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileHubCard(
                        icon = Icons.AutoMirrored.Filled.CompareArrows,
                        title = "Compare Facilities",
                        subtitle = "Side-by-side matrix",
                        onClick = onOpenCompare,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileHubCard(
                        icon = Icons.Filled.Emergency,
                        title = "Emergency 999",
                        subtitle = "ER & Blood Banks",
                        onClick = onOpenEmergency,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Saved Hospitals & Doctors
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Saved Hospitals & Doctors (${favorites.size})", style = MaterialTheme.typography.titleMedium)
                    }
                    if (favorites.isEmpty()) {
                        Text(
                            "Tap the bookmark icon on any hospital or doctor to save them here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        favorites.forEach { fav ->
                            Surface(
                                onClick = { onOpenFavoriteTarget(fav) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(fav.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text("${fav.targetType.replaceFirstChar { it.uppercase() }} · ${fav.subtitle}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bangladesh & Notification Preferences
        item {
            val activeLang = profile?.language ?: guestLanguage
            val activeDarkMode = profile?.darkMode ?: guestDarkMode
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Bangladesh & Display Settings", style = MaterialTheme.typography.titleMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Language (English / বাংলা)", style = MaterialTheme.typography.bodyMedium)
                            Text(if (activeLang == "bn") "বাংলা সক্রিয়" else "English active", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = activeLang == "bn",
                            onCheckedChange = { isBn ->
                                onSetGuestLanguage(if (isBn) "bn" else "en")
                            }
                        )
                    }

                    Text("Theme Appearance", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (mode, label) ->
                            FilterChip(
                                selected = activeDarkMode == mode,
                                onClick = { onSetGuestDarkMode(mode) },
                                label = { Text(label) }
                            )
                        }
                    }

                    if (isSignedIn && profile != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("24-Hour Time (Asia/Dhaka BST)", style = MaterialTheme.typography.bodyMedium)
                                Text(if (profile.use24HourTime) "e.g. 18:30 BST" else "e.g. 6:30 PM", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = profile.use24HourTime,
                                onCheckedChange = { onSaveProfile(profile.copy(use24HourTime = it)) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Appointment Reminders", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = profile.notifyReminders,
                                onCheckedChange = { onSaveProfile(profile.copy(notifyReminders = it)) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Health Announcements", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = profile.notifyAnnouncements,
                                onCheckedChange = { onSaveProfile(profile.copy(notifyAnnouncements = it)) }
                            )
                        }
                    }
                }
            }
        }

        // Account Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isSignedIn) {
                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sign Out")
                    }

                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_account_button")
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = EmergencyRed)
                        Spacer(Modifier.width(6.dp))
                        Text("Delete Profile Data & Sign Out", color = EmergencyRed)
                    }
                }

                Text(
                    text = "Sample Directory Notice: Hospital, doctor, diagnostic fee, and chamber schedule entries shown in this build are curated sample data for demonstration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showEditDialog && profile != null) {
        var name by remember { mutableStateOf(profile.displayName) }
        var phone by remember { mutableStateOf(profile.phone) }
        var bloodGroup by remember { mutableStateOf(profile.bloodGroup.ifBlank { "O+" }) }
        var gender by remember { mutableStateOf(profile.gender.ifBlank { "Female" }) }
        var ageText by remember { mutableStateOf(if (profile.age > 0) profile.age.toString() else "28") }
        var division by remember { mutableStateOf(profile.division) }
        var district by remember { mutableStateOf(profile.district) }
        var area by remember { mutableStateOf(profile.area) }
        var emgName by remember { mutableStateOf(profile.emergencyContactName) }
        var emgPhone by remember { mutableStateOf(profile.emergencyContactPhone) }
        var phoneError by remember { mutableStateOf<String?>(null) }
        var emgPhoneError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Patient Profile") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = null
                        },
                        label = { Text("BD Mobile (01XXXXXXXXX)") },
                        isError = phoneError != null,
                        supportingText = phoneError?.let { msg -> { Text(msg) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = { Text("Age") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-").forEach { bg ->
                            FilterChip(
                                selected = bloodGroup == bg,
                                onClick = { bloodGroup = bg },
                                label = { Text(bg) }
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BangladeshHealthcareCatalog.areas.take(6).forEach { a ->
                            FilterChip(
                                selected = area == a.name,
                                onClick = {
                                    area = a.name
                                    district = a.district
                                    division = a.division
                                },
                                label = { Text(a.name) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = emgName,
                        onValueChange = { emgName = it },
                        label = { Text("Emergency Contact Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emgPhone,
                        onValueChange = {
                            emgPhone = it
                            emgPhoneError = null
                        },
                        label = { Text("Emergency Contact Phone") },
                        isError = emgPhoneError != null,
                        supportingText = emgPhoneError?.let { msg -> { Text(msg) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (phone.isNotBlank() && !LocationHelper.isValidBangladeshPhone(phone)) {
                        phoneError = "Use valid BD format e.g. 017XXXXXXXX"
                        return@Button
                    }
                    if (emgPhone.isNotBlank() && !LocationHelper.isValidBangladeshPhone(emgPhone)) {
                        emgPhoneError = "Use valid BD format e.g. 017XXXXXXXX"
                        return@Button
                    }
                    onSaveProfile(
                        profile.copy(
                            displayName = name.ifBlank { profile.displayName },
                            phone = phone,
                            age = (ageText.toLongOrNull() ?: 0L).coerceIn(0L, 120L),
                            gender = gender,
                            bloodGroup = bloodGroup,
                            division = division,
                            district = district,
                            area = area,
                            emergencyContactName = emgName,
                            emergencyContactPhone = emgPhone
                        )
                    )
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account Profile?") },
            text = { Text("This deletes your profile document from MedCare BD and signs you out of Google Sign-In.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteProfileData()
                    }
                ) { Text("Delete & Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileHubCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
