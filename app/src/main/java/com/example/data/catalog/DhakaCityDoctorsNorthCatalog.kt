package com.example.data.catalog

import com.example.data.model.Doctor
import com.example.data.model.DoctorChamber

internal fun makeDhakaChamber(
    chamberId: String,
    hospitalId: String,
    hospitalName: String,
    chamberName: String,
    address: String,
    area: String,
    feeBdt: Int,
    daysSummary: String,
    openDays: Set<Int>,
    start: String,
    end: String,
    breakSlot: String?,
    slots: List<String>
): DoctorChamber = DoctorChamber(
    chamberId = chamberId,
    hospitalId = hospitalId,
    hospitalName = hospitalName,
    chamberName = chamberName,
    address = address,
    area = area,
    district = "Dhaka",
    consultationFeeBdt = feeBdt,
    availableDaysSummary = daysSummary,
    weeklySchedule = BangladeshHealthcareCatalog.buildWeeklySchedule(
        openDays = openDays,
        startTime = start,
        endTime = end,
        breakSlot = breakSlot,
        slots = slots
    )
)

internal fun makeDhakaDoctor(
    id: String,
    name: String,
    nameBn: String,
    qualification: String,
    designation: String,
    specialization: String,
    expYears: Int,
    bmdc: String,
    rating: Double,
    reviews: Int,
    bio: String,
    chambers: List<DoctorChamber>,
    topRated: Boolean = false
): Doctor = Doctor(
    id = id,
    name = name,
    nameBn = nameBn,
    qualification = qualification,
    designation = designation,
    specialization = specialization,
    experienceYears = expYears,
    bmdcRegNo = bmdc,
    verified = true,
    languages = listOf("Bangla", "English"),
    rating = rating,
    reviewCount = reviews,
    bio = bio,
    chambers = chambers,
    isTopRated = topRated
)

object DhakaCityDoctorsNorthCatalog {

    private val eveningStandard = listOf("17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30")
    private val morningStandard = listOf("09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30")

    val doctors: List<Doctor> = listOf(
        // --- Mirpur Doctors ---
        makeDhakaDoctor(
            id = "doc_fazila_mirpur",
            name = "Prof. Dr. Fazila-Tun-Nesa Malik",
            nameBn = "অধ্যাপক ডা. ফজিলা-তুন-নেসা মালিক",
            qualification = "MBBS, FCPS (Medicine), MRCP (UK), FACC",
            designation = "Chief Consultant Clinical & Interventional Cardiology",
            specialization = "Cardiology",
            expYears = 25,
            bmdc = "BMDC A-21450",
            rating = 4.9,
            reviews = 412,
            bio = "Senior cardiologist leading coronary angioplasty, heart failure, and hypertension management in Mirpur.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_fazila_nhf", "hosp_nhf", "National Heart Foundation Hospital",
                    "NHF Senior Cardiac Suite 1", "Section 2, Mirpur, Dhaka 1216", "Mirpur", 1000,
                    "Sat - Wed · 9:30 AM - 1:00 PM", setOf(1, 2, 3, 4, 5), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_fazila_pop", "hosp_popular_mirpur", "Popular Diagnostic Centre (Mirpur)",
                    "Mirpur Cardiology Room 302", "Section 6, Mirpur 10 Link, Dhaka", "Mirpur", 1200,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_shamsul_kidney",
            name = "Prof. Dr. Harun Ur Rashid",
            nameBn = "অধ্যাপক ডা. হারুন উর রশিদ",
            qualification = "MBBS, FCPS (Medicine), PhD (Nephrology, UK)",
            designation = "Professor & Senior Consultant Nephrologist",
            specialization = "Nephrology",
            expYears = 28,
            bmdc = "BMDC A-18310",
            rating = 4.9,
            reviews = 340,
            bio = "Renal disease, chronic kidney disease, dialysis, and kidney transplant specialist practicing in Mirpur.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_harun_kidney", "hosp_kidney_mirpur", "Kidney Foundation Hospital & Research Institute",
                    "Renal Care Room 101", "Plot 5/2, Section 2, Mirpur, Dhaka", "Mirpur", 1000,
                    "Sat, Sun, Tue, Thu · 9:30 AM - 1:00 PM", setOf(1, 2, 4, 6), "09:30", "13:00", "11:30", morningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_rafiqul_mirpur",
            name = "Dr. Rafiqul Islam Bhuiyan",
            nameBn = "ডা. রফিকুল ইসলাম ভূঁইয়া",
            qualification = "MBBS, FCPS (Medicine), MD (Internal Medicine)",
            designation = "Senior Consultant Internal Medicine",
            specialization = "Medicine",
            expYears = 16,
            bmdc = "BMDC A-39412",
            rating = 4.6,
            reviews = 218,
            bio = "Internal medicine, dengue fever, diabetes, and thyroid care specialist serving Mirpur 10 and Mirpur 11.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_rafiq_ibh", "hosp_islamibank_mirpur", "Islami Bank Hospital Mirpur",
                    "Medicine Room 204", "Section 11, Mirpur, Dhaka 1216", "Mirpur", 800,
                    "Sat - Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5, 6), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_rafiq_ibns", "hosp_ibnsina_mirpur", "Ibn Sina Diagnostic & Consultation Center (Mirpur)",
                    "Chamber 405, Mirpur 10", "Mirpur 10, Dhaka 1216", "Mirpur", 800,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_nasrin_mirpur",
            name = "Dr. Nasrin Sultana Parvin",
            nameBn = "ডা. নাসরিন সুলতানা পারভীন",
            qualification = "MBBS, FCPS (Obs & Gynae), DGO",
            designation = "Consultant Gynecologist & Laparoscopic Surgeon",
            specialization = "Gynecology & Obstetrics",
            expYears = 14,
            bmdc = "BMDC A-44820",
            rating = 4.7,
            reviews = 235,
            bio = "Antenatal care, painless delivery, PCOS, and gynecological surgery specialist in Mirpur.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_nasrin_alhelal", "hosp_alhelal_mirpur", "Al-Helal Specialized Hospital Ltd.",
                    "Gynae Suite 301", "150 Rokeya Sarani, Mirpur 10, Dhaka", "Mirpur", 800,
                    "Sat, Sun, Tue, Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 4, 6), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_nasrin_azmal", "hosp_azmal_mirpur", "Dr. Azmal Hospital Ltd.",
                    "Maternity OPD 2", "Section 6, Mirpur, Dhaka", "Mirpur", 700,
                    "Mon, Wed · 5:00 PM - 9:00 PM", setOf(3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_tariqul_delta",
            name = "Prof. Dr. Tariqul Islam Chowdhury",
            nameBn = "অধ্যাপক ডা. তারিকুল ইসলাম চৌধুরী",
            qualification = "MBBS, MD (Oncology), FCPS (Radiotherapy)",
            designation = "Senior Consultant Clinical Oncology",
            specialization = "Oncology",
            expYears = 20,
            bmdc = "BMDC A-30519",
            rating = 4.7,
            reviews = 186,
            bio = "Chemotherapy, targeted cancer therapy, and precision radiotherapy specialist at Delta Hospital Mirpur and NICRH.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_tariq_delta", "hosp_delta_mirpur", "Delta Hospital Limited",
                    "Oncology Board Room 105", "Mirpur 1, Dhaka 1216", "Mirpur", 1000,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_mizanur_eye_mirpur",
            name = "Dr. Mizanur Rahman Khan",
            nameBn = "ডা. মিজানুর রহমান খান",
            qualification = "MBBS, FCPS (Ophthalmology), DO",
            designation = "Senior Consultant Cataract & Retina",
            specialization = "Ophthalmology",
            expYears = 15,
            bmdc = "BMDC A-41208",
            rating = 4.6,
            reviews = 178,
            bio = "Micro-incision phaco cataract surgery, diabetic retinopathy, and LASIK evaluation specialist.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_mizan_eyemir", "hosp_eye_mirpur", "Bangladesh Eye Hospital (Mirpur)",
                    "Eye Clinic Room 4", "Mirpur Section 2, Dhaka 1216", "Mirpur", 900,
                    "Sat - Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5, 6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_sabina_dental",
            name = "Dr. Sabina Yasmin",
            nameBn = "ডা. সাবিনা ইয়াসমিন",
            qualification = "BDS (DU), FCPS (Oral & Maxillofacial Surgery)",
            designation = "Associate Professor Dental & Jaw Surgery",
            specialization = "Dental & Maxillofacial",
            expYears = 13,
            bmdc = "BMDC D-2418",
            rating = 4.6,
            reviews = 154,
            bio = "Impacted wisdom teeth, jaw trauma, dental implants, and root canal specialist at Dhaka Dental College Mirpur.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_sabina_dental", "hosp_dental_mirpur", "Dhaka Dental College & Hospital",
                    "Maxillofacial OPD 3", "Mirpur Section 14, Dhaka 1206", "Mirpur", 600,
                    "Sat - Wed · 9:30 AM - 1:00 PM", setOf(1, 2, 3, 4, 5), "09:30", "13:00", "11:30", morningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_khalid_marks",
            name = "Dr. Khalid Mahmud",
            nameBn = "ডা. খালিদ মাহমুদ",
            qualification = "MBBS, MS (ENT), Fellowship Head-Neck Surgery",
            designation = "Associate Professor ENT",
            specialization = "ENT",
            expYears = 14,
            bmdc = "BMDC A-43910",
            rating = 4.5,
            reviews = 142,
            bio = "Endoscopic sinus surgery, tonsillectomy, vertigo, and hearing disorders specialist in Mirpur 14.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_khalid_marks", "hosp_marks_mirpur", "Marks Medical College & Hospital",
                    "ENT Chamber 210", "Mirpur 14, Dhaka 1206", "Mirpur", 700,
                    "Sat - Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5, 6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        // --- Gulshan / Banani / Bashundhara / Mohakhali / Badda Doctors ---
        makeDhakaDoctor(
            id = "doc_zahir_evercare",
            name = "Dr. Zahirul Haque",
            nameBn = "ডা. জহিরুল হক",
            qualification = "MBBS, MD (Pulmonology), FCCP (USA)",
            designation = "Senior Consultant Pulmonology & Critical Care",
            specialization = "Pulmonology",
            expYears = 18,
            bmdc = "BMDC A-35112",
            rating = 4.8,
            reviews = 245,
            bio = "Asthma, COPD, sleep apnea, interstitial lung disease, and bronchoscopy specialist.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_zahir_ever", "hosp_evercare", "Evercare Hospital Dhaka",
                    "Respiratory Clinic Level 4", "Bashundhara R/A, Dhaka 1229", "Bashundhara R/A", 1600,
                    "Sun - Thu · 9:30 AM - 1:00 PM", setOf(2, 3, 4, 5, 6), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_zahir_nidch", "hosp_nidch", "National Institute of Diseases of the Chest & Hospital (NIDCH)",
                    "Chest OPD Unit 2", "Mohakhali, Dhaka 1212", "Mohakhali", 700,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_sumaiya_banani",
            name = "Dr. Sumaiya Rahman",
            nameBn = "ডা. সুমাইয়া রহমান",
            qualification = "MBBS, FCPS (Dermatology & Venereology)",
            designation = "Consultant Clinical & Aesthetic Dermatologist",
            specialization = "Dermatology",
            expYears = 11,
            bmdc = "BMDC A-53042",
            rating = 4.7,
            reviews = 184,
            bio = "Eczema, psoriasis, acne scars, hair fall, and pediatric skin care specialist in Banani and Badda.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_sumaiya_praava", "hosp_praava_banani", "Praava Health Bangladesh",
                    "Dermatology Suite 3", "Road 17, Banani, Dhaka 1213", "Banani", 1400,
                    "Sat - Wed · 9:30 AM - 1:00 PM", setOf(1, 2, 3, 4, 5), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_sumaiya_badda", "hosp_popular_badda", "Popular Diagnostic Centre (Badda)",
                    "Skin Chamber 201", "North Badda, Progoti Sarani, Dhaka", "Badda", 900,
                    "Sun, Tue, Thu · 5:00 PM - 9:00 PM", setOf(2, 4, 6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_imran_united",
            name = "Dr. Imran Hossain Chowdhury",
            nameBn = "ডা. ইমরান হোসেন চৌধুরী",
            qualification = "MBBS, MD (Gastroenterology), MACP",
            designation = "Consultant Gastroenterologist & Hepatologist",
            specialization = "Gastroenterology",
            expYears = 14,
            bmdc = "BMDC A-46720",
            rating = 4.8,
            reviews = 212,
            bio = "Fatty liver, IBS, GERD, hepatitis B/C, and therapeutic endoscopy specialist in Gulshan & Mohakhali.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_imran_united", "hosp_united", "United Hospital Limited",
                    "GI & Liver Suite 305", "Gulshan 2, Dhaka 1212", "Gulshan", 1500,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_imran_icddrb", "hosp_icddrb", "icddr,b Dhaka Hospital",
                    "GI Research Clinic", "Mohakhali, Dhaka 1212", "Mohakhali", 800,
                    "Thu · 9:30 AM - 1:00 PM", setOf(6), "09:30", "13:00", "11:30", morningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_mahmuda_badda",
            name = "Dr. Mahmuda Begum",
            nameBn = "ডা. মাহমুদা বেগম",
            qualification = "MBBS, FCPS (Obs & Gynae)",
            designation = "Senior Consultant Gynecology & Obstetrics",
            specialization = "Gynecology & Obstetrics",
            expYears = 15,
            bmdc = "BMDC A-42801",
            rating = 4.6,
            reviews = 198,
            bio = "High-risk pregnancy, cesarean section, and infertility counseling specialist serving Badda, Rampura, and Banasree.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_mahmuda_farazy", "hosp_farazy_badda", "Farazy Hospital Limited",
                    "Gynae Wing 202", "Banasree Main Road, Badda, Dhaka", "Badda", 800,
                    "Sat - Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5, 6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_sabbir_tejgaon",
            name = "Dr. Sabbir Ahmed",
            nameBn = "ডা. সাব্বির আহমেদ",
            qualification = "MBBS, FCPS (General Surgery), FACS",
            designation = "Consultant Laparoscopic & General Surgeon",
            specialization = "General Surgery",
            expYears = 16,
            bmdc = "BMDC A-40588",
            rating = 4.6,
            reviews = 165,
            bio = "Gallbladder laparoscopy, hernia repair, appendicitis, and minimally invasive surgery specialist in Tejgaon & Mohakhali.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_sabbir_impulse", "hosp_impulse_tejgaon", "Impulse Hospital",
                    "Surgical OPD 401", "Tejgaon Industrial Area, Dhaka 1208", "Tejgaon", 1000,
                    "Sat, Sun, Tue, Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 4, 6), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_sabbir_universal", "hosp_universal_mohakhali", "Universal Medical College & Hospital",
                    "Surgery Chamber 12", "Mohakhali, Dhaka 1215", "Mohakhali", 900,
                    "Mon, Wed · 5:00 PM - 9:00 PM", setOf(3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        // --- Uttara & Cantonment Doctors ---
        makeDhakaDoctor(
            id = "doc_ashiq_uttara",
            name = "Dr. Ashiqur Rahman",
            nameBn = "ডা. আশিকুর রহমান",
            qualification = "MBBS, MD (Cardiology), NICVD",
            designation = "Senior Consultant Interventional Cardiologist",
            specialization = "Cardiology",
            expYears = 15,
            bmdc = "BMDC A-43219",
            rating = 4.7,
            reviews = 224,
            bio = "Chest pain, coronary stenting, ECG/echo evaluation, and hypertension care in Uttara.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_ashiq_ship", "hosp_ship_uttara", "Ship International Hospital",
                    "Cardiac OPD 304", "Sector 12, Uttara, Dhaka 1230", "Uttara", 1200,
                    "Sat - Wed · 9:30 AM - 1:00 PM", setOf(1, 2, 3, 4, 5), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_ashiq_lubana", "hosp_lubana_uttara", "Lubana General Hospital & Cardiac Centre",
                    "Cardiac Suite 2", "Sector 13, Uttara, Dhaka 1230", "Uttara", 900,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_shamima_uttara",
            name = "Dr. Shamima Akter Banu",
            nameBn = "ডা. শামীমা আক্তার বানু",
            qualification = "MBBS, FCPS (Medicine), MACP",
            designation = "Consultant Internal Medicine",
            specialization = "Medicine",
            expYears = 12,
            bmdc = "BMDC A-50114",
            rating = 4.6,
            reviews = 189,
            bio = "Adult medicine, fever, diabetes, respiratory infection, and liver screening specialist in Uttara.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_shamima_poputt", "hosp_popular_uttara", "Popular Diagnostic Centre (Uttara)",
                    "Medicine Room 208", "Sector 4, Uttara, Dhaka 1230", "Uttara", 900,
                    "Sat - Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5, 6), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_shamima_ibnutt", "hosp_ibnsina_uttara", "Ibn Sina Diagnostic & Consultation Center (Uttara)",
                    "Chamber 301", "Sector 13, Uttara, Dhaka 1230", "Uttara", 800,
                    "Sun, Tue, Thu · 9:30 AM - 1:00 PM", setOf(2, 4, 6), "09:30", "13:00", "11:30", morningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_saiful_uttara_ortho",
            name = "Dr. Saiful Islam",
            nameBn = "ডা. সাইফুল ইসলাম",
            qualification = "MBBS, MS (Orthopedics), NITOR",
            designation = "Consultant Orthopedic & Spine Surgeon",
            specialization = "Orthopedics",
            expYears = 13,
            bmdc = "BMDC A-47812",
            rating = 4.6,
            reviews = 152,
            bio = "Fracture care, knee arthritis, disc prolapse, and sports injury specialist serving Uttara and Cantonment.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_saiful_kurm", "hosp_kurmitola", "Kurmitola General Hospital",
                    "Orthopedic OPD 4", "Cantonment / Banani Link, Dhaka", "Banani", 500,
                    "Sun - Thu · 9:30 AM - 1:00 PM", setOf(2, 3, 4, 5, 6), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_saiful_cres", "hosp_lazz_uttara", "Uttara Crescent Clinic & Hospital",
                    "Ortho Chamber 6", "Sector 7, Uttara, Dhaka 1230", "Uttara", 800,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        )
    )
}
