package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalDao {

    @Query("SELECT * FROM hospitals ORDER BY CASE WHEN dataSource = 'Directory' THEN 0 ELSE 1 END, isPopular DESC, rating DESC, name ASC")
    fun observeAllHospitals(): Flow<List<HospitalEntity>>

    @Query("SELECT * FROM hospitals WHERE id = :id LIMIT 1")
    suspend fun getHospitalById(id: String): HospitalEntity?

    @Query("SELECT * FROM hospitals")
    suspend fun getAllHospitalsOnce(): List<HospitalEntity>

    @Query("SELECT COUNT(*) FROM hospitals")
    suspend fun countHospitals(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHospitals(hospitals: List<HospitalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHospital(hospital: HospitalEntity)

    @Query("DELETE FROM hospitals WHERE dataSource != 'Directory'")
    suspend fun clearCachedRemoteHospitals()
}

@Dao
interface DoctorDao {

    @Query("SELECT * FROM doctors ORDER BY isTopRated DESC, rating DESC, name ASC")
    fun observeAllDoctors(): Flow<List<DoctorEntity>>

    @Query("SELECT COUNT(*) FROM doctors")
    suspend fun countDoctors(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDoctors(doctors: List<DoctorEntity>)
}

@Dao
interface DiagnosticTestDao {

    @Query("SELECT * FROM diagnostic_tests ORDER BY category ASC, name ASC")
    fun observeAllTests(): Flow<List<DiagnosticTestEntity>>

    @Query("SELECT * FROM diagnostic_tests WHERE id = :id LIMIT 1")
    suspend fun getTestById(id: String): DiagnosticTestEntity?

    @Query("SELECT COUNT(*) FROM diagnostic_tests")
    suspend fun countTests(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTests(tests: List<DiagnosticTestEntity>)
}

@Dao
interface LocalActivityDao {

    @Query("SELECT targetId FROM recent_views WHERE targetType = :type ORDER BY viewedAtMillis DESC LIMIT 6")
    fun observeRecentIds(type: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordView(view: RecentViewEntity)

    @Query("SELECT query FROM search_history ORDER BY searchedAtMillis DESC LIMIT 8")
    fun observeRecentSearches(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordSearch(entry: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearch(query: String)
}

@Dao
interface DiagnosticPriceDao {

    @Query("SELECT * FROM hospital_diagnostic_prices ORDER BY priceAvailable DESC, currentPriceBdt ASC, testName ASC")
    fun observeAllPrices(): Flow<List<HospitalDiagnosticPriceEntity>>

    @Query("SELECT * FROM hospital_diagnostic_prices WHERE hospitalId = :hospitalId ORDER BY priceAvailable DESC, currentPriceBdt ASC, testName ASC")
    fun observePricesForHospital(hospitalId: String): Flow<List<HospitalDiagnosticPriceEntity>>

    @Query("SELECT * FROM hospital_diagnostic_prices WHERE canonicalTestId = :testId ORDER BY priceAvailable DESC, currentPriceBdt ASC, hospitalName ASC")
    fun observePricesForTest(testId: String): Flow<List<HospitalDiagnosticPriceEntity>>

    @Query("SELECT * FROM hospital_diagnostic_prices WHERE hospitalId = :hospitalId")
    suspend fun getPricesForHospitalOnce(hospitalId: String): List<HospitalDiagnosticPriceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrices(prices: List<HospitalDiagnosticPriceEntity>)

    @Query("SELECT * FROM diagnostic_price_history ORDER BY changedAtMillis DESC LIMIT 200")
    fun observeAllHistory(): Flow<List<DiagnosticPriceHistoryEntity>>

    @Query("SELECT * FROM diagnostic_price_history WHERE hospitalId = :hospitalId ORDER BY changedAtMillis DESC LIMIT 100")
    fun observeHistoryForHospital(hospitalId: String): Flow<List<DiagnosticPriceHistoryEntity>>

    @Query("SELECT * FROM diagnostic_price_history WHERE canonicalTestId = :testId ORDER BY changedAtMillis DESC LIMIT 100")
    fun observeHistoryForTest(testId: String): Flow<List<DiagnosticPriceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(entries: List<DiagnosticPriceHistoryEntity>)

    @Query("SELECT * FROM hospital_price_sync_meta ORDER BY lastSyncedMillis DESC")
    fun observeAllSyncMeta(): Flow<List<HospitalPriceSyncMetaEntity>>

    @Query("SELECT * FROM hospital_price_sync_meta WHERE hospitalId = :hospitalId LIMIT 1")
    fun observeSyncMetaForHospital(hospitalId: String): Flow<HospitalPriceSyncMetaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncMeta(meta: HospitalPriceSyncMetaEntity)
}

