package com.example.data.model

enum class FacilityType(val labelEn: String, val labelBn: String) {
    GOVERNMENT("Government", "সরকারি"),
    PRIVATE("Private", "বেসরকারি"),
    SPECIALIZED("Specialized", "বিশেষায়িত"),
    MEDICAL_COLLEGE("Medical College", "মেডিকেল কলেজ"),
    CLINIC("Clinic", "ক্লিনিক"),
    DIAGNOSTIC_CENTER("Diagnostic Center", "ডায়াগনস্টিক সেন্টার")
}

data class AreaInfo(
    val name: String,
    val district: String,
    val division: String,
    val lat: Double,
    val lng: Double
)

data class Hospital(
    val id: String,
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
    val isPopular: Boolean = false,
    val dataSource: String = "Directory"
)

enum class SlotStatus {
    AVAILABLE,
    BOOKED,
    BREAK,
    CLOSED
}

data class ScheduleSlot(
    val time24h: String,
    val status: SlotStatus
)

data class DayChamberSchedule(
    val weekdayIndex: Int, // 1=Mon..7=Sun (ISO) or custom label
    val weekdayShort: String,
    val isOpen: Boolean,
    val startTime24h: String,
    val endTime24h: String,
    val breakWindow: String,
    val slotDurationMins: Int,
    val slots: List<ScheduleSlot>,
    val holidayNote: String? = null
)

data class DoctorChamber(
    val chamberId: String,
    val hospitalId: String,
    val hospitalName: String,
    val chamberName: String,
    val address: String,
    val area: String,
    val district: String,
    val consultationFeeBdt: Int,
    val availableDaysSummary: String,
    val weeklySchedule: List<DayChamberSchedule>
)

data class Doctor(
    val id: String,
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
    val isTopRated: Boolean = false
)

data class CenterTestPrice(
    val centerId: String,
    val centerName: String,
    val area: String,
    val district: String,
    val priceBdt: Int,
    val homeCollectionFeeBdt: Int,
    val homeCollectionAvailable: Boolean,
    val reportDeliveryHours: Int
)

data class DiagnosticTest(
    val id: String,
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
    val lowestPriceBdt: Int get() = centerPrices.minOfOrNull { it.priceBdt } ?: 0
    val supportsHomeCollection: Boolean get() = centerPrices.any { it.homeCollectionAvailable }
}

data class EmergencyServiceItem(
    val id: String,
    val title: String,
    val titleBn: String,
    val category: String, // "National", "Ambulance", "Blood Bank", "ER Hospital"
    val phone: String,
    val address: String,
    val area: String,
    val district: String,
    val available247: Boolean,
    val latitude: Double,
    val longitude: Double
)
