package com.example.ui.state

data class LocalizedText(
    val navHome: String,
    val navHospitals: String,
    val navDoctors: String,
    val navDiagnostic: String,
    val navProfile: String,
    val searchPlaceholder: String,
    val emergencyTitle: String,
    val emergencySubtitle: String,
    val nearbyHospitals: String,
    val popularHospitals: String,
    val topDoctors: String,
    val diagnosticServices: String,
    val recentlyViewed: String,
    val quickAppointments: String,
    val quickCompare: String,
    val quickTests: String,
    val quickRecords: String,
    val bookAppointment: String,
    val directions: String,
    val callNow: String,
    val comparePrices: String,
    val homeSample: String,
    val verifiedBadge: String
)

fun stringsFor(language: String): LocalizedText =
    if (language == "bn") {
        LocalizedText(
            navHome = "হোম",
            navHospitals = "হাসপাতাল",
            navDoctors = "ডাক্তার",
            navDiagnostic = "ডায়াগনস্টিক",
            navProfile = "প্রোফাইল",
            searchPlaceholder = "হাসপাতাল, ডাক্তার, টেস্ট বা এলাকা খুঁজুন...",
            emergencyTitle = "২৪/৭ জরুরি স্বাস্থ্যসেবা",
            emergencySubtitle = "নিকটস্থ ইমার্জেন্সি হাসপাতাল, অ্যাম্বুলেন্স ও ব্লাড ব্যাংক",
            nearbyHospitals = "নিকটবর্তী হাসপাতাল",
            popularHospitals = "জনপ্রিয় হাসপাতাল",
            topDoctors = "শীর্ষ রেটেড ডাক্তার",
            diagnosticServices = "ডায়াগনস্টিক ও টেস্ট সেবা",
            recentlyViewed = "সম্প্রতি দেখা",
            quickAppointments = "অ্যাপয়েন্টমেন্ট",
            quickCompare = "তুলনা করুন",
            quickTests = "ল্যাব টেস্ট",
            quickRecords = "স্বাস্থ্য নথি",
            bookAppointment = "অ্যাপয়েন্টমেন্ট বুক",
            directions = "গুগল ম্যাপে দিকনির্দেশ",
            callNow = "কল করুন",
            comparePrices = "খরচ তুলনা",
            homeSample = "হোম স্যাম্পল কালেকশন",
            verifiedBadge = "যাচাইকৃত"
        )
    } else {
        LocalizedText(
            navHome = "Home",
            navHospitals = "Hospitals",
            navDoctors = "Doctors",
            navDiagnostic = "Diagnostic",
            navProfile = "Profile",
            searchPlaceholder = "Search hospitals, doctors, tests, or area...",
            emergencyTitle = "24/7 Emergency Healthcare",
            emergencySubtitle = "Nearby ER hospitals, ambulances & blood banks",
            nearbyHospitals = "Nearby Hospitals",
            popularHospitals = "Popular Hospitals",
            topDoctors = "Top-Rated Doctors",
            diagnosticServices = "Diagnostic & Testing",
            recentlyViewed = "Recently Viewed",
            quickAppointments = "Appointments",
            quickCompare = "Compare",
            quickTests = "Lab Tests",
            quickRecords = "Health Records",
            bookAppointment = "Book Appointment",
            directions = "Directions",
            callNow = "Call",
            comparePrices = "Compare Prices",
            homeSample = "Home Sample",
            verifiedBadge = "Verified"
        )
    }
