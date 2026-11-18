package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.catalog.BangladeshHealthcareCatalog
import com.example.data.model.Hospital
import com.example.data.repository.LocationHelper
import com.example.ui.theme.EmergencyRed
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    allHospitals: List<Hospital>,
    distanceKmProvider: (Hospital) -> Double,
    onBack: () -> Unit,
    onOpenHospitalDetail: (Hospital) -> Unit
) {
    val context = LocalContext.current
    val erHospitals = allHospitals
        .filter { it.hasEmergency247 }
        .sortedBy { distanceKmProvider(it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Directory") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("emergency_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    color = EmergencyRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Emergency, contentDescription = null, tint = EmergencyRed)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Directory Notice: MedCare BD provides verified emergency contact numbers and Google Maps navigation. It is not an emergency dispatch service — in life-threatening emergencies, dial 999 directly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                Text("National Helplines, Ambulances & Blood Banks", style = MaterialTheme.typography.titleLarge)
            }

            items(BangladeshHealthcareCatalog.emergencyServices, key = { it.id }) { svc ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FacilityTag(svc.category, isUrgent = svc.category == "National")
                            if (svc.available247) {
                                Text("24/7 Active", style = MaterialTheme.typography.labelSmall, color = EmergencyRed, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(svc.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(svc.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { LocationHelper.openDialer(context, svc.phone) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Call ${svc.phone}")
                            }
                            if (svc.district != "All") {
                                OutlinedButton(
                                    onClick = {
                                        LocationHelper.openGoogleMapsDirections(context, svc.latitude, svc.longitude, svc.title)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Navigation, contentDescription = "Navigate", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text("Nearby 24-Hour Emergency Hospitals", style = MaterialTheme.typography.titleLarge)
            }

            items(erHospitals, key = { it.id }) { hospital ->
                val km = distanceKmProvider(hospital)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(hospital.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${hospital.area}, ${hospital.district} · ${String.format(Locale.US, "%.1f", km)} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { LocationHelper.openDialer(context, hospital.emergencyPhone) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("ER Call")
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
                                Spacer(Modifier.width(6.dp))
                                Text("Navigate")
                            }
                            OutlinedButton(
                                onClick = { onOpenHospitalDetail(hospital) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Info")
                            }
                        }
                    }
                }
            }
        }
    }
}
