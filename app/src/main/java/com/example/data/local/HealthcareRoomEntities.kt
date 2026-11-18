package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.CenterTestPrice
import com.example.data.model.DiagnosticTest
import com.example.data.model.Doctor
import com.example.data.model.DoctorChamber
import com.example.data.model.FacilityType
import com.example.data.model.Hospital

@Entity(
    tableName = "hospitals",
    indices = [
        Index(value = ["name"]),
        Index(value = ["area", "district"])
    ]
)
data class HospitalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nameBn: String,
    val type: FacilityType,
    val description: String,
    val address: String,
    val area: String,
    val district: String,
    val division: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val emergencyPhone: String,
    val website: String,
    val email: String,
    val operatingHours: String,
    val hasEmergency247: Boolean,
    val hasIcu: Boolean,
    val hasCcu: Boolean,
    val hasNicu: Boolean,
    val hasAmbulance: Boolean,
    val hasBloodBank: Boolean,
    val verified: Boolean,
    val rating: Double,
    val reviewCount: Int,
    val departments: List<String>,
    val amenities: List<String>,
    val isPopular: Boolean,
    val dataSource: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toModel(): Hospital = Hospital(
        id = id,
        name = name,
        nameBn = nameBn,
        type = type,
        description = description,
        address = address,
        area = area,
        district = district,
        division = division,
        latitude = latitude,
        longitude = longitude,
        phone = phone,
        emergencyPhone = emergencyPhone,
        website = website,
        email = email,
        operatingHours = operatingHours,
        hasEmergency247 = hasEmergency247,
        hasIcu = hasIcu,
        hasCcu = hasCcu,
        hasNicu = hasNicu,
        hasAmbulance = hasAmbulance,
        hasBloodBank = hasBloodBank,
        verified = verified,
        rating = rating,
        reviewCount = reviewCount,
        departments = departments,
        amenities = amenities,
        isPopular = isPopular,
        dataSource = dataSource
    )

    companion object {
        fun fromModel(h: Hospital): HospitalEntity = HospitalEntity(
            id = h.id,
            name = h.name,
            nameBn = h.nameBn,
            type = h.type,
            description = h.description,
            address = h.address,
            area = h.area,
            district = h.district,
            division = h.division,
            latitude = h.latitude,
            longitude = h.longitude,
            phone = h.phone,
            emergencyPhone = h.emergencyPhone,
            website = h.website,
            email = h.email,
            operatingHours = h.operatingHours,
            hasEmergency247 = h.hasEmergency247,
            hasIcu = h.hasIcu,
            hasCcu = h.hasCcu,
            hasNicu = h.hasNicu,
            hasAmbulance = h.hasAmbulance,
            hasBloodBank = h.hasBloodBank,
            verified = h.verified,
            rating = h.rating,
            reviewCount = h.reviewCount,
            departments = h.departments,
            amenities = h.amenities,
            isPopular = h.isPopular,
            dataSource = h.dataSource
        )
    }
}

@Entity(
    tableName = "doctors",
    indices = [Index(value = ["specialization"])]
)
data class DoctorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nameBn: String,
    val qualification: String,
    val designation: String,
    val specialization: String,
    val experienceYears: Int,
    val bmdcRegNo: String,
    val verified: Boolean,
    val languages: List<String>,
    val rating: Double,
    val reviewCount: Int,
    val bio: String,
    val chambers: List<DoctorChamber>,
    val isTopRated: Boolean
) {
    fun toModel(): Doctor = Doctor(
        id = id,
        name = name,
        nameBn = nameBn,
        qualification = qualification,
        designation = designation,
        specialization = specialization,
        experienceYears = experienceYears,
        bmdcRegNo = bmdcRegNo,
        verified = verified,
        languages = languages,
        rating = rating,
        reviewCount = reviewCount,
        bio = bio,
        chambers = chambers,
        isTopRated = isTopRated
    )

    companion object {
        fun fromModel(d: Doctor): DoctorEntity = DoctorEntity(
            id = d.id,
            name = d.name,
            nameBn = d.nameBn,
            qualification = d.qualification,
            designation = d.designation,
            specialization = d.specialization,
            experienceYears = d.experienceYears,
            bmdcRegNo = d.bmdcRegNo,
            verified = d.verified,
            languages = d.languages,
            rating = d.rating,
            reviewCount = d.reviewCount,
            bio = d.bio,
            chambers = d.chambers,
            isTopRated = d.isTopRated
        )
    }
}

@Entity(tableName = "diagnostic_tests")
data class DiagnosticTestEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortName: String = "",
    val nameBn: String,
    val category: String,
    val subcategory: String = "",
    val description: String,
    val whatItDetects: String = "",
    val preparationInstructions: String,
    val sampleType: String,
    val normalReferenceRange: String = "",
    val procedure: String = "",
    val estimatedDelivery: String,
    val relatedAliases: List<String> = emptyList(),
    val requiredCapabilities: List<String> = emptyList(),
    val centerPrices: List<CenterTestPrice>
) {
    fun toModel(): DiagnosticTest = DiagnosticTest(
        id = id,
        name = name,
        shortName = shortName,
        nameBn = nameBn,
        category = category,
        subcategory = subcategory,
        description = description,
        whatItDetects = whatItDetects,
        preparationInstructions = preparationInstructions,
        sampleType = sampleType,
        normalReferenceRange = normalReferenceRange,
        procedure = procedure,
        estimatedDelivery = estimatedDelivery,
        relatedAliases = relatedAliases,
        requiredCapabilities = requiredCapabilities,
        centerPrices = centerPrices
    )

    companion object {
        fun fromModel(t: DiagnosticTest): DiagnosticTestEntity = DiagnosticTestEntity(
            id = t.id,
            name = t.name,
            shortName = t.shortName,
            nameBn = t.nameBn,
            category = t.category,
            subcategory = t.subcategory,
            description = t.description,
            whatItDetects = t.whatItDetects,
            preparationInstructions = t.preparationInstructions,
            sampleType = t.sampleType,
            normalReferenceRange = t.normalReferenceRange,
            procedure = t.procedure,
            estimatedDelivery = t.estimatedDelivery,
            relatedAliases = t.relatedAliases,
            requiredCapabilities = t.requiredCapabilities,
            centerPrices = t.centerPrices
        )
    }
}

@Entity(tableName = "recent_views")
data class RecentViewEntity(
    @PrimaryKey val compositeKey: String, // e.g., "hospital:hosp_dmch"
    val targetId: String,
    val targetType: String, // "hospital" | "doctor"
    val viewedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "hospital_diagnostic_prices",
    indices = [
        Index(value = ["hospitalId"]),
        Index(value = ["canonicalTestId"])
    ]
)
data class HospitalDiagnosticPriceEntity(
    @PrimaryKey val id: String, // "${hospitalId}__${canonicalTestId}"
    val hospitalId: String,
    val hospitalName: String,
    val canonicalTestId: String,
    val testName: String,
    val category: String,
    val currentPriceBdt: Int?,
    val previousPriceBdt: Int?,
    val priceAvailable: Boolean,
    val homeCollectionAvailable: Boolean,
    val homeCollectionFeeBdt: Int,
    val reportDeliveryHours: Int,
    val sourceName: String,
    val sourceUrl: String,
    val verificationStatus: String = "VERIFIED", // "VERIFIED" | "UNVERIFIED" | "UNAVAILABLE"
    val lastCheckedMillis: Long,
    val lastUpdatedMillis: Long,
    val statusNote: String = ""
)

@Entity(
    tableName = "diagnostic_price_history",
    indices = [
        Index(value = ["hospitalId"]),
        Index(value = ["canonicalTestId"]),
        Index(value = ["changedAtMillis"])
    ]
)
data class DiagnosticPriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Long = 0,
    val priceRecordId: String,
    val hospitalId: String,
    val hospitalName: String,
    val canonicalTestId: String,
    val testName: String,
    val previousPriceBdt: Int?,
    val currentPriceBdt: Int?,
    val changedAtMillis: Long,
    val sourceUrl: String,
    val changeReason: String // "INITIAL_FETCH" | "PRICE_INCREASE" | "PRICE_DECREASE" | "UNAVAILABLE"
)

@Entity(tableName = "hospital_price_sync_meta")
data class HospitalPriceSyncMetaEntity(
    @PrimaryKey val hospitalId: String,
    val hospitalName: String,
    val sourceUrl: String,
    val syncState: String, // "VERIFIED" | "NO_PUBLIC_PRICE_LIST" | "RESTRICTED" | "NO_OFFICIAL_URL" | "NETWORK_ERROR"
    val statusMessage: String,
    val verifiedPriceCount: Int,
    val lastSyncedMillis: Long
)

fun HospitalDiagnosticPriceEntity.toFallbackDiagnosticTest(): DiagnosticTest = DiagnosticTest(
    id = canonicalTestId,
    name = testName,
    shortName = "",
    nameBn = testName,
    category = category,
    subcategory = "Synced Portal Test",
    description = "Diagnostic test discovered from ${sourceName.ifBlank { "official hospital portals" }}.",
    whatItDetects = "Clinical diagnostic parameter published by the reporting hospital lab.",
    preparationInstructions = "Confirm preparation requirements with the hospital laboratory desk.",
    sampleType = "Clinical Sample",
    normalReferenceRange = "Refer to laboratory reference values for age and sex.",
    procedure = "Standard clinical laboratory collection and analysis.",
    estimatedDelivery = "${reportDeliveryHours.coerceAtLeast(1)} Hours",
    centerPrices = emptyList()
)
