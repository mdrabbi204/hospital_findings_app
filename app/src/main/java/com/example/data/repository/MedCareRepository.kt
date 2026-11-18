package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.Appointment
import com.example.data.model.FavoriteItem
import com.example.data.model.MedicalRecord
import com.example.data.model.Review
import com.example.data.model.TestBooking
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MedCareRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun <T> Flow<T>.handleSnapshotErrors(
        uid: String,
        opType: OperationType,
        path: String
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, opType, path)
        }
        throw error
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snap ->
                    snap.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .handleSnapshotErrors(userId, OperationType.GET, path)
        )
    }

    suspend fun ensureUserProfileExists(
        userId: String,
        displayName: String,
        email: String
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            val docRef = db.collection("users").document(uid)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val initial = UserProfile(
                    userId = uid,
                    displayName = displayName.ifBlank { "Patient" }.take(80),
                    email = email.take(120)
                )
                docRef.set(initial.toCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        val path = "users/${profile.userId}"
        return try {
            val uid = requireUserId()
            check(uid == profile.userId) { "Refusing cross-user profile write." }
            val updates = buildMap<String, Any> {
                put("displayName", profile.displayName.ifBlank { "Patient" }.take(80))
                put("phone", profile.phone.take(20))
                put("gender", profile.gender.take(24))
                put("age", profile.age.coerceIn(0L, 120L))
                put("bloodGroup", profile.bloodGroup.take(10))
                put("division", profile.division.ifBlank { "Dhaka" }.take(60))
                put("district", profile.district.ifBlank { "Dhaka" }.take(60))
                put("area", profile.area.ifBlank { "Dhanmondi" }.take(80))
                put("emergencyContactName", profile.emergencyContactName.take(80))
                put("emergencyContactPhone", profile.emergencyContactPhone.take(20))
                put("language", if (profile.language == "bn") "bn" else "en")
                put("use24HourTime", profile.use24HourTime)
                put("darkMode", if (profile.darkMode in listOf("system", "light", "dark")) profile.darkMode else "system")
                put("notifyConfirmations", profile.notifyConfirmations)
                put("notifyReminders", profile.notifyReminders)
                put("notifyAnnouncements", profile.notifyAnnouncements)
                profile.photoUri?.let { put("photoUri", it.take(500)) }
                put("updatedAt", FieldValue.serverTimestamp())
            }
            db.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeFavorites(userId: String): Flow<List<FavoriteItem>> = flow {
        val path = "users/$userId/favorites"
        emitAll(
            db.collection("users").document(userId).collection("favorites")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        FavoriteItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .handleSnapshotErrors(userId, OperationType.LIST, path)
        )
    }

    suspend fun toggleFavorite(
        userId: String,
        targetId: String,
        targetType: String,
        title: String,
        subtitle: String,
        currentlyFavorite: Boolean
    ): Result<Unit> {
        val docId = "${targetType}_$targetId".replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
        val path = "users/$userId/favorites/$docId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user favorite write." }
            val docRef = db.collection("users").document(uid).collection("favorites").document(docId)
            if (currentlyFavorite) {
                docRef.delete().await()
            } else {
                val payload = mapOf(
                    "targetId" to targetId.take(128),
                    "targetType" to targetType,
                    "title" to title.ifBlank { "Saved Facility" }.take(120),
                    "subtitle" to subtitle.take(160),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeAppointments(userId: String): Flow<List<Appointment>> = flow {
        val path = "appointments"
        emitAll(
            db.collection("appointments")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        Appointment::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .handleSnapshotErrors(userId, OperationType.LIST, path)
        )
    }

    suspend fun bookAppointment(
        userId: String,
        doctorId: String,
        doctorName: String,
        specialization: String,
        hospitalId: String,
        hospitalName: String,
        chamberName: String,
        appointmentDate: String,
        slotTime: String,
        patientName: String,
        patientPhone: String,
        consultationFeeBdt: Long,
        notes: String
    ): Result<String> {
        val path = "appointments"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user appointment booking." }
            require(patientName.isNotBlank()) { "Patient name is required." }
            require(patientPhone.isNotBlank()) { "Mobile number is required." }

            val slotKey = "${doctorId}_${chamberName}_${appointmentDate}_$slotTime"
            // Check for duplicate active booking in user's appointments
            val existing = db.collection("appointments")
                .whereEqualTo("userId", uid)
                .whereEqualTo("slotKey", slotKey)
                .get()
                .await()
            val hasConflict = existing.documents.any {
                val st = it.getString("status") ?: ""
                st == "pending" || st == "confirmed"
            }
            if (hasConflict) {
                return Result.failure(IllegalStateException("This slot is already booked."))
            }

            val docRef = db.collection("appointments").document()
            val payload = mapOf(
                "userId" to uid,
                "doctorId" to doctorId.take(128),
                "doctorName" to doctorName.take(120),
                "specialization" to specialization.take(80),
                "hospitalId" to hospitalId.take(128),
                "hospitalName" to hospitalName.take(140),
                "chamberName" to chamberName.take(140),
                "appointmentDate" to appointmentDate.take(20),
                "slotTime" to slotTime.take(30),
                "slotKey" to slotKey.take(180),
                "patientName" to patientName.trim().take(80),
                "patientPhone" to patientPhone.trim().take(20),
                "consultationFeeBdt" to consultationFeeBdt.coerceIn(0L, 50000L),
                "notes" to notes.trim().take(400),
                "status" to "confirmed",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            addNotification(
                uid,
                "Appointment Confirmed",
                "$doctorName on $appointmentDate at $slotTime ($chamberName).",
                "appointment"
            )
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun rescheduleAppointment(
        userId: String,
        appointmentId: String,
        doctorId: String,
        chamberName: String,
        newDate: String,
        newSlotTime: String,
        notes: String
    ): Result<Unit> {
        val path = "appointments/$appointmentId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user update." }
            require(newDate.isNotBlank()) { "Appointment date is required." }
            require(newSlotTime.isNotBlank()) { "Time slot is required." }
            val slotKey = "${doctorId}_${chamberName}_${newDate}_$newSlotTime"
            val existing = db.collection("appointments")
                .whereEqualTo("userId", uid)
                .whereEqualTo("slotKey", slotKey)
                .get()
                .await()
            val hasConflict = existing.documents.any {
                it.id != appointmentId && (it.getString("status") == "pending" || it.getString("status") == "confirmed")
            }
            if (hasConflict) {
                return Result.failure(IllegalStateException("This time slot is already booked."))
            }
            db.collection("appointments").document(appointmentId).update(
                mapOf(
                    "appointmentDate" to newDate.trim().take(20),
                    "slotTime" to newSlotTime.trim().take(30),
                    "slotKey" to slotKey.take(180),
                    "notes" to notes.trim().take(400),
                    "status" to "confirmed",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            addNotification(
                uid,
                "Appointment Rescheduled",
                "Updated to $newDate at $newSlotTime ($chamberName).",
                "schedule"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun cancelAppointment(userId: String, appointmentId: String, doctorName: String): Result<Unit> {
        val path = "appointments/$appointmentId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user cancellation." }
            db.collection("appointments").document(appointmentId).update(
                mapOf(
                    "status" to "cancelled",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            addNotification(
                uid,
                "Appointment Cancelled",
                "Your appointment with $doctorName has been cancelled.",
                "appointment"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeTestBookings(userId: String): Flow<List<TestBooking>> = flow {
        val path = "testBookings"
        emitAll(
            db.collection("testBookings")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        TestBooking::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .handleSnapshotErrors(userId, OperationType.LIST, path)
        )
    }

    suspend fun bookDiagnosticTest(
        userId: String,
        testId: String,
        testName: String,
        centerId: String,
        centerName: String,
        bookingDate: String,
        timeWindow: String,
        homeCollection: Boolean,
        collectionAddress: String,
        patientName: String,
        patientPhone: String,
        priceBdt: Long
    ): Result<String> {
        val path = "testBookings"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user test booking." }
            require(patientName.isNotBlank()) { "Patient name is required." }
            require(patientPhone.isNotBlank()) { "Mobile number is required." }

            val docRef = db.collection("testBookings").document()
            val payload = mapOf(
                "userId" to uid,
                "testId" to testId.take(128),
                "testName" to testName.take(120),
                "centerId" to centerId.take(128),
                "centerName" to centerName.take(140),
                "bookingDate" to bookingDate.take(20),
                "timeWindow" to timeWindow.take(40),
                "homeCollection" to homeCollection,
                "collectionAddress" to collectionAddress.trim().take(250),
                "patientName" to patientName.trim().take(80),
                "patientPhone" to patientPhone.trim().take(20),
                "priceBdt" to priceBdt.coerceIn(0L, 200000L),
                "status" to "confirmed",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            addNotification(
                uid,
                "Lab Test Booked",
                "$testName at $centerName for $bookingDate ($timeWindow).",
                "diagnostic"
            )
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun cancelTestBooking(userId: String, bookingId: String, testName: String): Result<Unit> {
        val path = "testBookings/$bookingId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user update." }
            val docRef = db.collection("testBookings").document(bookingId)
            val snap = docRef.get().await()
            val date = snap.getString("bookingDate") ?: LocationHelper.todayIsoDate()
            val win = snap.getString("timeWindow") ?: "08:00 - 10:00"
            val home = snap.getBoolean("homeCollection") ?: false
            val addr = snap.getString("collectionAddress") ?: ""
            docRef.update(
                mapOf(
                    "bookingDate" to date,
                    "timeWindow" to win,
                    "homeCollection" to home,
                    "collectionAddress" to addr,
                    "status" to "cancelled",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            addNotification(
                uid,
                "Diagnostic Booking Cancelled",
                "Cancelled booking for $testName.",
                "diagnostic"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeReviewsForTarget(targetId: String, targetType: String): Flow<List<Review>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val path = "reviews"
        emitAll(
            db.collection("reviews")
                .whereEqualTo("targetType", targetType)
                .whereEqualTo("targetId", targetId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        Review::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .handleSnapshotErrors(uid, OperationType.LIST, path)
        )
    }

    suspend fun submitReview(
        userId: String,
        authorName: String,
        targetId: String,
        targetType: String,
        overallRating: Int,
        doctorBehaviorRating: Int,
        staffBehaviorRating: Int,
        waitingTimeRating: Int,
        cleanlinessRating: Int,
        comment: String,
        verifiedBooking: Boolean
    ): Result<Unit> {
        val docId = "${userId}_${targetType}_$targetId".replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
        val path = "reviews/$docId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user review write." }
            val trimmed = comment.trim()
            require(trimmed.length >= 3) { "Please write at least 3 characters." }

            val docRef = db.collection("reviews").document(docId)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "overallRating" to overallRating.coerceIn(1, 5).toLong(),
                        "doctorBehaviorRating" to doctorBehaviorRating.coerceIn(1, 5).toLong(),
                        "staffBehaviorRating" to staffBehaviorRating.coerceIn(1, 5).toLong(),
                        "waitingTimeRating" to waitingTimeRating.coerceIn(1, 5).toLong(),
                        "cleanlinessRating" to cleanlinessRating.coerceIn(1, 5).toLong(),
                        "comment" to trimmed.take(600),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "authorName" to authorName.ifBlank { "Verified Patient" }.take(80),
                    "targetId" to targetId.take(128),
                    "targetType" to targetType,
                    "overallRating" to overallRating.coerceIn(1, 5).toLong(),
                    "doctorBehaviorRating" to doctorBehaviorRating.coerceIn(1, 5).toLong(),
                    "staffBehaviorRating" to staffBehaviorRating.coerceIn(1, 5).toLong(),
                    "waitingTimeRating" to waitingTimeRating.coerceIn(1, 5).toLong(),
                    "cleanlinessRating" to cleanlinessRating.coerceIn(1, 5).toLong(),
                    "comment" to trimmed.take(600),
                    "verifiedBooking" to verifiedBooking,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun reportReview(userId: String, reviewId: String, reason: String): Result<Unit> {
        val path = "reviewReports"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user report." }
            val docId = "${uid}_$reviewId".replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
            val payload = mapOf(
                "userId" to uid,
                "reviewId" to reviewId.take(128),
                "reason" to reason.ifBlank { "Inappropriate content" }.take(200),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("reviewReports").document(docId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeMedicalRecords(userId: String): Flow<List<MedicalRecord>> = flow {
        val path = "users/$userId/medicalRecords"
        emitAll(
            db.collection("users").document(userId).collection("medicalRecords")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        MedicalRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .handleSnapshotErrors(userId, OperationType.LIST, path)
        )
    }

    suspend fun addMedicalRecord(
        userId: String,
        title: String,
        recordType: String,
        facilityName: String,
        recordDate: String,
        notes: String,
        attachmentUri: String?
    ): Result<Unit> {
        val path = "users/$userId/medicalRecords"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user medical record write." }
            val docRef = db.collection("users").document(uid).collection("medicalRecords").document()
            val payload = buildMap<String, Any> {
                put("title", title.trim().ifBlank { "Medical Record" }.take(120))
                put("recordType", recordType)
                put("facilityName", facilityName.trim().take(120))
                put("recordDate", recordDate.trim().ifBlank { LocationHelper.todayIsoDate() }.take(30))
                put("notes", notes.trim().take(500))
                attachmentUri?.let { put("attachmentUri", it.take(500)) }
                put("createdAt", FieldValue.serverTimestamp())
                put("updatedAt", FieldValue.serverTimestamp())
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteMedicalRecord(userId: String, recordId: String): Result<Unit> {
        val path = "users/$userId/medicalRecords/$recordId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user delete." }
            db.collection("users").document(uid).collection("medicalRecords").document(recordId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeNotifications(userId: String): Flow<List<AppNotification>> = flow {
        val path = "users/$userId/notifications"
        emitAll(
            db.collection("users").document(userId).collection("notifications")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        AppNotification::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .handleSnapshotErrors(userId, OperationType.LIST, path)
        )
    }

    private suspend fun addNotification(
        userId: String,
        title: String,
        body: String,
        category: String
    ) {
        val docRef = db.collection("users").document(userId).collection("notifications").document()
        val payload = mapOf(
            "title" to title.take(120),
            "body" to body.take(400),
            "category" to category,
            "read" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        runCatching { docRef.set(payload).await() }
    }

    suspend fun markNotificationRead(userId: String, notificationId: String): Result<Unit> {
        val path = "users/$userId/notifications/$notificationId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user notification update." }
            db.collection("users").document(uid).collection("notifications").document(notificationId)
                .update(mapOf("read" to true, "updatedAt" to FieldValue.serverTimestamp()))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteUserAccountData(userId: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user account deletion." }
            db.collection("users").document(uid).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
