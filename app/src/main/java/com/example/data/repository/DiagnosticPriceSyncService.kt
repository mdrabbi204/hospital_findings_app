package com.example.data.repository

import android.util.Log
import com.example.data.catalog.MedicalTestMasterCatalog
import com.example.data.model.Hospital
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.Locale

data class ExtractedDiagnosticPrice(
    val canonicalTestId: String,
    val testName: String,
    val category: String,
    val priceBdt: Int,
    val sourceUrl: String,
    val verificationStatus: String = "VERIFIED" // "VERIFIED" | "UNVERIFIED"
)

sealed interface HospitalPriceFetchResult {
    data class Verified(
        val prices: List<ExtractedDiagnosticPrice>,
        val sourceName: String,
        val sourceUrl: String
    ) : HospitalPriceFetchResult

    data class Unavailable(
        val syncState: String, // "NO_PUBLIC_PRICE_LIST" | "RESTRICTED" | "NO_OFFICIAL_URL" | "NETWORK_ERROR"
        val sourceUrl: String,
        val reason: String
    ) : HospitalPriceFetchResult
}

data class OfficialPortalEndpoint(
    val sourceName: String,
    val candidateUrls: List<String>,
    val isRegisteredOfficialPortal: Boolean = true
)

object OfficialHospitalPortalRegistry {

    private val placeholderDomains = listOf(
        "medcare-bd.org",
        "-sample.bd",
        "example.com",
        "example.org",
        "localhost"
    )

    private val idToPortal: Map<String, OfficialPortalEndpoint> = mapOf(
        "hosp_popular_diag" to OfficialPortalEndpoint(
            sourceName = "Popular Diagnostic Centre Official Portal",
            candidateUrls = listOf(
                "https://www.populardiagnostic.com/test-charges",
                "https://www.populardiagnostic.com/our-services",
                "https://www.populardiagnostic.com"
            )
        ),
        "hosp_popular_mirpur" to OfficialPortalEndpoint(
            sourceName = "Popular Diagnostic Centre Official Portal",
            candidateUrls = listOf(
                "https://www.populardiagnostic.com/test-charges",
                "https://www.populardiagnostic.com"
            )
        ),
        "hosp_popular_uttara" to OfficialPortalEndpoint(
            sourceName = "Popular Diagnostic Centre Official Portal",
            candidateUrls = listOf(
                "https://www.populardiagnostic.com/test-charges",
                "https://www.populardiagnostic.com"
            )
        ),
        "hosp_ibnsina" to OfficialPortalEndpoint(
            sourceName = "Ibn Sina Trust Official Portal",
            candidateUrls = listOf(
                "https://www.ibnsinatrust.com/diagnostic_test.php",
                "https://www.ibnsinatrust.com/test_price.php",
                "https://www.ibnsinatrust.com"
            )
        ),
        "hosp_ibnsina_mirpur" to OfficialPortalEndpoint(
            sourceName = "Ibn Sina Trust Official Portal",
            candidateUrls = listOf(
                "https://www.ibnsinatrust.com/diagnostic_test.php",
                "https://www.ibnsinatrust.com"
            )
        ),
        "hosp_ibnsina_malibagh" to OfficialPortalEndpoint(
            sourceName = "Ibn Sina Trust Official Portal",
            candidateUrls = listOf(
                "https://www.ibnsinatrust.com/diagnostic_test.php",
                "https://www.ibnsinatrust.com"
            )
        ),
        "hosp_praava_banani" to OfficialPortalEndpoint(
            sourceName = "Praava Health Official Portal",
            candidateUrls = listOf(
                "https://www.praavahealth.com/lab-tests/",
                "https://www.praavahealth.com/health-checks/",
                "https://www.praavahealth.com"
            )
        ),
        "hosp_square" to OfficialPortalEndpoint(
            sourceName = "Square Hospital Official Website",
            candidateUrls = listOf(
                "https://www.squarehospital.com/departments/pathology-lab-services",
                "https://www.squarehospital.com/health-checkup",
                "https://www.squarehospital.com"
            )
        ),
        "hosp_evercare" to OfficialPortalEndpoint(
            sourceName = "Evercare Hospital Dhaka Official Website",
            candidateUrls = listOf(
                "https://www.evercarebd.com/dhaka/health-check",
                "https://www.evercarebd.com/health-check",
                "https://www.evercarebd.com"
            )
        ),
        "hosp_united" to OfficialPortalEndpoint(
            sourceName = "United Hospital Official Website",
            candidateUrls = listOf(
                "https://www.uhlbd.com/health-checkup",
                "https://www.uhlbd.com"
            )
        ),
        "hosp_labaid_dhanmondi" to OfficialPortalEndpoint(
            sourceName = "Labaid Diagnostics Official Portal",
            candidateUrls = listOf(
                "https://labaiddiagnostics.com",
                "https://labaid.com.bd"
            )
        ),
        "hosp_bshl" to OfficialPortalEndpoint(
            sourceName = "Bangladesh Specialized Hospital Official Website",
            candidateUrls = listOf(
                "https://www.bshl.com.bd/health-checkup",
                "https://www.bshl.com.bd"
            )
        ),
        "hosp_nhf" to OfficialPortalEndpoint(
            sourceName = "National Heart Foundation Official Website",
            candidateUrls = listOf(
                "https://www.nhf.org.bd"
            )
        ),
        "hosp_bsmmu" to OfficialPortalEndpoint(
            sourceName = "BSMMU Official Website",
            candidateUrls = listOf(
                "https://bsmmu.ac.bd"
            )
        ),
        "hosp_dmch" to OfficialPortalEndpoint(
            sourceName = "Dhaka Medical College Hospital Official Portal",
            candidateUrls = listOf(
                "https://dmch.gov.bd"
            )
        ),
        "hosp_icddrb" to OfficialPortalEndpoint(
            sourceName = "icddr,b Diagnostic Services",
            candidateUrls = listOf(
                "https://www.icddrb.org/how-we-can-help/diagnostic-services",
                "https://www.icddrb.org"
            )
        )
    )

    private val nameKeywordToPortal: List<Pair<String, OfficialPortalEndpoint>> = listOf(
        "popular diagnostic" to OfficialPortalEndpoint(
            "Popular Diagnostic Centre Official Portal",
            listOf("https://www.populardiagnostic.com/test-charges", "https://www.populardiagnostic.com")
        ),
        "ibn sina" to OfficialPortalEndpoint(
            "Ibn Sina Trust Official Portal",
            listOf("https://www.ibnsinatrust.com/diagnostic_test.php", "https://www.ibnsinatrust.com")
        ),
        "praava" to OfficialPortalEndpoint(
            "Praava Health Official Portal",
            listOf("https://www.praavahealth.com/lab-tests/", "https://www.praavahealth.com")
        ),
        "square hospital" to OfficialPortalEndpoint(
            "Square Hospital Official Website",
            listOf("https://www.squarehospital.com/health-checkup", "https://www.squarehospital.com")
        ),
        "evercare" to OfficialPortalEndpoint(
            "Evercare Hospital Dhaka Official Website",
            listOf("https://www.evercarebd.com/dhaka/health-check", "https://www.evercarebd.com")
        ),
        "labaid" to OfficialPortalEndpoint(
            "Labaid Diagnostics Official Portal",
            listOf("https://labaiddiagnostics.com", "https://labaid.com.bd")
        ),
        "united hospital" to OfficialPortalEndpoint(
            "United Hospital Official Website",
            listOf("https://www.uhlbd.com/health-checkup", "https://www.uhlbd.com")
        ),
        "bangladesh specialized" to OfficialPortalEndpoint(
            "Bangladesh Specialized Hospital Official Website",
            listOf("https://www.bshl.com.bd/health-checkup", "https://www.bshl.com.bd")
        ),
        "icddrb" to OfficialPortalEndpoint(
            "icddr,b Diagnostic Services",
            listOf("https://www.icddrb.org/how-we-can-help/diagnostic-services")
        ),
        "icddr,b" to OfficialPortalEndpoint(
            "icddr,b Diagnostic Services",
            listOf("https://www.icddrb.org/how-we-can-help/diagnostic-services")
        ),
        "asgar ali" to OfficialPortalEndpoint(
            "Asgar Ali Hospital Official Website",
            listOf("https://www.asgaralihospital.com")
        ),
        "medinova" to OfficialPortalEndpoint(
            "Medinova Medical Services Official Website",
            listOf("https://www.medinovamedicalbd.com")
        ),
        "green life" to OfficialPortalEndpoint(
            "Green Life Hospital Official Website",
            listOf("https://www.greenlifehospital.com.bd")
        )
    )

    fun isPlaceholderUrl(rawUrl: String): Boolean {
        val lower = rawUrl.trim().lowercase(Locale.US)
        if (lower.isBlank()) return true
        return placeholderDomains.any { lower.contains(it) }
    }

    fun resolveEndpoints(hospital: Hospital): OfficialPortalEndpoint? {
        idToPortal[hospital.id]?.let { mapped ->
            val extra = sanitizeWebsite(hospital.website)?.let { w ->
                listOf(w, "${w.trimEnd('/')}/diagnostics", "${w.trimEnd('/')}/health-check")
            }.orEmpty()
            return mapped.copy(candidateUrls = (mapped.candidateUrls + extra).distinct())
        }

        val lowerName = hospital.name.lowercase(Locale.US)
        nameKeywordToPortal.firstOrNull { lowerName.contains(it.first) }?.let { (_, endpoint) ->
            val extra = sanitizeWebsite(hospital.website)?.let { w -> listOf(w) }.orEmpty()
            return endpoint.copy(candidateUrls = (endpoint.candidateUrls + extra).distinct())
        }

        val cleanSite = sanitizeWebsite(hospital.website) ?: return null
        val base = cleanSite.trimEnd('/')
        return OfficialPortalEndpoint(
            sourceName = "${hospital.name} Website",
            candidateUrls = listOf(
                cleanSite,
                "$base/diagnostics",
                "$base/diagnostic",
                "$base/test-charges",
                "$base/health-check"
            ).distinct(),
            isRegisteredOfficialPortal = false
        )
    }

    fun sanitizeWebsite(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isBlank() || isPlaceholderUrl(trimmed)) return null
        val normalized = when {
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            trimmed.startsWith("http://", ignoreCase = true) -> "https://" + trimmed.removePrefix("http://")
            else -> "https://$trimmed"
        }
        return runCatching {
            val uri = URI(normalized)
            if (uri.host.isNullOrBlank()) null else normalized
        }.getOrNull()
    }
}

object DiagnosticPriceSyncService {

    private const val TAG = "DiagPriceSync"
    private const val CONNECT_TIMEOUT_MS = 8000
    private const val READ_TIMEOUT_MS = 8000
    private const val MAX_BODY_CHARS = 350_000
    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Mobile Safari/537.36 MedCareBD/1.0"

    private val diagnosticKeywordHints = listOf(
        "cbc", "complete blood", "hemoglobin", "haemoglobin", "platelet", "esr", "pbf", "retic",
        "blood sugar", "glucose", "hba1c", "ogtt", "lipid", "cholesterol", "triglyceride",
        "liver function", "lft", "sgpt", "sgot", "bilirubin", "amylase", "lipase",
        "kidney", "creatinine", "urea", "uric acid", "electrolyte", "calcium", "magnesium",
        "thyroid", "tsh", "ft3", "ft4", "cortisol", "prolactin", "testosterone", "amh", "pth",
        "vitamin d", "vitamin b12", "ferritin", "iron", "blood group", "beta-hcg", "hcg",
        "urine", "stool", "culture", "dengue", "ns1", "crp", "procalcitonin", "hbsag", "hcv",
        "hiv", "genexpert", "torch", "ana", "anti-ccp", "ecg", "echo", "echocardiogram", "ett",
        "holter", "troponin", "ctca", "x-ray", "xray", "ultrasound", "ultrasonography", "usg",
        "doppler", "ct scan", "hrct", "mri", "mra", "mammogram", "dexa", "pet-ct", "psa",
        "ca-125", "cea", "afp", "eeg", "emg", "ncs", "spirometry", "pft", "polysomnography",
        "endoscopy", "colonoscopy", "biopsy", "pap smear", "fnac", "rt-pcr", "karyotype", "d-dimer"
    )

    private val ignoredNoiseTokens = listOf(
        "hotline", "helpline", "ambulance", "emergency", "copyright", "privacy policy",
        "terms", "menu", "home", "contact us", "about us", "career", "news",
        "road", "house", "sector", "block", "dhaka", "bangladesh", "copyright"
    )

    private data class HttpResponsePayload(
        val code: Int,
        val contentType: String,
        val finalUrl: String,
        val body: String
    )

    suspend fun fetchHospitalDiagnosticPrices(hospital: Hospital): HospitalPriceFetchResult =
        withContext(Dispatchers.IO) {
            val endpoint = OfficialHospitalPortalRegistry.resolveEndpoints(hospital)
                ?: return@withContext HospitalPriceFetchResult.Unavailable(
                    syncState = "NO_OFFICIAL_URL",
                    sourceUrl = "",
                    reason = "No verified official website or public pricing API is registered for ${hospital.name}."
                )

            val visited = mutableSetOf<String>()
            val discoveredSubUrls = mutableListOf<String>()
            val aggregatedPrices = linkedMapOf<String, ExtractedDiagnosticPrice>()
            var anyRestricted = false
            var anyNetworkError = false
            var lastCheckedUrl = endpoint.candidateUrls.firstOrNull().orEmpty()
            var lastStatusCode = 0

            suspend fun probeUrl(url: String, discoverLinks: Boolean) {
                if (!visited.add(url)) return
                lastCheckedUrl = url
                val response = executeGet(url)
                if (response == null) {
                    anyNetworkError = true
                    return
                }
                lastStatusCode = response.code
                if (response.code == 401 || response.code == 403 || response.code == 429) {
                    anyRestricted = true
                    return
                }
                if (response.code !in 200..299 || response.body.isBlank()) {
                    return
                }

                val extracted = extractDiagnosticPricesFromContent(
                    rawContent = response.body,
                    contentType = response.contentType,
                    sourceUrl = response.finalUrl,
                    isOfficialPortal = endpoint.isRegisteredOfficialPortal
                )
                extracted.forEach { item ->
                    aggregatedPrices.putIfAbsent(item.canonicalTestId, item)
                }

                if (discoverLinks && aggregatedPrices.isEmpty()) {
                    discoveredSubUrls += discoverCandidateTestUrls(response.body, response.finalUrl)
                }
            }

            // Probe primary registered endpoints
            for (url in endpoint.candidateUrls.take(3)) {
                probeUrl(url, discoverLinks = true)
                if (aggregatedPrices.size >= 5) break
            }

            // Probe up to 2 diagnostic/price links discovered from the hospital's official homepage
            if (aggregatedPrices.isEmpty()) {
                for (subUrl in discoveredSubUrls.distinct().take(2)) {
                    probeUrl(subUrl, discoverLinks = false)
                    if (aggregatedPrices.size >= 5) break
                }
            }

            if (aggregatedPrices.isNotEmpty()) {
                return@withContext HospitalPriceFetchResult.Verified(
                    prices = aggregatedPrices.values.toList(),
                    sourceName = endpoint.sourceName,
                    sourceUrl = aggregatedPrices.values.first().sourceUrl
                )
            }

            val reason = when {
                anyRestricted -> "Official portal (${hostOf(lastCheckedUrl)}) restricts automated requests (HTTP $lastStatusCode)."
                anyNetworkError && lastStatusCode == 0 -> "Could not reach official portal (${hostOf(lastCheckedUrl)})."
                else -> "Official website (${hostOf(lastCheckedUrl)}) does not publish structured online diagnostic test prices."
            }
            val syncState = when {
                anyRestricted -> "RESTRICTED"
                anyNetworkError && lastStatusCode == 0 -> "NETWORK_ERROR"
                else -> "NO_PUBLIC_PRICE_LIST"
            }

            HospitalPriceFetchResult.Unavailable(
                syncState = syncState,
                sourceUrl = lastCheckedUrl,
                reason = reason
            )
        }

    private fun executeGet(urlString: String): HttpResponsePayload? {
        var currentUrl = urlString
        repeat(4) { hop ->
            var conn: HttpURLConnection? = null
            try {
                val url = URL(currentUrl)
                if (!url.protocol.equals("https", ignoreCase = true)) return null
                conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    instanceFollowRedirects = false
                    setRequestProperty("User-Agent", USER_AGENT)
                    setRequestProperty("Accept", "application/json, text/html;q=0.9,*/*;q=0.8")
                    setRequestProperty("Accept-Language", "en-US,en;q=0.9")
                }
                val code = conn.responseCode
                if (code in listOf(301, 302, 303, 307, 308) && hop < 3) {
                    val location = conn.getHeaderField("Location")?.trim()
                    if (location.isNullOrBlank()) return null
                    val resolved = runCatching { URI(currentUrl).resolve(location).toString() }.getOrNull()
                        ?: return null
                    if (!resolved.startsWith("https://", ignoreCase = true) ||
                        OfficialHospitalPortalRegistry.isPlaceholderUrl(resolved)
                    ) {
                        return null
                    }
                    currentUrl = resolved
                    return@repeat
                }

                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val body = stream?.use { input ->
                    BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                        val sb = StringBuilder()
                        val buf = CharArray(4096)
                        var read: Int
                        while (reader.read(buf).also { read = it } != -1 && sb.length < MAX_BODY_CHARS) {
                            sb.append(buf, 0, read)
                        }
                        sb.toString()
                    }
                }.orEmpty()

                return HttpResponsePayload(
                    code = code,
                    contentType = conn.contentType.orEmpty(),
                    finalUrl = conn.url?.toString() ?: currentUrl,
                    body = body
                )
            } catch (e: Exception) {
                Log.d(TAG, "Fetch failed for $currentUrl: ${e.message}")
                return null
            } finally {
                conn?.disconnect()
            }
        }
        return null
    }

    internal fun discoverCandidateTestUrls(html: String, baseUrl: String): List<String> {
        val baseUri = runCatching { URI(baseUrl) }.getOrNull() ?: return emptyList()
        val baseHost = baseUri.host ?: return emptyList()
        val linkRegex = Regex(
            """<a\s+[^>]*href\s*=\s*["']([^"'#]+)["'][^>]*>(.*?)</a>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val keywords = listOf("test", "price", "charge", "fee", "diagnostic", "pathology", "investigation", "lab", "health-check")
        val results = mutableListOf<String>()
        linkRegex.findAll(html).forEach { match ->
            val href = match.groupValues[1].trim()
            val label = stripHtmlTags(match.groupValues[2]).lowercase(Locale.US)
            val lowerHref = href.lowercase(Locale.US)
            if (keywords.any { lowerHref.contains(it) || label.contains(it) }) {
                val resolved = runCatching { baseUri.resolve(href) }.getOrNull()
                if (resolved != null &&
                    resolved.scheme.equals("https", ignoreCase = true) &&
                    resolved.host.equals(baseHost, ignoreCase = true)
                ) {
                    results += resolved.toString()
                }
            }
        }
        return results.distinct()
    }

    /**
     * Strictly parses online prices from JSON APIs, JSON-LD blocks, HTML tables,
     * and structured HTML blocks with explicit BDT currency markers.
     * Distinguishes VERIFIED (structured official tables/APIs on registered portals)
     * from UNVERIFIED (generic heuristic blocks or unverified websites).
     * Never guesses or fabricates prices.
     */
    internal fun extractDiagnosticPricesFromContent(
        rawContent: String,
        contentType: String,
        sourceUrl: String,
        isOfficialPortal: Boolean = true
    ): List<ExtractedDiagnosticPrice> {
        val trimmed = rawContent.trim()
        if (trimmed.isBlank()) return emptyList()

        val found = linkedMapOf<String, ExtractedDiagnosticPrice>()

        fun register(rawName: String, rawPrice: Int, structuredConfidence: Boolean) {
            if (rawPrice !in 50..150_000) return
            val cleanedName = cleanTestName(rawName)
            if (!isPlausibleTestName(cleanedName)) return
            val (canonicalId, canonicalName, category) = mapToCanonicalTest(cleanedName)
            val status = if (isOfficialPortal && structuredConfidence) "VERIFIED" else "UNVERIFIED"
            val existing = found[canonicalId]
            if (existing == null || (existing.verificationStatus != "VERIFIED" && status == "VERIFIED")) {
                found[canonicalId] = ExtractedDiagnosticPrice(
                    canonicalTestId = canonicalId,
                    testName = canonicalName,
                    category = category,
                    priceBdt = rawPrice,
                    sourceUrl = sourceUrl,
                    verificationStatus = status
                )
            }
        }

        // 1) Pure JSON response
        if (contentType.contains("json", ignoreCase = true) ||
            trimmed.startsWith("{") ||
            trimmed.startsWith("[")
        ) {
            extractFromJsonString(trimmed) { n, p -> register(n, p, structuredConfidence = true) }
            if (found.isNotEmpty()) return found.values.toList()
        }

        // 2) Embedded JSON-LD blocks in HTML
        val jsonLdRegex = Regex(
            """<script[^>]*type\s*=\s*["']application/ld\+json["'][^>]*>(.*?)</script>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        jsonLdRegex.findAll(trimmed).forEach { match ->
            extractFromJsonString(match.groupValues[1]) { n, p -> register(n, p, structuredConfidence = true) }
        }

        // 3) HTML Table rows (e.g. <tr><td>CBC (Complete Blood Count)</td><td>৳ 450</td></tr>)
        val cleanedHtml = trimmed
            .replace(Regex("""<script[^>]*>.*?</script>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), " ")
            .replace(Regex("""<style[^>]*>.*?</style>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), " ")

        val tableRegex = Regex("""<table[^>]*>(.*?)</table>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val trRegex = Regex("""<tr[^>]*>(.*?)</tr>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val cellRegex = Regex("""<t[hd][^>]*>(.*?)</t[hd]>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

        tableRegex.findAll(cleanedHtml).forEach { tableMatch ->
            val tableHtml = tableMatch.groupValues[1]
            val hasPriceHeader = Regex("""\b(price|rate|fee|charge|taka|tk|bdt)\b""", RegexOption.IGNORE_CASE)
                .containsMatchIn(stripHtmlTags(tableHtml.take(500)))

            trRegex.findAll(tableHtml).forEach { rowMatch ->
                val cells = cellRegex.findAll(rowMatch.groupValues[1])
                    .map { stripHtmlTags(it.groupValues[1]).trim() }
                    .filter { it.isNotBlank() }
                    .toList()
                if (cells.size >= 2) {
                    val candidateName = cells.firstOrNull { isPlausibleTestName(it) }
                    if (candidateName != null) {
                        val priceCell = cells.firstOrNull { cell ->
                            cell != candidateName && parseBdtPrice(cell, allowPlainNumber = hasPriceHeader) != null
                        }
                        val price = priceCell?.let { parseBdtPrice(it, allowPlainNumber = hasPriceHeader) }
                        if (price != null) {
                            register(candidateName, price, structuredConfidence = true)
                        }
                    }
                }
            }
        }

        // 4) Generic HTML blocks (cards/list items) with explicit BDT currency + diagnostic keywords -> marked UNVERIFIED unless verified via structured table/API
        val explicitCurrencyRegex = Regex(
            """(?:৳|BDT|Tk\.?|Taka)\s*([0-9]{1,3}(?:,[0-9]{2,3})+|[0-9]{2,6})|(?<![0-9,])([0-9]{1,3}(?:,[0-9]{2,3})+|[0-9]{2,6})\s*(?:BDT|Tk\.?|Taka|৳|/-)""",
            RegexOption.IGNORE_CASE
        )
        val blockRegex = Regex(
            """<(li|div|p|article|section)\b[^>]*>((?:(?!<\1\b).)*?)</\1>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        blockRegex.findAll(cleanedHtml).forEach { blockMatch ->
            val rawBlock = blockMatch.groupValues[2]
            if (rawBlock.length > 500) return@forEach // skip giant wrapper containers
            val text = stripHtmlTags(rawBlock)
            if (text.length !in 8..160) return@forEach
            val priceMatch = explicitCurrencyRegex.find(text) ?: return@forEach
            val digits = (priceMatch.groupValues[1].ifBlank { priceMatch.groupValues[2] })
                .replace(",", "")
                .toIntOrNull() ?: return@forEach

            val namePart = text.substring(0, priceMatch.range.first).trim(' ', ':', '-', '|', '—', '/')
            if (hasDiagnosticKeyword(namePart) && isPlausibleTestName(namePart)) {
                register(namePart, digits, structuredConfidence = false)
            }
        }

        return found.values.toList()
    }

    private fun extractFromJsonString(jsonText: String, onPair: (String, Int) -> Unit) {
        val text = jsonText.trim()
        if (text.startsWith("[")) {
            runCatching { JSONArray(text) }.getOrNull()?.let { traverseJsonArray(it, onPair) }
        } else if (text.startsWith("{")) {
            runCatching { JSONObject(text) }.getOrNull()?.let { traverseJsonObject(it, onPair) }
        }
    }

    private fun traverseJsonObject(obj: JSONObject, onPair: (String, Int) -> Unit) {
        val currency = obj.optString("priceCurrency", "")
        if (currency.isNotBlank() && !currency.equals("BDT", ignoreCase = true)) {
            return
        }
        val nameKeys = listOf("testName", "test_name", "serviceName", "service_name", "name", "title", "itemName")
        val priceKeys = listOf("priceBdt", "price_bdt", "price", "fee", "rate", "charge", "amount", "cost")

        val rawName = nameKeys.firstNotNullOfOrNull { key ->
            obj.optString(key).takeIf { it.isNotBlank() }
        }
        var rawPrice: Int? = null
        for (key in priceKeys) {
            if (!obj.has(key)) continue
            val value = obj.opt(key)
            val parsed = when {
                value == null || value === JSONObject.NULL -> null
                value is Number -> value.toInt()
                value is String -> parseBdtPrice(value, allowPlainNumber = true)
                else -> null
            }
            if (parsed != null) {
                rawPrice = parsed
                break
            }
        }
        if (rawPrice == null && obj.has("offers")) {
            val offer = obj.optJSONObject("offers")
            if (offer != null) {
                val offerCurrency = offer.optString("priceCurrency", "BDT")
                if (offerCurrency.equals("BDT", ignoreCase = true) || offerCurrency.isBlank()) {
                    val offerPrice = offer.opt("price")
                    rawPrice = when {
                        offerPrice == null || offerPrice === JSONObject.NULL -> null
                        offerPrice is Number -> offerPrice.toInt()
                        offerPrice is String -> parseBdtPrice(offerPrice, allowPlainNumber = true)
                        else -> null
                    }
                }
            }
        }

        if (rawName != null && rawPrice != null && hasDiagnosticKeyword(rawName)) {
            onPair(rawName, rawPrice)
        }

        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val child = obj.opt(k)
            if (child is JSONObject) {
                traverseJsonObject(child, onPair)
            } else if (child is JSONArray) {
                traverseJsonArray(child, onPair)
            }
        }
    }

    private fun traverseJsonArray(arr: JSONArray, onPair: (String, Int) -> Unit) {
        for (i in 0 until arr.length()) {
            val item = arr.opt(i)
            if (item is JSONObject) {
                traverseJsonObject(item, onPair)
            } else if (item is JSONArray) {
                traverseJsonArray(item, onPair)
            }
        }
    }

    private fun parseBdtPrice(text: String, allowPlainNumber: Boolean): Int? {
        val explicit = Regex(
            """(?:৳|BDT|Tk\.?|Taka)\s*([0-9]{1,3}(?:,[0-9]{2,3})+|[0-9]{2,6})|(?<![0-9,])([0-9]{1,3}(?:,[0-9]{2,3})+|[0-9]{2,6})\s*(?:BDT|Tk\.?|Taka|৳|/-)""",
            RegexOption.IGNORE_CASE
        ).find(text)
        if (explicit != null) {
            val digits = (explicit.groupValues[1].ifBlank { explicit.groupValues[2] })
                .replace(",", "")
                .toIntOrNull()
            if (digits != null && digits in 50..150_000) return digits
        }
        if (allowPlainNumber) {
            val clean = text.trim().replace(",", "")
            if (Regex("""^[0-9]{2,6}(?:\.0{1,2})?$""").matches(clean)) {
                val n = clean.substringBefore(".").toIntOrNull()
                if (n != null && n in 50..150_000 && n !in 1990..2035) return n
            }
        }
        return null
    }

    private fun stripHtmlTags(raw: String): String =
        raw.replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&#2547;", "৳")
            .replace("&#x09F3;", "৳")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun cleanTestName(raw: String): String =
        raw.replace(Regex("\\s+"), " ")
            .trim(' ', '-', ':', '|', '.', ',')

    private fun isPlausibleTestName(name: String): Boolean {
        if (name.length !in 3..95) return false
        if (!name.any { it.isLetter() }) return false
        val lower = name.lowercase(Locale.US)
        if (ignoredNoiseTokens.any { lower.contains(it) }) return false
        if (Regex("""^[0-9\s.,/-]+$""").matches(name)) return false
        return true
    }

    private fun hasDiagnosticKeyword(name: String): Boolean {
        val lower = name.lowercase(Locale.US)
        return diagnosticKeywordHints.any { lower.contains(it) }
    }

    internal fun mapToCanonicalTest(testName: String): Triple<String, String, String> {
        val lower = testName.lowercase(Locale.US)
        val bestMatch = MedicalTestMasterCatalog.masterMatchingRules
            .mapNotNull { rule ->
                val longestKeywordLen = rule.keywords
                    .filter { kw ->
                        if (kw.length <= 4) {
                            Regex("""\b${Regex.escape(kw)}\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower)
                        } else {
                            lower.contains(kw)
                        }
                    }
                    .maxOfOrNull { it.length }
                longestKeywordLen?.let { rule to it }
            }
            .maxByOrNull { it.second }
            ?.first

        if (bestMatch != null) {
            return Triple(bestMatch.canonicalId, bestMatch.canonicalName, bestMatch.category)
        }

        val slug = lower.replace(Regex("[^a-z0-9]+"), "_").trim('_').take(36)
        val inferredCategory = when {
            lower.contains("mri") || lower.contains("ct ") || lower.contains("x-ray") ||
                lower.contains("xray") || lower.contains("ultrasound") || lower.contains("usg") -> "Radiology & Imaging"
            lower.contains("ecg") || lower.contains("echo") || lower.contains("cardiac") || lower.contains("troponin") -> "Cardiology"
            lower.contains("thyroid") || lower.contains("hormone") || lower.contains("tsh") -> "Endocrinology"
            lower.contains("blood") || lower.contains("hemoglobin") || lower.contains("cbc") -> "Hematology"
            else -> "Clinical Pathology"
        }
        return Triple("ext_$slug", testName, inferredCategory)
    }

    private fun hostOf(url: String): String =
        runCatching { URI(url).host ?: url }.getOrDefault(url)
}
