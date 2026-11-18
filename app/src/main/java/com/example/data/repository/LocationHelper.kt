package com.example.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object LocationHelper {

    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoarseLocation(context: Context): Pair<Double, Double>? =
        suspendCancellableCoroutine { cont ->
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val cts = CancellationTokenSource()
                cont.invokeOnCancellation { cts.cancel() }
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            cont.resume(location.latitude to location.longitude)
                        } else {
                            client.lastLocation.addOnSuccessListener { last ->
                                cont.resume(last?.let { it.latitude to it.longitude })
                            }.addOnFailureListener {
                                cont.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }
            } catch (_: Exception) {
                cont.resume(null)
            }
        }

    fun openDialer(context: Context, phoneNumber: String) {
        val sanitized = phoneNumber.replace(" ", "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    fun openGoogleMapsDirections(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String
    ) {
        val encodedLabel = Uri.encode(label)
        val gmmUri = Uri.parse("google.navigation:q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val fallbackUri = Uri.parse(
            "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude($encodedLabel)"
        )
        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                context.startActivity(fallbackIntent)
            }
        }
    }

    fun isValidBangladeshPhone(raw: String): Boolean {
        val cleaned = raw.replace(" ", "").replace("-", "")
        return Regex("^(\\+8801|8801|01)[3-9]\\d{8}$").matches(cleaned)
    }

    fun formatTime(time24h: String, use24Hour: Boolean): String {
        if (use24Hour) return "$time24h BST"
        val parts = time24h.split(":")
        if (parts.size != 2) return time24h
        val hour = parts[0].toIntOrNull() ?: return time24h
        val min = parts[1]
        val suffix = if (hour >= 12) "PM" else "AM"
        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "$hour12:$min $suffix"
    }

    fun todayIsoDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(java.util.Calendar.getInstance().time)
    }

    fun nextIsoDateForWeekday(weekdayShort: String): String {
        val targetDay = when (weekdayShort.trim().take(3).lowercase(java.util.Locale.US)) {
            "sun" -> java.util.Calendar.SUNDAY
            "mon" -> java.util.Calendar.MONDAY
            "tue" -> java.util.Calendar.TUESDAY
            "wed" -> java.util.Calendar.WEDNESDAY
            "thu" -> java.util.Calendar.THURSDAY
            "fri" -> java.util.Calendar.FRIDAY
            "sat" -> java.util.Calendar.SATURDAY
            else -> return todayIsoDate()
        }
        val cal = java.util.Calendar.getInstance()
        val current = cal.get(java.util.Calendar.DAY_OF_WEEK)
        val deltaDays = (targetDay - current + 7) % 7
        cal.add(java.util.Calendar.DAY_OF_YEAR, deltaDays)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(cal.time)
    }
}
