package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.LocalHealthcareRepository
import com.example.data.local.MedCareRoomDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DiagnosticPriceSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = MedCareRoomDatabase.getInstance(applicationContext)
            val localRepo = LocalHealthcareRepository(
                hospitalDao = db.hospitalDao(),
                doctorDao = db.doctorDao(),
                diagnosticTestDao = db.diagnosticTestDao(),
                localActivityDao = db.localActivityDao(),
                diagnosticPriceDao = db.diagnosticPriceDao()
            )
            val summary = localRepo.syncDiagnosticPricesBatch(maxHospitals = 20)
            Log.i(
                TAG,
                "Periodic diagnostic price sync done: checked=${summary.hospitalsChecked}, " +
                    "verified=${summary.verifiedHospitals}, prices=${summary.updatedOrNewPrices}, changes=${summary.changesDetected}"
            )
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Diagnostic price sync worker failed (attempt=$runAttemptCount)", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private companion object {
        const val TAG = "DiagPriceSyncWorker"
    }
}

object DiagnosticSyncScheduler {
    private const val PERIODIC_WORK_NAME = "medcare_periodic_diagnostic_price_sync"
    private const val IMMEDIATE_WORK_NAME = "medcare_immediate_diagnostic_price_sync"

    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<DiagnosticPriceSyncWorker>(
            repeatInterval = 6,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun triggerImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DiagnosticPriceSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun formatTimestamp(millis: Long): String {
        if (millis <= 0L) return "Not synced yet"
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        return formatter.format(Date(millis))
    }

    fun formatRelativeTime(millis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        if (millis <= 0L) return "Never"
        val diff = (nowMillis - millis).coerceAtLeast(0L)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> formatTimestamp(millis)
        }
    }
}
