package com.example.data.catalog

import com.example.data.model.Doctor

object DhakaSpecializedDoctorsCatalog {

    private val eveningStandard = listOf("17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30")
    private val morningStandard = listOf("09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30")

    val doctors: List<Doctor> = listOf(
        makeDhakaDoctor(
            id = "doc_bsh_cardio_momen",
            name = "Prof. Dr. M. A. Muqueet",
            nameBn = "অধ্যাপক ডা. এম এ মুকিত",
            qualification = "MBBS, MD (Cardiology), FACC",
            designation = "Senior Consultant Cardiologist",
            specialization = "Cardiology",
            expYears = 22,
            bmdc = "BMDC A-25108",
            rating = 4.8,
            reviews = 290,
            bio = "Interventional cardiology, heart failure, hypertension, and cardiac ICU care at Bangladesh Specialized Hospital & BSMMU.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_muqueet_bsh", "hosp_bd_specialized", "Bangladesh Specialized Hospital",
                    "Cardiac Wing Suite 404", "21 Shyamoli, Mirpur Road, Dhaka 1207", "Shyamoli", 1500,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_bsh_gastro_shamsul",
            name = "Prof. Dr. Shamsul Haque",
            nameBn = "অধ্যাপক ডা. শামসুল হক",
            qualification = "MBBS, FCPS (Medicine), MD (Gastroenterology)",
            designation = "Senior Consultant Gastroenterologist & Hepatologist",
            specialization = "Gastroenterology",
            expYears = 21,
            bmdc = "BMDC A-26410",
            rating = 4.8,
            reviews = 245,
            bio = "Therapeutic ERCP, liver disease, IBS, and endoscopy specialist practicing at Bangladesh Specialized Hospital & Gastroliver Institute.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_shamsul_bsh", "hosp_bd_specialized", "Bangladesh Specialized Hospital",
                    "GI & Liver Suite 306", "21 Shyamoli, Mirpur Road, Dhaka 1207", "Shyamoli", 1400,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_shamsul_gastro", "hosp_gastroliver", "Sheikh Russel National Gastroliver Institute & Hospital",
                    "Hepatology OPD 3", "Mohakhali, Dhaka 1212", "Mohakhali", 600,
                    "Sun, Tue · 9:30 AM - 1:00 PM", setOf(2, 4), "09:30", "13:00", "11:30", morningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_bsh_neuro_kanak",
            name = "Prof. Dr. Kanak Kanti Barua",
            nameBn = "অধ্যাপক ডা. কনক কান্তি বড়ুয়া",
            qualification = "MBBS, FCPS (Surgery), MS (Neurosurgery), PhD",
            designation = "Senior Consultant Neurosurgeon",
            specialization = "Neurology",
            expYears = 30,
            bmdc = "BMDC A-14920",
            rating = 4.9,
            reviews = 420,
            bio = "Brain tumor, spine disc surgery, and neuro-trauma specialist practicing at Bangladesh Specialized Hospital & BSMMU.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_kanak_bsh", "hosp_bd_specialized", "Bangladesh Specialized Hospital",
                    "Neurosurgery Suite 502", "21 Shyamoli, Mirpur Road, Dhaka 1207", "Shyamoli", 1500,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_ibrahim_rashid",
            name = "Prof. Dr. M. A. Rashid",
            nameBn = "অধ্যাপক ডা. এম এ রশিদ",
            qualification = "MBBS, MPH, MD (Cardiology), FACC",
            designation = "Senior Consultant Cardiology",
            specialization = "Cardiology",
            expYears = 27,
            bmdc = "BMDC A-18700",
            rating = 4.9,
            reviews = 360,
            bio = "Preventive cardiology, ischemic heart disease, and interventional care at Ibrahim Cardiac Hospital Shahbagh.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_rashid_ibrahim", "hosp_ibrahim_cardiac", "Ibrahim Cardiac Hospital & Research Institute",
                    "Cardiac OPD Room 201", "Shahbagh, Dhaka 1000", "Shahbagh", 1200,
                    "Sun - Thu · 9:30 AM - 1:00 PM", setOf(2, 3, 4, 5, 6), "09:30", "13:00", "11:30", morningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_addin_nahid_gynae",
            name = "Prof. Dr. Nahid Yasmin",
            nameBn = "অধ্যাপক ডা. নাহিদ ইয়াসমিন",
            qualification = "MBBS, FCPS (Obs & Gynae), MS",
            designation = "Professor of Obstetrics & Gynecology",
            specialization = "Gynecology & Obstetrics",
            expYears = 21,
            bmdc = "BMDC A-27890",
            rating = 4.8,
            reviews = 315,
            bio = "High-risk pregnancy, normal delivery, and gynecological oncology specialist at Ad-din Medical College Hospital.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_nahid_addin", "hosp_addin_moghbazar", "Ad-din Medical College Hospital",
                    "Maternal Suite 2", "Bara Moghbazar, Dhaka 1217", "Moghbazar", 900,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_labaid_cancer_ehsan",
            name = "Prof. Dr. Qamruzzaman Chowdhury",
            nameBn = "অধ্যাপক ডা. কামরুজ্জামান চৌধুরী",
            qualification = "MBBS, FCPS (Radiotherapy), FACP",
            designation = "Senior Consultant Clinical & Radiation Oncologist",
            specialization = "Oncology",
            expYears = 24,
            bmdc = "BMDC A-22840",
            rating = 4.8,
            reviews = 250,
            bio = "Precision IMRT/IGRT radiotherapy, chemotherapy, and breast/lung cancer care at Labaid Cancer Hospital & Ahsania Mission.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_qamru_labaidcan", "hosp_labaid_cancer", "Labaid Cancer Hospital & Super Speciality Centre",
                    "Oncology Wing 3", "26 Green Road, Dhaka 1205", "Green Road", 1500,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_qamru_ahsania", "hosp_ahsania_cancer", "Ahsania Mission Cancer & General Hospital",
                    "Radiotherapy OPD 1", "Sector 10, Uttara, Dhaka 1230", "Uttara", 1000,
                    "Sun, Tue · 9:30 AM - 1:00 PM", setOf(2, 4), "09:30", "13:00", "11:30", morningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_ent_firoz_national",
            name = "Prof. Dr. Md. Abu Hanif",
            nameBn = "অধ্যাপক ডা. মো. আবু হানিফ",
            qualification = "MBBS, FCPS (ENT), MS",
            designation = "Professor of ENT & Head-Neck Surgery",
            specialization = "ENT",
            expYears = 22,
            bmdc = "BMDC A-25610",
            rating = 4.8,
            reviews = 230,
            bio = "Cochlear implant, skull base, sinus, and ear surgery specialist at National Institute of ENT & Bangladesh ENT Hospital.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_hanif_natent", "hosp_national_ent", "National Institute of ENT",
                    "ENT OPD 1", "Tejgaon, Dhaka 1208", "Tejgaon", 600,
                    "Sun, Tue · 9:30 AM - 1:00 PM", setOf(2, 4), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_hanif_bdent", "hosp_bd_ent_sohanbag", "Bangladesh ENT Hospital Ltd.",
                    "ENT Suite 202", "Sobhanbagh, Dhanmondi, Dhaka", "Dhanmondi", 1200,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_eye_dewan_dhan",
            name = "Dr. Mahbubur Rahman Chowdhury",
            nameBn = "ডা. মাহবুবুর রহমান চৌধুরী",
            qualification = "MBBS, FCPS (Ophthalmology), Fellowship Vitreo-Retina",
            designation = "Senior Consultant Ophthalmologist",
            specialization = "Ophthalmology",
            expYears = 18,
            bmdc = "BMDC A-34910",
            rating = 4.8,
            reviews = 240,
            bio = "Diabetic retinopathy, retinal detachment, phaco cataract, and LASIK specialist in Dhanmondi & Uttara.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_mahbub_bdeyedh", "hosp_bd_eye_dhanmondi", "Bangladesh Eye Hospital (Dhanmondi)",
                    "Retina Suite 3", "Satmasjid Road, Dhanmondi, Dhaka", "Dhanmondi", 1200,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_mahbub_bdeyeut", "hosp_bd_eye_uttara", "Bangladesh Eye Hospital (Uttara)",
                    "Eye Clinic Room 2", "Sector 4, Uttara, Dhaka 1230", "Uttara", 1000,
                    "Thu · 5:00 PM - 9:00 PM", setOf(6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_badda_amz_med",
            name = "Dr. Md. Mostafizur Rahman",
            nameBn = "ডা. মো. মোস্তাফিজুর রহমান",
            qualification = "MBBS, FCPS (Medicine), MD",
            designation = "Senior Consultant Internal Medicine",
            specialization = "Medicine",
            expYears = 15,
            bmdc = "BMDC A-42100",
            rating = 4.6,
            reviews = 180,
            bio = "Internal medicine, uncontrolled hypertension, diabetes, and respiratory care at AMZ Hospital Badda & Ibn Sina Badda.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_mostafiz_amz", "hosp_amz_badda", "AMZ Hospital Ltd.",
                    "Medicine Room 204", "Uttar Badda, Dhaka 1212", "Badda", 900,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_mostafiz_ibnbad", "hosp_ibnsina_badda", "Ibn Sina Diagnostic Center (Badda)",
                    "Chamber 302", "Uttar Badda, Dhaka 1212", "Badda", 800,
                    "Thu · 5:00 PM - 9:00 PM", setOf(6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_insaf_urology",
            name = "Prof. Dr. AKM Anwarul Islam",
            nameBn = "অধ্যাপক ডা. এ কে এম আনোয়ারুল ইসলাম",
            qualification = "MBBS, FCPS (Surgery), FRCS, FICS (Urology)",
            designation = "Senior Consultant Urologist",
            specialization = "Urology",
            expYears = 26,
            bmdc = "BMDC A-19405",
            rating = 4.8,
            reviews = 275,
            bio = "Prostate surgery, kidney stones, urinary stricture, and uro-oncology specialist at Insaf Barakah & Ibn Sina Specialized.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_anwarul_insaf", "hosp_insaf_barakah", "Insaf Barakah Kidney & General Hospital",
                    "Urology Suite 102", "Moghbazar, Dhaka 1217", "Moghbazar", 1000,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_anwarul_ibnspec", "hosp_ibnsina_spec_dhan", "Ibn Sina Specialized Hospital (Dhanmondi)",
                    "Urology Room 309", "Road 15/A, Dhanmondi, Dhaka", "Dhanmondi", 1200,
                    "Sun, Tue · 5:00 PM - 9:00 PM", setOf(2, 4), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        )
    )
}
