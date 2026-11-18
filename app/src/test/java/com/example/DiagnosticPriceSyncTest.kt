package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.catalog.MedicalTestMasterCatalog
import com.example.data.local.LocalHealthcareRepository
import com.example.data.local.MedCareRoomDatabase
import com.example.data.model.FacilityType
import com.example.data.model.Hospital
import com.example.data.repository.DiagnosticPriceSyncService
import com.example.data.repository.ExtractedDiagnosticPrice
import com.example.data.repository.HospitalPriceFetchResult
import com.example.data.repository.OfficialHospitalPortalRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DiagnosticPriceSyncTest {

    private lateinit var db: MedCareRoomDatabase
    private lateinit var repo: LocalHealthcareRepository

    private val testHospital = Hospital(
        id = "hosp_test_popular",
        name = "Popular Diagnostic Centre Test",
        nameBn = "পপুলার ডায়াগনস্টিক",
        type = FacilityType.DIAGNOSTIC_CENTER,
        description = "Diagnostic facility",
        address = "Dhanmondi, Dhaka",
        area = "Dhanmondi",
        district = "Dhaka",
        division = "Dhaka",
        latitude = 23.7465,
        longitude = 90.3760,
        phone = "09613787801",
        emergencyPhone = "09613787801",
        website = "https://www.populardiagnostic.com",
        email = "",
        operatingHours = "24 Hours",
        hasEmergency247 = true,
        hasIcu = false,
        hasCcu = false,
        hasNicu = false,
        hasAmbulance = true,
        hasBloodBank = false,
        verified = true,
        rating = 4.6,
        reviewCount = 100,
        departments = listOf("Pathology"),
        amenities = listOf("Home Sample Collection")
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MedCareRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = LocalHealthcareRepository(
            hospitalDao = db.hospitalDao(),
            doctorDao = db.doctorDao(),
            diagnosticTestDao = db.diagnosticTestDao(),
            localActivityDao = db.localActivityDao(),
            diagnosticPriceDao = db.diagnosticPriceDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `extractDiagnosticPricesFromContent parses html table and json without fabricating`() {
        val html = """
            <html><body>
            <table>
              <tr><th>Test Name</th><th>Price</th></tr>
              <tr><td>Complete Blood Count (CBC)</td><td>৳ 450</td></tr>
              <tr><td>Lipid Profile</td><td>1,200 BDT</td></tr>
              <tr><td>Hotline 10616</td><td>৳ 999999</td></tr>
            </table>
            </body></html>
        """.trimIndent()

        val extracted = DiagnosticPriceSyncService.extractDiagnosticPricesFromContent(
            rawContent = html,
            contentType = "text/html",
            sourceUrl = "https://www.populardiagnostic.com/test-charges"
        )
        assertEquals(2, extracted.size)
        assertEquals("test_cbc", extracted[0].canonicalTestId)
        assertEquals(450, extracted[0].priceBdt)
        assertEquals("test_lipid", extracted[1].canonicalTestId)
        assertEquals(1200, extracted[1].priceBdt)

        val emptyPage = DiagnosticPriceSyncService.extractDiagnosticPricesFromContent(
            rawContent = "<html><body><h1>Welcome to Hospital</h1><p>Call 01711000000</p></body></html>",
            contentType = "text/html",
            sourceUrl = "https://example-hospital.com"
        )
        assertTrue(emptyPage.isEmpty())
    }

    @Test
    fun `placeholder domains are rejected by OfficialHospitalPortalRegistry`() {
        assertTrue(OfficialHospitalPortalRegistry.isPlaceholderUrl("https://squarehospital-sample.bd"))
        assertTrue(OfficialHospitalPortalRegistry.isPlaceholderUrl("https://www.medcare-bd.org/test"))
        assertFalse(OfficialHospitalPortalRegistry.isPlaceholderUrl("https://www.populardiagnostic.com"))
    }

    @Test
    fun `sync detects price changes maintains previous price and handles unavailable gracefully`() = runBlocking {
        // 1. Initial verified online sync
        repo.syncDiagnosticPricesForHospital(testHospital) {
            HospitalPriceFetchResult.Verified(
                sourceName = "Official Portal",
                sourceUrl = "https://www.populardiagnostic.com/test-charges",
                prices = listOf(
                    ExtractedDiagnosticPrice(
                        canonicalTestId = "test_cbc",
                        testName = "Complete Blood Count (CBC)",
                        category = "Blood Test",
                        priceBdt = 450,
                        sourceUrl = "https://www.populardiagnostic.com/test-charges"
                    )
                )
            )
        }

        val afterInitial = repo.observePricesForHospital(testHospital.id).first()
        assertEquals(1, afterInitial.size)
        assertTrue(afterInitial[0].priceAvailable)
        assertEquals(450, afterInitial[0].currentPriceBdt)
        assertNull(afterInitial[0].previousPriceBdt)

        val historyAfterInitial = repo.observeHistoryForHospital(testHospital.id).first()
        assertEquals(1, historyAfterInitial.size)
        assertEquals("INITIAL_FETCH", historyAfterInitial[0].changeReason)

        // 2. Price change detected automatically (450 -> 500)
        repo.syncDiagnosticPricesForHospital(testHospital) {
            HospitalPriceFetchResult.Verified(
                sourceName = "Official Portal",
                sourceUrl = "https://www.populardiagnostic.com/test-charges",
                prices = listOf(
                    ExtractedDiagnosticPrice(
                        canonicalTestId = "test_cbc",
                        testName = "Complete Blood Count (CBC)",
                        category = "Blood Test",
                        priceBdt = 500,
                        sourceUrl = "https://www.populardiagnostic.com/test-charges"
                    )
                )
            )
        }

        val afterUpdate = repo.observePricesForHospital(testHospital.id).first()
        assertEquals(500, afterUpdate[0].currentPriceBdt)
        assertEquals(450, afterUpdate[0].previousPriceBdt)
        assertTrue(afterUpdate[0].priceAvailable)

        val historyAfterUpdate = repo.observeHistoryForHospital(testHospital.id).first()
        assertEquals(2, historyAfterUpdate.size)
        assertEquals("PRICE_INCREASE", historyAfterUpdate[0].changeReason)
        assertEquals(450, historyAfterUpdate[0].previousPriceBdt)
        assertEquals(500, historyAfterUpdate[0].currentPriceBdt)

        // 3. Official price list becomes unavailable -> Price Not Available (no fabricated prices)
        repo.syncDiagnosticPricesForHospital(testHospital) {
            HospitalPriceFetchResult.Unavailable(
                syncState = "NO_PUBLIC_PRICE_LIST",
                sourceUrl = "https://www.populardiagnostic.com",
                reason = "No public price list published"
            )
        }

        val afterUnavailable = repo.observePricesForHospital(testHospital.id).first()
        assertFalse(afterUnavailable[0].priceAvailable)
        assertNull(afterUnavailable[0].currentPriceBdt)
        assertEquals(500, afterUnavailable[0].previousPriceBdt)
        assertEquals("UNAVAILABLE", afterUnavailable[0].verificationStatus)
    }

    @Test
    fun `master catalog has complete clinical metadata and no fabricated prices`() {
        val tests = MedicalTestMasterCatalog.allTests
        assertTrue(tests.size >= 40)
        tests.forEach { test ->
            assertTrue("Missing shortName for ${test.id}", test.shortName.isNotBlank())
            assertTrue("Missing subcategory for ${test.id}", test.subcategory.isNotBlank())
            assertTrue("Missing whatItDetects for ${test.id}", test.whatItDetects.isNotBlank())
            assertTrue("Missing normalReferenceRange for ${test.id}", test.normalReferenceRange.isNotBlank())
            assertTrue("Missing procedure for ${test.id}", test.procedure.isNotBlank())
            assertTrue("Missing sampleType for ${test.id}", test.sampleType.isNotBlank())
            assertTrue("Missing preparation for ${test.id}", test.preparationInstructions.isNotBlank())
            assertTrue("Missing estimatedDelivery for ${test.id}", test.estimatedDelivery.isNotBlank())
            assertTrue("Missing aliases for ${test.id}", test.relatedAliases.isNotEmpty())
            assertTrue("Fabricated price found in ${test.id}", test.centerPrices.all { it.priceBdt == 0 })
        }
    }

    @Test
    fun `extractDiagnosticPricesFromContent tags verified vs unverified sources accurately`() {
        val html = """
            <html><body>
            <table>
              <tr><td>Complete Blood Count (CBC)</td><td>৳ 450</td></tr>
            </table>
            </body></html>
        """.trimIndent()

        val verified = DiagnosticPriceSyncService.extractDiagnosticPricesFromContent(
            rawContent = html,
            contentType = "text/html",
            sourceUrl = "https://www.populardiagnostic.com/test-charges",
            isOfficialPortal = true
        )
        assertEquals(1, verified.size)
        assertEquals("VERIFIED", verified[0].verificationStatus)

        val unverified = DiagnosticPriceSyncService.extractDiagnosticPricesFromContent(
            rawContent = html,
            contentType = "text/html",
            sourceUrl = "https://some-clinic-website.com/prices",
            isOfficialPortal = false
        )
        assertEquals(1, unverified.size)
        assertEquals("UNVERIFIED", unverified[0].verificationStatus)
    }

    @Test
    fun `hospital wise availability does not assume all hospitals offer advanced tests`() {
        val petCt = MedicalTestMasterCatalog.allTests.first { it.id == "test_pet_ct" }
        val eyeClinic = testHospital.copy(
            id = "hosp_eye_clinic_test",
            name = "Dhaka Eye Care Clinic",
            type = FacilityType.SPECIALIZED,
            hasIcu = false,
            departments = listOf("Ophthalmology", "Optometry"),
            amenities = listOf("Optical Shop")
        )
        assertFalse(
            MedicalTestMasterCatalog.isTestOfferedAtHospital(
                test = petCt,
                hospital = eyeClinic,
                hasLiveSyncedRecord = false
            )
        )
        assertTrue(
            MedicalTestMasterCatalog.isTestOfferedAtHospital(
                test = petCt,
                hospital = eyeClinic,
                hasLiveSyncedRecord = true
            )
        )
    }

    @Test
    fun `unverified collected price persists verificationStatus as UNVERIFIED`() = runBlocking {
        repo.syncDiagnosticPricesForHospital(testHospital) {
            HospitalPriceFetchResult.Verified(
                sourceName = "Generic Hospital Website",
                sourceUrl = "https://some-clinic.com/prices",
                prices = listOf(
                    ExtractedDiagnosticPrice(
                        canonicalTestId = "test_cbc",
                        testName = "Complete Blood Count",
                        category = "Hematology",
                        priceBdt = 420,
                        sourceUrl = "https://some-clinic.com/prices",
                        verificationStatus = "UNVERIFIED"
                    )
                )
            )
        }
        val rows = repo.observePricesForHospital(testHospital.id).first()
        assertEquals(1, rows.size)
        assertEquals("UNVERIFIED", rows[0].verificationStatus)
        assertEquals(420, rows[0].currentPriceBdt)
    }

    @Test
    fun `healthcare catalogs have unique ids and valid hospital chamber references`() {
        val hospitals = com.example.data.catalog.BangladeshHealthcareCatalog.hospitals
        val doctors = com.example.data.catalog.BangladeshHealthcareCatalog.doctors
        val tests = MedicalTestMasterCatalog.allTests
        val emergencyServices = com.example.data.catalog.BangladeshHealthcareCatalog.emergencyServices

        val hospitalIds = hospitals.map { it.id }
        assertEquals("Duplicate hospital IDs found", hospitalIds.size, hospitalIds.toSet().size)

        val doctorIds = doctors.map { it.id }
        assertEquals("Duplicate doctor IDs found", doctorIds.size, doctorIds.toSet().size)

        val testIds = tests.map { it.id }
        assertEquals("Duplicate test IDs found", testIds.size, testIds.toSet().size)

        val emergencyIds = emergencyServices.map { it.id }
        assertEquals("Duplicate emergency IDs found", emergencyIds.size, emergencyIds.toSet().size)

        val hospitalIdSet = hospitalIds.toSet()
        val missingHospitalRefs = mutableListOf<String>()
        val chamberIds = mutableListOf<String>()

        doctors.forEach { doctor ->
            assertTrue("Doctor ${doctor.id} has no chambers", doctor.chambers.isNotEmpty())
            doctor.chambers.forEach { chamber ->
                chamberIds.add(chamber.chamberId)
                if (chamber.hospitalId !in hospitalIdSet) {
                    missingHospitalRefs.add("${doctor.id} -> ${chamber.hospitalId} (${chamber.hospitalName})")
                }
                assertTrue(
                    "Chamber ${chamber.chamberId} of ${doctor.id} has empty weeklySchedule",
                    chamber.weeklySchedule.isNotEmpty()
                )
            }
        }
        assertEquals("Duplicate chamber IDs found", chamberIds.size, chamberIds.toSet().size)
        assertTrue(
            "Doctors reference missing hospitalIds: $missingHospitalRefs",
            missingHospitalRefs.isEmpty()
        )
    }

    @Test
    fun `nested html blocks and lakh comma prices parse accurately`() {
        val html = """
            <html><body>
              <div class="wrapper">
                <p>Complete Blood Count (CBC) - ৳ 550</p>
              </div>
              <div class="card"><span>MRI Brain Scan</span> : 12,500 BDT</div>
              <div class="lakh"><span>Whole Body PET-CT Scan</span> - ৳ 1,25,000</div>
            </body></html>
        """.trimIndent()

        val extracted = DiagnosticPriceSyncService.extractDiagnosticPricesFromContent(
            rawContent = html,
            contentType = "text/html",
            sourceUrl = "https://www.populardiagnostic.com/test-charges",
            isOfficialPortal = true
        )
        val byId = extracted.associateBy { it.canonicalTestId }
        assertEquals(550, byId["test_cbc"]?.priceBdt)
        assertEquals(12500, byId["test_mri"]?.priceBdt)
        assertEquals(125000, byId["test_pet_ct"]?.priceBdt)
        assertEquals("UNVERIFIED", byId["test_cbc"]?.verificationStatus)
    }

    @Test
    fun `private tertiary hospital with radiology offers MRI and CT while small clinic does not`() {
        val mriTest = MedicalTestMasterCatalog.allTests.first { it.id == "test_mri" }
        val tertiaryPrivate = testHospital.copy(
            id = "hosp_private_tertiary_test",
            name = "Private Tertiary Hospital",
            type = FacilityType.PRIVATE,
            hasIcu = true,
            departments = listOf("Medicine", "Cardiology", "Neurology", "Radiology & Imaging", "Surgery", "Pathology"),
            amenities = listOf("3T MRI", "CT Scan")
        )
        val smallClinic = testHospital.copy(
            id = "hosp_small_clinic_test",
            name = "Neighborhood Care Clinic",
            type = FacilityType.PRIVATE,
            hasIcu = false,
            departments = listOf("General Practice"),
            amenities = listOf("Pharmacy")
        )
        assertTrue(MedicalTestMasterCatalog.isTestOfferedAtHospital(mriTest, tertiaryPrivate, hasLiveSyncedRecord = false))
        assertFalse(MedicalTestMasterCatalog.isTestOfferedAtHospital(mriTest, smallClinic, hasLiveSyncedRecord = false))
    }

    @Test
    fun `location helper generates valid ISO booking dates`() {
        val isoRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")
        assertTrue(isoRegex.matches(com.example.data.repository.LocationHelper.todayIsoDate()))
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
            assertTrue(isoRegex.matches(com.example.data.repository.LocationHelper.nextIsoDateForWeekday(day)))
        }
    }
}

