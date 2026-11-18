package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.FacilityType
import com.example.data.model.Hospital
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.abs

data class AreaFetchResult(
    val hospitals: List<Hospital>,
    val sourceLabel: String,
    val usedGooglePlaces: Boolean,
    val note: String? = null
)

object AreaHospitalRemoteDataSource {

    private const val TAG = "AreaHospitalRemote"

    private val KNOWN_BD_AREAS = listOf(
        "Mirpur", "Shyamoli", "Dhanmondi", "Panthapath", "Uttara", "Gulshan", "Banani",
        "Mohammadpur", "Shahbagh", "Mohakhali", "Badda", "Malibagh", "Kakrail",
        "Moghbazar", "Motijheel", "Lalmatia", "Old Dhaka", "Jatrabari", "Savar",
        "Bashundhara", "Baridhara", "Tejgaon", "Farmgate", "Agargaon", "Kafrul",
        "Rampura", "Khilgaon", "Shantinagar", "Paltan", "Wari", "Keraniganj",
        "Gazipur", "Narayanganj", "Tongi", "Kallyanpur", "Adabor", "Banashree"
    )

    private val QUERY_SYNONYMS = mapOf(
        "ckd" to listOf("ckd", "kidney"),
        "dmch" to listOf("dmch", "dhaka medical"),
        "bsmmu" to listOf("bsmmu", "bangabandhu", "pg hospital"),
        "nicvd" to listOf("nicvd", "cardiovascular", "heart"),
        "nitor" to listOf("nitor", "traumatology", "pangu"),
        "nimh" to listOf("nimh", "mental"),
        "ssmc" to listOf("ssmc", "salimullah", "mitford"),
        "cmh" to listOf("cmh", "combined military"),
        "icddrb" to listOf("icddrb", "icddr")
    )

    private val STOP_WORDS = setOf("in", "at", "of", "the", "for", "and", "or", "to", "a")

    fun matchesQuery(hospital: Hospital, rawQuery: String): Boolean {
        val trimmed = rawQuery.trim()
        if (trimmed.isBlank()) return true

        val haystack = buildString {
            append(hospital.name).append(' ')
            append(hospital.nameBn).append(' ')
            append(hospital.area).append(' ')
            append(hospital.address).append(' ')
            append(hospital.district).append(' ')
            append(hospital.division).append(' ')
            append(hospital.description).append(' ')
            append(hospital.departments.joinToString(" ")).append(' ')
            append(hospital.amenities.joinToString(" "))
        }.lowercase().replace("centre", "center")

        val normalizedQuery = trimmed.lowercase().replace("centre", "center")
        if (haystack.contains(normalizedQuery)) return true

        val tokens = tokenizeQuery(normalizedQuery)
        if (tokens.isEmpty()) return true

        return tokens.all { token ->
            val variants = QUERY_SYNONYMS[token] ?: listOf(token)
            variants.any { haystack.contains(it) }
        }
    }

    private fun tokenizeQuery(query: String): List<String> =
        query.lowercase()
            .replace("centre", "center")
            .split(Regex("[\\s&,/()\\-+.]+"))
            .map { it.trim() }
            .filter { it.length >= 2 && it !in STOP_WORDS }
            .distinct()

    suspend fun fetchHospitalsForArea(
        areaName: String,
        district: String,
        division: String,
        centerLat: Double,
        centerLng: Double,
        query: String = ""
    ): AreaFetchResult = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        val mapsKey = BuildConfig.MAPS_API_KEY
        val hasGoogleKey = mapsKey.isNotBlank() && mapsKey != "MY_MAPS_API_KEY"

        if (hasGoogleKey) {
            val googleResults = fetchFromGooglePlacesNew(
                apiKey = mapsKey,
                areaName = areaName,
                district = district,
                division = division,
                centerLat = centerLat,
                centerLng = centerLng,
                query = cleanQuery
            ).ifEmpty {
                fetchFromGooglePlacesLegacy(
                    apiKey = mapsKey,
                    areaName = areaName,
                    district = district,
                    division = division,
                    centerLat = centerLat,
                    centerLng = centerLng,
                    query = cleanQuery
                )
            }
            if (googleResults.isNotEmpty()) {
                return@withContext AreaFetchResult(
                    hospitals = googleResults,
                    sourceLabel = "Google Maps Places",
                    usedGooglePlaces = true
                )
            }
        }

        val osmResults = if (cleanQuery.isNotBlank()) {
            fetchByNameFromOpenStreetMap(
                query = cleanQuery,
                areaName = areaName,
                district = district,
                division = division,
                centerLat = centerLat,
                centerLng = centerLng
            )
        } else {
            fetchFromOpenStreetMapOverpass(
                areaName = areaName,
                district = district,
                division = division,
                centerLat = centerLat,
                centerLng = centerLng
            )
        }

        val note = if (!hasGoogleKey) {
            if (cleanQuery.isNotBlank()) {
                "Searched Directory + Live OpenStreetMap. Add MAPS_API_KEY in AI Studio Secrets for Google Places results."
            } else {
                "Showing Directory + Live OpenStreetMap. Add MAPS_API_KEY in AI Studio Secrets for full Google Places data."
            }
        } else {
            "Google Places returned no extra items; showing Directory + Live OpenStreetMap."
        }

        AreaFetchResult(
            hospitals = osmResults,
            sourceLabel = if (osmResults.isNotEmpty()) "Live Map + Directory" else "Bangladesh Directory",
            usedGooglePlaces = false,
            note = note
        )
    }

    private fun buildGoogleSearchQuery(
        query: String,
        areaName: String,
        district: String
    ): String {
        if (query.isBlank()) {
            val targetArea = if (areaName != "All" && areaName.isNotBlank()) areaName else district.takeIf { it != "All" } ?: "Dhaka"
            val targetDistrict = if (district != "All" && district.isNotBlank()) district else "Dhaka"
            return "hospitals clinics diagnostic centers in $targetArea, $targetDistrict, Bangladesh"
        }

        val lower = query.lowercase()
        val hasMedKeyword = listOf("hospital", "clinic", "diagnostic", "medical", "center", "centre", "institute", "lab")
            .any { lower.contains(it) }
        val medSuffix = if (hasMedKeyword) "" else " hospital clinic diagnostic"
        val locPart = buildString {
            if (areaName != "All" && areaName.isNotBlank()) {
                append(", ").append(areaName)
            }
            if (district != "All" && district.isNotBlank()) {
                append(", ").append(district)
            }
            append(", Bangladesh")
        }
        return "$query$medSuffix$locPart"
    }

    private fun fetchFromGooglePlacesNew(
        apiKey: String,
        areaName: String,
        district: String,
        division: String,
        centerLat: Double,
        centerLng: Double,
        query: String
    ): List<Hospital> {
        val results = mutableListOf<Hospital>()
        val targetArea = if (areaName != "All" && areaName.isNotBlank()) areaName else ""
        val targetDistrict = if (district != "All" && district.isNotBlank()) district else "Dhaka"
        val searchText = buildGoogleSearchQuery(query, areaName, district)
        val biasRadius = if (query.isNotBlank() && targetArea.isBlank()) 40000.0 else 7500.0

        var pageToken: String? = null
        for (page in 0 until 2) {
            try {
                val url = URL("https://places.googleapis.com/v1/places:searchText")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 9000
                    readTimeout = 10000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("X-Goog-Api-Key", apiKey)
                    setRequestProperty(
                        "X-Goog-FieldMask",
                        "places.id,places.displayName,places.formattedAddress,places.location,places.rating,places.userRatingCount,places.nationalPhoneNumber,places.internationalPhoneNumber,places.websiteUri,places.types,places.regularOpeningHours,nextPageToken"
                    )
                }

                val bodyJson = JSONObject().apply {
                    put("textQuery", searchText)
                    put("pageSize", 20)
                    put(
                        "locationBias",
                        JSONObject().put(
                            "circle",
                            JSONObject()
                                .put(
                                    "center",
                                    JSONObject()
                                        .put("latitude", centerLat)
                                        .put("longitude", centerLng)
                                )
                                .put("radius", biasRadius)
                        )
                    )
                    if (!pageToken.isNullOrBlank()) {
                        put("pageToken", pageToken)
                    }
                }

                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(bodyJson.toString()) }
                val code = conn.responseCode
                if (code !in 200..299) {
                    Log.w(TAG, "Places API (New) HTTP $code")
                    conn.disconnect()
                    break
                }

                val raw = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
                conn.disconnect()

                val json = JSONObject(raw)
                val places = json.optJSONArray("places") ?: JSONArray()
                for (i in 0 until places.length()) {
                    parseGooglePlaceNew(places.optJSONObject(i), targetArea, targetDistrict, division)?.let {
                        results.add(it)
                    }
                }
                pageToken = json.optString("nextPageToken", "").takeIf { it.isNotBlank() }
                if (pageToken == null) break
            } catch (e: Exception) {
                Log.w(TAG, "Places API (New) request failed: ${e.message}")
                break
            }
        }
        return results
    }

    private fun parseGooglePlaceNew(
        obj: JSONObject?,
        areaName: String,
        district: String,
        division: String
    ): Hospital? {
        if (obj == null) return null
        val placeId = obj.optString("id").takeIf { it.isNotBlank() } ?: return null
        val displayNameObj = obj.optJSONObject("displayName")
        val name = displayNameObj?.optString("text")?.trim().orEmpty()
        if (name.isBlank()) return null

        val address = obj.optString("formattedAddress", "$district, Bangladesh")
        val resolvedArea = areaName.ifBlank { inferAreaFromAddress(address, district) }
        val loc = obj.optJSONObject("location") ?: return null
        val lat = loc.optDouble("latitude", Double.NaN)
        val lng = loc.optDouble("longitude", Double.NaN)
        if (lat.isNaN() || lng.isNaN()) return null

        val rating = obj.optDouble("rating", 4.2).coerceIn(1.0, 5.0)
        val reviewCount = obj.optInt("userRatingCount", 18)
        val phone = obj.optString("internationalPhoneNumber").ifBlank {
            obj.optString("nationalPhoneNumber").ifBlank { "999" }
        }
        val website = obj.optString("websiteUri", "")
        val typesArray = obj.optJSONArray("types") ?: JSONArray()
        val types = (0 until typesArray.length()).map { typesArray.optString(it) }
        val openNow = obj.optJSONObject("regularOpeningHours")?.optBoolean("openNow", false) ?: false

        val facilityType = classifyFacilityType(name, types)
        val has247 = name.contains("Hospital", true) && !name.contains("Diagnostic", true) &&
            (facilityType == FacilityType.GOVERNMENT || facilityType == FacilityType.MEDICAL_COLLEGE || facilityType == FacilityType.PRIVATE || facilityType == FacilityType.SPECIALIZED || openNow)

        return Hospital(
            id = "gp_${placeId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(80)}",
            name = name,
            nameBn = name,
            type = facilityType,
            description = "Discovered via Google Maps Places in $resolvedArea, $district. Verify department availability and doctor hours by calling directly.",
            address = address,
            area = resolvedArea,
            district = district,
            division = if (division != "All" && division.isNotBlank()) division else district,
            latitude = lat,
            longitude = lng,
            phone = phone,
            emergencyPhone = if (phone == "999") "999" else phone,
            website = website,
            email = "",
            operatingHours = if (has247) "Open 24 Hours (Verify on call)" else "Daily Consultation Hours",
            hasEmergency247 = has247,
            hasIcu = facilityType == FacilityType.PRIVATE || facilityType == FacilityType.MEDICAL_COLLEGE || facilityType == FacilityType.GOVERNMENT || facilityType == FacilityType.SPECIALIZED,
            hasCcu = facilityType == FacilityType.SPECIALIZED || facilityType == FacilityType.MEDICAL_COLLEGE,
            hasNicu = false,
            hasAmbulance = has247,
            hasBloodBank = facilityType == FacilityType.GOVERNMENT || facilityType == FacilityType.MEDICAL_COLLEGE,
            verified = true,
            rating = rating,
            reviewCount = reviewCount,
            departments = inferDepartments(name, facilityType),
            amenities = listOfNotNull(
                "Google Maps Verified",
                if (has247) "Emergency Care" else null,
                if (facilityType == FacilityType.DIAGNOSTIC_CENTER) "Lab Diagnostics" else null
            ),
            isPopular = reviewCount >= 100,
            dataSource = "Google Maps"
        )
    }

    private fun fetchFromGooglePlacesLegacy(
        apiKey: String,
        areaName: String,
        district: String,
        division: String,
        centerLat: Double,
        centerLng: Double,
        query: String
    ): List<Hospital> {
        val targetArea = if (areaName != "All" && areaName.isNotBlank()) areaName else ""
        val targetDistrict = if (district != "All" && district.isNotBlank()) district else "Dhaka"
        val qText = buildGoogleSearchQuery(query, areaName, district)
        val radiusMeters = if (query.isNotBlank() && targetArea.isBlank()) 40000 else 7500

        return try {
            val encodedQ = URLEncoder.encode(qText, "UTF-8")
            val url = URL(
                "https://maps.googleapis.com/maps/api/place/textsearch/json?query=$encodedQ&location=$centerLat,$centerLng&radius=$radiusMeters&key=$apiKey"
            )
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 9000
                readTimeout = 10000
            }
            val code = conn.responseCode
            if (code !in 200..299) {
                conn.disconnect()
                return emptyList()
            }
            val raw = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
            conn.disconnect()

            val json = JSONObject(raw)
            val arr = json.optJSONArray("results") ?: return emptyList()
            val list = mutableListOf<Hospital>()
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val placeId = item.optString("place_id").ifBlank { continue }
                val name = item.optString("name").trim().ifBlank { continue }
                val address = item.optString("formatted_address", "$targetDistrict, Bangladesh")
                val resolvedArea = targetArea.ifBlank { inferAreaFromAddress(address, targetDistrict) }
                val loc = item.optJSONObject("geometry")?.optJSONObject("location") ?: continue
                val lat = loc.optDouble("lat", Double.NaN)
                val lng = loc.optDouble("lng", Double.NaN)
                if (lat.isNaN() || lng.isNaN()) continue
                val rating = item.optDouble("rating", 4.1).coerceIn(1.0, 5.0)
                val count = item.optInt("user_ratings_total", 12)
                val typesArr = item.optJSONArray("types") ?: JSONArray()
                val types = (0 until typesArr.length()).map { typesArr.optString(it) }
                val facilityType = classifyFacilityType(name, types)
                val has247 = facilityType != FacilityType.DIAGNOSTIC_CENTER && facilityType != FacilityType.CLINIC

                list.add(
                    Hospital(
                        id = "gp_${placeId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(80)}",
                        name = name,
                        nameBn = name,
                        type = facilityType,
                        description = "Discovered via Google Maps in $resolvedArea, $targetDistrict.",
                        address = address,
                        area = resolvedArea,
                        district = targetDistrict,
                        division = if (division != "All" && division.isNotBlank()) division else targetDistrict,
                        latitude = lat,
                        longitude = lng,
                        phone = "999",
                        emergencyPhone = "999",
                        website = "",
                        email = "",
                        operatingHours = if (has247) "Open 24 Hours (Verify on call)" else "Daily Consultation Hours",
                        hasEmergency247 = has247,
                        hasIcu = facilityType == FacilityType.PRIVATE || facilityType == FacilityType.MEDICAL_COLLEGE || facilityType == FacilityType.SPECIALIZED,
                        hasCcu = facilityType == FacilityType.SPECIALIZED,
                        hasNicu = false,
                        hasAmbulance = has247,
                        hasBloodBank = false,
                        verified = true,
                        rating = rating,
                        reviewCount = count,
                        departments = inferDepartments(name, facilityType),
                        amenities = listOf("Google Maps Listed"),
                        isPopular = count >= 100,
                        dataSource = "Google Maps"
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.w(TAG, "Legacy Places failed: ${e.message}")
            emptyList()
        }
    }

    private fun fetchByNameFromOpenStreetMap(
        query: String,
        areaName: String,
        district: String,
        division: String,
        centerLat: Double,
        centerLng: Double
    ): List<Hospital> {
        val targetDistrict = if (district != "All" && district.isNotBlank()) district else "Dhaka"
        val targetDivision = if (division != "All" && division.isNotBlank()) division else targetDistrict
        val nominatimResults = fetchFromNominatim(query, areaName, targetDistrict, targetDivision)
        val overpassResults = fetchOverpassByName(query, areaName, targetDistrict, targetDivision, centerLat, centerLng)
        return mergeHospitals(nominatimResults, overpassResults)
    }

    private fun fetchFromNominatim(
        query: String,
        areaName: String,
        district: String,
        division: String
    ): List<Hospital> {
        val cleanVariants = buildList {
            add("$query, Bangladesh")
            val strippedPunctuation = query.replace("&", " ").replace(Regex("\\s+"), " ").trim()
            if (strippedPunctuation != query) {
                add("$strippedPunctuation, Bangladesh")
            }
            val tokens = tokenizeQuery(query).flatMap { QUERY_SYNONYMS[it] ?: listOf(it) }.distinct()
            if (tokens.isNotEmpty()) {
                val expanded = tokens.joinToString(" ")
                if (!expanded.equals(strippedPunctuation, ignoreCase = true)) {
                    add("$expanded Bangladesh")
                }
            }
        }.distinct()

        for (searchText in cleanVariants) {
            val found = queryNominatimOnce(searchText, areaName, district, division)
            if (found.isNotEmpty()) return found
        }
        return emptyList()
    }

    private fun queryNominatimOnce(
        searchText: String,
        areaName: String,
        district: String,
        division: String
    ): List<Hospital> {
        return try {
            val encoded = URLEncoder.encode(searchText, "UTF-8")
            val url = URL(
                "https://nominatim.openstreetmap.org/search?q=$encoded&format=jsonv2&addressdetails=1&countrycodes=bd&limit=20"
            )
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 9000
                setRequestProperty("User-Agent", "MedCareBD-Android/1.0 (healthcare-finder)")
            }
            if (conn.responseCode !in 200..299) {
                conn.disconnect()
                return emptyList()
            }
            val raw = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
            conn.disconnect()

            val arr = JSONArray(raw)
            val results = mutableListOf<Hospital>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val osmId = obj.optLong("osm_id", i.toLong())
                val osmType = obj.optString("osm_type", "node")
                val category = obj.optString("category", "")
                val type = obj.optString("type", "")
                val displayName = obj.optString("display_name", "")
                val rawName = obj.optString("name").ifBlank {
                    displayName.substringBefore(",").trim()
                }.trim()
                if (rawName.length < 3) continue

                val isHealthcare = category in listOf("amenity", "healthcare") &&
                    type in listOf("hospital", "clinic", "doctors", "laboratory", "centre", "medical")
                val hasMedicalName = listOf(
                    "hospital", "clinic", "diagnostic", "medical", "kidney", "urology",
                    "heart", "cardiac", "eye", "cancer", "health", "lab", "nursing", "maternity"
                ).any { rawName.contains(it, ignoreCase = true) }
                if (!isHealthcare && !hasMedicalName) continue

                val lat = obj.optString("lat").toDoubleOrNull() ?: continue
                val lng = obj.optString("lon").toDoubleOrNull() ?: continue
                val addrObj = obj.optJSONObject("address")
                val addrSuburb = addrObj?.let {
                    it.optString("suburb")
                        .ifBlank { it.optString("neighbourhood") }
                        .ifBlank { it.optString("quarter") }
                        .ifBlank { it.optString("city_district") }
                }.orEmpty()
                val addrCity = addrObj?.let {
                    it.optString("city")
                        .ifBlank { it.optString("town") }
                        .ifBlank { it.optString("state_district") }
                }.orEmpty()

                val resolvedArea = when {
                    areaName != "All" && areaName.isNotBlank() -> areaName
                    addrSuburb.isNotBlank() -> addrSuburb
                    else -> inferAreaFromAddress(displayName, district)
                }
                val resolvedDistrict = addrCity.ifBlank { district }
                val facilityType = classifyFacilityType(rawName, listOf(category, type))
                val hasEmergency = facilityType != FacilityType.DIAGNOSTIC_CENTER && facilityType != FacilityType.CLINIC

                results.add(
                    Hospital(
                        id = "osm_nom_${osmType}_$osmId",
                        name = rawName,
                        nameBn = rawName,
                        type = facilityType,
                        description = "Live map facility discovered via OpenStreetMap in $resolvedArea, $resolvedDistrict.",
                        address = displayName.ifBlank { "$resolvedArea, $resolvedDistrict, Bangladesh" },
                        area = resolvedArea,
                        district = resolvedDistrict,
                        division = division,
                        latitude = lat,
                        longitude = lng,
                        phone = "999",
                        emergencyPhone = "999",
                        website = "",
                        email = "",
                        operatingHours = if (hasEmergency) "Open 24 Hours (Verify on call)" else "Standard Consultation Hours",
                        hasEmergency247 = hasEmergency,
                        hasIcu = facilityType == FacilityType.PRIVATE || facilityType == FacilityType.MEDICAL_COLLEGE || facilityType == FacilityType.SPECIALIZED,
                        hasCcu = facilityType == FacilityType.SPECIALIZED,
                        hasNicu = false,
                        hasAmbulance = hasEmergency,
                        hasBloodBank = false,
                        verified = false,
                        rating = 4.3,
                        reviewCount = 0,
                        departments = inferDepartments(rawName, facilityType),
                        amenities = listOf("Live Map Facility"),
                        isPopular = false,
                        dataSource = "OpenStreetMap"
                    )
                )
            }
            results
        } catch (e: Exception) {
            Log.w(TAG, "Nominatim search failed: ${e.message}")
            emptyList()
        }
    }

    private fun fetchOverpassByName(
        query: String,
        areaName: String,
        district: String,
        division: String,
        centerLat: Double,
        centerLng: Double
    ): List<Hospital> {
        val tokens = tokenizeQuery(query)
            .flatMap { QUERY_SYNONYMS[it] ?: listOf(it) }
            .filter { it.length >= 3 && it !in setOf("hospital", "clinic", "medical", "center", "limited") }
            .ifEmpty { tokenizeQuery(query).filter { it.length >= 3 } }
            .distinct()
            .take(5)
        if (tokens.isEmpty()) return emptyList()

        val safeRegex = tokens.joinToString("|") { token ->
            token.replace(Regex("[^a-zA-Z0-9\\s]"), "")
        }.takeIf { it.isNotBlank() } ?: return emptyList()

        val radiusMeters = if (areaName == "All" || areaName.isBlank()) 45000 else 12000
        val overpassQuery = """
            [out:json][timeout:12];
            (
              node["amenity"~"hospital|clinic|doctors"]["name"~"$safeRegex",i](around:$radiusMeters,$centerLat,$centerLng);
              way["amenity"~"hospital|clinic|doctors"]["name"~"$safeRegex",i](around:$radiusMeters,$centerLat,$centerLng);
              node["healthcare"~"hospital|clinic|laboratory|centre|doctor"]["name"~"$safeRegex",i](around:$radiusMeters,$centerLat,$centerLng);
              way["healthcare"~"hospital|clinic|laboratory|centre|doctor"]["name"~"$safeRegex",i](around:$radiusMeters,$centerLat,$centerLng);
              node["amenity"~"hospital|clinic|doctors"]["name:en"~"$safeRegex",i](around:$radiusMeters,$centerLat,$centerLng);
              way["amenity"~"hospital|clinic|doctors"]["name:en"~"$safeRegex",i](around:$radiusMeters,$centerLat,$centerLng);
            );
            out center tags 60;
        """.trimIndent()

        return executeOverpassQuery(overpassQuery, areaName, district, division)
    }

    private fun fetchFromOpenStreetMapOverpass(
        areaName: String,
        district: String,
        division: String,
        centerLat: Double,
        centerLng: Double
    ): List<Hospital> {
        val targetDistrict = if (district != "All" && district.isNotBlank()) district else "Dhaka"
        val targetDivision = if (division != "All" && division.isNotBlank()) division else targetDistrict

        val overpassQuery = """
            [out:json][timeout:12];
            (
              node["amenity"~"hospital|clinic|doctors"](around:5000,$centerLat,$centerLng);
              way["amenity"~"hospital|clinic|doctors"](around:5000,$centerLat,$centerLng);
              node["healthcare"~"hospital|clinic|laboratory|centre|doctor"](around:5000,$centerLat,$centerLng);
              way["healthcare"~"hospital|clinic|laboratory|centre|doctor"](around:5000,$centerLat,$centerLng);
            );
            out center tags 100;
        """.trimIndent()

        return executeOverpassQuery(overpassQuery, areaName, targetDistrict, targetDivision)
    }

    private fun executeOverpassQuery(
        overpassQuery: String,
        areaName: String,
        targetDistrict: String,
        targetDivision: String
    ): List<Hospital> {
        return try {
            val url = URL("https://overpass-api.de/api/interpreter")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                setRequestProperty("User-Agent", "MedCareBD-Android/1.0")
            }
            val payload = "data=" + URLEncoder.encode(overpassQuery, "UTF-8")
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }

            if (conn.responseCode !in 200..299) {
                conn.disconnect()
                return emptyList()
            }
            val raw = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
            conn.disconnect()

            val elements = JSONObject(raw).optJSONArray("elements") ?: return emptyList()
            val results = mutableListOf<Hospital>()
            for (i in 0 until elements.length()) {
                val el = elements.optJSONObject(i) ?: continue
                val tags = el.optJSONObject("tags") ?: continue
                val name = tags.optString("name:en").ifBlank { tags.optString("name") }.trim()
                if (name.length < 3) continue
                val nameBn = tags.optString("name:bn").ifBlank { name }

                val lat = el.optDouble("lat", Double.NaN).takeIf { !it.isNaN() }
                    ?: el.optJSONObject("center")?.optDouble("lat", Double.NaN)
                    ?: continue
                val lng = el.optDouble("lon", Double.NaN).takeIf { !it.isNaN() }
                    ?: el.optJSONObject("center")?.optDouble("lon", Double.NaN)
                    ?: continue
                if (lat.isNaN() || lng.isNaN()) continue

                val elType = el.optString("type", "node")
                val elId = el.optLong("id", i.toLong())
                val amenity = tags.optString("amenity")
                val healthcare = tags.optString("healthcare")
                val facilityType = classifyFacilityType(name, listOf(amenity, healthcare))

                val road = tags.optString("addr:street")
                val house = tags.optString("addr:housenumber")
                val tagSuburb = tags.optString("addr:suburb").ifBlank { tags.optString("addr:city") }
                val fallbackArea = if (areaName != "All" && areaName.isNotBlank()) areaName else targetDistrict
                val resolvedArea = tagSuburb.ifBlank { inferAreaFromAddress("$house $road", fallbackArea) }
                val address = listOf(house, road, resolvedArea, targetDistrict)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                    .ifBlank { "$resolvedArea, $targetDistrict, Bangladesh" }

                val phone = tags.optString("phone").ifBlank { tags.optString("contact:phone") }.ifBlank { "999" }
                val website = tags.optString("website").ifBlank { tags.optString("contact:website") }
                val hasEmergency = tags.optString("emergency").equals("yes", true) ||
                    (facilityType != FacilityType.DIAGNOSTIC_CENTER && facilityType != FacilityType.CLINIC)
                val hours = tags.optString("opening_hours").ifBlank {
                    if (hasEmergency) "Open 24 Hours (Emergency)" else "Standard Consultation Hours"
                }

                results.add(
                    Hospital(
                        id = "osm_${elType}_$elId",
                        name = name,
                        nameBn = nameBn,
                        type = facilityType,
                        description = "Live map facility discovered in $resolvedArea, $targetDistrict.",
                        address = address,
                        area = resolvedArea,
                        district = targetDistrict,
                        division = targetDivision,
                        latitude = lat,
                        longitude = lng,
                        phone = phone,
                        emergencyPhone = if (phone == "999") "999" else phone,
                        website = website,
                        email = "",
                        operatingHours = hours,
                        hasEmergency247 = hasEmergency,
                        hasIcu = facilityType == FacilityType.PRIVATE || facilityType == FacilityType.MEDICAL_COLLEGE || facilityType == FacilityType.SPECIALIZED,
                        hasCcu = facilityType == FacilityType.SPECIALIZED,
                        hasNicu = false,
                        hasAmbulance = hasEmergency,
                        hasBloodBank = false,
                        verified = false,
                        rating = 4.2,
                        reviewCount = 0,
                        departments = inferDepartments(name, facilityType),
                        amenities = listOf("Live Map Facility"),
                        isPopular = false,
                        dataSource = "OpenStreetMap"
                    )
                )
            }
            results
        } catch (e: Exception) {
            Log.w(TAG, "Overpass request failed: ${e.message}")
            emptyList()
        }
    }

    private fun inferAreaFromAddress(address: String, fallback: String): String {
        val matched = KNOWN_BD_AREAS.firstOrNull { address.contains(it, ignoreCase = true) }
        return matched ?: fallback.ifBlank { "Dhaka" }
    }

    private fun classifyFacilityType(name: String, types: List<String>): FacilityType {
        val lower = name.lowercase()
        val joinedTypes = types.joinToString(" ").lowercase()
        return when {
            lower.contains("diagnostic") || lower.contains("laboratory") || lower.contains("pathology") ||
                joinedTypes.contains("laboratory") -> FacilityType.DIAGNOSTIC_CENTER
            lower.contains("medical college") || lower.contains("dental college") -> FacilityType.MEDICAL_COLLEGE
            lower.contains("govt") || lower.contains("government") || lower.contains("sadar") ||
                lower.contains("upazila health") || lower.contains("national institute") -> FacilityType.GOVERNMENT
            lower.contains("heart") || lower.contains("kidney") || lower.contains("urology") ||
                lower.contains("eye") || lower.contains("cancer") || lower.contains("specialized") ||
                lower.contains("ortho") || lower.contains("mental") || lower.contains("chest") -> FacilityType.SPECIALIZED
            lower.contains("clinic") || joinedTypes.contains("clinic") -> FacilityType.CLINIC
            else -> FacilityType.PRIVATE
        }
    }

    private fun inferDepartments(name: String, type: FacilityType): List<String> {
        val lower = name.lowercase()
        val depts = mutableListOf<String>()
        if (lower.contains("heart") || lower.contains("cardiac")) depts.add("Cardiology")
        if (lower.contains("kidney") || lower.contains("urology") || lower.contains("ckd")) {
            depts.add("Nephrology & Urology")
            depts.add("Dialysis & Kidney Transplant")
        }
        if (lower.contains("eye") || lower.contains("vision")) depts.add("Ophthalmology")
        if (lower.contains("dental")) depts.add("Dental & Maxillofacial")
        if (type == FacilityType.DIAGNOSTIC_CENTER) {
            depts.addAll(listOf("Pathology Lab", "Radiology & Imaging", "Specialist Chambers"))
        } else {
            depts.addAll(listOf("Medicine", "Emergency", "Gynecology & Obstetrics", "Pediatrics"))
        }
        return depts.distinct()
    }

    fun mergeHospitals(curated: List<Hospital>, live: List<Hospital>): List<Hospital> {
        val merged = curated.toMutableList()
        for (candidate in live) {
            val candidateNorm = normalizeName(candidate.name)
            val alreadyExists = merged.any { existing ->
                val existingNorm = normalizeName(existing.name)
                existing.id == candidate.id ||
                    existingNorm == candidateNorm ||
                    (existingNorm.length > 6 && candidateNorm.length > 6 &&
                        (existingNorm.contains(candidateNorm) || candidateNorm.contains(existingNorm)) &&
                        abs(existing.latitude - candidate.latitude) < 0.015 &&
                        abs(existing.longitude - candidate.longitude) < 0.015)
            }
            if (!alreadyExists) {
                merged.add(candidate)
            }
        }
        return merged
    }

    private fun normalizeName(name: String): String =
        name.lowercase()
            .replace("limited", "")
            .replace("ltd", "")
            .replace("hospital", "")
            .replace("centre", "center")
            .replace(Regex("[^a-z0-9]"), "")
}

