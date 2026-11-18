package com.example.data.catalog

import com.example.data.model.CenterTestPrice
import com.example.data.model.DiagnosticTest
import com.example.data.model.FacilityType
import com.example.data.model.Hospital
import java.util.Locale

data class MasterTestRule(
    val canonicalId: String,
    val canonicalName: String,
    val shortName: String,
    val category: String,
    val keywords: List<String>
)

object MedicalTestMasterCatalog {

    private val commonDiagnosticCenters = listOf(
        CenterTestPrice("hosp_popular_diag", "Popular Diagnostic Centre (Dhanmondi)", "Dhanmondi", "Dhaka", 0, 200, true, 4),
        CenterTestPrice("hosp_ibnsina", "Ibn Sina Diagnostic Center", "Dhanmondi", "Dhaka", 0, 150, true, 5),
        CenterTestPrice("hosp_popular_mirpur", "Popular Diagnostic Centre (Mirpur)", "Mirpur", "Dhaka", 0, 180, true, 4),
        CenterTestPrice("hosp_ibnsina_mirpur", "Ibn Sina Diagnostic (Mirpur)", "Mirpur", "Dhaka", 0, 150, true, 5),
        CenterTestPrice("hosp_popular_uttara", "Popular Diagnostic Centre (Uttara)", "Uttara", "Dhaka", 0, 200, true, 4),
        CenterTestPrice("hosp_ibnsina_malibagh", "Ibn Sina Diagnostic (Malibagh)", "Malibagh", "Dhaka", 0, 150, true, 5),
        CenterTestPrice("hosp_praava_banani", "Praava Health Bangladesh", "Banani", "Dhaka", 0, 250, true, 4),
        CenterTestPrice("hosp_square", "Square Hospitals Ltd.", "Panthapath", "Dhaka", 0, 300, true, 4),
        CenterTestPrice("hosp_evercare", "Evercare Hospital Dhaka", "Bashundhara R/A", "Dhaka", 0, 350, true, 4)
    )

    private val tertiaryAndMajorCenters = listOf(
        CenterTestPrice("hosp_square", "Square Hospitals Ltd.", "Panthapath", "Dhaka", 0, 300, true, 6),
        CenterTestPrice("hosp_evercare", "Evercare Hospital Dhaka", "Bashundhara R/A", "Dhaka", 0, 350, true, 6),
        CenterTestPrice("hosp_united", "United Hospital Limited", "Gulshan", "Dhaka", 0, 300, true, 6),
        CenterTestPrice("hosp_popular_diag", "Popular Diagnostic Centre (Dhanmondi)", "Dhanmondi", "Dhaka", 0, 200, true, 8),
        CenterTestPrice("hosp_ibnsina", "Ibn Sina Diagnostic Center", "Dhanmondi", "Dhaka", 0, 150, true, 8)
    )

    private val cardiacCenters = listOf(
        CenterTestPrice("hosp_nhf", "National Heart Foundation Hospital", "Mirpur", "Dhaka", 0, 0, false, 2),
        CenterTestPrice("hosp_square", "Square Hospitals Ltd.", "Panthapath", "Dhaka", 0, 0, false, 2),
        CenterTestPrice("hosp_evercare", "Evercare Hospital Dhaka", "Bashundhara R/A", "Dhaka", 0, 0, false, 2),
        CenterTestPrice("hosp_united", "United Hospital Limited", "Gulshan", "Dhaka", 0, 0, false, 2),
        CenterTestPrice("hosp_ibnsina", "Ibn Sina Diagnostic Center", "Dhanmondi", "Dhaka", 0, 0, false, 2),
        CenterTestPrice("hosp_popular_diag", "Popular Diagnostic Centre (Dhanmondi)", "Dhanmondi", "Dhaka", 0, 0, false, 2)
    )

    private val advancedImagingCenters = listOf(
        CenterTestPrice("hosp_evercare", "Evercare Hospital Dhaka", "Bashundhara R/A", "Dhaka", 0, 0, false, 12),
        CenterTestPrice("hosp_square", "Square Hospitals Ltd.", "Panthapath", "Dhaka", 0, 0, false, 12),
        CenterTestPrice("hosp_united", "United Hospital Limited", "Gulshan", "Dhaka", 0, 0, false, 12),
        CenterTestPrice("hosp_popular_diag", "Popular Diagnostic Centre (Dhanmondi)", "Dhanmondi", "Dhaka", 0, 0, false, 24),
        CenterTestPrice("hosp_ibnsina", "Ibn Sina Diagnostic Center", "Dhanmondi", "Dhaka", 0, 0, false, 24)
    )

    val allTests: List<DiagnosticTest> = listOf(
        // ==================== 1. HEMATOLOGY ====================
        DiagnosticTest(
            id = "test_cbc",
            name = "Complete Blood Count",
            shortName = "CBC",
            nameBn = "সম্পূর্ণ রক্ত পরীক্ষা (সিবিসি)",
            category = "Hematology",
            subcategory = "Cell Count & Differential",
            description = "Measures red blood cells, white blood cells with 5-part differential, hemoglobin, hematocrit, and platelet counts.",
            whatItDetects = "Anemia, bacterial or viral infections (including dengue thrombocytopenia), leukemia, and blood clotting cell abnormalities.",
            preparationInstructions = "No fasting required. Stay well-hydrated prior to blood draw.",
            sampleType = "Venous Blood (EDTA Tube)",
            normalReferenceRange = "Hb: Male 13.5–17.5 g/dL, Female 12.0–15.5 g/dL, Child 11.0–14.5 g/dL | WBC: 4,000–11,000 /µL | Platelets: 150,000–450,000 /µL",
            procedure = "A phlebotomist disinfects the antecubital vein and draws 2–3 mL of blood into an EDTA lavender-top tube for automated hematology analyzer counting.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("Full Blood Count", "FBC", "Hemogram", "CBC with ESR"),
            requiredCapabilities = listOf("Pathology", "Diagnostic", "Medicine", "Emergency"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_esr",
            name = "Erythrocyte Sedimentation Rate",
            shortName = "ESR",
            nameBn = "ইএসআর (রক্তের অবক্ষেপণ হার)",
            category = "Hematology",
            subcategory = "Inflammatory Markers",
            description = "Measures how quickly red blood cells settle at the bottom of a standardized vertical column in one hour.",
            whatItDetects = "Systemic inflammation, autoimmune flares (RA, SLE), chronic infections such as tuberculosis, and giant cell arteritis.",
            preparationInstructions = "No special fasting required; inform the lab of recent steroid or anti-inflammatory medication.",
            sampleType = "Venous Blood (Citrate / EDTA)",
            normalReferenceRange = "Male <50y: 0–15 mm/hr, >50y: 0–20 mm/hr | Female <50y: 0–20 mm/hr, >50y: 0–30 mm/hr | Child: 0–10 mm/hr",
            procedure = "Blood is drawn into an anticoagulated tube and placed vertically using the Westergren or automated optical method for 60 minutes.",
            estimatedDelivery = "3 - 4 Hours",
            relatedAliases = listOf("Sed Rate", "Westergren ESR"),
            requiredCapabilities = listOf("Pathology", "Diagnostic", "Medicine"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_pbf",
            name = "Peripheral Blood Film",
            shortName = "PBF",
            nameBn = "পেরিফেরাল ব্লাড ফিল্ম",
            category = "Hematology",
            subcategory = "Blood Morphology",
            description = "Microscopic examination of stained blood smear by a hematopathologist to inspect RBC, WBC, and platelet morphology.",
            whatItDetects = "Iron-deficiency vs megaloblastic anemia, hemolysis, malaria parasites, blast cells, and reactive lymphocytosis.",
            preparationInstructions = "No fasting required. Bring recent CBC report if available.",
            sampleType = "Venous Blood (EDTA Smear)",
            normalReferenceRange = "Normocytic normochromic RBCs; mature neutrophils and lymphocytes without atypical blasts or parasites.",
            procedure = "A thin blood drop is spread onto a glass slide, Leishman/Wright-stained, and reviewed under high-power oil-immersion microscopy.",
            estimatedDelivery = "6 - 12 Hours",
            relatedAliases = listOf("Blood Smear", "Peripheral Smear", "PBS"),
            requiredCapabilities = listOf("Pathology", "Hematology", "Diagnostic"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_retic",
            name = "Reticulocyte Count",
            shortName = "Retic",
            nameBn = "রেটিকুলোসাইট কাউন্ট",
            category = "Hematology",
            subcategory = "Bone Marrow Response",
            description = "Measures young immature red blood cells released from the bone marrow into the bloodstream.",
            whatItDetects = "Bone marrow response to anemia, hemolytic anemia, blood loss recovery, or marrow suppression.",
            preparationInstructions = "No special preparation required.",
            sampleType = "Venous Blood (EDTA)",
            normalReferenceRange = "Adults: 0.5%–2.5% of total RBCs | Newborns (1–2 days): 2.5%–6.0%",
            procedure = "Venous EDTA blood is stained with supravital dye or analyzed via flow cytometry to quantify reticulocyte percentage.",
            estimatedDelivery = "6 Hours",
            relatedAliases = listOf("Retic Count", "Immature Reticulocyte Fraction"),
            requiredCapabilities = listOf("Pathology", "Hematology"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_hb_electro",
            name = "Hemoglobin Electrophoresis",
            shortName = "Hb Electrophoresis",
            nameBn = "হিমোগ্লোবিন ইলেক্ট্রোফোরেসিস",
            category = "Hematology",
            subcategory = "Hemoglobinopathies",
            description = "Separates and quantifies hemoglobin variants (HbA, HbA2, HbF, HbE, HbS) via capillary electrophoresis or HPLC.",
            whatItDetects = "Beta-thalassemia trait/major, HbE trait/disease (highly prevalent in Bangladesh), and sickle cell disorders.",
            preparationInstructions = "No fasting needed. Inform lab if the patient received a blood transfusion within the last 3 months.",
            sampleType = "Venous Blood (EDTA)",
            normalReferenceRange = "Adults: HbA 95%–98%, HbA2 2.0%–3.5%, HbF <1.0% | Infants <6m have higher physiological HbF",
            procedure = "Hemolysed RBCs are passed through an HPLC or capillary electrophoresis column to separate hemoglobin fractions by charge.",
            estimatedDelivery = "24 - 48 Hours",
            relatedAliases = listOf("Hb Variant Analysis", "HPLC Hemoglobin", "Thalassemia Screening"),
            requiredCapabilities = listOf("Hematology", "Pathology"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_coag",
            name = "Coagulation Profile (PT, INR & APTT)",
            shortName = "PT / INR & APTT",
            nameBn = "কোয়াগুলেশন প্রোফাইল (পিটি/আইএনআর ও এপিটিটি)",
            category = "Hematology",
            subcategory = "Hemostasis & Coagulation",
            description = "Evaluates extrinsic (PT/INR) and intrinsic (APTT) blood clotting pathways before surgery or during anticoagulant therapy.",
            whatItDetects = "Warfarin/heparin dosing levels, liver synthetic dysfunction, vitamin K deficiency, and bleeding disorders like hemophilia.",
            preparationInstructions = "Avoid fatty meals just before collection; note exact time of any anticoagulant dose (Warfarin, Heparin).",
            sampleType = "Venous Blood (Sodium Citrate Blue-Top)",
            normalReferenceRange = "PT: 11–13.5 sec | INR (no anticoagulation): 0.8–1.1 (therapeutic warfarin: 2.0–3.0) | APTT: 25–35 sec",
            procedure = "Citrated plasma is separated by centrifugation and mixed with thromboplastin/calcium reagents in an optical coagulometer.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("Prothrombin Time", "INR", "Activated Partial Thromboplastin Time"),
            requiredCapabilities = listOf("Pathology", "Surgery", "Cardiology", "ICU"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_d_dimer",
            name = "Quantitative D-Dimer",
            shortName = "D-Dimer",
            nameBn = "ডি-ডাইমার পরীক্ষা",
            category = "Hematology",
            subcategory = "Thrombosis & Fibrinolysis",
            description = "Measures fibrin degradation fragments released into blood when a clot dissolves.",
            whatItDetects = "Deep vein thrombosis (DVT), pulmonary embolism (PE), disseminated intravascular coagulation (DIC), and severe inflammation.",
            preparationInstructions = "No fasting required; collect prior to starting thrombolytic therapy when possible.",
            sampleType = "Venous Plasma (Sodium Citrate)",
            normalReferenceRange = "Adults <50y: <0.50 µg/mL FEU (or <500 ng/mL) | Age-adjusted (>50y): Age × 10 ng/mL",
            procedure = "Citrated plasma undergoes quantitative immunoturbidimetric or fluorescent immunoassay measurement.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("Fibrin Degradation Fragment", "D-Dimer Quantitative"),
            requiredCapabilities = listOf("Emergency", "Pathology", "Cardiology", "ICU"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_iron_profile",
            name = "Serum Iron, TIBC & Ferritin Profile",
            shortName = "Iron Profile / Ferritin",
            nameBn = "আয়রন প্রোফাইল ও ফেরিটিন",
            category = "Hematology",
            subcategory = "Nutritional Anemia",
            description = "Measures circulating iron, total iron-binding capacity (transferrin saturation), and stored body iron (ferritin).",
            whatItDetects = "Iron-deficiency anemia, anemia of chronic disease, and iron overload syndromes (hemochromatosis).",
            preparationInstructions = "8–12 hours overnight fasting preferred; collect in the morning before taking iron supplements.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Serum Iron: Male 65–175 µg/dL, Female 50–170 µg/dL | Ferritin: Male 24–336 ng/mL, Female 11–307 ng/mL | TIBC: 240–450 µg/dL",
            procedure = "Fasting morning venous sample is centrifuged and assayed via colorimetric spectrophotometry and chemiluminescence.",
            estimatedDelivery = "6 - 12 Hours",
            relatedAliases = listOf("Serum Ferritin", "Transferrin Saturation", "Iron Studies"),
            requiredCapabilities = listOf("Pathology", "Diagnostic"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_blood_group",
            name = "ABO Blood Grouping & Rh Typing",
            shortName = "Blood Group & Rh",
            nameBn = "রক্তের গ্রুপ ও আরএইচ টাইপিং",
            category = "Hematology",
            subcategory = "Immunohematology",
            description = "Determines major blood group antigens (A, B, AB, O) and Rhesus D factor compatibility.",
            whatItDetects = "Safe blood transfusion compatibility, prenatal Rh incompatibility risk in pregnancy, and surgical pre-op typing.",
            preparationInstructions = "No fasting or preparation required.",
            sampleType = "Venous Blood (EDTA)",
            normalReferenceRange = "Categorical classification: Group A, B, AB, or O with Rh(D) Positive or Negative.",
            procedure = "Patient RBCs and plasma are tested by forward and reverse agglutination using monoclonal anti-A, anti-B, and anti-D antisera.",
            estimatedDelivery = "1 - 2 Hours",
            relatedAliases = listOf("ABO/Rh Typing", "Blood Grouping"),
            requiredCapabilities = listOf("Pathology", "Blood Bank", "Diagnostic", "Emergency"),
            centerPrices = commonDiagnosticCenters
        ),

        // ==================== 2. CLINICAL BIOCHEMISTRY & METABOLIC ====================
        DiagnosticTest(
            id = "test_blood_sugar",
            name = "Fasting & Postprandial Blood Glucose",
            shortName = "FBS / PPBS",
            nameBn = "ফাস্টিং ও খাবার পর ব্লাড সুগার",
            category = "Clinical Biochemistry",
            subcategory = "Diabetes & Glycemic Control",
            description = "Measures blood glucose after an overnight fast and/or 2 hours after a standardized meal.",
            whatItDetects = "Prediabetes, Diabetes Mellitus, gestational hyperglycemia, and fasting hypoglycemia.",
            preparationInstructions = "Fast strictly 8–10 hours (plain water permitted) for FBS; for PPBS draw exact 2 hours after starting meal.",
            sampleType = "Venous Plasma (Sodium Fluoride Grey-Top)",
            normalReferenceRange = "Fasting: 70–99 mg/dL (3.9–5.5 mmol/L) | 2-Hr Postprandial: <140 mg/dL (<7.8 mmol/L) | Diabetes threshold: FBS ≥126 mg/dL",
            procedure = "Blood drawn into a fluoride-oxalate tube inhibits glycolysis before enzymatic hexokinase/glucose oxidase photometric measurement.",
            estimatedDelivery = "2 - 4 Hours",
            relatedAliases = listOf("Fasting Plasma Glucose", "FPG", "RBS", "2HABF"),
            requiredCapabilities = listOf("Pathology", "Diagnostic", "Endocrinology", "Medicine"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_hba1c",
            name = "Glycated Hemoglobin (HbA1c)",
            shortName = "HbA1c",
            nameBn = "এইচবিএ১সি (৩ মাসের গড় সুগার)",
            category = "Clinical Biochemistry",
            subcategory = "Diabetes & Glycemic Control",
            description = "Quantifies glucose-bound hemoglobin to reflect average blood sugar levels over the preceding 8–12 weeks.",
            whatItDetects = "Long-term diabetes glycemic control, undiagnosed diabetes, and risk of diabetic microvascular complications.",
            preparationInstructions = "No fasting required; can be drawn at any time of day.",
            sampleType = "Venous Blood (EDTA)",
            normalReferenceRange = "Normal: <5.7% | Prediabetes: 5.7%–6.4% | Diabetes: ≥6.5% | Target for most diabetic adults: <7.0%",
            procedure = "Whole EDTA blood is analyzed using NGSP-certified HPLC or boronate affinity assay.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("A1C", "Glycosylated Hemoglobin"),
            requiredCapabilities = listOf("Pathology", "Diagnostic", "Endocrinology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_ogtt",
            name = "Oral Glucose Tolerance Test",
            shortName = "OGTT",
            nameBn = "ওরাল গ্লুকোজ টলারেন্স টেস্ট (ওজিটিটি)",
            category = "Clinical Biochemistry",
            subcategory = "Diabetes & Glycemic Control",
            description = "Serial glucose checks after drinking a standardized 75g anhydrous glucose load.",
            whatItDetects = "Gestational Diabetes Mellitus (GDM) in pregnancy and impaired glucose tolerance not caught by fasting sugar alone.",
            preparationInstructions = "Fast 8–12 hours overnight; stay seated quietly at the lab for 2 hours during the test without eating or smoking.",
            sampleType = "Serial Venous Plasma (0h, 1h, 2h)",
            normalReferenceRange = "Non-pregnant 2h: <140 mg/dL | Pregnancy (IADPSG): Fasting <92 mg/dL, 1h <180 mg/dL, 2h <153 mg/dL",
            procedure = "Baseline fasting sample is drawn, patient drinks 75g glucose solution within 5 minutes, and repeat venous samples are drawn at 1 and 2 hours.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("75g Glucose Tolerance Test", "GTT", "Pregnancy Sugar Challenge"),
            requiredCapabilities = listOf("Pathology", "Gynecology", "Endocrinology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_lipid",
            name = "Lipid Profile (Cholesterol Panel)",
            shortName = "Lipid Profile",
            nameBn = "লিপিড প্রোফাইল (কোলেস্টেরল পরীক্ষা)",
            category = "Clinical Biochemistry",
            subcategory = "Cardiometabolic Lipids",
            description = "Measures Total Cholesterol, LDL, HDL, VLDL, and Triglycerides to assess atherosclerotic cardiovascular risk.",
            whatItDetects = "Dyslipidemia, hypertriglyceridemia, familial hypercholesterolemia, and coronary heart disease risk.",
            preparationInstructions = "10–12 hours fasting required (plain water only; avoid alcohol for 24 hours prior).",
            sampleType = "Venous Serum",
            normalReferenceRange = "Total Chol: <200 mg/dL | LDL: <100 mg/dL | Triglycerides: <150 mg/dL | HDL: Male ≥40 mg/dL, Female ≥50 mg/dL",
            procedure = "Fasting serum is separated and analyzed by enzymatic spectrophotometric assay; LDL is calculated or directly measured.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("Fasting Lipid Panel", "Coronary Risk Lipid Panel"),
            requiredCapabilities = listOf("Pathology", "Diagnostic", "Cardiology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_lft",
            name = "Liver Function Test Panel",
            shortName = "LFT",
            nameBn = "লিভার ফাংশন টেস্ট (এলএফটি)",
            category = "Clinical Biochemistry",
            subcategory = "Hepatic Panel",
            description = "Evaluates hepatocellular enzymes (ALT/SGPT, AST/SGOT), biliary markers (ALP, GGT), total/direct bilirubin, and serum albumin.",
            whatItDetects = "Viral hepatitis (A/B/C/E), fatty liver disease (MASLD), obstructive jaundice, drug-induced liver injury, and cirrhosis.",
            preparationInstructions = "8 hours fasting recommended; avoid alcohol and strenuous exercise for 24 hours before sampling.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Total Bilirubin: 0.2–1.2 mg/dL | ALT (SGPT): Male <45 U/L, Female <34 U/L | AST (SGOT): <40 U/L | ALP: 44–147 U/L | Albumin: 3.5–5.0 g/dL",
            procedure = "Venous blood drawn in a clot-activator tube is centrifuged for serum and run on an automated clinical chemistry analyzer.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("Hepatic Function Panel", "SGPT & Bilirubin", "Liver Enzymes"),
            requiredCapabilities = listOf("Pathology", "Diagnostic", "Gastroenterology", "Medicine"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_kft",
            name = "Kidney Function Test (Creatinine, Urea & eGFR)",
            shortName = "KFT / RFT",
            nameBn = "কিডনি ফাংশন টেস্ট (ক্রিয়েটিনিন ও ইউরিয়া)",
            category = "Clinical Biochemistry",
            subcategory = "Renal Panel",
            description = "Measures serum creatinine, blood urea nitrogen (BUN), uric acid, and estimated glomerular filtration rate (eGFR).",
            whatItDetects = "Acute kidney injury, chronic kidney disease (CKD), dehydration, contrast-safety readiness, and gout.",
            preparationInstructions = "Avoid heavy high-protein meals and creatine supplements for 12 hours; normal hydration.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Creatinine: Male 0.7–1.3 mg/dL, Female 0.6–1.1 mg/dL, Child 0.3–0.7 mg/dL | Urea: 15–40 mg/dL | eGFR: ≥90 mL/min/1.73m²",
            procedure = "Serum separated from clotted venous blood is tested via kinetic Jaffe or enzymatic IDMS-traceable creatinine assay.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("Renal Function Test", "Serum Creatinine", "Renal Panel"),
            requiredCapabilities = listOf("Pathology", "Diagnostic", "Nephrology", "Urology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_electrolytes",
            name = "Serum Electrolytes (Na+, K+, Cl-, HCO3-)",
            shortName = "Electrolytes",
            nameBn = "সিরাম ইলেক্ট্রোলাইটস",
            category = "Clinical Biochemistry",
            subcategory = "Renal & Acid-Base",
            description = "Measures essential blood ions controlling fluid balance, nerve signaling, heart rhythm, and acid-base status.",
            whatItDetects = "Hyponatremia, hyperkalemia, dehydration from diarrhea/vomiting, kidney tubular disease, and diuretic side effects.",
            preparationInstructions = "No fasting required; avoid prolonged tourniquet clenching during blood draw to prevent false high potassium.",
            sampleType = "Venous Serum / Lithium Heparin",
            normalReferenceRange = "Na+: 135–145 mmol/L | K+: 3.5–5.1 mmol/L | Cl-: 98–107 mmol/L | HCO3-: 22–28 mmol/L",
            procedure = "Plasma or serum is analyzed immediately using ion-selective electrode (ISE) potentiometry.",
            estimatedDelivery = "2 - 4 Hours",
            relatedAliases = listOf("Serum Sodium & Potassium", "Basic Metabolic Electrolytes"),
            requiredCapabilities = listOf("Pathology", "Emergency", "ICU", "Nephrology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_uric_acid",
            name = "Serum Uric Acid",
            shortName = "Uric Acid",
            nameBn = "সিরাম ইউরিক অ্যাসিড",
            category = "Clinical Biochemistry",
            subcategory = "Metabolic & Rheumatology",
            description = "Quantifies purine metabolism end-product circulating in blood.",
            whatItDetects = "Hyperuricemia, gouty arthritis flares, uric acid kidney stones, and tumor lysis monitoring.",
            preparationInstructions = "4–8 hours fasting preferred; avoid red meat, seafood, and alcohol for 24 hours prior.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Male: 3.5–7.2 mg/dL | Female: 2.6–6.0 mg/dL | Child: 2.0–5.5 mg/dL",
            procedure = "Venous serum undergoes uricase enzymatic colorimetric testing.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("Blood Urate", "Serum Urate"),
            requiredCapabilities = listOf("Pathology", "Diagnostic"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_calcium_mg",
            name = "Serum Calcium, Phosphate & Magnesium",
            shortName = "Ca / PO4 / Mg",
            nameBn = "ক্যালসিয়াম, ফসফেট ও ম্যাগনেসিয়াম",
            category = "Clinical Biochemistry",
            subcategory = "Bone & Mineral Metabolism",
            description = "Evaluates blood mineral levels essential for bone strength, neuromuscular function, and cardiac stability.",
            whatItDetects = "Hypocalcemia, hyperparathyroidism, vitamin D deficiency osteomalacia, and renal mineral bone disorder.",
            preparationInstructions = "Morning fasting sample recommended; pause OTC calcium supplements for 24 hours if advised.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Total Calcium: 8.6–10.2 mg/dL | Phosphate: Adult 2.5–4.5 mg/dL, Child 4.0–7.0 mg/dL | Magnesium: 1.7–2.2 mg/dL",
            procedure = "Serum analyzed via Arsenazo III / molybdate spectrophotometric chemistry.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("Bone Mineral Panel", "Corrected Calcium"),
            requiredCapabilities = listOf("Pathology", "Endocrinology", "Orthopedics"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_vit_d_b12",
            name = "Vitamin D (25-OH) & Vitamin B12",
            shortName = "Vit D & B12",
            nameBn = "ভিটামিন ডি ও ভিটামিন বি১২",
            category = "Clinical Biochemistry",
            subcategory = "Vitamins & Nutrition",
            description = "Measures total 25-hydroxyvitamin D and cobalamin (B12) reserves.",
            whatItDetects = "Vitamin D deficiency (osteopenia, fatigue, muscle ache) and B12 deficiency (neuropathy, megaloblastic anemia).",
            preparationInstructions = "No strict fasting needed; avoid high-dose biotin supplements 48 hours before immunoassay.",
            sampleType = "Venous Serum",
            normalReferenceRange = "25-OH Vitamin D: Sufficient 30–100 ng/mL, Insufficient 20–29, Deficient <20 ng/mL | Vitamin B12: 200–900 pg/mL",
            procedure = "Serum is measured using automated electrochemiluminescence immunoassay (ECLIA).",
            estimatedDelivery = "12 - 24 Hours",
            relatedAliases = listOf("25-Hydroxyvitamin D", "Serum Cobalamin"),
            requiredCapabilities = listOf("Pathology", "Diagnostic"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_pancreatic",
            name = "Serum Amylase & Lipase",
            shortName = "Amylase & Lipase",
            nameBn = "অ্যামাইলেজ ও লাইপেজ",
            category = "Clinical Biochemistry",
            subcategory = "Pancreatic Enzymes",
            description = "Measures digestive enzymes released into circulation during pancreatic inflammation.",
            whatItDetects = "Acute or chronic pancreatitis, pancreatic duct obstruction, and severe epigastric abdominal emergencies.",
            preparationInstructions = "No fasting required during acute abdominal pain; avoid alcohol prior to routine testing.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Amylase: 28–100 U/L | Lipase: 13–60 U/L (Acute pancreatitis typically >3× upper limit)",
            procedure = "Venous serum is analyzed via enzymatic kinetic assay.",
            estimatedDelivery = "3 - 4 Hours",
            relatedAliases = listOf("Pancreatic Enzyme Panel"),
            requiredCapabilities = listOf("Pathology", "Emergency", "Gastroenterology"),
            centerPrices = commonDiagnosticCenters
        ),

        // ==================== 3. ENDOCRINOLOGY & HORMONES ====================
        DiagnosticTest(
            id = "test_thyroid",
            name = "Thyroid Function Test (TSH, FT3, FT4)",
            shortName = "TSH / FT3 / FT4",
            nameBn = "থাইরয়েড প্রোফাইল (টিএসএইচ, এফটি৩, এফটি৪)",
            category = "Endocrinology",
            subcategory = "Thyroid Axis",
            description = "Measures pituitary TSH alongside circulating free thyroid hormones FT3 and FT4.",
            whatItDetects = "Hypothyroidism, hyperthyroidism (Graves disease), subclinical thyroid dysfunction, and pregnancy thyroid screening.",
            preparationInstructions = "Draw blood in the morning before taking daily levothyroxine tablet; avoid biotin supplements for 48 hours.",
            sampleType = "Venous Serum",
            normalReferenceRange = "TSH: Adult 0.4–4.2 mIU/L (1st Trimester Pregnancy 0.1–2.5) | FT4: 0.8–1.8 ng/dL | FT3: 2.3–4.2 pg/mL",
            procedure = "Venous serum is tested by high-sensitivity third-generation chemiluminescent immunoassay (CLIA/ECLIA).",
            estimatedDelivery = "8 - 12 Hours",
            relatedAliases = listOf("Thyroid Panel", "Free Thyroid Profile", "TSH Reflex"),
            requiredCapabilities = listOf("Endocrinology", "Pathology", "Diagnostic"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_repro_hormones",
            name = "Reproductive Hormone Panel (FSH, LH, Prolactin, Testosterone)",
            shortName = "FSH / LH / PRL / Testosterone",
            nameBn = "হরমোন প্রোফাইল (এফএসএইচ, এলএইচ, প্রোল্যাক্টিন)",
            category = "Endocrinology",
            subcategory = "Reproductive Endocrinology",
            description = "Assesses hypothalamic-pituitary-gonadal hormone balance regulating fertility, ovulation, and androgen status.",
            whatItDetects = "PCOS, pituitary prolactinoma, premature ovarian insufficiency, male hypogonadism, and infertility causes.",
            preparationInstructions = "For menstruating women, collect on Day 2 or Day 3 of cycle unless instructed otherwise; morning resting draw for Prolactin/Testosterone.",
            sampleType = "Venous Serum",
            normalReferenceRange = "FSH (Follicular): 3.5–12.5 IU/L, Male: 1.5–12.4 IU/L | Prolactin: Male 4–15 ng/mL, Non-preg Female 4–23 ng/mL | Total Testosterone Male: 280–1100 ng/dL, Female: 15–70 ng/dL",
            procedure = "Morning serum is assayed via chemiluminescent immunoassay.",
            estimatedDelivery = "12 - 24 Hours",
            relatedAliases = listOf("Fertility Hormone Panel", "PCOS Panel"),
            requiredCapabilities = listOf("Endocrinology", "Gynecology", "Pathology"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_cortisol",
            name = "Serum Cortisol (Morning / Evening)",
            shortName = "Serum Cortisol",
            nameBn = "সিরাম কর্টিসল",
            category = "Endocrinology",
            subcategory = "Adrenal Function",
            description = "Measures circadian glucocorticoid stress hormone levels produced by the adrenal glands.",
            whatItDetects = "Adrenal insufficiency (Addison disease) and cortisol excess (Cushing syndrome).",
            preparationInstructions = "Collect strictly between 8:00–9:00 AM after calm rest; avoid exogenous steroids if ordered by endocrinologist.",
            sampleType = "Venous Serum",
            normalReferenceRange = "8 AM Fasting: 5–23 µg/dL | 4 PM Afternoon: 3–13 µg/dL",
            procedure = "Timed venous blood is centrifuged and measured by competitive immunoassay.",
            estimatedDelivery = "12 - 24 Hours",
            relatedAliases = listOf("AM Cortisol", "Adrenal Stress Hormone"),
            requiredCapabilities = listOf("Endocrinology", "Pathology"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_beta_hcg",
            name = "Quantitative Beta-hCG",
            shortName = "β-hCG",
            nameBn = "বিটা-এইচসিজি পরীক্ষা",
            category = "Endocrinology",
            subcategory = "Pregnancy & Gestational Markers",
            description = "Quantifies human chorionic gonadotropin hormone concentration in blood.",
            whatItDetects = "Early pregnancy confirmation, ectopic pregnancy monitoring, miscarriage follow-up, and molar/germ-cell tumors.",
            preparationInstructions = "No fasting required.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Non-pregnant Female & Male: <5 mIU/mL | Post-menopausal: <9.5 mIU/mL | Pregnant 4w: 5–426 mIU/mL (doubles ~48–72h)",
            procedure = "Venous serum is quantified on an automated sandwich immunoassay analyzer.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("Quantitative Pregnancy Blood Test", "Total hCG"),
            requiredCapabilities = listOf("Pathology", "Gynecology", "Emergency"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_amh",
            name = "Anti-Müllerian Hormone",
            shortName = "AMH",
            nameBn = "অ্যান্টি-মুলেরিয়ান হরমোন",
            category = "Endocrinology",
            subcategory = "Ovarian Reserve",
            description = "Reflects remaining ovarian follicular pool regardless of menstrual cycle day.",
            whatItDetects = "Diminished ovarian reserve, PCOS hyper-response risk, and IVF stimulation planning.",
            preparationInstructions = "Can be drawn on any day of the menstrual cycle without fasting.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Reproductive age (20–35y): 1.5–4.0 ng/mL | Low reserve: <1.0 ng/mL | PCOS often >4.5 ng/mL",
            procedure = "Serum sample is analyzed via high-sensitivity ECLIA immunoassay.",
            estimatedDelivery = "24 - 48 Hours",
            relatedAliases = listOf("Ovarian Reserve Marker"),
            requiredCapabilities = listOf("Endocrinology", "Gynecology"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_pth",
            name = "Intact Parathyroid Hormone",
            shortName = "PTH",
            nameBn = "প্যারাথাইরয়েড হরমোন (পিটিএইচ)",
            category = "Endocrinology",
            subcategory = "Calcium Regulation",
            description = "Measures intact parathyroid hormone alongside calcium balance.",
            whatItDetects = "Primary hyperparathyroidism, secondary hyperparathyroidism in CKD, and hypoparathyroidism.",
            preparationInstructions = "Morning fasting sample recommended; keep sample chilled immediately after draw.",
            sampleType = "Venous Plasma (Chilled EDTA) / Serum",
            normalReferenceRange = "Adults: 15–65 pg/mL (interpret alongside serum calcium)",
            procedure = "Chilled plasma/serum undergoes intact two-site sandwich immunoassay.",
            estimatedDelivery = "12 - 24 Hours",
            relatedAliases = listOf("iPTH", "Parathormone"),
            requiredCapabilities = listOf("Endocrinology", "Nephrology"),
            centerPrices = tertiaryAndMajorCenters
        ),

        // ==================== 4. CLINICAL PATHOLOGY (URINE & STOOL) ====================
        DiagnosticTest(
            id = "test_urine",
            name = "Urine Routine Examination",
            shortName = "Urine R/E",
            nameBn = "ইউরিন আর/ই (প্রস্রাব রুটিন পরীক্ষা)",
            category = "Clinical Pathology",
            subcategory = "Urinalysis",
            description = "Physical, dipstick chemical (glucose, protein, ketones, leukocyte esterase, nitrite), and microscopic urine sediment analysis.",
            whatItDetects = "Urinary tract infection (UTI), proteinuria, hematuria, kidney stones, and uncontrolled glycosuria.",
            preparationInstructions = "Collect clean-catch mid-stream first-morning urine in a sterile wide-mouth container.",
            sampleType = "Urine (Mid-stream Clean-Catch)",
            normalReferenceRange = "Color: Pale Yellow | pH: 4.5–8.0 | Protein/Glucose/Ketones: Negative | Pus Cells: 0–5 /HPF | RBC: 0–2 /HPF",
            procedure = "Urine undergoes automated chemistry strip reading followed by centrifugation and microscopy of sediment.",
            estimatedDelivery = "3 - 4 Hours",
            relatedAliases = listOf("Urinalysis", "Urine RME", "Routine Urinalysis"),
            requiredCapabilities = listOf("Pathology", "Diagnostic", "Urology", "Medicine"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_urine_cs",
            name = "Urine Culture & Antibiotic Sensitivity",
            shortName = "Urine C/S",
            nameBn = "ইউরিন কালচার ও সেন্সিটিভিটি",
            category = "Microbiology",
            subcategory = "Bacteriology Culture",
            description = "Incubates urine on selective agar to identify bacterial pathogens and determine effective antibiotics.",
            whatItDetects = "E. coli, Klebsiella, Pseudomonas, Enterococcus UTIs and ESBL/MDR antibiotic resistance.",
            preparationInstructions = "Collect mid-stream urine in a sterile container BEFORE starting antibiotics; deliver within 1 hour.",
            sampleType = "Urine (Sterile Mid-stream)",
            normalReferenceRange = "No significant bacterial growth (<10,000 CFU/mL); Significant UTI ≥100,000 CFU/mL",
            procedure = "Calibrated loop inoculation onto CLED/MacConkey agar for 24–48h incubation followed by Kirby-Bauer or automated MIC testing.",
            estimatedDelivery = "48 - 72 Hours",
            relatedAliases = listOf("Urine Culture & Antibiogram", "Urine C&S"),
            requiredCapabilities = listOf("Pathology", "Microbiology", "Diagnostic", "Urology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_urine_acr",
            name = "Urine Albumin-to-Creatinine Ratio (Microalbumin)",
            shortName = "Urine ACR",
            nameBn = "প্রস্রাবে মাইক্রোঅ্যালবুমিন (এসিআর)",
            category = "Clinical Pathology",
            subcategory = "Diabetic Nephropathy Screening",
            description = "Detects tiny amounts of albumin leaking into urine before routine dipsticks turn positive.",
            whatItDetects = "Early diabetic nephropathy, hypertensive kidney damage, and early glomerular disease.",
            preparationInstructions = "Avoid intense exercise for 24 hours; collect first-morning spot urine sample.",
            sampleType = "Spot Morning Urine",
            normalReferenceRange = "Normal: <30 mg/g creatinine | Moderately increased (microalbuminuria): 30–300 mg/g | Severely increased: >300 mg/g",
            procedure = "Spot urine is simultaneously assayed for immunoturbidimetric albumin and enzymatic creatinine.",
            estimatedDelivery = "6 - 12 Hours",
            relatedAliases = listOf("Spot Urine Microalbumin", "Urine Protein/Creatinine Ratio"),
            requiredCapabilities = listOf("Pathology", "Nephrology", "Endocrinology"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_stool",
            name = "Stool Routine & Occult Blood Test",
            shortName = "Stool R/E & FOBT",
            nameBn = "স্টুল আর/ই ও অকাল্ট ব্লাড",
            category = "Clinical Pathology",
            subcategory = "GI Parasitology & Bleeding",
            description = "Microscopic search for ova, cysts, parasites, pus/blood cells, plus immunochemical occult blood screening.",
            whatItDetects = "Amoebiasis, giardiasis, helminth worms, infectious dysentery, and hidden gastrointestinal bleeding.",
            preparationInstructions = "Collect fresh stool in a clean dry container free from urine contamination; avoid NSAIDs/iron if guaiac test used.",
            sampleType = "Fresh Stool",
            normalReferenceRange = "No ova, cysts, or trophozoites seen; Occult blood: Negative; Mucus/Pus cells: Absent or rare",
            procedure = "Saline and iodine wet-mount microscopy combined with fecal immunochemical hemoglobin cassette testing.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("Stool RME", "FIT / FOBT", "Ova & Parasites"),
            requiredCapabilities = listOf("Pathology", "Diagnostic", "Gastroenterology"),
            centerPrices = commonDiagnosticCenters
        ),

        // ==================== 5. MICROBIOLOGY, SEROLOGY & AUTOIMMUNE ====================
        DiagnosticTest(
            id = "test_dengue",
            name = "Dengue NS1 Antigen & IgM/IgG Antibody",
            shortName = "Dengue NS1 / Ab",
            nameBn = "ডেঙ্গু এনএস১ অ্যান্টিজেন ও অ্যান্টিবডি",
            category = "Microbiology & Serology",
            subcategory = "Vector-Borne Viral Panel",
            description = "Detects dengue viral NS1 protein during early fever (Days 1–5) and IgM/IgG antibodies in later days.",
            whatItDetects = "Acute primary or secondary Dengue fever infection.",
            preparationInstructions = "No fasting required; mention exact day of fever onset for test selection.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Negative / Non-Reactive",
            procedure = "Serum is tested by rapid immunochromatographic assay or ELISA microplate reader.",
            estimatedDelivery = "2 - 4 Hours",
            relatedAliases = listOf("Dengue Duo", "Dengue Antigen Test"),
            requiredCapabilities = listOf("Pathology", "Diagnostic", "Medicine", "Emergency"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_crp",
            name = "C-Reactive Protein (Quantitative / hs-CRP)",
            shortName = "CRP / hs-CRP",
            nameBn = "সি-রিঅ্যাক্টিভ প্রোটিন (সিআরপি)",
            category = "Immunology & Serology",
            subcategory = "Acute Phase Reactants",
            description = "Quantifies acute-phase hepatic protein produced rapidly in response to infection or vascular inflammation.",
            whatItDetects = "Acute bacterial infection, sepsis monitoring, post-op infection, and cardiovascular risk (hs-CRP).",
            preparationInstructions = "No fasting required.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Standard CRP: <5.0 mg/L (or <0.5 mg/dL) | hs-CRP CV Risk: Low <1.0, Average 1.0–3.0, High >3.0 mg/L",
            procedure = "Serum undergoes latex-enhanced immunoturbidimetric or nephelometric quantification.",
            estimatedDelivery = "4 - 6 Hours",
            relatedAliases = listOf("High-Sensitivity CRP", "Quantitative CRP"),
            requiredCapabilities = listOf("Pathology", "Diagnostic", "Medicine", "Emergency"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_pct",
            name = "Serum Procalcitonin",
            shortName = "PCT",
            nameBn = "সিরাম প্রোক্যালসিটোনিন",
            category = "Microbiology & Serology",
            subcategory = "Sepsis Biomarkers",
            description = "Specific biomarker distinguishing systemic bacterial sepsis from viral illness or non-infectious inflammation.",
            whatItDetects = "Bacterial sepsis, septic shock severity, and antibiotic stewardship response in ICU patients.",
            preparationInstructions = "No preparation required.",
            sampleType = "Venous Serum / Plasma",
            normalReferenceRange = "Normal: <0.10 ng/mL | Possible local bacterial infection: 0.25–0.50 ng/mL | High sepsis risk: >2.0 ng/mL",
            procedure = "Venous serum is run on an automated fluorescent or chemiluminescent analyzer.",
            estimatedDelivery = "3 - 6 Hours",
            relatedAliases = listOf("Sepsis Marker PCT"),
            requiredCapabilities = listOf("ICU", "Emergency", "Pathology"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_blood_cs",
            name = "Blood Culture & Sensitivity (Automated BacT/ALERT)",
            shortName = "Blood C/S",
            nameBn = "ব্লাড কালচার ও সেন্সিটিভিটি",
            category = "Microbiology & Serology",
            subcategory = "Bacteremia & Enteric Fever",
            description = "Detects bacteria or fungi circulating in blood (including Salmonella typhi) and identifies antibiotic sensitivity.",
            whatItDetects = "Typhoid/enteric fever, bacteremia, endocarditis, and neonatal or ICU sepsis.",
            preparationInstructions = "Collect strictly using sterile skin prep BEFORE starting antibiotics, ideally during fever spike.",
            sampleType = "Venous Blood (Aerobic/Anaerobic Culture Bottle)",
            normalReferenceRange = "No bacterial or fungal growth after 5 days of incubation.",
            procedure = "8–10 mL (1–3 mL pediatric) blood is inoculated into BacT/ALERT or BACTEC broth monitored continuously for microbial CO2 production.",
            estimatedDelivery = "48 - 120 Hours (Interim at 24h)",
            relatedAliases = listOf("Blood Culture", "BACTEC Blood Culture"),
            requiredCapabilities = listOf("Microbiology", "Pathology", "Medicine", "ICU"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_hbsag",
            name = "Hepatitis B Surface Antigen (ELISA / CMIA)",
            shortName = "HBsAg",
            nameBn = "হেপাটাইটিস বি সারফেস অ্যান্টিজেন",
            category = "Microbiology & Serology",
            subcategory = "Viral Hepatitis",
            description = "Detects surface antigen protein of Hepatitis B virus indicating active acute or chronic infection.",
            whatItDetects = "Acute or chronic Hepatitis B virus (HBV) infection and carrier status.",
            preparationInstructions = "No fasting required.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Non-Reactive / Negative (Index <1.0)",
            procedure = "Venous serum is tested by fourth-generation ELISA or chemiluminescent microparticle immunoassay (CMIA).",
            estimatedDelivery = "4 - 8 Hours",
            relatedAliases = listOf("Hepatitis B Screening", "Australia Antigen"),
            requiredCapabilities = listOf("Pathology", "Blood Bank", "Gastroenterology", "Diagnostic"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_anti_hcv",
            name = "Hepatitis C Antibody (Anti-HCV)",
            shortName = "Anti-HCV",
            nameBn = "হেপাটাইটিস সি অ্যান্টিবডি",
            category = "Microbiology & Serology",
            subcategory = "Viral Hepatitis",
            description = "Screens for antibodies produced against Hepatitis C virus.",
            whatItDetects = "Current or past Hepatitis C virus exposure (positive results confirmed via HCV RNA PCR).",
            preparationInstructions = "No fasting required.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Non-Reactive / Negative",
            procedure = "Serum analyzed by third-generation ELISA or CMIA.",
            estimatedDelivery = "6 - 12 Hours",
            relatedAliases = listOf("HCV Antibody", "Hepatitis C Screening"),
            requiredCapabilities = listOf("Pathology", "Blood Bank", "Gastroenterology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_hiv",
            name = "HIV 1 & 2 Antigen/Antibody Combo",
            shortName = "HIV I & II",
            nameBn = "এইচআইভি ১ ও ২ স্ক্রিনিং",
            category = "Microbiology & Serology",
            subcategory = "Retroviral Screening",
            description = "Fourth-generation assay detecting both HIV p24 antigen and HIV-1/HIV-2 antibodies.",
            whatItDetects = "Early and established HIV-1 or HIV-2 infection.",
            preparationInstructions = "No preparation required; confidential counseling recommended.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Non-Reactive / Negative",
            procedure = "Venous serum tested via 4th-gen CMIA/ELISA combo assay.",
            estimatedDelivery = "6 - 12 Hours",
            relatedAliases = listOf("HIV Combo", "HIV ELISA"),
            requiredCapabilities = listOf("Pathology", "Blood Bank", "Medicine"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_genexpert_tb",
            name = "GeneXpert MTB/RIF (TB PCR & Rifampicin Resistance)",
            shortName = "GeneXpert TB",
            nameBn = "জিনএক্সপার্ট টিবি (যক্ষ্মা পিসিআর)",
            category = "Microbiology & Serology",
            subcategory = "Mycobacteriology",
            description = "Rapid cartridge-based nucleic acid amplification test (CBNAAT) for Mycobacterium tuberculosis.",
            whatItDetects = "Pulmonary or extrapulmonary Tuberculosis DNA and rpoB gene Rifampicin drug resistance.",
            preparationInstructions = "Rinse mouth with plain water before coughing up deep early-morning sputum.",
            sampleType = "Sputum / BAL / Lymph Node Aspirate",
            normalReferenceRange = "MTB Not Detected",
            procedure = "Sputum is liquefied with sample reagent and processed inside an automated GeneXpert real-time PCR cartridge for ~2 hours.",
            estimatedDelivery = "6 - 24 Hours",
            relatedAliases = listOf("CBNAAT", "MTB/RIF Xpert", "Sputum TB PCR"),
            requiredCapabilities = listOf("Pulmonology", "Microbiology", "Medicine"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_torch",
            name = "TORCH Panel (Toxoplasma, Rubella, CMV, HSV IgG/IgM)",
            shortName = "TORCH Panel",
            nameBn = "টর্চ প্যানেল (গর্ভাবস্থা সংক্রমণ স্ক্রিন)",
            category = "Microbiology & Serology",
            subcategory = "Maternal-Fetal Infections",
            description = "Screens pregnant patients or newborns for congenital viral/parasitic infections.",
            whatItDetects = "Toxoplasmosis, Rubella, Cytomegalovirus (CMV), and Herpes Simplex Virus (HSV 1 & 2) immunity or acute infection.",
            preparationInstructions = "No fasting required.",
            sampleType = "Venous Serum",
            normalReferenceRange = "IgM: Negative (<0.8 Index) for all pathogens; IgG indicates past exposure or vaccine immunity",
            procedure = "Serum tested by multiplex ELISA or chemiluminescence.",
            estimatedDelivery = "24 Hours",
            relatedAliases = listOf("Pregnancy Infection Screen"),
            requiredCapabilities = listOf("Gynecology", "Pathology", "Pediatrics"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_autoimmune",
            name = "ANA (IFA), Rheumatoid Factor & Anti-CCP Panel",
            shortName = "ANA / RF / Anti-CCP",
            nameBn = "এএনএ, আরএফ ও অ্যান্টি-সিসিপি",
            category = "Immunology & Serology",
            subcategory = "Autoimmune & Rheumatology",
            description = "Detects antinuclear and anti-citrullinated antibodies associated with systemic connective tissue diseases.",
            whatItDetects = "Systemic Lupus Erythematosus (SLE), Rheumatoid Arthritis (RA), Sjogren syndrome, and autoimmune overlap.",
            preparationInstructions = "No fasting required.",
            sampleType = "Venous Serum",
            normalReferenceRange = "ANA (HEp-2 IFA): Negative (<1:80 titer) | RF: <14 IU/mL | Anti-CCP: <20 U/mL",
            procedure = "Serum undergoes indirect immunofluorescence on HEp-2 cells and quantitative ELISA/CMIA.",
            estimatedDelivery = "24 - 48 Hours",
            relatedAliases = listOf("Arthritis Panel", "Connective Tissue Screen"),
            requiredCapabilities = listOf("Pathology", "Medicine", "Orthopedics"),
            centerPrices = tertiaryAndMajorCenters
        ),

        // ==================== 6. CARDIOLOGY ====================
        DiagnosticTest(
            id = "test_ecg",
            name = "12-Lead Electrocardiogram",
            shortName = "ECG",
            nameBn = "ইসিজি (১২-লিড ইলেক্ট্রোকার্ডিওগ্রাম)",
            category = "Cardiology",
            subcategory = "Electrophysiology",
            description = "Records electrical impulses across 12 cardiac leads to map heart rate, rhythm, and myocardial ischemia.",
            whatItDetects = "Heart attack (STEMI/NSTEMI), atrial fibrillation, arrhythmias, conduction blocks, and ventricular hypertrophy.",
            preparationInstructions = "Avoid heavy creams/oil on chest skin; avoid caffeine or smoking 1 hour before test.",
            sampleType = "Non-invasive Surface Leads",
            normalReferenceRange = "Sinus rhythm 60–100 bpm | PR interval: 120–200 ms | QRS: <120 ms | QTc: Male <450 ms, Female <460 ms",
            procedure = "Patient lies flat while 10 skin electrodes (6 chest, 4 limb) record cardiac vectors for ~1 minute.",
            estimatedDelivery = "15 - 30 Minutes",
            relatedAliases = listOf("EKG", "Resting 12-Lead ECG"),
            requiredCapabilities = listOf("Cardiology", "Emergency", "Diagnostic", "Medicine"),
            centerPrices = cardiacCenters
        ),
        DiagnosticTest(
            id = "test_echo",
            name = "Color Doppler Echocardiogram",
            shortName = "2D Echo",
            nameBn = "কালার ডপলার ইকোকার্ডিওগ্রাম",
            category = "Cardiology",
            subcategory = "Cardiac Ultrasound",
            description = "Dynamic ultrasound imaging of heart chambers, valve leaflets, pericardium, and blood flow gradients.",
            whatItDetects = "Left ventricular ejection fraction (LVEF), heart failure, valve stenosis/regurgitation, congenital defects, and pericardial effusion.",
            preparationInstructions = "No fasting required. Bring previous ECG and cardiology prescription.",
            sampleType = "Transthoracic Cardiac Ultrasound",
            normalReferenceRange = "LVEF: 55%–70% | PASP: <35 mmHg | Normal valve morphology and chamber dimensions",
            procedure = "A cardiologist or echo sonographer applies acoustic gel to the left chest and captures B-mode, M-mode, and Doppler views over 20–30 minutes.",
            estimatedDelivery = "1 - 2 Hours",
            relatedAliases = listOf("Transthoracic Echocardiogram", "TTE", "Color Echo"),
            requiredCapabilities = listOf("Cardiology", "Heart"),
            centerPrices = cardiacCenters
        ),
        DiagnosticTest(
            id = "test_ett",
            name = "Exercise Tolerance Test (Treadmill Stress Test)",
            shortName = "ETT / TMT",
            nameBn = "ইটিটি (ট্রেডমিল স্ট্রেস টেস্ট)",
            category = "Cardiology",
            subcategory = "Stress Testing",
            description = "Monitors continuous 12-lead ECG and blood pressure while walking on a graded treadmill protocol (Bruce).",
            whatItDetects = "Exercise-induced myocardial ischemia, occult coronary artery disease, and chronotropic competence.",
            preparationInstructions = "Light meal 2–3 hours prior; wear comfortable running shoes; ask cardiologist whether to hold beta-blockers 24h before.",
            sampleType = "Dynamic Treadmill ECG",
            normalReferenceRange = "Target heart rate reached without angina, ≥1 mm ST-segment depression, or blood pressure drop.",
            procedure = "Resting baseline ECG/BP is taken, then patient walks through progressive 3-minute speed/incline stages under physician supervision.",
            estimatedDelivery = "1 - 2 Hours",
            relatedAliases = listOf("TMT", "Cardiac Stress Test", "Bruce Protocol ETT"),
            requiredCapabilities = listOf("Cardiology", "Heart"),
            centerPrices = cardiacCenters
        ),
        DiagnosticTest(
            id = "test_holter",
            name = "24-Hour Holter ECG Monitoring",
            shortName = "Holter ECG",
            nameBn = "২৪ ঘণ্টার হল্টার মনিটরিং",
            category = "Cardiology",
            subcategory = "Ambulatory Arrhythmia Monitoring",
            description = "Continuous portable 24–48 hour ECG recording during normal daily activities and sleep.",
            whatItDetects = "Paroxysmal atrial fibrillation, intermittent ventricular tachycardia, pauses, and palpitation/syncope correlation.",
            preparationInstructions = "Shower before lead placement; do not wet or remove the recorder during the 24-hour recording period.",
            sampleType = "Wearable Patch / Lead Recorder",
            normalReferenceRange = "Predominant sinus rhythm 50–100 bpm without sustained tachyarrhythmias or >2.0s pauses.",
            procedure = "Chest electrodes connect to a pocket-sized digital recorder worn for 24 hours; patient logs symptoms in a diary before returning the device.",
            estimatedDelivery = "24 Hours post-return",
            relatedAliases = listOf("Ambulatory ECG", "24h Holter"),
            requiredCapabilities = listOf("Cardiology", "Heart"),
            centerPrices = cardiacCenters
        ),
        DiagnosticTest(
            id = "test_troponin",
            name = "High-Sensitivity Cardiac Troponin-I & NT-proBNP",
            shortName = "hs-Trop I / BNP",
            nameBn = "ট্রপোনিন-আই ও এনটি-প্রোবিএনপি",
            category = "Cardiology",
            subcategory = "Cardiac Biomarkers",
            description = "Measures structural myocardial injury protein (Troponin-I) and ventricular wall-stretch peptide (NT-proBNP).",
            whatItDetects = "Acute myocardial infarction (heart attack), myocarditis, and acute vs chronic heart failure.",
            preparationInstructions = "Immediate draw; no fasting required.",
            sampleType = "Venous Plasma / Serum",
            normalReferenceRange = "hs-Troponin-I: Male <34 pg/mL, Female <16 pg/mL (assay-specific 99th percentile) | NT-proBNP <75y: <125 pg/mL",
            procedure = "Stat venous blood draw analyzed via high-sensitivity quantitative chemiluminescence; often repeated at 0h and 3h.",
            estimatedDelivery = "1 - 2 Hours (Stat)",
            relatedAliases = listOf("Troponin-I", "BNP", "Heart Attack Blood Test"),
            requiredCapabilities = listOf("Cardiology", "Emergency", "ICU", "Heart"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_ctca",
            name = "CT Coronary Angiography",
            shortName = "CTCA",
            nameBn = "সিটি করোনারি এনজিওগ্রাম",
            category = "Cardiology",
            subcategory = "Non-Invasive Coronary Imaging",
            description = "ECG-gated multidetector contrast CT imaging visualizing coronary arteries and calcium plaque.",
            whatItDetects = "Coronary artery stenosis, soft vs calcified atherosclerotic plaques, anomalous coronary vessels, and graft patency.",
            preparationInstructions = "4–6 hours fasting; carry recent Serum Creatinine report; avoid caffeine 12h prior; heart rate control (<65 bpm) often used.",
            sampleType = "Contrast Cardiac CT",
            normalReferenceRange = "Coronary Calcium Score (Agatston) = 0; widely patent LMS, LAD, LCX, RCA without ≥50% stenosis.",
            procedure = "IV cannula delivers iodinated contrast synced to ECG gating on a ≥64/128/256-slice CT scanner during brief breath-holds.",
            estimatedDelivery = "12 - 24 Hours",
            relatedAliases = listOf("CT Coronary Angiogram", "Coronary Calcium & CTA"),
            requiredCapabilities = listOf("Cardiology", "Radiology", "Heart"),
            centerPrices = advancedImagingCenters
        ),

        // ==================== 7. RADIOLOGY & DIAGNOSTIC IMAGING ====================
        DiagnosticTest(
            id = "test_xray",
            name = "Digital Chest X-Ray (PA / Lateral View)",
            shortName = "CXR",
            nameBn = "ডিজিটাল চেস্ট এক্স-রে",
            category = "Radiology & Imaging",
            subcategory = "Plain Radiography",
            description = "High-resolution digital radiograph of the thoracic cage, lungs, mediastinum, and heart silhouette.",
            whatItDetects = "Pneumonia, pulmonary tuberculosis, pleural effusion, pneumothorax, cardiomegaly, and rib fractures.",
            preparationInstructions = "Remove metallic jewelry, bras with underwire, and buttons; inform technologist immediately if pregnant.",
            sampleType = "Digital X-Ray Imaging",
            normalReferenceRange = "Clear lung fields, sharp costophrenic angles, cardiothoracic ratio <0.50, intact bony thorax.",
            procedure = "Patient stands facing the digital detector taking a deep breath and holding for <1 second during low-dose exposure.",
            estimatedDelivery = "1 - 2 Hours",
            relatedAliases = listOf("Chest Radiograph", "X-Ray Chest PA"),
            requiredCapabilities = listOf("Radiology", "Diagnostic", "Emergency", "Pulmonology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_usg",
            name = "Whole Abdomen Ultrasonography",
            shortName = "USG Whole Abdomen",
            nameBn = "পুরো পেটের আল্ট্রাসনোগ্রাফি (ইউএসজি)",
            category = "Radiology & Imaging",
            subcategory = "Ultrasound",
            description = "Radiation-free high-frequency sound-wave scan of liver, gallbladder, biliary tree, pancreas, spleen, kidneys, and bladder.",
            whatItDetects = "Fatty liver, gallstones, acute cholecystitis, appendicitis, kidney stones, hydronephrosis, prostate enlargement, and ascites.",
            preparationInstructions = "Fast 6–8 hours prior to scan; drink 3–4 glasses of water 1 hour before and hold urine for a full bladder.",
            sampleType = "Abdominal Ultrasound",
            normalReferenceRange = "Normal hepatic echotexture, thin-walled calculus-free gallbladder, unobstructed kidneys, normal prostate/uterus.",
            procedure = "Warm water-based gel is applied over the abdomen and scanned with a curved ultrasound transducer for 15–20 minutes.",
            estimatedDelivery = "1 - 3 Hours",
            relatedAliases = listOf("Abdominal Ultrasound", "USG KUB & Hepatobiliary"),
            requiredCapabilities = listOf("Radiology", "Diagnostic", "Gastroenterology", "Gynecology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_doppler",
            name = "Color Doppler Vascular Ultrasound (Limbs / Carotid)",
            shortName = "Color Doppler",
            nameBn = "কালার ডপলার আল্ট্রাসাউন্ড",
            category = "Radiology & Imaging",
            subcategory = "Vascular Ultrasound",
            description = "Assesses arterial and venous blood flow direction, velocity, and vessel compressibility.",
            whatItDetects = "Deep vein thrombosis (DVT), peripheral arterial disease, varicose venous insufficiency, and carotid artery stroke stenosis.",
            preparationInstructions = "No special preparation for limbs; loose clothing recommended.",
            sampleType = "Duplex Vascular Ultrasound",
            normalReferenceRange = "Patent vessels with phasic compressible venous flow and triphasic arterial waveforms without plaque stenosis.",
            procedure = "Targeted high-frequency linear probe assesses color and spectral Doppler waveforms over 25–35 minutes.",
            estimatedDelivery = "2 - 4 Hours",
            relatedAliases = listOf("Duplex Doppler", "DVT Scan", "Carotid Doppler"),
            requiredCapabilities = listOf("Radiology", "Cardiology", "Neurology", "Surgery"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_ct",
            name = "Computed Tomography (CT Brain / Chest / Abdomen)",
            shortName = "CT Scan",
            nameBn = "সিটি স্ক্যান (ব্রেইন / বুক / পেট)",
            category = "Radiology & Imaging",
            subcategory = "Computed Tomography",
            description = "Cross-sectional X-ray tomography reconstructed into multi-planar 3D slices.",
            whatItDetects = "Acute stroke hemorrhage, skull trauma, abdominal tumors, kidney stones (NCCT KUB), and pulmonary embolism.",
            preparationInstructions = "4 hours fasting if IV contrast ordered; carry recent Serum Creatinine & eGFR; report contrast allergy or metformin use.",
            sampleType = "Helical Multi-Slice CT",
            normalReferenceRange = "Age-appropriate parenchyma without acute hemorrhage, mass lesion, herniation, or free fluid.",
            procedure = "Patient lies supine on a motorized table gliding through a wide ring gantry over 5–15 minutes.",
            estimatedDelivery = "6 - 24 Hours (Emergency 1h)",
            relatedAliases = listOf("NCCT", "CECT", "CAT Scan"),
            requiredCapabilities = listOf("Radiology", "Emergency", "Neurology"),
            centerPrices = advancedImagingCenters
        ),
        DiagnosticTest(
            id = "test_hrct_chest",
            name = "High-Resolution CT Chest",
            shortName = "HRCT Chest",
            nameBn = "এইচআরসিটি বুক (ফুসফুস সিটি)",
            category = "Radiology & Imaging",
            subcategory = "Thoracic CT",
            description = "Thin-slice (1 mm) lung CT visualizing fine interstitial parenchyma and small airways without contrast.",
            whatItDetects = "Interstitial lung disease (ILD), bronchiectasis, COVID/atypical viral pneumonitis, and occupational lung fibrosis.",
            preparationInstructions = "No fasting or contrast typically required; remove chest metal items.",
            sampleType = "Non-Contrast Thin-Slice CT",
            normalReferenceRange = "Clear pulmonary parenchyma without ground-glass opacities, honeycombing, consolidation, or bronchiectasis.",
            procedure = "Rapid inspiratory/expiratory breath-hold spiral scan taking ~5 minutes.",
            estimatedDelivery = "6 - 12 Hours",
            relatedAliases = listOf("Thin-Slice Lung CT", "Chest HRCT"),
            requiredCapabilities = listOf("Radiology", "Pulmonology"),
            centerPrices = advancedImagingCenters
        ),
        DiagnosticTest(
            id = "test_mri",
            name = "Magnetic Resonance Imaging (1.5T / 3T MRI)",
            shortName = "MRI (Brain / Spine / Joint)",
            nameBn = "এমআরআই স্ক্যান (১.৫/৩-টেসলা)",
            category = "Radiology & Imaging",
            subcategory = "Magnetic Resonance",
            description = "Radiation-free high-field magnetic imaging offering superior soft-tissue, neural, spinal disc, and cartilage contrast.",
            whatItDetects = "Ischemic stroke, brain tumors, lumbar/cervical disc prolapse, ACL/meniscal knee tears, and multiple sclerosis.",
            preparationInstructions = "Remove all metal/watches/cards; inform staff of pacemakers, aneurysm clips, cochlear implants, or claustrophobia; Creatinine needed if gadolinium contrast ordered.",
            sampleType = "1.5T / 3T MRI Imaging",
            normalReferenceRange = "Normal signal intensity across cortex, brainstem, intervertebral discs, cord, or articular cartilage.",
            procedure = "Patient lies still inside an illuminated tunnel wearing ear protection while magnetic pulse sequences run for 20–45 minutes.",
            estimatedDelivery = "12 - 24 Hours",
            relatedAliases = listOf("3T MRI", "MRI Brain", "MRI Lumbosacral Spine"),
            requiredCapabilities = listOf("Radiology", "Neurology", "Orthopedics"),
            centerPrices = advancedImagingCenters
        ),
        DiagnosticTest(
            id = "test_mra",
            name = "MR Angiography / Venography",
            shortName = "MRA / MRV",
            nameBn = "এমআর এনজিওগ্রাফি / ভেনোগ্রাফি",
            category = "Radiology & Imaging",
            subcategory = "Vascular MRI",
            description = "Radiation-free magnetic resonance blood-vessel mapping of the cerebral Circle of Willis or dural venous sinuses.",
            whatItDetects = "Intracranial aneurysms, arterial stenosis, arteriovenous malformations (AVM), and cerebral venous sinus thrombosis.",
            preparationInstructions = "Strict metal safety screening; usually done without contrast (Time-of-Flight) unless neck/arch vessels included.",
            sampleType = "Vascular MRI",
            normalReferenceRange = "Normal caliber, symmetric branching of ICA, MCA, ACA, PCA, basilar artery, and patent dural sinuses.",
            procedure = "Specialized flow-sensitive MRI sequences acquired over 25–35 minutes.",
            estimatedDelivery = "12 - 24 Hours",
            relatedAliases = listOf("Brain MRA", "TOF MRA"),
            requiredCapabilities = listOf("Radiology", "Neurology"),
            centerPrices = advancedImagingCenters
        ),
        DiagnosticTest(
            id = "test_mammogram",
            name = "Digital Bilateral Mammography",
            shortName = "Mammogram",
            nameBn = "ডিজিটাল ম্যামোগ্রাম",
            category = "Radiology & Imaging",
            subcategory = "Breast Screening",
            description = "Low-dose dedicated breast radiography (CC and MLO views) for early microcalcification and lump detection.",
            whatItDetects = "Early subclinical breast carcinoma, clustered microcalcifications, architectural distortion, and benign cysts/fibroadenoma.",
            preparationInstructions = "Schedule 1 week after menstruation starts; do not apply deodorant, talcum powder, or lotion on underarms/breasts on scan day.",
            sampleType = "Digital Mammography",
            normalReferenceRange = "BI-RADS 1 (Negative) or BI-RADS 2 (Benign finding).",
            procedure = "Each breast is gently compressed between smooth plates for a few seconds to obtain two standard views (~15 minutes total).",
            estimatedDelivery = "12 - 24 Hours",
            relatedAliases = listOf("3D Tomosynthesis", "Breast Screening X-Ray"),
            requiredCapabilities = listOf("Radiology", "Oncology", "Gynecology"),
            centerPrices = advancedImagingCenters
        ),
        DiagnosticTest(
            id = "test_dexa",
            name = "Bone Mineral Densitometry (DEXA Scan)",
            shortName = "DEXA Scan",
            nameBn = "হাড়ের ঘনত্ব পরীক্ষা (ডেক্সা)",
            category = "Radiology & Imaging",
            subcategory = "Bone Densitometry",
            description = "Dual-energy X-ray absorptiometry quantifying bone mineral density at lumbar spine and hip.",
            whatItDetects = "Osteopenia, osteoporosis, and 10-year osteoporotic fracture risk.",
            preparationInstructions = "Pause calcium supplements 24 hours before scan; wear metal-free clothing; avoid recent barium/contrast studies for 7 days.",
            sampleType = "Dual-Energy X-Ray",
            normalReferenceRange = "WHO T-score: Normal ≥ -1.0 | Osteopenia: -1.0 to -2.5 | Osteoporosis: ≤ -2.5",
            procedure = "Patient lies flat on an open padded table for 10–15 minutes while a low-dose scanner arm passes overhead.",
            estimatedDelivery = "4 - 12 Hours",
            relatedAliases = listOf("BMD Scan", "DXA Bone Density"),
            requiredCapabilities = listOf("Radiology", "Orthopedics", "Endocrinology"),
            centerPrices = advancedImagingCenters
        ),
        DiagnosticTest(
            id = "test_pet_ct",
            name = "Whole-Body 18F-FDG PET-CT Scan",
            shortName = "PET-CT",
            nameBn = "হোল-বডি পেট-সিটি স্ক্যান",
            category = "Radiology & Imaging",
            subcategory = "Nuclear & Molecular Imaging",
            description = "Combines metabolic radiotracer activity (18F-FDG) with anatomical CT across the body for oncology staging.",
            whatItDetects = "Cancer primary/metastasis staging, lymphoma treatment response, cancer recurrence, and fever of unknown origin.",
            preparationInstructions = "Strict 6 hours fasting; low-carb diet 24h prior; blood glucose <150–200 mg/dL required before tracer injection; avoid exercise 24h prior.",
            sampleType = "Nuclear Radiotracer PET-CT",
            normalReferenceRange = "Physiological tracer distribution (brain, heart, urinary excretion) without hypermetabolic FDG-avid lesions.",
            procedure = "IV 18F-FDG radiotracer is injected followed by 60 minutes quiet uptake rest, then a 25–30 minute head-to-thigh PET-CT scan.",
            estimatedDelivery = "24 - 48 Hours",
            relatedAliases = listOf("FDG PET Scan", "Oncology PET-CT"),
            requiredCapabilities = listOf("Oncology", "Nuclear Medicine"),
            centerPrices = listOf(
                CenterTestPrice("hosp_evercare", "Evercare Hospital Dhaka", "Bashundhara R/A", "Dhaka", 0, 0, false, 48),
                CenterTestPrice("hosp_united", "United Hospital Limited", "Gulshan", "Dhaka", 0, 0, false, 48),
                CenterTestPrice("hosp_square", "Square Hospitals Ltd.", "Panthapath", "Dhaka", 0, 0, false, 48)
            )
        ),

        // ==================== 8. ONCOLOGY & TUMOR MARKERS ====================
        DiagnosticTest(
            id = "test_psa",
            name = "Prostate-Specific Antigen (Total & Free PSA)",
            shortName = "PSA",
            nameBn = "পিএসএ (প্রোস্টেট স্ক্রিনিং)",
            category = "Oncology & Tumor Markers",
            subcategory = "Urological Markers",
            description = "Measures prostate gland protein levels in male blood to evaluate benign enlargement vs malignancy.",
            whatItDetects = "Prostate cancer screening/monitoring, benign prostatic hyperplasia (BPH), and acute prostatitis.",
            preparationInstructions = "Avoid ejaculation, cycling, and digital rectal exam/prostate massage for 48 hours prior to blood draw.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Age-adjusted Total PSA: 40–49y <2.5, 50–59y <3.5, 60–69y <4.5, 70–79y <6.5 ng/mL | Free/Total >25% favors benign",
            procedure = "Venous serum tested via quantitative chemiluminescent immunoassay.",
            estimatedDelivery = "6 - 12 Hours",
            relatedAliases = listOf("Total PSA", "Free/Total PSA Ratio"),
            requiredCapabilities = listOf("Urology", "Oncology", "Pathology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_ca125",
            name = "Cancer Antigen 125",
            shortName = "CA-125",
            nameBn = "সিএ-১২৫ টিউমার মার্কার",
            category = "Oncology & Tumor Markers",
            subcategory = "Gynecologic Oncology",
            description = "Glycoprotein serum biomarker mainly used to evaluate ovarian masses and track epithelial ovarian cancer treatment.",
            whatItDetects = "Ovarian malignancy treatment response/recurrence (can also rise in endometriosis, fibroids, or pelvic infection).",
            preparationInstructions = "Avoid testing during active menstruation if possible to reduce benign elevation.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Adult Female: <35 U/mL",
            procedure = "Venous serum analyzed via ECLIA/CMIA immunoassay.",
            estimatedDelivery = "12 - 24 Hours",
            relatedAliases = listOf("Ovarian Tumor Marker CA-125"),
            requiredCapabilities = listOf("Oncology", "Gynecology", "Pathology"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_cea_ca199",
            name = "Carcinoembryonic Antigen & CA 19-9",
            shortName = "CEA & CA 19-9",
            nameBn = "সিইএ ও সিএ ১৯-৯",
            category = "Oncology & Tumor Markers",
            subcategory = "GI & Hepatobiliary Oncology",
            description = "Measures gastrointestinal epithelial tumor markers used in staging and post-treatment surveillance.",
            whatItDetects = "Colorectal, pancreatic, gastric, and biliary tract malignancy monitoring.",
            preparationInstructions = "No fasting required; inform lab about smoking history (smokers have slightly higher baseline CEA).",
            sampleType = "Venous Serum",
            normalReferenceRange = "CEA: Non-smoker <3.0 ng/mL, Smoker <5.0 ng/mL | CA 19-9: <37 U/mL",
            procedure = "Serum measured on automated immunoassay platform.",
            estimatedDelivery = "12 - 24 Hours",
            relatedAliases = listOf("GI Tumor Markers", "Pancreatic/Colon Markers"),
            requiredCapabilities = listOf("Oncology", "Gastroenterology", "Pathology"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_afp",
            name = "Alpha-Fetoprotein",
            shortName = "AFP",
            nameBn = "আলফা-ফিটোপ্রোটিন (এএফপি)",
            category = "Oncology & Tumor Markers",
            subcategory = "Hepatic & Germ-Cell Oncology",
            description = "Quantifies oncofetal serum protein elevated in liver cancer and certain germ-cell tumors.",
            whatItDetects = "Hepatocellular carcinoma (HCC) surveillance in cirrhosis/HBV, testicular/ovarian yolk-sac tumors, and prenatal screening.",
            preparationInstructions = "No fasting required.",
            sampleType = "Venous Serum",
            normalReferenceRange = "Non-pregnant Adult: <10 ng/mL (or <8.5 IU/mL); higher physiologically in pregnancy and newborns.",
            procedure = "Venous serum tested via chemiluminescence.",
            estimatedDelivery = "12 - 24 Hours",
            relatedAliases = listOf("Serum AFP", "Liver Tumor Marker"),
            requiredCapabilities = listOf("Oncology", "Gastroenterology", "Pathology"),
            centerPrices = tertiaryAndMajorCenters
        ),

        // ==================== 9. NEUROLOGY, PULMONOLOGY & GASTROENTEROLOGY PROCEDURES ====================
        DiagnosticTest(
            id = "test_eeg",
            name = "Electroencephalogram (Routine / Sleep-Deprived)",
            shortName = "EEG",
            nameBn = "ইইজি (মস্তিষ্কের বৈদ্যুতিক পরীক্ষা)",
            category = "Neurology",
            subcategory = "Neurophysiology",
            description = "Records brain cortical electrical wave patterns using scalp electrodes.",
            whatItDetects = "Epilepsy seizure foci, encephalopathy, absence seizures, and brain wave slowing.",
            preparationInstructions = "Wash hair cleanly the night before without oil, conditioner, or hair spray; follow sleep-deprivation timing if ordered.",
            sampleType = "Scalp Surface Electrodes",
            normalReferenceRange = "Symmetric posterior dominant alpha rhythm (8–13 Hz) when relaxed with eyes closed; no epileptiform spikes.",
            procedure = "Technician attaches 21–25 paste electrodes to the scalp and records resting, hyperventilation, and photic stimulation segments for 30–45 minutes.",
            estimatedDelivery = "24 Hours",
            relatedAliases = listOf("Brain Wave Test", "Video EEG"),
            requiredCapabilities = listOf("Neurology"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_ncs_emg",
            name = "Nerve Conduction Study & Electromyography",
            shortName = "NCS / EMG",
            nameBn = "নার্ভ কন্ডাকশন ও ইএমজি",
            category = "Neurology",
            subcategory = "Neuromuscular Diagnostics",
            description = "Measures electrical nerve impulse speed (NCS) and muscle fiber motor-unit potentials (EMG).",
            whatItDetects = "Diabetic peripheral neuropathy, carpal tunnel syndrome, Guillain-Barre syndrome (GBS), radiculopathy, and myopathy.",
            preparationInstructions = "Avoid skin lotions or oils on limbs; inform physician of blood thinners or pacemaker before needle EMG.",
            sampleType = "Surface & Fine Needle Electrodes",
            normalReferenceRange = "Median/Ulnar motor conduction >50 m/s, Tibial/Peroneal >40 m/s; normal distal latencies and amplitudes.",
            procedure = "Small brief electrical pulses stimulate peripheral nerves (NCS), followed if needed by thin concentric needle muscle sampling (EMG) for 30–60 minutes.",
            estimatedDelivery = "24 Hours",
            relatedAliases = listOf("NCV Study", "Electromyogram"),
            requiredCapabilities = listOf("Neurology", "Orthopedics"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_pft",
            name = "Pulmonary Function Test (Spirometry with Bronchodilator)",
            shortName = "Spirometry / PFT",
            nameBn = "স্পাইরোমেট্রি (ফুসফুস কার্যকারিতা পরীক্ষা)",
            category = "Pulmonology",
            subcategory = "Respiratory Physiology",
            description = "Measures lung volumes and forced expiratory airflow rates (FEV1, FVC, FEV1/FVC) before and after bronchodilator.",
            whatItDetects = "Bronchial asthma (reversibility), COPD (fixed obstruction), and restrictive lung fibrosis.",
            preparationInstructions = "Hold short-acting inhalers 6 hours and long-acting bronchodilators 12–24 hours if instructed; avoid heavy meals/smoking 2h prior.",
            sampleType = "Forced Expiratory Breath Test",
            normalReferenceRange = "FEV1 & FVC ≥80% predicted for age/height/sex; FEV1/FVC ratio ≥0.70 (or >LLN); post-bronchodilator <12% change.",
            procedure = "Seated wearing a nose clip, patient inhales maximally and blasts air forcefully into a sterile spirometer mouthpiece until lungs empty.",
            estimatedDelivery = "1 - 2 Hours",
            relatedAliases = listOf("Lung Function Test", "Spirometry"),
            requiredCapabilities = listOf("Pulmonology", "Medicine"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_sleep_study",
            name = "Overnight Polysomnography (Sleep Study)",
            shortName = "PSG / Sleep Study",
            nameBn = "স্লিপ স্টাডি (পলিসমনোগ্রাফি)",
            category = "Pulmonology",
            subcategory = "Sleep Medicine",
            description = "Overnight multichannel monitoring of brain sleep stages, airflow, snoring, chest effort, ECG, and oxygen saturation.",
            whatItDetects = "Obstructive sleep apnea (OSA), central sleep apnea, nocturnal hypoxia, and periodic limb movement disorder.",
            preparationInstructions = "Avoid naps, caffeine, and alcohol on test day; bring comfortable sleepwear and regular evening medications.",
            sampleType = "Overnight Multi-Sensor Monitoring",
            normalReferenceRange = "Apnea-Hypopnea Index (AHI): Normal <5 events/hr | Mild 5–15 | Moderate 15–30 | Severe >30 events/hr; SpO2 ≥90%",
            procedure = "Sensors are placed gently on scalp, face, chest, and finger inside a quiet sleep suite while sleep architecture is recorded overnight.",
            estimatedDelivery = "48 Hours",
            relatedAliases = listOf("Sleep Apnea Test", "Level-1 PSG"),
            requiredCapabilities = listOf("Pulmonology", "Neurology", "ENT"),
            centerPrices = listOf(
                CenterTestPrice("hosp_evercare", "Evercare Hospital Dhaka", "Bashundhara R/A", "Dhaka", 0, 0, false, 48),
                CenterTestPrice("hosp_square", "Square Hospitals Ltd.", "Panthapath", "Dhaka", 0, 0, false, 48),
                CenterTestPrice("hosp_united", "United Hospital Limited", "Gulshan", "Dhaka", 0, 0, false, 48)
            )
        ),
        DiagnosticTest(
            id = "test_endoscopy",
            name = "Upper GI Endoscopy (Esophagogastroduodenoscopy)",
            shortName = "UGI Endoscopy / OGD",
            nameBn = "আপার জিআই এন্ডোস্কোপি",
            category = "Gastroenterology",
            subcategory = "Diagnostic Endoscopy",
            description = "Direct high-definition fiberoptic visualization of the esophagus, stomach, and duodenum with optional H. pylori/biopsy.",
            whatItDetects = "Gastritis, peptic ulcers, GERD/Barrett esophagus, esophageal varices, H. pylori infection, and upper GI malignancy.",
            preparationInstructions = "Strict 6–8 hours fasting (no food or liquids); arrange an escort if IV sedation is chosen; review antiplatelet medications.",
            sampleType = "Endoscopic Visual Exam + Optional Biopsy",
            normalReferenceRange = "Smooth pink esophageal, gastric, and duodenal mucosa without ulcers, varices, strictures, or mass lesions.",
            procedure = "Throat topical anesthetic (plus optional light IV sedation) is given; gastroenterologist guides a thin flexible video endoscope for 10–15 minutes.",
            estimatedDelivery = "Immediate Report (Biopsy 3–5d)",
            relatedAliases = listOf("Gastroscopy", "EGD", "OGD"),
            requiredCapabilities = listOf("Gastroenterology", "Surgery"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_colonoscopy",
            name = "Diagnostic Video Colonoscopy",
            shortName = "Colonoscopy",
            nameBn = "কোলোনোস্কোপি পরীক্ষা",
            category = "Gastroenterology",
            subcategory = "Lower GI Endoscopy",
            description = "Direct visualization of the entire large intestine (rectum to cecum) with capability for polyp removal and biopsy.",
            whatItDetects = "Colorectal polyps, colon carcinoma, ulcerative colitis/Crohn disease, diverticulosis, and lower GI bleeding sources.",
            preparationInstructions = "Split-dose oral bowel prep laxative solution the evening/morning prior + clear liquid diet 24h prior; arrange escort for sedation.",
            sampleType = "Colonic Visual Exam + Biopsy",
            normalReferenceRange = "Clean colonic lumen with intact vascular mucosal pattern and no polyps, ulcers, or masses.",
            procedure = "Under monitored sedation, a flexible video colonoscope gently inspects the large bowel over 20–35 minutes.",
            estimatedDelivery = "Immediate Report (Biopsy 3–5d)",
            relatedAliases = listOf("Lower GI Endoscopy", "Total Colonoscopy"),
            requiredCapabilities = listOf("Gastroenterology", "Surgery"),
            centerPrices = tertiaryAndMajorCenters
        ),

        // ==================== 10. HISTOPATHOLOGY, CYTOLOGY & MOLECULAR GENETICS ====================
        DiagnosticTest(
            id = "test_pap_smear",
            name = "Cervical Pap Smear / Liquid-Based Cytology",
            shortName = "Pap Smear / LBC",
            nameBn = "প্যাপ স্মিয়ার / সার্ভিক্যাল সাইটোলজি",
            category = "Histopathology & Cytology",
            subcategory = "Cervical Cancer Screening",
            description = "Microscopic cellular screening of exfoliated cervical transformation-zone cells.",
            whatItDetects = "Cervical precancerous lesions (LSIL/HSIL/CIN), early cervical cancer, and infectious cervicitis.",
            preparationInstructions = "Schedule 5+ days after period ends; avoid intercourse, douching, or vaginal creams/suppositories for 48 hours prior.",
            sampleType = "Cervical Cytology Brush / Vial",
            normalReferenceRange = "Negative for Intraepithelial Lesion or Malignancy (NILM)",
            procedure = "Gynecologist gently collects cervical cells with a soft cytobrush during a brief speculum exam (~5 minutes) and places into preservative vial.",
            estimatedDelivery = "48 - 72 Hours",
            relatedAliases = listOf("LBC Cytology", "Cervical Smear"),
            requiredCapabilities = listOf("Gynecology", "Pathology", "Oncology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_biopsy",
            name = "Histopathology Tissue Biopsy Examination",
            shortName = "Biopsy / HPE",
            nameBn = "হিস্টোপ্যাথলজি বায়োপসি পরীক্ষা",
            category = "Histopathology & Cytology",
            subcategory = "Surgical Pathology",
            description = "Tissue fixation, paraffin processing, H&E staining, and specialist pathologist diagnosis of excised tissue or core biopsy.",
            whatItDetects = "Definitive benign vs malignant diagnosis, tumor grade/margins, chronic inflammatory colitis, and infectious granulomas (TB).",
            preparationInstructions = "Depends on biopsy procedure site; surgical/endoscopic specimen placed immediately in 10% neutral buffered formalin.",
            sampleType = "Formalin-Fixed Tissue Specimen",
            normalReferenceRange = "Normal tissue architecture without dysplasia, granuloma, or neoplastic infiltration.",
            procedure = "Submitted tissue is grossed, embedded in paraffin wax, sectioned at 4 µm, stained with H&E, and examined microscopically.",
            estimatedDelivery = "3 - 5 Working Days",
            relatedAliases = listOf("HPE", "Surgical Pathology", "Tru-Cut Biopsy"),
            requiredCapabilities = listOf("Pathology", "Oncology", "Surgery"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_fnac",
            name = "Fine Needle Aspiration Cytology",
            shortName = "FNAC",
            nameBn = "এফএনএসি (সূক্ষ্ম সুঁই পরীক্ষা)",
            category = "Histopathology & Cytology",
            subcategory = "Cytopathology",
            description = "Minimally invasive aspiration of cells from palpable or USG-guided lumps (thyroid, breast, lymph node).",
            whatItDetects = "Tuberculous lymphadenitis, colloid goiter vs papillary thyroid carcinoma, fibroadenoma vs breast carcinoma.",
            preparationInstructions = "No fasting required; inform doctor if taking aspirin, clopidogrel, or warfarin.",
            sampleType = "Fine Needle Cellular Aspirate",
            normalReferenceRange = "Benign reactive cellular smear without atypical or malignant cells.",
            procedure = "A thin 23–25G needle extracts cellular fluid from the nodule in ~5 minutes, smeared onto slides and stained.",
            estimatedDelivery = "24 - 48 Hours",
            relatedAliases = listOf("Needle Aspiration Cytology", "USG-Guided FNAC"),
            requiredCapabilities = listOf("Pathology", "Surgery", "Oncology"),
            centerPrices = commonDiagnosticCenters
        ),
        DiagnosticTest(
            id = "test_rt_pcr",
            name = "Real-Time RT-PCR (HBV DNA / HCV RNA / Respiratory Viral)",
            shortName = "RT-PCR",
            nameBn = "রিয়েল-টাইম আরটি-পিসিআর",
            category = "Molecular & Genetics",
            subcategory = "Quantitative Viral Load PCR",
            description = "Amplifies viral RNA or DNA to measure exact quantitative viral copies/mL or identify respiratory viruses.",
            whatItDetects = "Hepatitis B viral load, Hepatitis C active viremia, SARS-CoV-2, Influenza, and HPV high-risk genotypes.",
            preparationInstructions = "No fasting required; collect in sterile EDTA or viral transport medium per test target.",
            sampleType = "Plasma (EDTA) / Nasopharyngeal Swab",
            normalReferenceRange = "Target Not Detected / Below Lower Limit of Quantification (<10–20 IU/mL depending on assay)",
            procedure = "Automated nucleic acid extraction followed by fluorescent TaqMan real-time thermocycling.",
            estimatedDelivery = "24 - 48 Hours",
            relatedAliases = listOf("HBV DNA PCR", "HCV RNA Viral Load", "Quantitative PCR"),
            requiredCapabilities = listOf("Molecular", "Microbiology", "Gastroenterology"),
            centerPrices = tertiaryAndMajorCenters
        ),
        DiagnosticTest(
            id = "test_karyotype_ngs",
            name = "Cytogenetic Karyotyping & BRCA / Hereditary Cancer NGS Panel",
            shortName = "Karyotype / BRCA NGS",
            nameBn = "ক্যারিওটাইপিং ও জেনেটিক প্যানেল",
            category = "Molecular & Genetics",
            subcategory = "Clinical Genetics & Genomics",
            description = "Examines complete chromosome structure (46,XX / 46,XY) or next-generation sequencing of hereditary cancer genes.",
            whatItDetects = "Down/Turner/Klinefelter syndromes, recurrent miscarriage translocations, and BRCA1/BRCA2 hereditary breast-ovarian risk.",
            preparationInstructions = "No fasting; bring clinical genetic history/pedigree if available.",
            sampleType = "Venous Blood (Sodium Heparin / EDTA)",
            normalReferenceRange = "Normal diploid karyotype: 46,XX (Female) or 46,XY (Male); No pathogenic sequence variants detected.",
            procedure = "Lymphocyte culture G-banding microscopy (Karyotype) or targeted library preparation with Illumina NGS sequencing.",
            estimatedDelivery = "10 - 21 Days",
            relatedAliases = listOf("Chromosomal Analysis", "Genetic Panel", "BRCA1/2 Sequencing"),
            requiredCapabilities = listOf("Genetics", "Oncology", "Gynecology"),
            centerPrices = listOf(
                CenterTestPrice("hosp_evercare", "Evercare Hospital Dhaka", "Bashundhara R/A", "Dhaka", 0, 0, false, 240),
                CenterTestPrice("hosp_square", "Square Hospitals Ltd.", "Panthapath", "Dhaka", 0, 0, false, 240),
                CenterTestPrice("hosp_bsmmu", "Bangabandhu Sheikh Mujib Medical University (BSMMU)", "Shahbag", "Dhaka", 0, 0, false, 336)
            )
        )
    )

    val masterMatchingRules: List<MasterTestRule> = allTests.map { test ->
        val baseKeywords = buildList {
            if (test.shortName.isNotBlank()) {
                test.shortName.split("/", "&", ",").map { it.trim().lowercase(Locale.US) }
                    .filter { it.length >= 3 }
                    .forEach(::add)
            }
            add(test.name.lowercase(Locale.US))
            test.relatedAliases.forEach { add(it.lowercase(Locale.US)) }
        }
        val extra = when (test.id) {
            "test_cbc" -> listOf("complete blood count", "cbc", "haemogram", "hemogram", "blood cp")
            "test_blood_sugar" -> listOf("fasting blood sugar", "fbs", "ppbs", "blood glucose", "rbs")
            "test_hba1c" -> listOf("hba1c", "glycosylated hemoglobin", "glycated hemoglobin")
            "test_lipid" -> listOf("lipid profile", "cholesterol panel", "fasting lipid")
            "test_lft" -> listOf("liver function", "lft", "sgpt", "sgot", "bilirubin")
            "test_kft" -> listOf("kidney function", "kft", "rft", "renal function", "serum creatinine", "creatinine")
            "test_thyroid" -> listOf("thyroid", "tsh", "ft3", "ft4")
            "test_urine" -> listOf("urine r/e", "urine re", "urine routine", "urinalysis", "urine rme")
            "test_ecg" -> listOf("ecg", "ekg", "electrocardiogram", "e.c.g")
            "test_echo" -> listOf("echocardiogram", "2d echo", "color doppler echo", "echo ")
            "test_xray" -> listOf("chest x-ray", "x-ray chest", "xray chest", "cxr", "x-ray")
            "test_usg" -> listOf("whole abdomen", "ultrasonography", "ultrasound", "usg")
            "test_ct" -> listOf("ct scan", "computed tomography", "ct brain", "ncct", "cect")
            "test_hrct_chest" -> listOf("hrct chest", "hrct", "high-resolution ct chest")
            "test_mri" -> listOf("mri", "magnetic resonance")
            "test_pet_ct" -> listOf("pet-ct scan", "pet-ct", "pet ct", "fdg pet", "whole-body 18f-fdg pet-ct")
            else -> emptyList()
        }
        MasterTestRule(
            canonicalId = test.id,
            canonicalName = test.name,
            shortName = test.shortName,
            category = test.category,
            keywords = (extra + baseKeywords).distinct()
        )
    }

    private fun hasImagingCapability(combinedProfile: String): Boolean =
        combinedProfile.contains("radiology") ||
            combinedProfile.contains("imaging") ||
            combinedProfile.contains("ct scan") ||
            combinedProfile.contains("tomography") ||
            Regex("""\bmri\b""").containsMatchIn(combinedProfile) ||
            Regex("""\bct\b""").containsMatchIn(combinedProfile)

    /**
     * Evaluates realistic hospital-wise test availability without assuming every hospital
     * performs every test. Checks explicit known offering centers, synced official portal data,
     * and facility specialty/department capabilities.
     */
    fun isTestOfferedAtHospital(
        test: DiagnosticTest,
        hospital: Hospital,
        hasLiveSyncedRecord: Boolean
    ): Boolean {
        if (hasLiveSyncedRecord) return true
        if (test.centerPrices.any { it.centerId == hospital.id }) return true

        val nameLower = hospital.name.lowercase(Locale.US)
        val deptsLower = hospital.departments.joinToString(" ").lowercase(Locale.US)
        val amenitiesLower = hospital.amenities.joinToString(" ").lowercase(Locale.US)
        val combinedProfile = "$nameLower $deptsLower $amenitiesLower"

        // Specialized single-discipline clinics (e.g. eye/dental/orthopedic-only) don't offer unrelated high-tier tests
        val isSingleSpecialtyEyeOrDental = (nameLower.contains("eye") || nameLower.contains("vision") ||
            nameLower.contains("dental") || nameLower.contains("orthopedic")) &&
            !nameLower.contains("general") && !nameLower.contains("medical college")

        if (isSingleSpecialtyEyeOrDental && test.category !in listOf("Hematology", "Clinical Biochemistry")) {
            return false
        }

        // High-end/specialized modalities require tertiary multi-specialty or explicit department support
        return when (test.id) {
            "test_pet_ct", "test_karyotype_ngs" ->
                combinedProfile.contains("oncology") || combinedProfile.contains("cancer") ||
                    combinedProfile.contains("nuclear") || combinedProfile.contains("genetics") ||
                    hospital.type == FacilityType.MEDICAL_COLLEGE ||
                    (hospital.hasIcu && hospital.departments.size >= 8)

            "test_sleep_study" ->
                combinedProfile.contains("pulmonology") || combinedProfile.contains("sleep") ||
                    combinedProfile.contains("respiratory") || (hospital.hasIcu && hospital.departments.size >= 8)

            "test_ct", "test_hrct_chest", "test_mri", "test_mra", "test_mammogram", "test_dexa", "test_ctca" ->
                (hospital.type in listOf(FacilityType.DIAGNOSTIC_CENTER, FacilityType.MEDICAL_COLLEGE, FacilityType.SPECIALIZED) &&
                    (hasImagingCapability(combinedProfile) || hospital.hasIcu || hospital.departments.size >= 6)) ||
                    (hospital.type == FacilityType.PRIVATE &&
                        (hasImagingCapability(combinedProfile) || (hospital.hasIcu && hospital.departments.size >= 6)))

            else -> {
                if (test.requiredCapabilities.isEmpty()) true
                else test.requiredCapabilities.any { cap ->
                    combinedProfile.contains(cap.lowercase(Locale.US))
                } || hospital.type == FacilityType.DIAGNOSTIC_CENTER ||
                    hospital.type == FacilityType.MEDICAL_COLLEGE ||
                    (hospital.hasEmergency247 && hospital.departments.size >= 5)
            }
        }
    }
}
