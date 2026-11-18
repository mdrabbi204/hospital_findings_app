package com.example.data.local

import com.example.data.catalog.BangladeshHealthcareCatalog
import com.example.data.model.DiagnosticTest
import com.example.data.model.Doctor
import com.example.data.model.Hospital
import com.example.data.repository.AreaHospitalRemoteDataSource
import com.example.data.repository.DiagnosticPriceSyncService
import com.example.data.repository.HospitalPriceFetchResult
import com.example.data.repository.OfficialHospitalPortalRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

data class HospitalPriceSyncSummary(
    val hospitalsChecked: Int,
    val verifiedHospitals: Int,
    val updatedOrNewPrices: Int,
    val changesDetected: Int,
    val unavailableHospitals: Int
)

class LocalHealthcareRepository(
    private val hospitalDao: HospitalDao,
    private val doctorDao: DoctorDao,
    private val diagnosticTestDao: DiagnosticTestDao,
    private val localActivityDao: LocalActivityDao,
    private val diagnosticPriceDao: DiagnosticPriceDao
) {
    private val seedMutex = Mutex()

    fun observeAllHospitals(): Flow<List<Hospital>> =
        hospitalDao.observeAllHospitals().map { list -> list.map { it.toModel() } }

    fun observeAllDoctors(): Flow<List<Doctor>> =
        doctorDao.observeAllDoctors().map { list -> list.map { it.toModel() } }

    fun observeAllDiagnosticTests(): Flow<List<DiagnosticTest>> =
        diagnosticTestDao.observeAllTests().map { list -> list.map { it.toModel() } }

    fun observeRecentHospitalIds(): Flow<List<String>> =
        localActivityDao.observeRecentIds("hospital")

    fun observeRecentDoctorIds(): Flow<List<String>> =
        localActivityDao.observeRecentIds("doctor")

    fun observeRecentSearches(): Flow<List<String>> =
        localActivityDao.observeRecentSearches()

    fun observeAllDiagnosticPrices(): Flow<List<HospitalDiagnosticPriceEntity>> =
        diagnosticPriceDao.observeAllPrices()

    fun observePricesForHospital(hospitalId: String): Flow<List<HospitalDiagnosticPriceEntity>> =
        diagnosticPriceDao.observePricesForHospital(hospitalId)

    fun observePricesForTest(testId: String): Flow<List<HospitalDiagnosticPriceEntity>> =
        diagnosticPriceDao.observePricesForTest(testId)

    fun observeAllPriceHistory(): Flow<List<DiagnosticPriceHistoryEntity>> =
        diagnosticPriceDao.observeAllHistory()

    fun observeHistoryForHospital(hospitalId: String): Flow<List<DiagnosticPriceHistoryEntity>> =
        diagnosticPriceDao.observeHistoryForHospital(hospitalId)

    fun observeHistoryForTest(testId: String): Flow<List<DiagnosticPriceHistoryEntity>> =
        diagnosticPriceDao.observeHistoryForTest(testId)

    fun observeAllSyncMeta(): Flow<List<HospitalPriceSyncMetaEntity>> =
        diagnosticPriceDao.observeAllSyncMeta()

    suspend fun getAllHospitalsOnce(): List<Hospital> = withContext(Dispatchers.IO) {
        hospitalDao.getAllHospitalsOnce().map { it.toModel() }
    }

    suspend fun seedCatalogIfNeeded() = withContext(Dispatchers.IO) {
        seedMutex.withLock {
            val catalogHospitals = BangladeshHealthcareCatalog.hospitals
            val existingCount = hospitalDao.countHospitals()
            val hasNewestCatalogEntry = hospitalDao.getHospitalById("hosp_ckd_urology") != null
            if (existingCount < catalogHospitals.size || !hasNewestCatalogEntry) {
                hospitalDao.upsertHospitals(catalogHospitals.map { HospitalEntity.fromModel(it) })
            }

            val catalogDoctors = BangladeshHealthcareCatalog.doctors
            if (doctorDao.countDoctors() != catalogDoctors.size) {
                doctorDao.upsertDoctors(catalogDoctors.map { DoctorEntity.fromModel(it) })
            }

            val catalogTests = BangladeshHealthcareCatalog.diagnosticTests
            val hasMasterUpgrade = diagnosticTestDao.getTestById("test_pet_ct") != null &&
                !(diagnosticTestDao.getTestById("test_cbc")?.shortName.isNullOrBlank())
            if (diagnosticTestDao.countTests() != catalogTests.size || !hasMasterUpgrade) {
                diagnosticTestDao.upsertTests(catalogTests.map { DiagnosticTestEntity.fromModel(it) })
            }
        }
    }

    suspend fun cacheDiscoveredHospitals(discovered: List<Hospital>): Int = withContext(Dispatchers.IO) {
        if (discovered.isEmpty()) return@withContext 0
        val existing = hospitalDao.getAllHospitalsOnce().map { it.toModel() }
        val merged = AreaHospitalRemoteDataSource.mergeHospitals(curated = existing, live = discovered)
        val existingIds = existing.map { it.id }.toHashSet()
        val toInsert = merged.filter { it.id !in existingIds }
        if (toInsert.isNotEmpty()) {
            hospitalDao.upsertHospitals(toInsert.map { HospitalEntity.fromModel(it) })
        }
        toInsert.size
    }

    suspend fun getHospitalById(id: String): Hospital? = withContext(Dispatchers.IO) {
        hospitalDao.getHospitalById(id)?.toModel()
    }

    suspend fun markHospitalViewed(hospitalId: String) = withContext(Dispatchers.IO) {
        localActivityDao.recordView(
            RecentViewEntity(
                compositeKey = "hospital:$hospitalId",
                targetId = hospitalId,
                targetType = "hospital"
            )
        )
    }

    suspend fun markDoctorViewed(doctorId: String) = withContext(Dispatchers.IO) {
        localActivityDao.recordView(
            RecentViewEntity(
                compositeKey = "doctor:$doctorId",
                targetId = doctorId,
                targetType = "doctor"
            )
        )
    }

    suspend fun recordSearchQuery(query: String) = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.length >= 2) {
            localActivityDao.recordSearch(SearchHistoryEntity(query = clean))
        }
    }

    suspend fun removeSearchQuery(query: String) = withContext(Dispatchers.IO) {
        localActivityDao.deleteSearch(query)
    }

    suspend fun clearCachedRemoteHospitals() = withContext(Dispatchers.IO) {
        hospitalDao.clearCachedRemoteHospitals()
    }

    suspend fun syncDiagnosticPricesForHospital(
        hospital: Hospital,
        fetcher: suspend (Hospital) -> HospitalPriceFetchResult = DiagnosticPriceSyncService::fetchHospitalDiagnosticPrices
    ): HospitalPriceSyncSummary = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = diagnosticPriceDao.getPricesForHospitalOnce(hospital.id)
        val result = fetcher(hospital)

        when (result) {
            is HospitalPriceFetchResult.Verified -> {
                val (entities, history, changesCount) = computeVerifiedPriceDiff(
                    hospital = hospital,
                    existing = existing,
                    result = result,
                    nowMillis = now
                )
                if (entities.isNotEmpty()) {
                    diagnosticPriceDao.upsertPrices(entities)
                }
                if (history.isNotEmpty()) {
                    diagnosticPriceDao.insertPriceHistory(history)
                }
                val verifiedCount = result.prices.count { it.verificationStatus == "VERIFIED" }
                val unverifiedCount = result.prices.size - verifiedCount
                val syncState = if (verifiedCount > 0) "VERIFIED" else "UNVERIFIED"
                val statusMsg = buildString {
                    append("Synced ${result.prices.size} online price(s) from ${result.sourceName}")
                    if (unverifiedCount > 0 && verifiedCount == 0) {
                        append(" (unverified source)")
                    } else if (unverifiedCount > 0) {
                        append(" ($verifiedCount verified, $unverifiedCount unverified)")
                    }
                }
                diagnosticPriceDao.upsertSyncMeta(
                    HospitalPriceSyncMetaEntity(
                        hospitalId = hospital.id,
                        hospitalName = hospital.name,
                        sourceUrl = result.sourceUrl,
                        syncState = syncState,
                        statusMessage = statusMsg,
                        verifiedPriceCount = verifiedCount,
                        lastSyncedMillis = now
                    )
                )
                HospitalPriceSyncSummary(
                    hospitalsChecked = 1,
                    verifiedHospitals = if (verifiedCount > 0) 1 else 0,
                    updatedOrNewPrices = result.prices.size,
                    changesDetected = changesCount,
                    unavailableHospitals = 0
                )
            }

            is HospitalPriceFetchResult.Unavailable -> {
                if (result.syncState == "NO_PUBLIC_PRICE_LIST" || result.syncState == "NO_OFFICIAL_URL") {
                    val invalidated = existing.filter { it.priceAvailable && it.currentPriceBdt != null }
                    if (invalidated.isNotEmpty()) {
                        val updatedRows = invalidated.map { old ->
                            old.copy(
                                previousPriceBdt = old.currentPriceBdt,
                                currentPriceBdt = null,
                                priceAvailable = false,
                                verificationStatus = "UNAVAILABLE",
                                lastCheckedMillis = now,
                                lastUpdatedMillis = now,
                                statusNote = result.reason
                            )
                        }
                        val historyRows = invalidated.map { old ->
                            DiagnosticPriceHistoryEntity(
                                priceRecordId = old.id,
                                hospitalId = hospital.id,
                                hospitalName = hospital.name,
                                canonicalTestId = old.canonicalTestId,
                                testName = old.testName,
                                previousPriceBdt = old.currentPriceBdt,
                                currentPriceBdt = null,
                                changedAtMillis = now,
                                sourceUrl = result.sourceUrl,
                                changeReason = "UNAVAILABLE"
                            )
                        }
                        diagnosticPriceDao.upsertPrices(updatedRows)
                        diagnosticPriceDao.insertPriceHistory(historyRows)
                    }
                }

                diagnosticPriceDao.upsertSyncMeta(
                    HospitalPriceSyncMetaEntity(
                        hospitalId = hospital.id,
                        hospitalName = hospital.name,
                        sourceUrl = result.sourceUrl,
                        syncState = result.syncState,
                        statusMessage = result.reason,
                        verifiedPriceCount = existing.count {
                            it.priceAvailable && it.currentPriceBdt != null && it.verificationStatus == "VERIFIED"
                        },
                        lastSyncedMillis = now
                    )
                )
                HospitalPriceSyncSummary(
                    hospitalsChecked = 1,
                    verifiedHospitals = 0,
                    updatedOrNewPrices = 0,
                    changesDetected = 0,
                    unavailableHospitals = 1
                )
            }
        }
    }

    suspend fun syncDiagnosticPricesBatch(
        hospitals: List<Hospital>? = null,
        maxHospitals: Int = 18,
        fetcher: suspend (Hospital) -> HospitalPriceFetchResult = DiagnosticPriceSyncService::fetchHospitalDiagnosticPrices
    ): HospitalPriceSyncSummary = withContext(Dispatchers.IO) {
        seedCatalogIfNeeded()
        val pool = hospitals ?: hospitalDao.getAllHospitalsOnce().map { it.toModel() }
        // Prioritize hospitals that have a real official website or portal endpoint
        val prioritized = pool
            .sortedByDescending { OfficialHospitalPortalRegistry.resolveEndpoints(it) != null }
            .distinctBy { it.id }
            .take(maxHospitals)

        if (prioritized.isEmpty()) {
            return@withContext HospitalPriceSyncSummary(0, 0, 0, 0, 0)
        }

        val semaphore = Semaphore(permits = 4)
        val results = coroutineScope {
            prioritized.map { hosp ->
                async {
                    semaphore.withPermit {
                        syncDiagnosticPricesForHospital(hosp, fetcher)
                    }
                }
            }.awaitAll()
        }

        HospitalPriceSyncSummary(
            hospitalsChecked = results.sumOf { it.hospitalsChecked },
            verifiedHospitals = results.sumOf { it.verifiedHospitals },
            updatedOrNewPrices = results.sumOf { it.updatedOrNewPrices },
            changesDetected = results.sumOf { it.changesDetected },
            unavailableHospitals = results.sumOf { it.unavailableHospitals }
        )
    }

    internal fun computeVerifiedPriceDiff(
        hospital: Hospital,
        existing: List<HospitalDiagnosticPriceEntity>,
        result: HospitalPriceFetchResult.Verified,
        nowMillis: Long
    ): Triple<List<HospitalDiagnosticPriceEntity>, List<DiagnosticPriceHistoryEntity>, Int> {
        val existingByTestId = existing.associateBy { it.canonicalTestId }
        val updatedEntities = mutableListOf<HospitalDiagnosticPriceEntity>()
        val historyEntries = mutableListOf<DiagnosticPriceHistoryEntity>()
        var changeCount = 0

        val fetchedIds = HashSet<String>(result.prices.size)

        result.prices.forEach { ext ->
            fetchedIds.add(ext.canonicalTestId)
            val recordId = "${hospital.id}__${ext.canonicalTestId}"
            val old = existingByTestId[ext.canonicalTestId]
            val catalogMeta = BangladeshHealthcareCatalog.diagnosticTests
                .firstOrNull { it.id == ext.canonicalTestId }
                ?.centerPrices
                ?.firstOrNull { it.centerId == hospital.id }

            val homeAvail = catalogMeta?.homeCollectionAvailable ?: old?.homeCollectionAvailable ?: false
            val homeFee = catalogMeta?.homeCollectionFeeBdt ?: old?.homeCollectionFeeBdt ?: 0
            val deliveryHrs = catalogMeta?.reportDeliveryHours ?: old?.reportDeliveryHours ?: 6
            val verificationStatus = ext.verificationStatus.ifBlank { "VERIFIED" }
            val verifiedNote = if (verificationStatus == "VERIFIED") {
                "Verified official price from ${result.sourceName}"
            } else {
                "Collected from ${result.sourceName} (unverified format)"
            }

            when {
                old == null || old.currentPriceBdt == null -> {
                    updatedEntities += HospitalDiagnosticPriceEntity(
                        id = recordId,
                        hospitalId = hospital.id,
                        hospitalName = hospital.name,
                        canonicalTestId = ext.canonicalTestId,
                        testName = ext.testName,
                        category = ext.category,
                        currentPriceBdt = ext.priceBdt,
                        previousPriceBdt = old?.previousPriceBdt,
                        priceAvailable = true,
                        homeCollectionAvailable = homeAvail,
                        homeCollectionFeeBdt = homeFee,
                        reportDeliveryHours = deliveryHrs,
                        sourceName = result.sourceName,
                        sourceUrl = ext.sourceUrl,
                        verificationStatus = verificationStatus,
                        lastCheckedMillis = nowMillis,
                        lastUpdatedMillis = nowMillis,
                        statusNote = verifiedNote
                    )
                    historyEntries += DiagnosticPriceHistoryEntity(
                        priceRecordId = recordId,
                        hospitalId = hospital.id,
                        hospitalName = hospital.name,
                        canonicalTestId = ext.canonicalTestId,
                        testName = ext.testName,
                        previousPriceBdt = old?.previousPriceBdt,
                        currentPriceBdt = ext.priceBdt,
                        changedAtMillis = nowMillis,
                        sourceUrl = ext.sourceUrl,
                        changeReason = "INITIAL_FETCH"
                    )
                    changeCount++
                }

                old.currentPriceBdt != ext.priceBdt -> {
                    val reason = if (ext.priceBdt > old.currentPriceBdt) "PRICE_INCREASE" else "PRICE_DECREASE"
                    updatedEntities += old.copy(
                        testName = ext.testName,
                        category = ext.category,
                        currentPriceBdt = ext.priceBdt,
                        previousPriceBdt = old.currentPriceBdt,
                        priceAvailable = true,
                        homeCollectionAvailable = homeAvail,
                        homeCollectionFeeBdt = homeFee,
                        reportDeliveryHours = deliveryHrs,
                        sourceName = result.sourceName,
                        sourceUrl = ext.sourceUrl,
                        verificationStatus = verificationStatus,
                        lastCheckedMillis = nowMillis,
                        lastUpdatedMillis = nowMillis,
                        statusNote = "Price changed automatically from ৳${old.currentPriceBdt} to ৳${ext.priceBdt}"
                    )
                    historyEntries += DiagnosticPriceHistoryEntity(
                        priceRecordId = recordId,
                        hospitalId = hospital.id,
                        hospitalName = hospital.name,
                        canonicalTestId = ext.canonicalTestId,
                        testName = ext.testName,
                        previousPriceBdt = old.currentPriceBdt,
                        currentPriceBdt = ext.priceBdt,
                        changedAtMillis = nowMillis,
                        sourceUrl = ext.sourceUrl,
                        changeReason = reason
                    )
                    changeCount++
                }

                else -> {
                    updatedEntities += old.copy(
                        sourceName = result.sourceName,
                        sourceUrl = ext.sourceUrl,
                        verificationStatus = verificationStatus,
                        lastCheckedMillis = nowMillis,
                        priceAvailable = true,
                        statusNote = verifiedNote
                    )
                }
            }
        }

        // Detect prices previously available from this portal that are no longer listed
        existing.filter { it.priceAvailable && it.currentPriceBdt != null && it.canonicalTestId !in fetchedIds }
            .forEach { stale ->
                updatedEntities += stale.copy(
                    previousPriceBdt = stale.currentPriceBdt,
                    currentPriceBdt = null,
                    priceAvailable = false,
                    verificationStatus = "UNAVAILABLE",
                    lastCheckedMillis = nowMillis,
                    lastUpdatedMillis = nowMillis,
                    statusNote = "Removed from latest official listing"
                )
                historyEntries += DiagnosticPriceHistoryEntity(
                    priceRecordId = stale.id,
                    hospitalId = hospital.id,
                    hospitalName = hospital.name,
                    canonicalTestId = stale.canonicalTestId,
                    testName = stale.testName,
                    previousPriceBdt = stale.currentPriceBdt,
                    currentPriceBdt = null,
                    changedAtMillis = nowMillis,
                    sourceUrl = result.sourceUrl,
                    changeReason = "UNAVAILABLE"
                )
                changeCount++
            }

        return Triple(updatedEntities, historyEntries, changeCount)
    }
}
