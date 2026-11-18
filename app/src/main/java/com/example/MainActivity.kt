package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.LocalHealthcareRepository
import com.example.data.local.MedCareRoomDatabase
import com.example.data.local.toFallbackDiagnosticTest
import com.example.data.repository.MedCareRepository
import com.example.data.sync.DiagnosticSyncScheduler
import com.example.ui.auth.GoogleSignInPromptDialog
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.signOutUser
import com.example.ui.screens.AppointmentsScreen
import com.example.ui.screens.CompareScreen
import com.example.ui.screens.DiagnosticScreen
import com.example.ui.screens.DoctorDetailScreen
import com.example.ui.screens.DoctorsScreen
import com.example.ui.screens.EmergencyScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HospitalDetailScreen
import com.example.ui.screens.HospitalsScreen
import com.example.ui.screens.MedicalRecordsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TestDetailScreen
import com.example.ui.state.MedCareViewModel
import com.example.ui.state.UiState
import com.example.ui.state.stringsFor
import com.example.ui.theme.MedCareTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object Routes {
    const val HOME = "home"
    const val HOSPITALS = "hospitals"
    const val DOCTORS = "doctors"
    const val DIAGNOSTIC = "diagnostic"
    const val PROFILE = "profile"
    const val HOSPITAL_DETAIL = "hospital_detail/{hospitalId}"
    const val DOCTOR_DETAIL = "doctor_detail/{doctorId}"
    const val TEST_DETAIL = "test_detail/{testId}"
    const val COMPARE = "compare"
    const val EMERGENCY = "emergency"
    const val APPOINTMENTS = "appointments"
    const val MEDICAL_RECORDS = "medical_records"

    fun hospitalDetail(id: String) = "hospital_detail/$id"
    fun doctorDetail(id: String) = "doctor_detail/$id"
    fun testDetail(id: String) = "test_detail/$id"
}

private data class BottomNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }
}

@Composable
fun AppRoot(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    // Silently restore an authorized Google session in the background without blocking guest browsing
    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { /* AuthStateListener updates state */ },
                onUnauthenticated = { /* Continue seamlessly in guest browsing mode */ },
                scope = scope
            )
        }
    }

    MedCareApp(
        currentUser = currentUser,
        credentialManager = credentialManager
    )
}

@Composable
fun MedCareApp(
    currentUser: FirebaseUser?,
    credentialManager: CredentialManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel: MedCareViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val roomDb = MedCareRoomDatabase.getInstance(app)
                val localRepo = LocalHealthcareRepository(
                    hospitalDao = roomDb.hospitalDao(),
                    doctorDao = roomDb.doctorDao(),
                    diagnosticTestDao = roomDb.diagnosticTestDao(),
                    localActivityDao = roomDb.localActivityDao(),
                    diagnosticPriceDao = roomDb.diagnosticPriceDao()
                )
                MedCareRepository(db).let { repo ->
                    MedCareViewModel(repo, localRepo, currentUser?.uid)
                }
            }
        }
    )

    LaunchedEffect(currentUser?.uid) {
        viewModel.onAuthStateChanged(
            uid = currentUser?.uid,
            displayName = currentUser?.displayName,
            email = currentUser?.email
        )
    }

    val activeUid by viewModel.currentUserIdFlow.collectAsStateWithLifecycle()
    val isSignedIn = activeUid != null

    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val profile = (profileState as? UiState.Success)?.data
    val guestLanguage by viewModel.guestLanguage.collectAsStateWithLifecycle()
    val guestDarkMode by viewModel.guestDarkMode.collectAsStateWithLifecycle()
    val signInPromptReason by viewModel.signInPromptReason.collectAsStateWithLifecycle()

    val effectiveLanguage = profile?.language ?: guestLanguage
    val effectiveDarkMode = profile?.darkMode ?: guestDarkMode
    val darkTheme = when (effectiveDarkMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    MedCareTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

        LaunchedEffect(snackbarMsg) {
            val msg = snackbarMsg ?: return@LaunchedEffect
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }

        val favoritesState by viewModel.favoritesState.collectAsStateWithLifecycle()
        val favorites = (favoritesState as? UiState.Success)?.data.orEmpty()
        val favoriteHospitalIds = remember(favorites) {
            favorites.filter { it.targetType == "hospital" }.map { it.targetId }.toSet()
        }
        val favoriteDoctorIds = remember(favorites) {
            favorites.filter { it.targetType == "doctor" }.map { it.targetId }.toSet()
        }

        val appointmentsState by viewModel.appointmentsState.collectAsStateWithLifecycle()
        val appointments = (appointmentsState as? UiState.Success)?.data.orEmpty()
        val testBookingsState by viewModel.testBookingsState.collectAsStateWithLifecycle()
        val medicalRecordsState by viewModel.medicalRecordsState.collectAsStateWithLifecycle()
        val records = (medicalRecordsState as? UiState.Success)?.data.orEmpty()
        val notificationsState by viewModel.notificationsState.collectAsStateWithLifecycle()
        val notifications = (notificationsState as? UiState.Success)?.data.orEmpty()
        val reviewsState by viewModel.targetReviewsState.collectAsStateWithLifecycle()

        val allHospitals by viewModel.allHospitals.collectAsStateWithLifecycle()
        val allDoctors by viewModel.allDoctors.collectAsStateWithLifecycle()
        val allDiagnosticTests by viewModel.allDiagnosticTests.collectAsStateWithLifecycle()
        val allDiagnosticPrices by viewModel.allDiagnosticPrices.collectAsStateWithLifecycle()
        val allPriceHistory by viewModel.allPriceHistory.collectAsStateWithLifecycle()
        val allPriceSyncMeta by viewModel.allPriceSyncMeta.collectAsStateWithLifecycle()
        val isSyncingPrices by viewModel.isSyncingPrices.collectAsStateWithLifecycle()
        val priceSyncStatus by viewModel.priceSyncStatus.collectAsStateWithLifecycle()
        val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
        val isFetchingArea by viewModel.isFetchingArea.collectAsStateWithLifecycle()
        val areaSyncStatus by viewModel.areaSyncStatus.collectAsStateWithLifecycle()
        val hospitalFilter by viewModel.hospitalFilter.collectAsStateWithLifecycle()
        val doctorFilter by viewModel.doctorFilter.collectAsStateWithLifecycle()
        val diagnosticFilter by viewModel.diagnosticFilter.collectAsStateWithLifecycle()
        val globalSearch by viewModel.globalSearchQuery.collectAsStateWithLifecycle()
        val compareHospitalIds by viewModel.compareHospitalIds.collectAsStateWithLifecycle()
        val recentHospitalIds by viewModel.recentHospitalIds.collectAsStateWithLifecycle()
        val recentDoctorIds by viewModel.recentDoctorIds.collectAsStateWithLifecycle()
        val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
        val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            DiagnosticSyncScheduler.schedulePeriodicSync(context)
        }

        LaunchedEffect(hospitalFilter.area, hospitalFilter.district, hospitalFilter.division) {
            viewModel.syncAreaHospitals(force = false)
        }

        val strings = stringsFor(effectiveLanguage)
        val tabs = remember(strings) {
            listOf(
                BottomNavTab(Routes.HOME, strings.navHome, Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
                BottomNavTab(Routes.HOSPITALS, strings.navHospitals, Icons.Filled.LocalHospital, Icons.Outlined.LocalHospital, "nav_hospitals"),
                BottomNavTab(Routes.DOCTORS, strings.navDoctors, Icons.Filled.MedicalServices, Icons.Outlined.MedicalServices, "nav_doctors"),
                BottomNavTab(Routes.DIAGNOSTIC, strings.navDiagnostic, Icons.Filled.Science, Icons.Outlined.Science, "nav_diagnostic"),
                BottomNavTab(Routes.PROFILE, strings.navProfile, Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
            )
        }

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val showTopLevelNav = currentRoute in tabs.map { it.route }

        fun navigateToTab(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 680.dp && showTopLevelNav

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (showTopLevelNav && !useRail) {
                        NavigationBar {
                            tabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                            tabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = { Text(tab.label, maxLines = 1) },
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = Routes.HOME,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(Routes.HOME) {
                            HomeScreen(
                                allHospitals = allHospitals,
                                allDoctors = allDoctors,
                                allDiagnosticTests = allDiagnosticTests,
                                profile = profile,
                                isSignedIn = isSignedIn,
                                language = effectiveLanguage,
                                strings = strings,
                                globalSearch = globalSearch,
                                isSearchingLive = isFetchingArea,
                                onSearchChange = viewModel::updateGlobalSearch,
                                onSearchLive = { q -> viewModel.searchHospitalsLive(q) },
                                onOpenHospitalsWithQuery = { q ->
                                    viewModel.updateHospitalFilter {
                                        it.copy(
                                            query = q,
                                            division = "All",
                                            district = "All",
                                            area = "All"
                                        )
                                    }
                                    viewModel.searchHospitalsLive(q, immediate = true)
                                    navigateToTab(Routes.HOSPITALS)
                                },
                                distanceKmProvider = viewModel::distanceToHospitalKm,
                                recentHospitalIds = recentHospitalIds,
                                recentDoctorIds = recentDoctorIds,
                                notifications = notifications,
                                onMarkNotificationRead = viewModel::markNotificationRead,
                                onToggleLanguage = viewModel::toggleLanguage,
                                onRequestSignIn = { viewModel.requestSignIn() },
                                onOpenEmergency = { navController.navigate(Routes.EMERGENCY) },
                                onOpenAppointments = { navController.navigate(Routes.APPOINTMENTS) },
                                onOpenCompare = { navController.navigate(Routes.COMPARE) },
                                onOpenRecords = { navController.navigate(Routes.MEDICAL_RECORDS) },
                                onOpenDiagnosticTab = { navigateToTab(Routes.DIAGNOSTIC) },
                                onOpenHospitalsTab = { area ->
                                    if (area != null) {
                                        viewModel.updateHospitalFilter { it.copy(area = area) }
                                    }
                                    navigateToTab(Routes.HOSPITALS)
                                },
                                onOpenDoctorsWithSpecialization = { spec ->
                                    viewModel.updateDoctorFilter { it.copy(specialization = spec) }
                                    navigateToTab(Routes.DOCTORS)
                                },
                                onOpenHospitalDetail = { h ->
                                    viewModel.markHospitalViewed(h.id)
                                    navController.navigate(Routes.hospitalDetail(h.id))
                                },
                                onOpenDoctorDetail = { d ->
                                    viewModel.markDoctorViewed(d.id)
                                    navController.navigate(Routes.doctorDetail(d.id))
                                },
                                onOpenTestDetail = { t ->
                                    navController.navigate(Routes.testDetail(t.id))
                                },
                                diagnosticPrices = allDiagnosticPrices
                            )
                        }

                        composable(Routes.HOSPITALS) {
                            HospitalsScreen(
                                hospitals = allHospitals,
                                isFetchingArea = isFetchingArea,
                                areaSyncStatus = areaSyncStatus,
                                filter = hospitalFilter,
                                onFilterChange = viewModel::updateHospitalFilter,
                                onSyncArea = { force, q -> viewModel.syncAreaHospitals(force = force, customQuery = q) },
                                onSearchLive = { q -> viewModel.searchHospitalsLive(q) },
                                recentSearches = recentSearches,
                                onDeleteRecentSearch = viewModel::removeSearchHistoryItem,
                                favoriteHospitalIds = favoriteHospitalIds,
                                compareHospitalIds = compareHospitalIds,
                                userCoordinates = deviceLocation ?: viewModel.effectiveUserCoordinates(),
                                hasLocationPermission = hasLocationPermission,
                                distanceKmProvider = viewModel::distanceToHospitalKm,
                                onLocationPermissionResult = { granted ->
                                    viewModel.refreshLocation(context, granted)
                                    if (!granted) {
                                        viewModel.showMessage("Using selected Dhaka/BD area center for distance.")
                                    }
                                },
                                onToggleFavorite = { h, isFav ->
                                    viewModel.toggleFavorite(h.id, "hospital", h.name, "${h.area}, ${h.district}", isFav)
                                },
                                onToggleCompare = { h ->
                                    viewModel.toggleCompareHospital(h.id)?.let { viewModel.showMessage(it) }
                                },
                                onOpenCompare = { navController.navigate(Routes.COMPARE) },
                                onOpenHospitalDetail = { h ->
                                    viewModel.markHospitalViewed(h.id)
                                    navController.navigate(Routes.hospitalDetail(h.id))
                                }
                            )
                        }

                        composable(Routes.DOCTORS) {
                            DoctorsScreen(
                                filter = doctorFilter,
                                onFilterChange = viewModel::updateDoctorFilter,
                                favoriteDoctorIds = favoriteDoctorIds,
                                onToggleFavorite = { d, isFav ->
                                    viewModel.toggleFavorite(d.id, "doctor", d.name, d.specialization, isFav)
                                },
                                onOpenDoctorDetail = { d ->
                                    viewModel.markDoctorViewed(d.id)
                                    navController.navigate(Routes.doctorDetail(d.id))
                                },
                                allDoctors = allDoctors
                            )
                        }

                        composable(Routes.DIAGNOSTIC) {
                            DiagnosticScreen(
                                filter = diagnosticFilter,
                                onFilterChange = viewModel::updateDiagnosticFilter,
                                onOpenTestDetail = { t ->
                                    navController.navigate(Routes.testDetail(t.id))
                                },
                                allTests = allDiagnosticTests,
                                diagnosticPrices = allDiagnosticPrices,
                                syncMetaList = allPriceSyncMeta,
                                isSyncingPrices = isSyncingPrices,
                                priceSyncStatus = priceSyncStatus,
                                onSyncAllPrices = { viewModel.syncAllDiagnosticPrices() }
                            )
                        }

                        composable(Routes.PROFILE) {
                            ProfileScreen(
                                isSignedIn = isSignedIn,
                                profile = profile,
                                guestLanguage = guestLanguage,
                                guestDarkMode = guestDarkMode,
                                favorites = favorites,
                                appointmentCount = appointments.size,
                                recordCount = records.size,
                                onRequestSignIn = { viewModel.requestSignIn() },
                                onSaveProfile = viewModel::saveUserProfile,
                                onSetGuestLanguage = viewModel::setGuestOrUserLanguage,
                                onSetGuestDarkMode = viewModel::setGuestOrUserDarkMode,
                                onOpenAppointments = { navController.navigate(Routes.APPOINTMENTS) },
                                onOpenRecords = { navController.navigate(Routes.MEDICAL_RECORDS) },
                                onOpenEmergency = { navController.navigate(Routes.EMERGENCY) },
                                onOpenCompare = { navController.navigate(Routes.COMPARE) },
                                onOpenFavoriteTarget = { fav ->
                                    when (fav.targetType) {
                                        "hospital" -> navController.navigate(Routes.hospitalDetail(fav.targetId))
                                        "doctor" -> navController.navigate(Routes.doctorDetail(fav.targetId))
                                    }
                                },
                                onSignOut = {
                                    signOutUser(credentialManager, scope)
                                },
                                onDeleteProfileData = {
                                    viewModel.deleteUserProfileData {
                                        signOutUser(credentialManager, scope)
                                    }
                                }
                            )
                        }

                        composable(
                            route = Routes.HOSPITAL_DETAIL,
                            arguments = listOf(navArgument("hospitalId") { type = NavType.StringType })
                        ) { entry ->
                            val id = entry.arguments?.getString("hospitalId") ?: ""
                            val hospital = allHospitals.firstOrNull { it.id == id } ?: viewModel.getHospitalById(id)
                            if (hospital != null) {
                                HospitalDetailScreen(
                                    hospital = hospital,
                                    isSignedIn = isSignedIn,
                                    distanceKm = viewModel.distanceToHospitalKm(hospital),
                                    isFavorite = hospital.id in favoriteHospitalIds,
                                    isCompared = hospital.id in compareHospitalIds,
                                    reviewsState = reviewsState,
                                    onObserveReviews = viewModel::setReviewTarget,
                                    onRequestSignIn = { viewModel.requestSignIn("Sign in with Google to read and write verified reviews for ${hospital.name}.") },
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = {
                                        viewModel.toggleFavorite(
                                            hospital.id, "hospital", hospital.name, "${hospital.area}, ${hospital.district}",
                                            hospital.id in favoriteHospitalIds
                                        )
                                    },
                                    onToggleCompare = {
                                        viewModel.toggleCompareHospital(hospital.id)?.let { viewModel.showMessage(it) }
                                    },
                                    onOpenDoctor = { d ->
                                        viewModel.markDoctorViewed(d.id)
                                        navController.navigate(Routes.doctorDetail(d.id))
                                    },
                                    onOpenTest = { t ->
                                        navController.navigate(Routes.testDetail(t.id))
                                    },
                                    onSubmitReview = { o, d, s, w, c, comment ->
                                        viewModel.submitReview(hospital.id, "hospital", o, d, s, w, c, comment)
                                    },
                                    onReportReview = viewModel::reportReview,
                                    allDoctors = allDoctors,
                                    allTests = allDiagnosticTests,
                                    diagnosticPrices = allDiagnosticPrices.filter { it.hospitalId == hospital.id },
                                    priceHistory = allPriceHistory,
                                    syncMeta = allPriceSyncMeta.firstOrNull { it.hospitalId == hospital.id },
                                    isSyncingPrices = isSyncingPrices,
                                    onSyncHospitalPrices = { viewModel.syncHospitalDiagnosticPrices(hospital) }
                                )
                            }
                        }

                        composable(
                            route = Routes.DOCTOR_DETAIL,
                            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
                        ) { entry ->
                            val id = entry.arguments?.getString("doctorId") ?: ""
                            val doctor = allDoctors.firstOrNull { it.id == id }
                            if (doctor != null) {
                                DoctorDetailScreen(
                                    doctor = doctor,
                                    isSignedIn = isSignedIn,
                                    profile = profile,
                                    activeAppointments = appointments,
                                    isFavorite = doctor.id in favoriteDoctorIds,
                                    reviewsState = reviewsState,
                                    onObserveReviews = viewModel::setReviewTarget,
                                    onRequestSignIn = { viewModel.requestSignIn("Sign in with Google to view reviews or book an appointment with ${doctor.name}.") },
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = {
                                        viewModel.toggleFavorite(
                                            doctor.id, "doctor", doctor.name, doctor.specialization,
                                            doctor.id in favoriteDoctorIds
                                        )
                                    },
                                    onBookSlot = { hospId, hospName, chName, date, slot, pName, pPhone, fee, notes, onDone ->
                                        viewModel.bookAppointment(
                                            doctor, hospId, hospName, chName, date, slot, pName, pPhone, fee, notes, onDone
                                        )
                                    },
                                    onSubmitReview = { o, d, s, w, c, comment ->
                                        viewModel.submitReview(doctor.id, "doctor", o, d, s, w, c, comment)
                                    },
                                    onReportReview = viewModel::reportReview
                                )
                            }
                        }

                        composable(
                            route = Routes.TEST_DETAIL,
                            arguments = listOf(navArgument("testId") { type = NavType.StringType })
                        ) { entry ->
                            val id = entry.arguments?.getString("testId") ?: ""
                            val test = allDiagnosticTests.firstOrNull { it.id == id }
                                ?: allDiagnosticPrices.firstOrNull { it.canonicalTestId == id }?.toFallbackDiagnosticTest()
                            if (test != null) {
                                val associatedCenterIds = test.centerPrices.map { it.centerId }.toSet() +
                                    allDiagnosticPrices.filter { it.canonicalTestId == test.id }.map { it.hospitalId }.toSet()
                                TestDetailScreen(
                                    test = test,
                                    profile = profile,
                                    onBack = { navController.popBackStack() },
                                    onBookTest = { cp, date, win, home, addr, pName, pPhone, total, onDone ->
                                        viewModel.bookDiagnosticTest(
                                            test, cp.centerId, cp.centerName, date, win, home, addr, pName, pPhone, total, onDone
                                        )
                                    },
                                    allHospitals = allHospitals,
                                    diagnosticPrices = allDiagnosticPrices,
                                    priceHistory = allPriceHistory,
                                    syncMetaList = allPriceSyncMeta,
                                    isSyncingPrices = isSyncingPrices,
                                    onSyncPrices = {
                                        viewModel.syncAllDiagnosticPrices(preferredHospitalIds = associatedCenterIds)
                                    },
                                    onOpenHospitalDetail = { h ->
                                        viewModel.markHospitalViewed(h.id)
                                        navController.navigate(Routes.hospitalDetail(h.id))
                                    }
                                )
                            }
                        }

                        composable(Routes.COMPARE) {
                            CompareScreen(
                                allHospitals = allHospitals,
                                selectedHospitalIds = compareHospitalIds,
                                distanceKmProvider = viewModel::distanceToHospitalKm,
                                onRemoveHospital = { id -> viewModel.toggleCompareHospital(id) },
                                onClearAll = viewModel::clearCompare,
                                onBack = { navController.popBackStack() },
                                onOpenHospitalDetail = { h ->
                                    navController.navigate(Routes.hospitalDetail(h.id))
                                },
                                allDoctors = allDoctors,
                                allTests = allDiagnosticTests,
                                diagnosticPrices = allDiagnosticPrices
                            )
                        }

                        composable(Routes.EMERGENCY) {
                            EmergencyScreen(
                                allHospitals = allHospitals,
                                distanceKmProvider = viewModel::distanceToHospitalKm,
                                onBack = { navController.popBackStack() },
                                onOpenHospitalDetail = { h ->
                                    navController.navigate(Routes.hospitalDetail(h.id))
                                }
                            )
                        }

                        composable(Routes.APPOINTMENTS) {
                            AppointmentsScreen(
                                isSignedIn = isSignedIn,
                                appointmentsState = appointmentsState,
                                testBookingsState = testBookingsState,
                                use24HourTime = profile?.use24HourTime ?: false,
                                onRequestSignIn = { viewModel.requestSignIn("Sign in with Google to view and manage your bookings.") },
                                onRescheduleAppointment = viewModel::rescheduleAppointment,
                                onCancelAppointment = viewModel::cancelAppointment,
                                onCancelTestBooking = viewModel::cancelTestBooking,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.MEDICAL_RECORDS) {
                            MedicalRecordsScreen(
                                isSignedIn = isSignedIn,
                                recordsState = medicalRecordsState,
                                onRequestSignIn = { viewModel.requestSignIn("Sign in with Google to store and view your private health records.") },
                                onAddRecord = viewModel::addMedicalRecord,
                                onDeleteRecord = viewModel::deleteMedicalRecord,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }

            if (signInPromptReason != null) {
                GoogleSignInPromptDialog(
                    reason = signInPromptReason ?: "",
                    onDismiss = viewModel::dismissSignInPrompt,
                    onAuthSuccess = viewModel::dismissSignInPrompt
                )
            }
        }
    }
}
