package com.example.ui.state

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.BangladeshHealthcareCatalog
import com.example.data.local.DiagnosticPriceHistoryEntity
import com.example.data.local.HospitalDiagnosticPriceEntity
import com.example.data.local.HospitalPriceSyncMetaEntity
import com.example.data.local.LocalHealthcareRepository
import com.example.data.model.AppNotification
import com.example.data.model.Appointment
import com.example.data.model.DiagnosticTest
import com.example.data.model.Doctor
import com.example.data.model.FacilityType
import com.example.data.model.FavoriteItem
import com.example.data.model.Hospital
import com.example.data.model.MedicalRecord
import com.example.data.model.Review
import com.example.data.model.TestBooking
import com.example.data.model.UserProfile
import com.example.data.repository.AreaHospitalRemoteDataSource
import com.example.data.repository.LocationHelper
import com.example.data.repository.MedCareRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class HospitalFilterState(
    val query: String = "",
    val division: String = "All",
    val district: String = "All",
    val area: String = "All",
    val facilityType: FacilityType? = null,
    val onlyEmergency247: Boolean = false,
    val onlyIcu: Boolean = false,
    val minRating: Double = 0.0,
    val mapViewEnabled: Boolean = false
)

data class DoctorFilterState(
    val query: String = "",
    val specialization: String = "All",
    val area: String = "All",
    val minExperienceYears: Int = 0,
    val maxFeeBdt: Int = 5000,
    val minRating: Double = 0.0,
    val weekdayShort: String = "All"
)

data class DiagnosticFilterState(
    val query: String = "",
    val category: String = "All",
    val subcategory: String = "All",
    val sampleFamily: String = "All",
    val onlyHomeCollection: Boolean = false,
    val onlyVerifiedPrice: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class MedCareViewModel(
    private val repository: MedCareRepository,
    private val localRepository: LocalHealthcareRepository,
    initialUserId: String?
) : ViewModel() {

    private companion object {
        const val TAG = "MedCareVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }

    private val _currentUserId = MutableStateFlow(initialUserId)
    val currentUserIdFlow: StateFlow<String?> = _currentUserId.asStateFlow()

    // Guest-mode local preferences (automatically synced to Firestore profile when signed in)
    private val _guestLanguage = MutableStateFlow("en")
    val guestLanguage: StateFlow<String> = _guestLanguage.asStateFlow()

    private val _guestDarkMode = MutableStateFlow("system")
    val guestDarkMode: StateFlow<String> = _guestDarkMode.asStateFlow()

    private val _signInPromptReason = MutableStateFlow<String?>(null)
    val signInPromptReason: StateFlow<String?> = _signInPromptReason.asStateFlow()

    init {
        viewModelScope.launch {
            localRepository.seedCatalogIfNeeded()
        }
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null && currentUser.uid == initialUserId) {
            viewModelScope.launch {
                repository.ensureUserProfileExists(
                    userId = currentUser.uid,
                    displayName = currentUser.displayName ?: "Patient",
                    email = currentUser.email ?: ""
                )
            }
        }
    }

    fun onAuthStateChanged(uid: String?, displayName: String?, email: String?) {
        if (_currentUserId.value == uid) return
        _currentUserId.value = uid
        if (uid != null) {
            _signInPromptReason.value = null
            viewModelScope.launch {
                repository.ensureUserProfileExists(
                    userId = uid,
                    displayName = displayName ?: "Patient",
                    email = email ?: ""
                )
            }
        }
    }

    fun requestSignIn(reason: String = "Sign in with Google to save favorites, book appointments, and sync your medical records.") {
        _signInPromptReason.value = reason
    }

    fun dismissSignInPrompt() {
        _signInPromptReason.value = null
    }

    private fun <T> Flow<T>.toUiState(
        defaultError: String,
        initial: UiState<T> = UiState.Loading
    ): StateFlow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, defaultError, rootError)
                emit(UiState.Error(rootError.message ?: defaultError))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = initial
            )

    val userProfileState: StateFlow<UiState<UserProfile?>> =
        _currentUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(null)
            else repository.observeUserProfile(uid)
        }.toUiState("Failed to load user profile", UiState.Success(null))

    val favoritesState: StateFlow<UiState<List<FavoriteItem>>> =
        _currentUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else repository.observeFavorites(uid)
        }.toUiState("Failed to load favorites", UiState.Success(emptyList()))

    val appointmentsState: StateFlow<UiState<List<Appointment>>> =
        _currentUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else repository.observeAppointments(uid)
        }.toUiState("Failed to load appointments", UiState.Success(emptyList()))

    val testBookingsState: StateFlow<UiState<List<TestBooking>>> =
        _currentUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else repository.observeTestBookings(uid)
        }.toUiState("Failed to load test bookings", UiState.Success(emptyList()))

    val medicalRecordsState: StateFlow<UiState<List<MedicalRecord>>> =
        _currentUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else repository.observeMedicalRecords(uid)
        }.toUiState("Failed to load medical records", UiState.Success(emptyList()))

    val notificationsState: StateFlow<UiState<List<AppNotification>>> =
        _currentUserId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else repository.observeNotifications(uid)
        }.toUiState("Failed to load notifications", UiState.Success(emptyList()))

    // Active review target observation (only queries Firestore when user is signed in)
    private val activeReviewTarget = MutableStateFlow<Pair<String, String>?>(null)

    val targetReviewsState: StateFlow<UiState<List<Review>>> =
        combine(activeReviewTarget, _currentUserId) { pair, uid -> pair to uid }
            .flatMapLatest { (pair, uid) ->
                if (pair == null || uid == null) flowOf(emptyList())
                else repository.observeReviewsForTarget(pair.first, pair.second)
            }.toUiState("Failed to load reviews", UiState.Success(emptyList()))

    fun setReviewTarget(targetId: String, targetType: String) {
        activeReviewTarget.value = targetId to targetType
    }

    // Live area-wise hospital discovery backed by SQLite Room Database
    private val _isFetchingArea = MutableStateFlow(false)
    val isFetchingArea: StateFlow<Boolean> = _isFetchingArea.asStateFlow()

    private val _areaSyncStatus = MutableStateFlow<String?>(null)
    val areaSyncStatus: StateFlow<String?> = _areaSyncStatus.asStateFlow()

    private val fetchedAreaKeys = mutableSetOf<String>()
    private val inFlightAreaKeys = mutableSetOf<String>()
    private var liveSearchJob: Job? = null
    private var activeSyncCount = 0

    val allHospitals: StateFlow<List<Hospital>> = localRepository.observeAllHospitals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = BangladeshHealthcareCatalog.hospitals
        )

    val allDoctors: StateFlow<List<Doctor>> = localRepository.observeAllDoctors()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = BangladeshHealthcareCatalog.doctors
        )

    val allDiagnosticTests: StateFlow<List<DiagnosticTest>> = localRepository.observeAllDiagnosticTests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = BangladeshHealthcareCatalog.diagnosticTests
        )

    val allDiagnosticPrices: StateFlow<List<HospitalDiagnosticPriceEntity>> = localRepository.observeAllDiagnosticPrices()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val allPriceHistory: StateFlow<List<DiagnosticPriceHistoryEntity>> = localRepository.observeAllPriceHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    val allPriceSyncMeta: StateFlow<List<HospitalPriceSyncMetaEntity>> = localRepository.observeAllSyncMeta()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    private val _isSyncingPrices = MutableStateFlow(false)
    val isSyncingPrices: StateFlow<Boolean> = _isSyncingPrices.asStateFlow()

    private val _priceSyncStatus = MutableStateFlow<String?>(null)
    val priceSyncStatus: StateFlow<String?> = _priceSyncStatus.asStateFlow()

    fun syncHospitalDiagnosticPrices(hospital: Hospital) {
        if (_isSyncingPrices.value) {
            _snackbarMessage.value = "Price sync already in progress..."
            return
        }
        viewModelScope.launch {
            _isSyncingPrices.value = true
            try {
                val summary = localRepository.syncDiagnosticPricesForHospital(hospital)
                val msg = when {
                    summary.verifiedHospitals > 0 ->
                        "Synced ${summary.updatedOrNewPrices} verified price(s) (${summary.changesDetected} update(s))"
                    summary.updatedOrNewPrices > 0 ->
                        "Collected ${summary.updatedOrNewPrices} unverified online price(s) (${summary.changesDetected} update(s))"
                    else ->
                        "Checked official source: online price list unavailable"
                }
                _priceSyncStatus.value = msg
                _snackbarMessage.value = msg
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Hospital diagnostic price sync failed", e)
                val msg = "Could not sync prices for ${hospital.name}"
                _priceSyncStatus.value = msg
                _snackbarMessage.value = msg
            } finally {
                _isSyncingPrices.value = false
            }
        }
    }

    fun syncAllDiagnosticPrices(preferredHospitalIds: Set<String> = emptySet()) {
        if (_isSyncingPrices.value) {
            _snackbarMessage.value = "Price sync already in progress..."
            return
        }
        viewModelScope.launch {
            _isSyncingPrices.value = true
            try {
                val hospitals = allHospitals.value
                val ordered = if (preferredHospitalIds.isNotEmpty()) {
                    hospitals.sortedByDescending { it.id in preferredHospitalIds }
                } else {
                    hospitals
                }
                val summary = localRepository.syncDiagnosticPricesBatch(
                    hospitals = ordered,
                    maxHospitals = 18
                )
                val msg = when {
                    summary.verifiedHospitals > 0 ->
                        "Checked ${summary.hospitalsChecked} portals: ${summary.verifiedHospitals} verified, ${summary.changesDetected} change(s)"
                    summary.updatedOrNewPrices > 0 ->
                        "Checked ${summary.hospitalsChecked} websites: ${summary.updatedOrNewPrices} unverified price(s) collected"
                    else ->
                        "Checked ${summary.hospitalsChecked} official portals: no public price tables available"
                }
                _priceSyncStatus.value = msg
                _snackbarMessage.value = msg
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Batch diagnostic price sync failed", e)
                _priceSyncStatus.value = "Diagnostic price sync failed"
                _snackbarMessage.value = "Diagnostic price sync failed"
            } finally {
                _isSyncingPrices.value = false
            }
        }
    }

    val recentSearches: StateFlow<List<String>> = localRepository.observeRecentSearches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    fun getHospitalById(id: String): Hospital? =
        allHospitals.value.firstOrNull { it.id == id }

    // Device GPS location (or selected area hub fallback)
    private val _deviceLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val deviceLocation: StateFlow<Pair<Double, Double>?> = _deviceLocation.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    fun refreshLocation(context: Context, granted: Boolean) {
        _hasLocationPermission.value = granted
        if (!granted) return
        viewModelScope.launch {
            _deviceLocation.value = LocationHelper.getCurrentCoarseLocation(context)
            syncAreaHospitals(force = true)
        }
    }

    fun effectiveUserCoordinates(): Pair<Double, Double> {
        _deviceLocation.value?.let { return it }
        val profile = (userProfileState.value as? UiState.Success)?.data
        val areaMatch = BangladeshHealthcareCatalog.areas.firstOrNull {
            it.name.equals(profile?.area, ignoreCase = true)
        } ?: BangladeshHealthcareCatalog.areas.first()
        return areaMatch.lat to areaMatch.lng
    }

    fun distanceToHospitalKm(hospital: Hospital): Double {
        val (uLat, uLng) = effectiveUserCoordinates()
        return LocationHelper.distanceKm(uLat, uLng, hospital.latitude, hospital.longitude)
    }

    // Search & Filters
    private val _hospitalFilter = MutableStateFlow(HospitalFilterState())
    val hospitalFilter: StateFlow<HospitalFilterState> = _hospitalFilter.asStateFlow()

    private val _doctorFilter = MutableStateFlow(DoctorFilterState())
    val doctorFilter: StateFlow<DoctorFilterState> = _doctorFilter.asStateFlow()

    private val _diagnosticFilter = MutableStateFlow(DiagnosticFilterState())
    val diagnosticFilter: StateFlow<DiagnosticFilterState> = _diagnosticFilter.asStateFlow()

    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    fun updateGlobalSearch(q: String) {
        _globalSearchQuery.value = q
    }

    fun updateHospitalFilter(transform: (HospitalFilterState) -> HospitalFilterState) {
        val previous = _hospitalFilter.value
        val updated = transform(previous)
        _hospitalFilter.value = updated
        if (updated.area != previous.area ||
            updated.district != previous.district ||
            updated.division != previous.division
        ) {
            syncAreaHospitals(force = false, customQuery = updated.query)
        }
    }

    fun searchHospitalsLive(query: String, immediate: Boolean = false) {
        val cleanQuery = query.trim()
        liveSearchJob?.cancel()
        if (cleanQuery.length < 3) return

        val filter = _hospitalFilter.value
        val key = "search|${filter.division}|${filter.district}|${filter.area}|${cleanQuery.lowercase()}"
        if (!immediate && (key in fetchedAreaKeys || key in inFlightAreaKeys)) return

        liveSearchJob = viewModelScope.launch {
            if (!immediate) delay(450)
            syncAreaHospitals(force = immediate, customQuery = cleanQuery)
        }
    }

    fun syncAreaHospitals(force: Boolean = false, customQuery: String = "") {
        val filter = _hospitalFilter.value
        val cleanQuery = customQuery.trim()
        val keyPrefix = if (cleanQuery.isNotBlank()) "search" else "area"
        val key = "$keyPrefix|${filter.division}|${filter.district}|${filter.area}|${cleanQuery.lowercase()}"
        if (!force && (key in fetchedAreaKeys || key in inFlightAreaKeys)) {
            return
        }
        inFlightAreaKeys.add(key)

        val profile = (userProfileState.value as? UiState.Success)?.data
        val fallbackArea = BangladeshHealthcareCatalog.areas.firstOrNull {
            it.name.equals(profile?.area, ignoreCase = true)
        } ?: BangladeshHealthcareCatalog.areas.first()

        val matchedArea = when {
            filter.area != "All" -> BangladeshHealthcareCatalog.areas.firstOrNull {
                it.name.equals(filter.area, ignoreCase = true)
            }
            filter.district != "All" -> BangladeshHealthcareCatalog.areas.firstOrNull {
                it.district.equals(filter.district, ignoreCase = true)
            }
            filter.division != "All" -> BangladeshHealthcareCatalog.areas.firstOrNull {
                it.division.equals(filter.division, ignoreCase = true)
            }
            else -> fallbackArea
        }

        val (centerLat, centerLng) = if (filter.area != "All" || filter.district != "All" || filter.division != "All") {
            (matchedArea?.lat ?: fallbackArea.lat) to (matchedArea?.lng ?: fallbackArea.lng)
        } else {
            effectiveUserCoordinates()
        }

        val targetArea = when {
            filter.area != "All" -> filter.area
            cleanQuery.isNotBlank() -> "All"
            else -> matchedArea?.name ?: fallbackArea.name
        }
        val targetDistrict = when {
            filter.district != "All" -> filter.district
            cleanQuery.isNotBlank() -> "Dhaka"
            else -> matchedArea?.district ?: fallbackArea.district
        }
        val targetDivision = when {
            filter.division != "All" -> filter.division
            cleanQuery.isNotBlank() -> "All"
            else -> matchedArea?.division ?: fallbackArea.division
        }

        viewModelScope.launch {
            activeSyncCount++
            _isFetchingArea.value = true
            try {
                if (force && cleanQuery.isNotBlank()) {
                    localRepository.recordSearchQuery(cleanQuery)
                }
                val result = AreaHospitalRemoteDataSource.fetchHospitalsForArea(
                    areaName = targetArea,
                    district = targetDistrict,
                    division = targetDivision,
                    centerLat = centerLat,
                    centerLng = centerLng,
                    query = cleanQuery
                )
                val cachedNew = if (result.hospitals.isNotEmpty()) {
                    localRepository.cacheDiscoveredHospitals(result.hospitals)
                } else 0
                fetchedAreaKeys.add(key)
                _areaSyncStatus.value = when {
                    cleanQuery.isNotBlank() && result.hospitals.isNotEmpty() ->
                        "${result.sourceLabel} · ${result.hospitals.size} match(es) cached in SQLite DB"
                    cleanQuery.isNotBlank() ->
                        result.note ?: "${result.sourceLabel} · Searched \"$cleanQuery\" (SQLite DB)"
                    result.hospitals.isNotEmpty() && cachedNew > 0 ->
                        "${result.sourceLabel} · +$cachedNew saved to SQLite DB ($targetArea)"
                    result.hospitals.isNotEmpty() ->
                        "${result.sourceLabel} · ${result.hospitals.size} live in $targetArea (SQLite DB)"
                    else ->
                        result.note ?: "${result.sourceLabel} · $targetArea (SQLite DB)"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Area hospital sync failed", e)
                _areaSyncStatus.value = if (cleanQuery.isNotBlank()) {
                    "Showing SQLite DB matches for \"$cleanQuery\""
                } else {
                    "Showing SQLite DB ($targetArea)"
                }
            } finally {
                inFlightAreaKeys.remove(key)
                activeSyncCount = (activeSyncCount - 1).coerceAtLeast(0)
                _isFetchingArea.value = activeSyncCount > 0
            }
        }
    }

    fun updateDoctorFilter(transform: (DoctorFilterState) -> DoctorFilterState) {
        _doctorFilter.update(transform)
    }

    fun updateDiagnosticFilter(transform: (DiagnosticFilterState) -> DiagnosticFilterState) {
        _diagnosticFilter.update { previous ->
            val updated = transform(previous)
            if (updated.category != previous.category && updated.subcategory == previous.subcategory) {
                updated.copy(subcategory = "All")
            } else {
                updated
            }
        }
    }

    fun removeSearchHistoryItem(query: String) {
        viewModelScope.launch {
            localRepository.removeSearchQuery(query)
        }
    }

    // Comparison & Recently Viewed (backed by SQLite Room)
    private val _compareHospitalIds = MutableStateFlow<List<String>>(emptyList())
    val compareHospitalIds: StateFlow<List<String>> = _compareHospitalIds.asStateFlow()

    fun toggleCompareHospital(hospitalId: String): String? {
        val current = _compareHospitalIds.value
        return if (hospitalId in current) {
            _compareHospitalIds.value = current - hospitalId
            null
        } else if (current.size >= 3) {
            "You can compare up to 3 facilities at once."
        } else {
            _compareHospitalIds.value = current + hospitalId
            null
        }
    }

    fun clearCompare() {
        _compareHospitalIds.value = emptyList()
    }

    val recentHospitalIds: StateFlow<List<String>> = localRepository.observeRecentHospitalIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    val recentDoctorIds: StateFlow<List<String>> = localRepository.observeRecentDoctorIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    fun markHospitalViewed(id: String) {
        viewModelScope.launch {
            localRepository.markHospitalViewed(id)
        }
    }

    fun markDoctorViewed(id: String) {
        viewModelScope.launch {
            localRepository.markDoctorViewed(id)
        }
    }

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun toggleLanguage() {
        val uid = _currentUserId.value
        val profile = (userProfileState.value as? UiState.Success)?.data
        if (uid == null || profile == null) {
            _guestLanguage.update { if (it == "bn") "en" else "bn" }
            return
        }
        val nextLang = if (profile.language == "bn") "en" else "bn"
        _guestLanguage.value = nextLang
        saveUserProfile(profile.copy(language = nextLang))
    }

    fun setGuestOrUserLanguage(language: String) {
        val nextLang = if (language == "bn") "bn" else "en"
        _guestLanguage.value = nextLang
        val uid = _currentUserId.value
        val profile = (userProfileState.value as? UiState.Success)?.data
        if (uid != null && profile != null) {
            saveUserProfile(profile.copy(language = nextLang))
        }
    }

    fun setGuestOrUserDarkMode(mode: String) {
        val valid = if (mode in listOf("system", "light", "dark")) mode else "system"
        _guestDarkMode.value = valid
        val uid = _currentUserId.value
        val profile = (userProfileState.value as? UiState.Success)?.data
        if (uid != null && profile != null) {
            saveUserProfile(profile.copy(darkMode = valid))
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        val uid = _currentUserId.value
        if (uid == null) {
            requestSignIn("Sign in with Google to save your patient profile.")
            return
        }
        viewModelScope.launch {
            val res = repository.updateUserProfile(profile.copy(userId = uid))
            res.onSuccess { _snackbarMessage.value = "Profile updated" }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Failed to update profile" }
        }
    }

    fun toggleFavorite(
        targetId: String,
        targetType: String,
        title: String,
        subtitle: String,
        currentlyFavorite: Boolean
    ) {
        val uid = _currentUserId.value
        if (uid == null) {
            requestSignIn("Sign in with Google to save favorite hospitals and doctors.")
            return
        }
        viewModelScope.launch {
            val res = repository.toggleFavorite(
                uid,
                targetId,
                targetType,
                title,
                subtitle,
                currentlyFavorite
            )
            res.onSuccess {
                _snackbarMessage.value = if (currentlyFavorite) "Removed from saved" else "Saved to favorites"
            }.onFailure {
                _snackbarMessage.value = it.localizedMessage ?: "Could not update favorite"
            }
        }
    }

    fun bookAppointment(
        doctor: Doctor,
        hospitalId: String,
        hospitalName: String,
        chamberName: String,
        appointmentDate: String,
        slotTime: String,
        patientName: String,
        patientPhone: String,
        feeBdt: Int,
        notes: String,
        onBooked: () -> Unit
    ) {
        val uid = _currentUserId.value
        if (uid == null) {
            requestSignIn("Sign in with Google to confirm your appointment with ${doctor.name}.")
            return
        }
        if (patientName.isBlank()) {
            _snackbarMessage.value = "Please enter the patient name"
            return
        }
        if (appointmentDate.isBlank() || slotTime.isBlank()) {
            _snackbarMessage.value = "Please select a valid appointment date and time"
            return
        }
        if (!LocationHelper.isValidBangladeshPhone(patientPhone)) {
            _snackbarMessage.value = "Enter a valid Bangladesh mobile (e.g. 017XXXXXXXX)"
            return
        }
        viewModelScope.launch {
            val res = repository.bookAppointment(
                userId = uid,
                doctorId = doctor.id,
                doctorName = doctor.name,
                specialization = doctor.specialization,
                hospitalId = hospitalId,
                hospitalName = hospitalName,
                chamberName = chamberName,
                appointmentDate = appointmentDate,
                slotTime = slotTime,
                patientName = patientName,
                patientPhone = patientPhone,
                consultationFeeBdt = feeBdt.toLong(),
                notes = notes
            )
            res.onSuccess {
                _snackbarMessage.value = "Appointment confirmed for $appointmentDate at $slotTime"
                onBooked()
            }.onFailure {
                _snackbarMessage.value = it.localizedMessage ?: "Failed to book appointment"
            }
        }
    }

    fun rescheduleAppointment(
        appointment: Appointment,
        newDate: String,
        newSlot: String,
        notes: String
    ) {
        val uid = _currentUserId.value ?: return
        if (newDate.isBlank() || newSlot.isBlank()) {
            _snackbarMessage.value = "Please enter a valid date and time slot"
            return
        }
        viewModelScope.launch {
            val res = repository.rescheduleAppointment(
                uid,
                appointment.id,
                appointment.doctorId,
                appointment.chamberName,
                newDate,
                newSlot,
                notes
            )
            res.onSuccess { _snackbarMessage.value = "Appointment rescheduled" }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Reschedule failed" }
        }
    }

    fun cancelAppointment(appointment: Appointment) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val res = repository.cancelAppointment(uid, appointment.id, appointment.doctorName)
            res.onSuccess { _snackbarMessage.value = "Appointment cancelled" }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Cancel failed" }
        }
    }

    fun bookDiagnosticTest(
        test: DiagnosticTest,
        centerId: String,
        centerName: String,
        bookingDate: String,
        timeWindow: String,
        homeCollection: Boolean,
        collectionAddress: String,
        patientName: String,
        patientPhone: String,
        totalPriceBdt: Int,
        onBooked: () -> Unit
    ) {
        val uid = _currentUserId.value
        if (uid == null) {
            requestSignIn("Sign in with Google to confirm your booking for ${test.name}.")
            return
        }
        if (patientName.isBlank()) {
            _snackbarMessage.value = "Please enter the patient name"
            return
        }
        if (bookingDate.isBlank() || timeWindow.isBlank()) {
            _snackbarMessage.value = "Please choose a preferred date and time window"
            return
        }
        if (!LocationHelper.isValidBangladeshPhone(patientPhone)) {
            _snackbarMessage.value = "Enter a valid Bangladesh mobile (e.g. 017XXXXXXXX)"
            return
        }
        if (homeCollection && collectionAddress.isBlank()) {
            _snackbarMessage.value = "Please enter your Dhaka/BD home collection address"
            return
        }
        viewModelScope.launch {
            val res = repository.bookDiagnosticTest(
                userId = uid,
                testId = test.id,
                testName = test.name,
                centerId = centerId,
                centerName = centerName,
                bookingDate = bookingDate,
                timeWindow = timeWindow,
                homeCollection = homeCollection,
                collectionAddress = collectionAddress,
                patientName = patientName,
                patientPhone = patientPhone,
                priceBdt = totalPriceBdt.toLong()
            )
            res.onSuccess {
                _snackbarMessage.value = "Diagnostic test booked for $bookingDate"
                onBooked()
            }.onFailure {
                _snackbarMessage.value = it.localizedMessage ?: "Failed to book test"
            }
        }
    }

    fun cancelTestBooking(booking: TestBooking) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val res = repository.cancelTestBooking(uid, booking.id, booking.testName)
            res.onSuccess { _snackbarMessage.value = "Test booking cancelled" }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Cancel failed" }
        }
    }

    fun submitReview(
        targetId: String,
        targetType: String,
        overall: Int,
        doctorBehavior: Int,
        staffBehavior: Int,
        waitingTime: Int,
        cleanliness: Int,
        comment: String
    ) {
        val uid = _currentUserId.value
        if (uid == null) {
            requestSignIn("Sign in with Google to publish a verified review.")
            return
        }
        val profile = (userProfileState.value as? UiState.Success)?.data
        val author = profile?.displayName ?: Firebase.auth.currentUser?.displayName ?: "Patient"
        val appointments = (appointmentsState.value as? UiState.Success)?.data.orEmpty()
        val testBookings = (testBookingsState.value as? UiState.Success)?.data.orEmpty()
        val isVerified = appointments.any {
            (it.hospitalId == targetId || it.doctorId == targetId) && it.status != "cancelled"
        } || testBookings.any { it.centerId == targetId && it.status != "cancelled" }

        viewModelScope.launch {
            val res = repository.submitReview(
                userId = uid,
                authorName = author,
                targetId = targetId,
                targetType = targetType,
                overallRating = overall,
                doctorBehaviorRating = doctorBehavior,
                staffBehaviorRating = staffBehavior,
                waitingTimeRating = waitingTime,
                cleanlinessRating = cleanliness,
                comment = comment,
                verifiedBooking = isVerified
            )
            res.onSuccess { _snackbarMessage.value = "Review published" }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Failed to submit review" }
        }
    }

    fun reportReview(reviewId: String, reason: String) {
        val uid = _currentUserId.value
        if (uid == null) {
            requestSignIn("Sign in with Google to report reviews.")
            return
        }
        viewModelScope.launch {
            val res = repository.reportReview(uid, reviewId, reason)
            res.onSuccess { _snackbarMessage.value = "Review reported for moderation" }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Failed to report review" }
        }
    }

    fun addMedicalRecord(
        title: String,
        recordType: String,
        facilityName: String,
        recordDate: String,
        notes: String,
        attachmentUri: String?
    ) {
        val uid = _currentUserId.value
        if (uid == null) {
            requestSignIn("Sign in with Google to store private medical records.")
            return
        }
        viewModelScope.launch {
            val res = repository.addMedicalRecord(
                uid, title, recordType, facilityName, recordDate, notes, attachmentUri
            )
            res.onSuccess { _snackbarMessage.value = "Health record saved" }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Could not save record" }
        }
    }

    fun deleteMedicalRecord(recordId: String) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val res = repository.deleteMedicalRecord(uid, recordId)
            res.onSuccess { _snackbarMessage.value = "Record removed" }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Could not delete record" }
        }
    }

    fun markNotificationRead(notificationId: String) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            repository.markNotificationRead(uid, notificationId)
        }
    }

    fun deleteUserProfileData(onDeleted: () -> Unit) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val res = repository.deleteUserAccountData(uid)
            res.onSuccess {
                _snackbarMessage.value = "Profile data cleared"
                onDeleted()
            }.onFailure {
                _snackbarMessage.value = it.localizedMessage ?: "Failed to delete profile"
            }
        }
    }
}
