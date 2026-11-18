package com.example.data.catalog

import com.example.data.model.Doctor

object DhakaCityDoctorsSouthCentralCatalog {

    private val eveningStandard = listOf("17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30")
    private val morningStandard = listOf("09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30")

    val doctors: List<Doctor> = listOf(
        // --- Shahbagh / Panthapath / Dhanmondi / Green Road / Farmgate ---
        makeDhakaDoctor(
            id = "doc_abdullah_bsmmu",
            name = "Prof. Dr. ABM Abdullah",
            nameBn = "অধ্যাপক ডা. এ বি এম আব্দুল্লাহ",
            qualification = "MBBS, MRCP (UK), FRCP (Edin), FCPS",
            designation = "Emeritus Professor of Internal Medicine",
            specialization = "Medicine",
            expYears = 34,
            bmdc = "BMDC A-11240",
            rating = 4.9,
            reviews = 640,
            bio = "Renowned Bangladeshi internal medicine physician specializing in complex fevers, hypertension, diabetes, and multisystem disorders.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_abdullah_bsmmu", "hosp_bsmmu", "Bangabandhu Sheikh Mujib Medical University (BSMMU)",
                    "Medicine Faculty Room 302", "Shahbagh, Dhaka 1000", "Shahbagh", 1000,
                    "Sun, Tue · 9:30 AM - 1:00 PM", setOf(2, 4), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_abdullah_green", "hosp_central_green", "Central Hospital Limited",
                    "Senior Medicine Room 1", "House 2, Road 5, Green Road, Dhaka", "Green Road", 1500,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_faruque_birdem",
            name = "Prof. Dr. Faruque Pathan",
            nameBn = "অধ্যাপক ডা. ফারুক পাঠান",
            qualification = "MBBS, MD (Endocrinology), MACP",
            designation = "Senior Consultant Endocrinology & Diabetes",
            specialization = "Endocrinology",
            expYears = 24,
            bmdc = "BMDC A-22190",
            rating = 4.9,
            reviews = 390,
            bio = "Diabetes, gestational diabetes, thyroid disorders, PCOS, and metabolic bone care specialist at BIRDEM & Shahbagh.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_faruque_birdem", "hosp_birdem", "BIRDEM General Hospital",
                    "Endocrinology Suite 204", "Shahbagh, Dhaka 1000", "Shahbagh", 1000,
                    "Sat - Wed · 9:30 AM - 1:00 PM", setOf(1, 2, 3, 4, 5), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_faruque_pop", "hosp_popular_mch", "Popular Medical College Hospital",
                    "Endocrine Room 406", "Dhanmondi Road 2, Dhaka 1205", "Dhanmondi", 1200,
                    "Sat, Mon, Thu · 5:00 PM - 9:00 PM", setOf(1, 3, 6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_lutfor_cardio_labaid",
            name = "Prof. Dr. Lutfor Rahman",
            nameBn = "অধ্যাপক ডা. লুৎফর রহমান",
            qualification = "MBBS, MS (Cardiothoracic Surgery), FACS",
            designation = "Chief Cardiac Surgeon",
            specialization = "Cardiology",
            expYears = 26,
            bmdc = "BMDC A-19870",
            rating = 4.9,
            reviews = 455,
            bio = "Beating-heart coronary bypass (CABG), valve replacement, and complex adult cardiac surgery specialist.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_lutfor_labaid", "hosp_labaid", "Labaid Specialized Hospital",
                    "Cardiac Surgery Suite 501", "Road 4, Dhanmondi, Dhaka 1205", "Dhanmondi", 1500,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_quazi_dewan_neuro",
            name = "Prof. Dr. Quazi Deen Mohammad",
            nameBn = "অধ্যাপক ডা. কাজী দ্বীন মোহাম্মদ",
            qualification = "MBBS, MD (Neurology), FCPS (Medicine)",
            designation = "Senior Consultant Neurology",
            specialization = "Neurology",
            expYears = 32,
            bmdc = "BMDC A-12890",
            rating = 4.9,
            reviews = 510,
            bio = "Stroke prevention, movement disorders, epilepsy, and headache specialist in Dhanmondi and NINS.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_deen_nins", "hosp_nins", "National Institute of Neurosciences & Hospital (NINS)",
                    "Neurology Director OPD", "Agargaon, Sher-e-Bangla Nagar, Dhaka", "Kafrul", 1000,
                    "Sun, Tue · 9:30 AM - 1:00 PM", setOf(2, 4), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_deen_anwer", "hosp_anwer_khan", "Anwer Khan Modern Medical College Hospital",
                    "Neuro Suite 303", "Road 8, Dhanmondi, Dhaka 1205", "Dhanmondi", 1500,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_mamun_al_mahtab",
            name = "Prof. Dr. Mamun Al Mahtab",
            nameBn = "অধ্যাপক ডা. মামুন আল মাহতাব",
            qualification = "MBBS, MSc (Gastro, UK), MD (Hepatology), FACG",
            designation = "Professor of Hepatology & Liver Care",
            specialization = "Gastroenterology",
            expYears = 21,
            bmdc = "BMDC A-27650",
            rating = 4.8,
            reviews = 325,
            bio = "Hepatitis, fatty liver, cirrhosis, endoscopic band ligation, and digestive care specialist.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_mamun_labaid", "hosp_labaid", "Labaid Specialized Hospital",
                    "Liver & GI Chamber 210", "Road 4, Dhanmondi, Dhaka 1205", "Dhanmondi", 1400,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_mamun_brb", "hosp_brb", "BRB Hospitals Limited",
                    "Hepatobiliary Wing 4", "West Panthapath, Dhaka 1215", "Panthapath", 1400,
                    "Sun, Tue · 5:00 PM - 9:00 PM", setOf(2, 4), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_pran_gopal_ent",
            name = "Prof. Dr. Pran Gopal Datta",
            nameBn = "অধ্যাপক ডা. প্রাণ গোপাল দত্ত",
            qualification = "MBBS, MCPS, ACORL, PhD (ENT)",
            designation = "Senior Consultant ENT & Micro-Ear Surgery",
            specialization = "ENT",
            expYears = 30,
            bmdc = "BMDC A-14200",
            rating = 4.9,
            reviews = 430,
            bio = "Microscopic ear surgery, cochlear implant, voice disorders, and sinus surgery specialist on Green Road.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_pran_green", "hosp_green_life", "Green Life Medical College Hospital",
                    "ENT Senior Suite 2", "Green Road, Dhaka 1205", "Green Road", 1500,
                    "Sat, Sun, Tue, Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 4, 6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_ava_hossain_eye",
            name = "Prof. Dr. Ava Hossain",
            nameBn = "অধ্যাপক ডা. আভা হোসেন",
            qualification = "MBBS, FCPS (Ophthalmology), FICS",
            designation = "Senior Consultant Ophthalmologist",
            specialization = "Ophthalmology",
            expYears = 23,
            bmdc = "BMDC A-24510",
            rating = 4.8,
            reviews = 240,
            bio = "Glaucoma, cataract surgery, and diabetic eye care specialist at Ispahani Islamia Eye Hospital Farmgate.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_ava_ispahani", "hosp_ispahani_eye", "Ispahani Islamia Eye Institute & Hospital",
                    "Glaucoma & Cataract Room 5", "Farmgate, Dhaka 1215", "Farmgate", 900,
                    "Sun - Thu · 9:30 AM - 1:00 PM", setOf(2, 3, 4, 5, 6), "09:30", "13:00", "11:30", morningStandard
                )
            )
        ),
        // --- Shyamoli / Mohammadpur Specialists ---
        makeDhakaDoctor(
            id = "doc_amjad_ortho_nitor",
            name = "Prof. Dr. Md. Amjad Hossain",
            nameBn = "অধ্যাপক ডা. মো. আমজাদ হোসেন",
            qualification = "MBBS, MS (Orthopedics), AO Trauma Fellow (Germany)",
            designation = "Chief Orthopedic & Joint Replacement Surgeon",
            specialization = "Orthopedics",
            expYears = 27,
            bmdc = "BMDC A-19220",
            rating = 4.8,
            reviews = 340,
            bio = "Hip & knee arthroplasty, complex pelvic trauma, and limb salvage surgery specialist in Shyamoli & Panthapath.",
            topRated = true,
            chambers = listOf(
                makeDhakaChamber(
                    "ch_amjad_nitor", "hosp_nitor", "National Institute of Traumatology & Orthopedic Rehabilitation (NITOR)",
                    "Trauma Unit 1", "Shyamoli / Sher-e-Bangla Nagar, Dhaka", "Shyamoli", 600,
                    "Sun, Tue, Thu · 9:30 AM - 1:00 PM", setOf(2, 4, 6), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_amjad_labaid", "hosp_labaid", "Labaid Specialized Hospital",
                    "Ortho Suite 402", "Road 4, Dhanmondi, Dhaka 1205", "Dhanmondi", 1400,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_sultana_shishu",
            name = "Prof. Dr. Manzoor Hussain",
            nameBn = "অধ্যাপক ডা. মনজুর হোসেন",
            qualification = "MBBS, FCPS (Pediatrics), FRCP (UK)",
            designation = "Senior Consultant Pediatrician",
            specialization = "Pediatrics",
            expYears = 26,
            bmdc = "BMDC A-20410",
            rating = 4.8,
            reviews = 310,
            bio = "Childhood asthma, pediatric infections, nutrition, and newborn intensive care specialist at Bangladesh Shishu Hospital.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_manzoor_shishu", "hosp_shishu", "Bangladesh Shishu Hospital & Institute",
                    "Pediatric Room 108", "Sher-e-Bangla Nagar, Shyamoli, Dhaka", "Shyamoli", 700,
                    "Sat - Wed · 9:30 AM - 1:00 PM", setOf(1, 2, 3, 4, 5), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_manzoor_care", "hosp_care_mohammadpur", "Care Medical College Hospital",
                    "Child Care Suite 2", "Iqbal Road, Mohammadpur, Dhaka", "Mohammadpur", 1000,
                    "Sat, Mon, Thu · 5:00 PM - 9:00 PM", setOf(1, 3, 6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_mehtab_nimh",
            name = "Dr. Mekhala Sarkar",
            nameBn = "ডা. মেখলা সরকার",
            qualification = "MBBS, FCPS (Psychiatry)",
            designation = "Associate Professor & Consultant Psychiatrist",
            specialization = "Psychiatry",
            expYears = 15,
            bmdc = "BMDC A-41890",
            rating = 4.8,
            reviews = 205,
            bio = "Depression, anxiety disorders, OCD, sleep issues, and adolescent mental wellness specialist at NIMH Shyamoli.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_mekhala_nimh", "hosp_nimh", "National Institute of Mental Health (NIMH)",
                    "Psychiatry OPD 4", "Sher-e-Bangla Nagar, Shyamoli, Dhaka", "Shyamoli", 600,
                    "Sun, Tue · 9:30 AM - 1:00 PM", setOf(2, 4), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_mekhala_bmch", "hosp_bmch_mohammadpur", "Bangladesh Medical College Hospital",
                    "Mind Care Room 12", "Mohammadpur / Satmasjid Link, Dhaka", "Mohammadpur", 1000,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_shahnaz_suhrawardy",
            name = "Dr. Shahnaz Begum",
            nameBn = "ডা. শাহনাজ বেগম",
            qualification = "MBBS, FCPS (Obs & Gynae), MS",
            designation = "Consultant Gynecologist & Obstetrician",
            specialization = "Gynecology & Obstetrics",
            expYears = 14,
            bmdc = "BMDC A-45901",
            rating = 4.6,
            reviews = 180,
            bio = "Maternity care, normal delivery support, fibroids, and gynecologic endoscopy in Shyamoli & Mohammadpur.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_shahnaz_suh", "hosp_suhrawardy", "Shaheed Suhrawardy Medical College & Hospital",
                    "Gynae OPD 6", "Shyamoli, Dhaka 1207", "Shyamoli", 600,
                    "Sun - Thu · 9:30 AM - 1:00 PM", setOf(2, 3, 4, 5, 6), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_shahnaz_kallyan", "hosp_ibnsina_kallyanpur", "Ibn Sina Medical College Hospital (Kallyanpur)",
                    "Gynae Chamber 304", "Kallyanpur, Mirpur Road, Dhaka", "Mirpur", 800,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        // --- Kakrail / Shantinagar / Malibagh / Khilgaon / Mugda / Motijheel ---
        makeDhakaDoctor(
            id = "doc_arif_kakrail",
            name = "Dr. Md. Arifur Rahman",
            nameBn = "ডা. মো. আরিফুর রহমান",
            qualification = "MBBS, FCPS (Medicine), MD (Nephrology)",
            designation = "Consultant Medicine & Kidney Specialist",
            specialization = "Nephrology",
            expYears = 14,
            bmdc = "BMDC A-46102",
            rating = 4.6,
            reviews = 175,
            bio = "Hypertension-related kidney care, electrolyte imbalance, and dialysis management in Kakrail & Motijheel.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_arif_ibmot", "hosp_islamibank_motijheel", "Islami Bank Central Hospital (Kakrail / Motijheel)",
                    "Nephrology Room 207", "Kakrail, Dhaka 1000", "Kakrail", 800,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_arif_mugda", "hosp_mugda", "Mugda Medical College & Hospital",
                    "Medicine OPD 8", "Mugda, Dhaka 1214", "Mugda", 500,
                    "Sun, Tue, Thu · 9:30 AM - 1:00 PM", setOf(2, 4, 6), "09:30", "13:00", "11:30", morningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_kazi_shantinagar",
            name = "Dr. Kazi Tariqul Islam",
            nameBn = "ডা. কাজী তারিকুল ইসলাম",
            qualification = "MBBS, FCPS (ENT), MS",
            designation = "Senior Consultant ENT Surgeon",
            specialization = "ENT",
            expYears = 17,
            bmdc = "BMDC A-37240",
            rating = 4.6,
            reviews = 192,
            bio = "Sinusitis, nasal polyps, tonsil, and thyroid surgery specialist serving Shantinagar, Kakrail, and Eskaton.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_kazi_holy", "hosp_holy_family", "Holy Family Red Crescent Medical College Hospital",
                    "ENT Chamber 104", "Eskaton Garden, Kakrail, Dhaka", "Kakrail", 900,
                    "Sun, Tue, Thu · 9:30 AM - 1:00 PM", setOf(2, 4, 6), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_kazi_shant", "hosp_popular_shantinagar", "Popular Diagnostic Centre (Shantinagar)",
                    "ENT Suite 402", "Shantinagar Chowrasta, Dhaka", "Shantinagar", 900,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_sharmin_malibagh",
            name = "Dr. Sharmin Jahan",
            nameBn = "ডা. শারমিন জাহান",
            qualification = "MBBS, MD (Neurology), BSMMU",
            designation = "Consultant Neurologist",
            specialization = "Neurology",
            expYears = 11,
            bmdc = "BMDC A-54210",
            rating = 4.7,
            reviews = 160,
            bio = "Headache, vertigo, sciatica, stroke follow-up, and nerve conduction care in Malibagh & Khilgaon.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_sharmin_ibnmal", "hosp_ibnsina_malibagh", "Ibn Sina Diagnostic & Consultation Center (Malibagh)",
                    "Neurology Chamber 308", "DIT Road, Malibagh, Dhaka", "Malibagh", 900,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_sharmin_khidmah", "hosp_khidmah_khilgaon", "Khidmah Hospital Pvt. Ltd.",
                    "Neuro OPD 3", "Khilgaon Bishwa Road, Dhaka", "Khilgaon", 800,
                    "Thu · 5:00 PM - 9:00 PM", setOf(6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_nizam_malibagh_derm",
            name = "Dr. Nizam Uddin Ahmed",
            nameBn = "ডা. নিজাম উদ্দিন আহমেদ",
            qualification = "MBBS, DDV, FCPS (Dermatology)",
            designation = "Consultant Skin, Allergy & Laser Specialist",
            specialization = "Dermatology",
            expYears = 13,
            bmdc = "BMDC A-48120",
            rating = 4.5,
            reviews = 148,
            bio = "Fungal infections, vitiligo, chronic urticaria, and cosmetic dermatology specialist in Malibagh.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_nizam_medinova", "hosp_medinova_malibagh", "Medinova Medical Services (Malibagh)",
                    "Skin & Laser Room 205", "New Circular Road, Malibagh, Dhaka", "Malibagh", 900,
                    "Sat - Thu · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5, 6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        // --- Old Dhaka / Mitford / Jatrabari / Savar Doctors ---
        makeDhakaDoctor(
            id = "doc_monirul_olddhaka",
            name = "Prof. Dr. Monirul Alam",
            nameBn = "অধ্যাপক ডা. মনিরুল আলম",
            qualification = "MBBS, FCPS (Medicine), MRCP",
            designation = "Senior Consultant Internal Medicine",
            specialization = "Medicine",
            expYears = 22,
            bmdc = "BMDC A-25904",
            rating = 4.7,
            reviews = 288,
            bio = "Senior internal medicine and infectious disease specialist serving Old Dhaka and Mitford.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_monir_mitford", "hosp_ssmc_mitford", "Sir Salimullah Medical College & Mitford Hospital",
                    "Medicine Room 14", "Mitford Road, Old Dhaka 1100", "Old Dhaka", 600,
                    "Sun, Tue, Thu · 9:30 AM - 1:00 PM", setOf(2, 4, 6), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_monir_asgar", "hosp_asgar_ali", "Asgar Ali Hospital",
                    "Medicine Suite 301", "Gandaria, Old Dhaka 1204", "Old Dhaka", 1200,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_fahmida_asgar",
            name = "Dr. Fahmida Rashid",
            nameBn = "ডা. ফাহমিদা রশিদ",
            qualification = "MBBS, FCPS (Obs & Gynae)",
            designation = "Consultant Gynecology & Obstetrics",
            specialization = "Gynecology & Obstetrics",
            expYears = 13,
            bmdc = "BMDC A-49200",
            rating = 4.7,
            reviews = 194,
            bio = "Maternity care, laparoscopic gynecology, and high-risk obstetrics specialist in Old Dhaka & Jatrabari.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_fahmida_asgar", "hosp_asgar_ali", "Asgar Ali Hospital",
                    "Obs & Gynae Suite 408", "Gandaria, Old Dhaka 1204", "Old Dhaka", 1100,
                    "Sat - Wed · 5:00 PM - 9:00 PM", setOf(1, 2, 3, 4, 5), "17:00", "21:00", "19:00", eveningStandard
                ),
                makeDhakaChamber(
                    "ch_fahmida_jatra", "hosp_popular_jatrabari", "Popular Diagnostic Centre (Jatrabari)",
                    "Gynae Room 202", "South Jatrabari, Dhaka 1204", "Jatrabari", 800,
                    "Thu · 5:00 PM - 9:00 PM", setOf(6), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        ),
        makeDhakaDoctor(
            id = "doc_anwar_dmch_surg",
            name = "Prof. Dr. Anwarul Islam",
            nameBn = "অধ্যাপক ডা. আনোয়ারুল ইসলাম",
            qualification = "MBBS, FCPS (Surgery), FRCS (Glasgow)",
            designation = "Professor of General & Laparoscopic Surgery",
            specialization = "General Surgery",
            expYears = 23,
            bmdc = "BMDC A-24112",
            rating = 4.8,
            reviews = 276,
            bio = "Hepatobiliary, colorectal, breast, and laparoscopic surgery specialist at DMCH & Savar.",
            chambers = listOf(
                makeDhakaChamber(
                    "ch_anwar_dmch", "hosp_dmch", "Dhaka Medical College Hospital",
                    "Surgery Unit 2 OPD", "Shahbagh, Dhaka 1000", "Shahbagh", 600,
                    "Sun, Tue · 9:30 AM - 1:00 PM", setOf(2, 4), "09:30", "13:00", "11:30", morningStandard
                ),
                makeDhakaChamber(
                    "ch_anwar_enam", "hosp_enam_savar", "Enam Medical College & Hospital",
                    "Surgical Suite 3", "Savar, Dhaka 1340", "Savar", 900,
                    "Sat, Mon, Wed · 5:00 PM - 9:00 PM", setOf(1, 3, 5), "17:00", "21:00", "19:00", eveningStandard
                )
            )
        )
    )
}
