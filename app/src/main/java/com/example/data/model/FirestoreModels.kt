package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val age: Long = 0L,
    val bloodGroup: String = "",
    val division: String = "Dhaka",
    val district: String = "Dhaka",
    val area: String = "Dhanmondi",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val language: String = "en",
    val use24HourTime: Boolean = false,
    val darkMode: String = "system",
    val notifyConfirmations: Boolean = true,
    val notifyReminders: Boolean = true,
    val notifyAnnouncements: Boolean = true,
    val photoUri: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "displayName" to displayName.ifBlank { "Patient" }.take(80),
        "email" to email.take(120),
        "phone" to phone.take(20),
        "gender" to gender.take(24),
        "age" to age.coerceIn(0L, 120L),
        "bloodGroup" to bloodGroup.take(10),
        "division" to division.ifBlank { "Dhaka" }.take(60),
        "district" to district.ifBlank { "Dhaka" }.take(60),
        "area" to area.ifBlank { "Dhanmondi" }.take(80),
        "emergencyContactName" to emergencyContactName.take(80),
        "emergencyContactPhone" to emergencyContactPhone.take(20),
        "language" to if (language == "bn") "bn" else "en",
        "use24HourTime" to use24HourTime,
        "darkMode" to if (darkMode in listOf("system", "light", "dark")) darkMode else "system",
        "notifyConfirmations" to notifyConfirmations,
        "notifyReminders" to notifyReminders,
        "notifyAnnouncements" to notifyAnnouncements,
        "photoUri" to photoUri?.take(500),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null } as Map<String, Any>
}

@Keep
@IgnoreExtraProperties
data class FavoriteItem(
    @DocumentId val id: String = "",
    val targetId: String = "",
    val targetType: String = "hospital",
    val title: String = "",
    val subtitle: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MedicalRecord(
    @DocumentId val id: String = "",
    val title: String = "",
    val recordType: String = "Prescription",
    val facilityName: String = "",
    val recordDate: String = "",
    val notes: String = "",
    val attachmentUri: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = "appointment",
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Appointment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val specialization: String = "",
    val hospitalId: String = "",
    val hospitalName: String = "",
    val chamberName: String = "",
    val appointmentDate: String = "",
    val slotTime: String = "",
    val slotKey: String = "",
    val patientName: String = "",
    val patientPhone: String = "",
    val consultationFeeBdt: Long = 0L,
    val notes: String = "",
    val status: String = "confirmed",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TestBooking(
    @DocumentId val id: String = "",
    val userId: String = "",
    val testId: String = "",
    val testName: String = "",
    val centerId: String = "",
    val centerName: String = "",
    val bookingDate: String = "",
    val timeWindow: String = "",
    val homeCollection: Boolean = false,
    val collectionAddress: String = "",
    val patientName: String = "",
    val patientPhone: String = "",
    val priceBdt: Long = 0L,
    val status: String = "confirmed",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Review(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val targetId: String = "",
    val targetType: String = "hospital",
    val overallRating: Long = 5L,
    val doctorBehaviorRating: Long = 5L,
    val staffBehaviorRating: Long = 5L,
    val waitingTimeRating: Long = 5L,
    val cleanlinessRating: Long = 5L,
    val comment: String = "",
    val verifiedBooking: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
