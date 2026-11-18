package com.example.data.catalog

import com.example.data.model.Doctor

object DhakaCityDoctorsExtendedCatalog {

    private val eveningStandard = listOf("17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30")
    private val morningStandard = listOf("09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30")

    val doctors: List<Doctor> = listOf(
        makeDhakaDoctor(
            id = "doc_wadud_nicvd",
            name = "Prof. Dr. Abdul Wadud Chowdhury",
            nameBn = "অধ্যাপক ডা. আব্দুল ওয়াদুদ চৌধুরী",
            qualification = "MBBS, MD (Cardiology), FCPS (Medicine), FACC",
            designation = "Professor of Cardiology",
            specialization = "Cardiology",
            expYears = 24,
            bmdc = "BMDC A-23104",
            rating = 4.9,
            reviews = 365,
            bio = "Acute coronary care, heart failure, arrhythmia, and preventive cardiology specialist at NICVD & DMCH.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_wadud_nicvd", "hosp_nicvd", "National Institute of Cardiovascular Diseases (NICVD)",
                    "Cardiac OPD 2", "Sher-e-Bangla Nagar, Shyamoli, Dhaka", "Shyamoli", 700,
                    "Sun, Tue, Thu · 9:30 AM - 1:00 PM", setOf(2, 4, 6), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_wadud_popshant", "hosp_popular_shantinagar", "Popular Diagnostic Centre (Shantinagar)",
                    "Cardiology Room 309", "Shantinagar, Dhaka 1217", "Shantinagar", 1200,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_tohid_urology_nikdu",
            name = "Prof. Dr. Md. Tohidul Islam",
            nameBn = "অধ্যাপক ডা. মো. তৌহিদুল ইসলাম",
            qualification = "MBBS, MS (Urology), FACS",
            designation = "Senior Consultant Urologist & Endo-Urology Surgeon",
            specialization = "Urology",
            expYears = 19,
            bmdc = "BMDC A-31902",
            rating = 4.7,
            reviews = 195,
            bio = "Kidney stone laser surgery (RIRS/PCNL), prostate care, and reconstructive urology at NIKDU & Panthapath.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_tohid_nikdu", "hosp_nikdu", "National Institute of Kidney Diseases & Urology (NIKDU)",
                    "Urology OPD 5", "Sher-e-Bangla Nagar, Shyamoli, Dhaka", "Shyamoli", 600,
                    "Sun, Tue · 9:30 AM - 1:00 PM", setOf(2, 4), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_tohid_samorita", "hosp_samorita", "Samorita Hospital Limited",
                    "Urology Suite 305", "89/1 Panthapath, Dhaka 1215", "Panthapath", 1100,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_rubina_pediatrics_mirpur",
            name = "Dr. Rubina Yasmin",
            nameBn = "ডা. রুবিনা ইয়াসমিন",
            qualification = "MBBS, FCPS (Pediatrics), MD",
            designation = "Consultant Child Specialist & Neonatologist",
            specialization = "Pediatrics",
            expYears = 13,
            bmdc = "BMDC A-48712",
            rating = 4.7,
            reviews = 214,
            bio = "Newborn jaundice, childhood asthma, nutrition, and pediatric fever care in Mirpur 10 & Mirpur 11.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_rubina_popmir", "hosp_popular_mirpur", "Popular Diagnostic Centre (Mirpur)",
                    "Child Care Room 206", "Section 6, Mirpur 10 Link, Dhaka", "Mirpur", 800,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_rubina_ibhmir", "hosp_islamibank_mirpur", "Islami Bank Hospital Mirpur",
                    "Pediatric Room 108", "Section 11, Mirpur, Dhaka", "Mirpur", 700,
                    "Thu · 5:00 PM - 9:00 PM", setOf(6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_tanvir_ortho_mirpur",
            name = "Dr. Md. Shahidul Islam Akon",
            nameBn = "ডা. মো. শহীদুল ইসলাম আকন",
            qualification = "MBBS, MS (Orthopedics), Fellow Spine Surgery",
            designation = "Consultant Orthopedic & Trauma Surgeon",
            specialization = "Orthopedics",
            expYears = 15,
            bmdc = "BMDC A-42650",
            rating = 4.6,
            reviews = 185,
            bio = "Back pain, knee ligament injury, fracture fixation, and arthritis specialist in Mirpur & Dhanmondi.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_akon_ibnsmir", "hosp_ibnsina_mirpur", "Ibn Sina Diagnostic & Consultation Center (Mirpur)",
                    "Ortho Room 304", "Mirpur 10, Dhaka 1216", "Mirpur", 800,
                    "Sat, Sun, Tue, Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 4, 6), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_akon_alhelal", "hosp_alhelal_mirpur", "Al-Helal Specialized Hospital Ltd.",
                    "Trauma & Ortho Room 202", "Rokeya Sarani, Mirpur 10, Dhaka", "Mirpur", 800,
                    "Mon, Wed · 5:00 PM - 9:00 PM", setOf(3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_delwar_pulmo_asgar",
            name = "Dr. Delwar Hossain",
            nameBn = "ডা. দেলোয়ার হোসেন",
            qualification = "MBBS, MD (Chest Diseases), FCCP",
            designation = "Consultant Pulmonologist & Critical Care",
            specialization = "Pulmonology",
            expYears = 16,
            bmdc = "BMDC A-40120",
            rating = 4.7,
            reviews = 176,
            bio = "Chronic cough, bronchial asthma, COPD, pneumonia, and sleep medicine specialist in Old Dhaka & Uttara.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_delwar_asgar", "hosp_asgar_ali", "Asgar Ali Hospital",
                    "Pulmonology Suite 310", "Gandaria, Old Dhaka 1204", "Old Dhaka", 1100,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_delwar_poputt", "hosp_popular_uttara", "Popular Diagnostic Centre (Uttara)",
                    "Chest Room 305", "Sector 4, Uttara, Dhaka 1230", "Uttara", 900,
                    "Thu · 5:00 PM - 9:00 PM", setOf(6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_parveen_evercare_gynae",
            name = "Dr. Parveen Fatima",
            nameBn = "অধ্যাপক ডা. পারভীন ফাতেমা",
            qualification = "MBBS, FCPS (Obs & Gynae), Fellowship Reproductive Medicine",
            designation = "Senior Consultant Fertility & Gynecology",
            specialization = "Gynecology & Obstetrics",
            expYears = 25,
            bmdc = "BMDC A-21980",
            rating = 4.9,
            reviews = 312,
            bio = "Infertility, IVF counseling, high-risk obstetrics, and minimally invasive gynecologic surgery in Bashundhara & Banani.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_parveen_ever", "hosp_evercare", "Evercare Hospital Dhaka",
                    "Fertility & Gynae Wing Level 5", "Bashundhara R/A, Dhaka 1229", "Bashundhara R/A", 1600,
                    "Sun - Thu · 9:30 AM - 1:00 PM", setOf(2, 3, 4, 5, 6), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_parveen_praava", "hosp_praava_banani", "Praava Health Bangladesh",
                    "Women's Health Suite 2", "Road 17, Banani, Dhaka 1213", "Banani", 1400,
                    "Sat, Mon · 5:00 PM - 9:00 PM", setOf(1, 3), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_hasib_neuro_mirpur",
            name = "Dr. Hasibul Hasan",
            nameBn = "ডা. হাসিবুল হাসান",
            qualification = "MBBS, MD (Neurology), BSMMU",
            designation = "Consultant Neurologist",
            specialization = "Neurology",
            expYears = 12,
            bmdc = "BMDC A-51430",
            rating = 4.6,
            reviews = 168,
            bio = "Migraine, stroke rehabilitation, cervical spondylosis, and seizure management in Mirpur & Kallyanpur.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_hasib_popmir", "hosp_popular_mirpur", "Popular Diagnostic Centre (Mirpur)",
                    "Neuro Suite 402", "Section 6, Mirpur, Dhaka 1216", "Mirpur", 900,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_hasib_kallyan", "hosp_ibnsina_kallyanpur", "Ibn Sina Medical College Hospital (Kallyanpur)",
                    "Neuro OPD 205", "Kallyanpur, Mirpur Road, Dhaka", "Mirpur", 800,
                    "Sun, Tue, Thu · 5:00 PM - 9:00 PM", setOf(2, 4, 6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_farid_gastro_mugda",
            name = "Dr. Farid Ahmed",
            nameBn = "ডা. ফরিদ আহমেদ",
            qualification = "MBBS, MD (Gastroenterology)",
            designation = "Consultant Gastroenterologist & Endoscopist",
            specialization = "Gastroenterology",
            expYears = 13,
            bmdc = "BMDC A-47990",
            rating = 4.6,
            reviews = 158,
            bio = "Acidity, ulcer, fatty liver, colonoscopy, and GI bleeding specialist serving Mugda, Khilgaon & Badda.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_farid_khidmah", "hosp_khidmah_khilgaon", "Khidmah Hospital Pvt. Ltd.",
                    "GI & Liver Room 204", "Khilgaon Bishwa Road, Dhaka", "Khilgaon", 800,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_farid_farazy", "hosp_farazy_badda", "Farazy Hospital Limited",
                    "Gastro Room 109", "Banasree / Badda, Dhaka", "Badda", 800,
                    "Thu · 5:00 PM - 9:00 PM", setOf(6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_salma_derm_dhanmondi",
            name = "Dr. Salma Chowdhury",
            nameBn = "ডা. সালমা চৌধুরী",
            qualification = "MBBS, FCPS (Dermatology), DDV",
            designation = "Senior Consultant Dermatologist",
            specialization = "Dermatology",
            expYears = 14,
            bmdc = "BMDC A-45109",
            rating = 4.7,
            reviews = 210,
            bio = "Skin allergy, melasma, alopecia, acne, and pediatric dermatology specialist in Dhanmondi & Mirpur.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_salma_popdhan", "hosp_popular_diag", "Popular Diagnostic Centre (Dhanmondi)",
                    "Skin Suite 308", "Road 2, Dhanmondi, Dhaka 1205", "Dhanmondi", 1000,
                    "Sat, Sun, Tue, Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 4, 6), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_salma_popmir", "hosp_popular_mirpur", "Popular Diagnostic Centre (Mirpur)",
                    "Skin Room 211", "Section 6, Mirpur, Dhaka 1216", "Mirpur", 900,
                    "Mon, Wed · 5:00 PM - 9:00 PM", setOf(3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_mahfuz_peds_badda",
            name = "Dr. Mahfuzur Rahman",
            nameBn = "ডা. মাহফুজুর রহমান",
            qualification = "MBBS, DCH, FCPS (Pediatrics)",
            designation = "Consultant Pediatrician",
            specialization = "Pediatrics",
            expYears = 12,
            bmdc = "BMDC A-50882",
            rating = 4.6,
            reviews = 172,
            bio = "Child growth, immunization, neonatal checkups, and respiratory care in Badda, Gulshan & Uttara.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_mahfuz_farazy", "hosp_farazy_badda", "Farazy Hospital Limited",
                    "Child Care Room 104", "Banasree / Badda, Dhaka", "Badda", 700,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_mahfuz_kuwait", "hosp_kuwait_uttara", "Kuwait Bangladesh Friendship Govt. Hospital",
                    "Pediatric OPD 2", "Sector 6, Uttara, Dhaka 1230", "Uttara", 500,
                    "Sun, Tue · 9:30 AM - 1:00 PM", setOf(2, 4), "09:30", "13:00", "11:30", morningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_kamal_cardio_universal",
            name = "Dr. Kamal Uddin Ahmed",
            nameBn = "ডা. কামাল উদ্দিন আহমেদ",
            qualification = "MBBS, MD (Cardiology), FSCAI",
            designation = "Senior Consultant Cardiologist",
            specialization = "Cardiology",
            expYears = 17,
            bmdc = "BMDC A-36810",
            rating = 4.7,
            reviews = 205,
            bio = "Angiography, pacemaker care, hypertension, and cardiac rehabilitation in Mohakhali & Motijheel.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_kamal_univ", "hosp_universal_mohakhali", "Universal Medical College & Hospital",
                    "Cardiac Wing 201", "Mohakhali, Dhaka 1215", "Mohakhali", 1100,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_kamal_ibmot", "hosp_islamibank_motijheel", "Islami Bank Central Hospital (Kakrail / Motijheel)",
                    "Cardiac Room 302", "Kakrail / Motijheel, Dhaka", "Kakrail", 900,
                    "Thu · 5:00 PM - 9:00 PM", setOf(6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_sohel_ent_uttara",
            name = "Dr. Sohel Mahmud",
            nameBn = "ডা. সোহেল মাহমুদ",
            qualification = "MBBS, MS (ENT), Fellowship Otology",
            designation = "Consultant ENT & Neck Surgeon",
            specialization = "ENT",
            expYears = 11,
            bmdc = "BMDC A-53990",
            rating = 4.6,
            reviews = 145,
            bio = "Pediatric adenoids/tonsils, sinusitis, ear microsurgery, and voice care in Uttara.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_sohel_ship", "hosp_ship_uttara", "Ship International Hospital",
                    "ENT Clinic Room 208", "Sector 12, Uttara, Dhaka 1230", "Uttara", 1000,
                    "Sat - Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5, 6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        )
    )
}
