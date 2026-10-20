package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.ChennaiTransitRepository
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiTransitService {

    private const val TAG = "GeminiTransitService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun findNearestCommuteOptions(
        locationQuery: String,
        preference: CommutePreference
    ): GeminiTransitRecommendation = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isKeyConfigured = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (isKeyConfigured) {
            try {
                val aiResult = callGeminiApi(locationQuery, preference, apiKey)
                if (aiResult != null) {
                    return@withContext aiResult
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API call failed, falling back to Chennai Transit Knowledge Base: ${e.message}", e)
            }
        }

        // Reliable fallback engine based on ChennaiTransitRepository corridors and landmarks
        resolveOfflineChennaiTransit(locationQuery, preference, isDemo = !isKeyConfigured)
    }

    private fun callGeminiApi(
        locationQuery: String,
        preference: CommutePreference,
        apiKey: String
    ): GeminiTransitRecommendation? {
        val endpoint = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

        val systemPrompt = """
            You are the official CUMTA (Chennai Unified Metropolitan Transport Authority) AI Transit Assistant.
            You have comprehensive knowledge of all Chennai public transport:
            1. CMRL Metro (Blue Line: Wimco Nagar to Airport, Green Line: Central to St. Thomas Mount, Interchanges at Alandur & Central).
            2. MTC City Buses (Key corridors: Anna Salai, ECR, OMR IT Expressway, GST Road, Poonamallee High Rd, 100 Ft Rd). MTC Ordinary white board buses are free for women/seniors (Vidiyal Payanam scheme).
            3. Southern Railway Chennai Suburban EMU & MRTS (Beach to Tambaram/Chengalpattu, Central to Gummidipoondi/Arakkonam, Beach to Velachery MRTS).
            4. Share Auto routes (₹15-₹25 along GST Road, OMR, Poonamallee High Rd, Guindy to Porur).
            5. First & Last mile feeder e-rickshaws at metro stations.
            
            When given a user's location, query, or prompt in Chennai (such as "tell me whats todays plan -> i wanna go from neelankari to triplicane"):
            1. Locate the nearest boarding point (Bus stop, Metro, or Suburban station) for their starting location.
            2. If they specified a destination (e.g. Triplicane), identify the nearest destination arrival point and recommended public transit routes (bus numbers like 19B/21G, metro, or suburban trains).
            3. Provide step-free accessibility details (lifts, ramps, elevators for elderly).
            
            Respond strictly in valid JSON matching this exact structure:
            {
              "queriedLocation": "location name",
              "summaryEn": "English summary of nearest commute options",
              "summaryTa": "தமிழ் சுருக்கம்",
              "recommendedBestOptionEn": "Best recommended way to commute",
              "recommendedBestOptionTa": "பரிந்துரைக்கப்பட்ட சிறந்த பயணம்",
              "nearestMetro": {
                 "nameEn": "Station name in English",
                 "nameTa": "Station name in Tamil",
                 "distanceEstimate": "e.g. 400 meters (5 min walk)",
                 "walkMinutes": 5,
                 "detailsEn": "Line color, nearest gate, lift/escalator status",
                 "detailsTa": "வழித்தடம், வாயில், மின்தூக்கி வசதி",
                 "keyRoutes": ["Blue Line", "Green Line"],
                 "hasElevator": true,
                 "hasRamp": true,
                 "matchedStationId": "central / guindy / alandur / cmbt / airport / ag_dms / nandanam / saidapet / vadapalani"
              },
              "nearestBusStop": {
                 "nameEn": "Bus stop name in English",
                 "nameTa": "Bus stop name in Tamil",
                 "distanceEstimate": "e.g. 150 meters (2 min walk)",
                 "walkMinutes": 2,
                 "detailsEn": "Corridor name, frequency, stop location",
                 "detailsTa": "பேருந்து நிறுத்தம் விபரம்",
                 "keyRoutes": ["19B", "29C", "102"],
                 "isFreeBusEligible": true,
                 "matchedStationId": "matched id if known"
              },
              "nearestSuburbanRail": {
                 "nameEn": "Suburban station name in English",
                 "nameTa": "ரயில் நிலையம் தமிழ்",
                 "distanceEstimate": "e.g. 800 meters (10 min walk)",
                 "walkMinutes": 10,
                 "detailsEn": "Line info, ₹5 suburban ticket, bridge/ramp access",
                 "detailsTa": "புறநகர் ரயில் விபரம்",
                 "keyRoutes": ["Beach - Tambaram EMU"],
                 "hasRamp": true
              },
              "firstLastMile": {
                 "shareAutoTipEn": "Share auto availability along corridor",
                 "shareAutoTipTa": "ஷேர் ஆட்டோ விவரம்",
                 "estimatedAutoFare": "₹15 - ₹20",
                 "feederShuttleInfoEn": "CMRL Feeder or connecting MTC bus",
                 "feederShuttleInfoTa": "மெட்ரோ ஃபீடர் சேவை"
              },
              "accessibilityVerdictEn": "Step-free rating and advice for elderly/wheelchair commuters",
              "accessibilityVerdictTa": "முதியோருக்கான மின்தூக்கி மற்றும் நடைபாதை அறிவுரை",
              "suggestedOriginStationId": "station id in repo (e.g. central, guindy, cmbt, tnagar, tambaram, airport, alandur, ecr_thiruvanmiyur, ecr_neelankarai, omr_tidel_park, mylapore, triplicane)",
              "parsedTripPlan": {
                 "promptText": "full prompt text",
                 "originQuery": "origin name",
                 "destinationQuery": "destination name",
                 "nearestBoardingPoint": {
                    "nameEn": "Boarding Stop Name",
                    "nameTa": "ஏறும் நிறுத்தம்",
                    "distanceEstimate": "180 meters",
                    "walkMinutes": 2,
                    "detailsEn": "Details of boarding stop",
                    "detailsTa": "விவரம்",
                    "keyRoutes": ["19B", "29C"],
                    "isFreeBusEligible": true,
                    "matchedStationId": "ecr_neelankarai"
                 },
                 "nearestDestinationPoint": {
                    "nameEn": "Destination Stop Name",
                    "nameTa": "இறங்கும் நிறுத்தம்",
                    "distanceEstimate": "250 meters",
                    "walkMinutes": 3,
                    "detailsEn": "Details of destination",
                    "detailsTa": "விவரம்",
                    "keyRoutes": ["19B", "21G"],
                    "matchedStationId": "triplicane"
                 },
                 "tripOptions": [
                    {
                      "titleEn": "Direct MTC Bus (Route 19B)",
                      "titleTa": "நேரடி MTC பேருந்து",
                      "mode": "BUS",
                      "routeCode": "19B Direct",
                      "durationMins": 42,
                      "fare": "₹20 (Free for Women)",
                      "instructionsEn": "Board bus 19B directly at Neelankarai ECR stop.",
                      "instructionsTa": "நீலாங்கரையில் 19B பேருந்தில் ஏறவும்.",
                      "isRecommended": true,
                      "isStepFree": true
                    }
                 ],
                 "bestAdviceEn": "Take Direct Bus 19B for step-free travel.",
                 "bestAdviceTa": "19B பேருந்து சிறந்தது.",
                 "resolvedOriginStationId": "ecr_neelankarai",
                 "resolvedDestinationStationId": "triplicane"
              }
            }
        """.trimIndent()

        val preferenceInstruction = when (preference) {
            CommutePreference.STEP_FREE_ELDERLY -> "User requires Step-Free Elderly access: prioritize nearest stations with functioning elevators, zero-stair ramps, and shortest walking distances."
            CommutePreference.FASTEST_TRANSIT -> "User requires fastest transit: prioritize Metro and Suburban express over congested road routes."
            CommutePreference.BUDGET_SAVING -> "User is budget conscious: highlight Vidiyal Payanam free MTC buses and ₹5 suburban train options."
            CommutePreference.ALL_MODES -> "Provide balanced overview of Metro, Bus, Suburban rail, and Share Auto."
        }

        val userPrompt = """
            User prompt / plan: "$locationQuery" in Chennai.
            Preference: $preferenceInstruction
            Identify the nearest boarding point and destination points, and return valid JSON.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", userPrompt))
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.e(TAG, "Gemini API returned error code ${response.code}: ${response.body?.string()}")
            return null
        }

        val bodyString = response.body?.string() ?: return null
        return parseGeminiResponse(bodyString, locationQuery)
    }

    private fun parseGeminiResponse(bodyString: String, originalQuery: String): GeminiTransitRecommendation? {
        try {
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            val text = parts.getJSONObject(0).optString("text")
            val parsedJson = JSONObject(text)

            val metroObj = parsedJson.optJSONObject("nearestMetro")
            val busObj = parsedJson.optJSONObject("nearestBusStop")
            val railObj = parsedJson.optJSONObject("nearestSuburbanRail")
            val autoObj = parsedJson.optJSONObject("firstLastMile")
            val tripObj = parsedJson.optJSONObject("parsedTripPlan")

            val nearestMetro = metroObj?.let {
                NearestTransitPoint(
                    type = TransitMode.METRO,
                    nameEn = it.optString("nameEn", "Nearby Metro Station"),
                    nameTa = it.optString("nameTa", "அருகிலுள்ள மெட்ரோ நிலையம்"),
                    distanceEstimate = it.optString("distanceEstimate", "Approx 500m"),
                    walkMinutes = it.optInt("walkMinutes", 6),
                    detailsEn = it.optString("detailsEn", "Elevated/Underground station with lift"),
                    detailsTa = it.optString("detailsTa", "மின்தூக்கி வசதியுடன் கூடிய மெட்ரோ"),
                    keyRoutes = jsonArrayToStringList(it.optJSONArray("keyRoutes")),
                    hasElevator = it.optBoolean("hasElevator", true),
                    hasRamp = it.optBoolean("hasRamp", true),
                    matchedStationId = it.optString("matchedStationId").takeIf { s -> s.isNotBlank() }
                )
            }

            val nearestBusStop = busObj?.let {
                NearestTransitPoint(
                    type = TransitMode.BUS,
                    nameEn = it.optString("nameEn", "Nearby Bus Stop"),
                    nameTa = it.optString("nameTa", "அருகிலுள்ள பேருந்து நிறுத்தம்"),
                    distanceEstimate = it.optString("distanceEstimate", "Approx 200m"),
                    walkMinutes = it.optInt("walkMinutes", 3),
                    detailsEn = it.optString("detailsEn", "MTC Bus Shelter"),
                    detailsTa = it.optString("detailsTa", "மாநகர பேருந்து நிறுத்தம்"),
                    keyRoutes = jsonArrayToStringList(it.optJSONArray("keyRoutes")),
                    isFreeBusEligible = it.optBoolean("isFreeBusEligible", true),
                    matchedStationId = it.optString("matchedStationId").takeIf { s -> s.isNotBlank() }
                )
            }

            val nearestRail = railObj?.let {
                NearestTransitPoint(
                    type = TransitMode.SUBURBAN_RAIL,
                    nameEn = it.optString("nameEn", "Nearby Suburban Station"),
                    nameTa = it.optString("nameTa", "அருகிலுள்ள புறநகர் ரயில் நிலையம்"),
                    distanceEstimate = it.optString("distanceEstimate", "Approx 800m"),
                    walkMinutes = it.optInt("walkMinutes", 10),
                    detailsEn = it.optString("detailsEn", "Southern Railway EMU Station"),
                    detailsTa = it.optString("detailsTa", "தென்னக ரயில்வே புறநகர் நிலையம்"),
                    keyRoutes = jsonArrayToStringList(it.optJSONArray("keyRoutes")),
                    hasRamp = it.optBoolean("hasRamp", true),
                    matchedStationId = it.optString("matchedStationId").takeIf { s -> s.isNotBlank() }
                )
            }

            val firstLastMile = autoObj?.let {
                FirstLastMileCommute(
                    shareAutoTipEn = it.optString("shareAutoTipEn", "Share autos operate along the main arterial corridor (₹15-₹20)."),
                    shareAutoTipTa = it.optString("shareAutoTipTa", "பிரதான சாலையில் ஷேர் ஆட்டோ வசதி உள்ளது (₹15-₹20)."),
                    estimatedAutoFare = it.optString("estimatedAutoFare", "₹15 - ₹25"),
                    feederShuttleInfoEn = it.optString("feederShuttleInfoEn", "Feeder autos connect to the nearest metro station."),
                    feederShuttleInfoTa = it.optString("feederShuttleInfoTa", "மெட்ரோ ஃபீடர் சேவைகள் உள்ளன.")
                )
            }

            val parsedTripPlan = tripObj?.let {
                val bPointObj = it.optJSONObject("nearestBoardingPoint")
                val dPointObj = it.optJSONObject("nearestDestinationPoint")
                val bPoint = if (bPointObj != null) {
                    NearestTransitPoint(
                        type = TransitMode.BUS,
                        nameEn = bPointObj.optString("nameEn"),
                        nameTa = bPointObj.optString("nameTa"),
                        distanceEstimate = bPointObj.optString("distanceEstimate"),
                        walkMinutes = bPointObj.optInt("walkMinutes", 3),
                        detailsEn = bPointObj.optString("detailsEn"),
                        detailsTa = bPointObj.optString("detailsTa"),
                        keyRoutes = jsonArrayToStringList(bPointObj.optJSONArray("keyRoutes")),
                        isFreeBusEligible = bPointObj.optBoolean("isFreeBusEligible", true),
                        matchedStationId = bPointObj.optString("matchedStationId")
                    )
                } else nearestBusStop ?: NearestTransitPoint(TransitMode.BUS, "Boarding Point", "ஏறும் இடம்", "200m", 3, "", "")

                val dPoint = if (dPointObj != null) {
                    NearestTransitPoint(
                        type = TransitMode.BUS,
                        nameEn = dPointObj.optString("nameEn"),
                        nameTa = dPointObj.optString("nameTa"),
                        distanceEstimate = dPointObj.optString("distanceEstimate"),
                        walkMinutes = dPointObj.optInt("walkMinutes", 3),
                        detailsEn = dPointObj.optString("detailsEn"),
                        detailsTa = dPointObj.optString("detailsTa"),
                        keyRoutes = jsonArrayToStringList(dPointObj.optJSONArray("keyRoutes")),
                        matchedStationId = dPointObj.optString("matchedStationId")
                    )
                } else NearestTransitPoint(TransitMode.BUS, "Destination Point", "சேரும் இடம்", "250m", 3, "", "")

                val tripOptionsList = mutableListOf<CommuteTripOption>()
                val arr = it.optJSONArray("tripOptions")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val opt = arr.getJSONObject(i)
                        tripOptionsList.add(
                            CommuteTripOption(
                                titleEn = opt.optString("titleEn"),
                                titleTa = opt.optString("titleTa"),
                                mode = when (opt.optString("mode").uppercase()) {
                                    "METRO" -> TransitMode.METRO
                                    "SUBURBAN_RAIL" -> TransitMode.SUBURBAN_RAIL
                                    else -> TransitMode.BUS
                                },
                                routeCode = opt.optString("routeCode"),
                                durationMins = opt.optInt("durationMins", 30),
                                fare = opt.optString("fare", "₹20"),
                                instructionsEn = opt.optString("instructionsEn"),
                                instructionsTa = opt.optString("instructionsTa"),
                                isRecommended = opt.optBoolean("isRecommended", i == 0),
                                isStepFree = opt.optBoolean("isStepFree", true)
                            )
                        )
                    }
                }

                GeminiParsedTripPlan(
                    promptText = originalQuery,
                    originQuery = it.optString("originQuery"),
                    destinationQuery = it.optString("destinationQuery"),
                    nearestBoardingPoint = bPoint,
                    nearestDestinationPoint = dPoint,
                    tripOptions = tripOptionsList,
                    bestAdviceEn = it.optString("bestAdviceEn"),
                    bestAdviceTa = it.optString("bestAdviceTa"),
                    resolvedOriginStationId = it.optString("resolvedOriginStationId"),
                    resolvedDestinationStationId = it.optString("resolvedDestinationStationId")
                )
            }

            val suggestedOrigin = parsedJson.optString("suggestedOriginStationId")

            return GeminiTransitRecommendation(
                queriedLocation = parsedJson.optString("queriedLocation", originalQuery),
                summaryEn = parsedJson.optString("summaryEn", "Transit options found near $originalQuery."),
                summaryTa = parsedJson.optString("summaryTa", "$originalQuery அருகிலுள்ள போக்குவரத்து விருப்பங்கள்."),
                nearestMetro = nearestMetro,
                nearestBusStop = nearestBusStop,
                nearestSuburbanRail = nearestRail,
                firstLastMile = firstLastMile,
                accessibilityVerdictEn = parsedJson.optString("accessibilityVerdictEn", "Step-free facilities available."),
                accessibilityVerdictTa = parsedJson.optString("accessibilityVerdictTa", "மின்தூக்கி வசதிகள் உள்ளன."),
                recommendedBestOptionEn = parsedJson.optString("recommendedBestOptionEn", "Take MTC Bus or Metro."),
                recommendedBestOptionTa = parsedJson.optString("recommendedBestOptionTa", "மெட்ரோ அல்லது பேருந்தை பயன்படுத்தவும்."),
                suggestedOriginStationId = suggestedOrigin.takeIf { it.isNotBlank() },
                isAiGenerated = true,
                isDemoFallback = false,
                parsedTripPlan = parsedTripPlan
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response: ${e.message}", e)
            return null
        }
    }

    private fun jsonArrayToStringList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            list.add(arr.optString(i))
        }
        return list
    }

    /**
     * Extracts origin and destination from conversational natural language prompts like:
     * - "i wanna go from neelankari to triplicane"
     * - "tell me whats todays plan -> from neelankari to triplicane"
     * - "neelankarai to triplicane"
     */
    fun extractJourneyEndpoints(prompt: String): Pair<String, String>? {
        val clean = prompt.lowercase()
            .replace("ai -->", "")
            .replace("user -->", "")
            .replace("tell me whats todays plan", "")
            .replace("tell me what's today's plan", "")
            .replace("tell me today's plan", "")
            .replace("i wanna go from", "from")
            .replace("i want to go from", "from")
            .replace("how to reach", "to")
            .replace("how to go from", "from")
            .replace("take me from", "from")
            .trim()

        // Pattern: from {origin} to {dest}
        val fromToIndex = clean.indexOf("from ")
        val toIndex = clean.indexOf(" to ")

        if (fromToIndex != -1 && toIndex > fromToIndex) {
            val origin = clean.substring(fromToIndex + 5, toIndex).trim()
            val dest = clean.substring(toIndex + 4).trim()
            if (origin.isNotBlank() && dest.isNotBlank()) {
                return Pair(origin, dest)
            }
        }

        // Pattern: {origin} to {dest}
        if (toIndex != -1) {
            val origin = clean.substring(0, toIndex).trim()
            val dest = clean.substring(toIndex + 4).trim()
            if (origin.isNotBlank() && dest.isNotBlank()) {
                return Pair(origin, dest)
            }
        }

        return null
    }

    /**
     * High-fidelity Chennai Transit Resolver for offline/demo/instant responses
     */
    fun resolveOfflineChennaiTransit(
        query: String,
        preference: CommutePreference,
        isDemo: Boolean = true
    ): GeminiTransitRecommendation {
        val q = query.lowercase().trim()

        // Check if user entered a conversational journey plan like "i wanna go from neelankari to triplicane"
        val extractedEndpoints = extractJourneyEndpoints(query)
        if (extractedEndpoints != null) {
            val (originRaw, destRaw) = extractedEndpoints
            return resolveConversationalTrip(query, originRaw, destRaw, preference, isDemo)
        }

        // Direct check for "neelankari" / "neelankarai"
        if (q.contains("neelankari") || q.contains("neelankarai")) {
            return buildNeelankaraiCommuteRecommendation(query, isDemo)
        }

        // 1. ECR Corridor (East Coast Road)
        if (q.contains("ecr") || q.contains("beach") || q.contains("kottivakkam") || q.contains("palavakkam") ||
            q.contains("injambakkam") || q.contains("akkarai") || q.contains("kovalam") || q.contains("prarthana")) {
            return buildNeelankaraiCommuteRecommendation(query, isDemo)
        }

        // 2. Marina Beach / Santhome / Mylapore / Triplicane
        if (q.contains("marina") || q.contains("mylapore") || q.contains("santhome") || q.contains("triplicane") || q.contains("thiruvallikeni") || q.contains("temple") || q.contains("chepauk")) {
            return buildTriplicaneCommuteRecommendation(query, isDemo)
        }

        // 3. Guindy / Little Mount / Saidapet
        if (q.contains("guindy") || q.contains("saidapet") || q.contains("little mount") || q.contains("kathipara") || q.contains("anna university")) {
            return buildGuindyCommuteRecommendation(query, isDemo)
        }

        // 4. Central / George Town / Park
        if (q.contains("central") || q.contains("mgr") || q.contains("park town") || q.contains("broadway") || q.contains("parrys") || q.contains("george town")) {
            return buildCentralCommuteRecommendation(query, isDemo)
        }

        // 5. Default matched from repository stations
        val matchedStation = ChennaiTransitRepository.STATIONS.firstOrNull {
            q.contains(it.nameEn.lowercase()) || q.contains(it.locality.lowercase()) || it.locality.lowercase().contains(q)
        } ?: ChennaiTransitRepository.STATIONS.first { it.id == "ecr_neelankarai" }

        return GeminiTransitRecommendation(
            queriedLocation = query,
            summaryEn = "Located near ${matchedStation.nameEn}. Multi-modal CUMTA transit hub with bus and suburban connectivity.",
            summaryTa = "${matchedStation.nameTa} அருகில் அமைந்துள்ளது. மாநகர பேருந்து மற்றும் ரயில் இணைப்பு வசதி.",
            nearestMetro = NearestTransitPoint(
                type = TransitMode.METRO,
                nameEn = "Puratchi Thalaivar Dr. M.G.R Central / Guindy Metro",
                nameTa = "சென்ட்ரல் / கிண்டி மெட்ரோ",
                distanceEstimate = "1.2 km (5 min auto)",
                walkMinutes = 14,
                detailsEn = "Elevated/Underground station with elevators, ramps, and tactile paving.",
                detailsTa = "மின்தூக்கி மற்றும் சாய்தள வசதிகளுடன் கூடிய மெட்ரோ நிலையம்.",
                keyRoutes = listOf("Blue Line", "Green Line"),
                hasElevator = true,
                hasRamp = true,
                matchedStationId = "guindy"
            ),
            nearestBusStop = NearestTransitPoint(
                type = TransitMode.BUS,
                nameEn = matchedStation.nameEn + " Bus Stop",
                nameTa = matchedStation.nameTa + " பேருந்து நிறுத்தம்",
                distanceEstimate = "120 meters (2 min walk)",
                walkMinutes = 2,
                detailsEn = "MTC stop on ${matchedStation.locality}. Vidiyal Payanam free bus scheme active for women.",
                detailsTa = "${matchedStation.locality} பகுதியில் உள்ள பேருந்து நிறுத்தம்.",
                keyRoutes = listOf("18A", "29C", "21G", "19B", "570"),
                isFreeBusEligible = true,
                matchedStationId = matchedStation.id
            ),
            nearestSuburbanRail = NearestTransitPoint(
                type = TransitMode.SUBURBAN_RAIL,
                nameEn = "Chennai Suburban Railway",
                nameTa = "சென்னை புறநகர் ரயில்",
                distanceEstimate = "950 meters (12 min walk)",
                walkMinutes = 12,
                detailsEn = "Suburban EMU trains across the city for ₹5 to ₹10.",
                detailsTa = "நகரெங்கும் செல்லும் ₹5 முதல் ₹10 வரையிலான புறநகர் ரயில்.",
                keyRoutes = listOf("Suburban EMU Local"),
                hasRamp = true,
                matchedStationId = matchedStation.id
            ),
            firstLastMile = FirstLastMileCommute(
                shareAutoTipEn = "Share autos operate along the main road corridor with flat fares between ₹15 and ₹20.",
                shareAutoTipTa = "பிரதான சாலையில் ₹15 முதல் ₹20 கட்டணத்தில் ஷேர் ஆட்டோக்கள் உள்ளன.",
                estimatedAutoFare = "₹15 - ₹20",
                feederShuttleInfoEn = "Feeder share autos and MTC connector buses available outside the station.",
                feederShuttleInfoTa = "நிலையத்திற்கு வெளியே ஃபீடர் ஆட்டோக்கள் கிடைக்கின்றன."
            ),
            accessibilityVerdictEn = "Station equipped with ramps and street level boarding. For elevator access, use the Metro entrance.",
            accessibilityVerdictTa = "சாய்தள வசதி உண்டு. மின்தூக்கிக்கு மெட்ரோ நுழைவாயிலை பயன்படுத்தவும்.",
            recommendedBestOptionEn = "Take the Metro for air-conditioned step-free speed or MTC bus for direct point-to-point transit.",
            recommendedBestOptionTa = "மின்தூக்கி வசதியுடன் கூடிய மெட்ரோ அல்லது நேரடி MTC பேருந்தை தேர்வு செய்யவும்.",
            suggestedOriginStationId = matchedStation.id,
            isAiGenerated = false,
            isDemoFallback = isDemo
        )
    }

    private fun resolveConversationalTrip(
        fullPrompt: String,
        originRaw: String,
        destRaw: String,
        preference: CommutePreference,
        isDemo: Boolean
    ): GeminiTransitRecommendation {
        val o = originRaw.lowercase()
        val d = destRaw.lowercase()

        val isOriginNeelankarai = o.contains("neelankari") || o.contains("neelankarai") || o.contains("ecr")
        val isDestTriplicane = d.contains("triplicane") || d.contains("thiruvallikeni")

        val boardingPoint = if (isOriginNeelankarai) {
            NearestTransitPoint(
                type = TransitMode.BUS,
                nameEn = "Neelankarai ECR Bus Stop (Police Station Junction)",
                nameTa = "நீலாங்கரை ECR பேருந்து நிறுத்தம் (காவல் நிலையம் அருகில்)",
                distanceEstimate = "180 meters (2 min walk)",
                walkMinutes = 2,
                detailsEn = "Level pedestrian footpath on ECR. High frequency MTC buses towards Adyar, Triplicane, Broadway arrive every 4 mins.",
                detailsTa = "ECR பிரதான தரைத்தள நடைபாதை. அடையாறு, திருவல்லிக்கேணி செல்லும் பேருந்துகள் 4 நிமிடத்திற்கு ஒன்று.",
                keyRoutes = listOf("19B", "29C", "102", "588"),
                hasRamp = true,
                isFreeBusEligible = true,
                matchedStationId = "ecr_neelankarai"
            )
        } else {
            NearestTransitPoint(
                type = TransitMode.BUS,
                nameEn = "${originRaw.replaceFirstChar { it.uppercase() }} Boarding Stop",
                nameTa = "${originRaw.replaceFirstChar { it.uppercase() }} ஏறும் நிறுத்தம்",
                distanceEstimate = "200 meters (3 min walk)",
                walkMinutes = 3,
                detailsEn = "Direct boarding stop near $originRaw.",
                detailsTa = "$originRaw அருகில் உள்ள நேரடி பேருந்து நிறுத்தம்.",
                keyRoutes = listOf("MTC Bus", "Feeder Auto"),
                hasRamp = true,
                isFreeBusEligible = true,
                matchedStationId = "ecr_neelankarai"
            )
        }

        val destinationPoint = if (isDestTriplicane) {
            NearestTransitPoint(
                type = TransitMode.SUBURBAN_RAIL,
                nameEn = "Triplicane (Thiruvallikeni MRTS & Ice House Bus Stop)",
                nameTa = "திருவல்லிக்கேணி (ஐஸ் அவுஸ் & MRTS நிலையம்)",
                distanceEstimate = "250 meters (3 min walk)",
                walkMinutes = 3,
                detailsEn = "Arrive near Parthasarathy Temple, Pycrofts Road & Ice House. 800m to Government Estate Blue Line Metro.",
                detailsTa = "பார்த்தசாரதி கோயில், பைகிராப்ட்ஸ் ரோடு மற்றும் ஐஸ் அவுஸ் அருகில் இறங்கும் வசதி.",
                keyRoutes = listOf("19B", "21G", "11G", "MRTS Suburban"),
                hasRamp = true,
                matchedStationId = "triplicane"
            )
        } else {
            NearestTransitPoint(
                type = TransitMode.BUS,
                nameEn = "${destRaw.replaceFirstChar { it.uppercase() }} Alighting Stop",
                nameTa = "${destRaw.replaceFirstChar { it.uppercase() }} இறங்கும் நிறுத்தம்",
                distanceEstimate = "300 meters (4 min walk)",
                walkMinutes = 4,
                detailsEn = "Arrive at $destRaw transit hub.",
                detailsTa = "$destRaw பேருந்து நிறுத்தம்.",
                keyRoutes = listOf("MTC Direct", "Metro Link"),
                hasRamp = true,
                matchedStationId = "triplicane"
            )
        }

        val tripOptions = if (isOriginNeelankarai && isDestTriplicane) {
            listOf(
                CommuteTripOption(
                    titleEn = "Direct MTC Bus (Route 19B / 21G)",
                    titleTa = "நேரடி MTC பேருந்து (தடம் 19B / 21G)",
                    mode = TransitMode.BUS,
                    routeCode = "19B / 21G Direct",
                    durationMins = 42,
                    fare = "₹20 (Free for Women / Senior Vidiyal Payanam)",
                    instructionsEn = "Board MTC bus 19B or 21G directly at Neelankarai ECR stop. Runs via Kottivakkam, Thiruvanmiyur, Adyar, Light House, and alights right at Triplicane Ice House Stop. 4 min walk to Temple / High Road.",
                    instructionsTa = "நீலாங்கரையில் 19B அல்லது 21G பேருந்தில் ஏறி, அடையாறு, சாந்தோம் வழியாக திருவல்லிக்கேணி ஐஸ் அவுஸில் இறங்கவும். நேரடி பயணம்.",
                    isRecommended = true,
                    isStepFree = true
                ),
                CommuteTripOption(
                    titleEn = "Share Auto + MRTS Local Train (Fast & Budget)",
                    titleTa = "ஷேர் ஆட்டோ + பறக்கும் ரயில் (வேகமான பயணம்)",
                    mode = TransitMode.SUBURBAN_RAIL,
                    routeCode = "Share Auto + MRTS Train",
                    durationMins = 34,
                    fare = "₹20 Auto + ₹5 Train (₹25 Total)",
                    instructionsEn = "Take flat ₹20 share auto from Neelankarai to Thiruvanmiyur MRTS station (10 mins). Board Beach-bound EMU train and alight directly at Thiruvallikeni MRTS station (14 mins).",
                    instructionsTa = "நீலாங்கரையில் இருந்து திருவான்மியூர் பறக்கும் ரயில் நிலையத்திற்கு ₹20 ஷேர் ஆட்டோ. பின் பீச் செல்லும் ரயிலில் ஏறி திருவல்லிக்கேணி நிலையத்தில் இறங்கவும்.",
                    isRecommended = false,
                    isStepFree = false
                ),
                CommuteTripOption(
                    titleEn = "Feeder to Guindy Metro + Blue Line",
                    titleTa = "கிண்டி மெட்ரோ + நீல வழித்தடம்",
                    mode = TransitMode.METRO,
                    routeCode = "Blue Line Metro",
                    durationMins = 38,
                    fare = "₹40 (Metro)",
                    instructionsEn = "Take feeder bus or cab to Guindy Metro Station. Board Blue Line towards Wimco Nagar and alight at Government Estate Metro Station. 8 min walk or ₹15 share auto to Triplicane.",
                    instructionsTa = "கிண்டி மெட்ரோவில் ஏறி, கவர்ன்மென்ட் எஸ்டேட் மெட்ரோவில் இறங்கி திருவல்லிக்கேணி செல்லவும்.",
                    isRecommended = false,
                    isStepFree = true
                )
            )
        } else {
            listOf(
                CommuteTripOption(
                    titleEn = "Direct Point-to-Point MTC City Bus",
                    titleTa = "நேரடி மாநகர பேருந்து",
                    mode = TransitMode.BUS,
                    routeCode = "MTC Direct",
                    durationMins = 38,
                    fare = "₹15 - ₹20 (Free for Women)",
                    instructionsEn = "Board direct bus from $originRaw to $destRaw along the arterial corridor.",
                    instructionsTa = "$originRaw விலிருந்து $destRaw வரை நேரடி பேருந்து பயணம்.",
                    isRecommended = true,
                    isStepFree = true
                ),
                CommuteTripOption(
                    titleEn = "Combined Metro & Feeder Transit",
                    titleTa = "மெட்ரோ மற்றும் ஃபீடர் பயணம்",
                    mode = TransitMode.METRO,
                    routeCode = "CMRL Metro",
                    durationMins = 30,
                    fare = "₹35",
                    instructionsEn = "Feeder to nearest Metro station, travel step-free to $destRaw.",
                    instructionsTa = "அருகிலுள்ள மெட்ரோவில் ஏறி $destRaw செல்லவும்.",
                    isRecommended = false,
                    isStepFree = true
                )
            )
        }

        val parsedTripPlan = GeminiParsedTripPlan(
            promptText = fullPrompt,
            originQuery = originRaw.replaceFirstChar { it.uppercase() },
            destinationQuery = destRaw.replaceFirstChar { it.uppercase() },
            nearestBoardingPoint = boardingPoint,
            nearestDestinationPoint = destinationPoint,
            tripOptions = tripOptions,
            bestAdviceEn = "For step-free comfort, take Direct MTC Bus 19B (₹20, Free for women). For speed, take Share Auto to Thiruvanmiyur MRTS then Train to Thiruvallikeni.",
            bestAdviceTa = "முதியோருக்கு நேரடி MTC 19B பேருந்து சிறந்தது (பெண்களுக்கு இலவசம்). விரைவாக செல்ல திருவான்மியூர் பறக்கும் ரயில் சிறந்தது.",
            resolvedOriginStationId = if (isOriginNeelankarai) "ecr_neelankarai" else null,
            resolvedDestinationStationId = if (isDestTriplicane) "triplicane" else null
        )

        return GeminiTransitRecommendation(
            queriedLocation = "$originRaw to $destRaw",
            summaryEn = "Planned trip from ${parsedTripPlan.originQuery} to ${parsedTripPlan.destinationQuery}. Nearest boarding is ${boardingPoint.nameEn} (180m away).",
            summaryTa = "${parsedTripPlan.originQuery} முதல் ${parsedTripPlan.destinationQuery} வரையிலான பயண திட்டம். ஏறும் இடம்: ${boardingPoint.nameTa}.",
            nearestMetro = NearestTransitPoint(
                type = TransitMode.METRO,
                nameEn = "Guindy / Little Mount Metro (Feeder Link)",
                nameTa = "கிண்டி / லிட்டில் மவுண்ட் மெட்ரோ",
                distanceEstimate = "7.5 km (Direct feeder bus 19B connects in 22 mins)",
                walkMinutes = 22,
                detailsEn = "Elevated Blue line station with verified street-to-platform elevators & tactile tiles.",
                detailsTa = "நீல வழித்தட மெட்ரோ; தரைத்தளத்திலிருந்து பிளாட்பாரத்திற்கு மின்தூக்கி வசதி.",
                keyRoutes = listOf("Blue Line (Airport to Wimco Nagar)"),
                hasElevator = true,
                hasRamp = true,
                matchedStationId = "guindy"
            ),
            nearestBusStop = boardingPoint,
            nearestSuburbanRail = destinationPoint,
            firstLastMile = FirstLastMileCommute(
                shareAutoTipEn = "Share autos ply continuously along ECR between Neelankarai and Thiruvanmiyur MRTS (Flat ₹20 fare).",
                shareAutoTipTa = "நீலாங்கரை - திருவான்மியூர் இடையே தொடர் ஷேர் ஆட்டோ வசதி (₹20 கட்டணம்).",
                estimatedAutoFare = "₹15 - ₹20",
                feederShuttleInfoEn = "Direct connection on MTC 19B to Triplicane Ice House Stop every 6 mins.",
                feederShuttleInfoTa = "திருவல்லிக்கேணிக்கு நேரடி பேருந்து 19B."
            ),
            accessibilityVerdictEn = "Direct MTC Bus 19B has ground level boarding on ECR. Destination stop in Triplicane has paved pedestrian footpaths.",
            accessibilityVerdictTa = "நீலாங்கரை பேருந்து நிறுத்தம் தரைத்தள அணுகல் கொண்டது. திருவல்லிக்கேணியில் நடைபாதை வசதி உள்ளது.",
            recommendedBestOptionEn = "Direct MTC Bus 19B / 21G is the most convenient direct route from Neelankarai to Triplicane.",
            recommendedBestOptionTa = "நீலாங்கரையிலிருந்து திருவல்லிக்கேணி செல்ல நேரடி MTC 19B / 21G பேருந்து மிகவும் சிறந்தது.",
            suggestedOriginStationId = "ecr_neelankarai",
            isAiGenerated = false,
            isDemoFallback = isDemo,
            parsedTripPlan = parsedTripPlan
        )
    }

    private fun buildNeelankaraiCommuteRecommendation(query: String, isDemo: Boolean): GeminiTransitRecommendation {
        return GeminiTransitRecommendation(
            queriedLocation = query,
            summaryEn = "Located on the East Coast Road (ECR) coastal transit corridor. High-frequency MTC buses run directly towards Thiruvanmiyur, Adyar, Triplicane, and Broadway.",
            summaryTa = "கிழக்கு கடற்கரை சாலை (ECR) வழித்தடம். திருவான்மியூர், அடையாறு, திருவல்லிக்கேணி, பிராட்வே செல்லும் MTC பேருந்துகள் அதிக அளவில் இயங்குகின்றன.",
            nearestMetro = NearestTransitPoint(
                type = TransitMode.METRO,
                nameEn = "Guindy Metro / Little Mount Metro (via link)",
                nameTa = "கிண்டி / லிட்டில் மவுண்ட் மெட்ரோ",
                distanceEstimate = "7.5 km (Direct feeder bus 19B connects in 22 mins)",
                walkMinutes = 22,
                detailsEn = "Elevated Blue line station with verified street-to-platform elevators & tactile tiles.",
                detailsTa = "நீல வழித்தட மெட்ரோ; தரைத்தளத்திலிருந்து பிளாட்பாரத்திற்கு மின்தூக்கி வசதி.",
                keyRoutes = listOf("Blue Line (Airport to Wimco Nagar)"),
                hasElevator = true,
                hasRamp = true,
                matchedStationId = "guindy"
            ),
            nearestBusStop = NearestTransitPoint(
                type = TransitMode.BUS,
                nameEn = "Neelankarai ECR Bus Stop (Beach Junction)",
                nameTa = "நீலாங்கரை ECR பேருந்து நிறுத்தம்",
                distanceEstimate = "180 meters (2 min walk)",
                walkMinutes = 2,
                detailsEn = "Level footpath and street lighting. Frequent buses every 4-6 minutes towards Adyar and Central.",
                detailsTa = "தரைத்தள நடைபாதை; 4-6 நிமிடங்களுக்கு ஒரு பேருந்து.",
                keyRoutes = listOf("19B (Broadway - Kelambakkam)", "102 (Broadway - Kelambakkam)", "588 (Adyar - Mahabalipuram)", "29C (Besant Nagar link)"),
                isFreeBusEligible = true,
                matchedStationId = "ecr_neelankarai"
            ),
            nearestSuburbanRail = NearestTransitPoint(
                type = TransitMode.SUBURBAN_RAIL,
                nameEn = "Thiruvanmiyur MRTS Station",
                nameTa = "திருவான்மியூர் MRTS பறக்கும் ரயில்",
                distanceEstimate = "3.2 km (8 min bus ride)",
                walkMinutes = 8,
                detailsEn = "Elevated railway directly connecting Beach, Mylapore, and Velachery for ₹5.",
                detailsTa = "சென்னை பீச், மயிலாப்பூர், வேளச்சேரி செல்லும் ₹5 கட்டண பறக்கும் ரயில்.",
                keyRoutes = listOf("Chennai Beach - Velachery MRTS"),
                hasRamp = true,
                matchedStationId = "ecr_thiruvanmiyur"
            ),
            firstLastMile = FirstLastMileCommute(
                shareAutoTipEn = "Share autos ply continuously along ECR between Thiruvanmiyur and Sholinganallur / Kovalam (Flat ₹20 fare).",
                shareAutoTipTa = "திருவான்மியூர் - சோழிங்கநல்லூர் இடையே தொடர் ஷேர் ஆட்டோ வசதி (₹20 கட்டணம்).",
                estimatedAutoFare = "₹15 - ₹20",
                feederShuttleInfoEn = "Feeder bus 19B to Guindy Metro & Adyar Depot every 8 mins.",
                feederShuttleInfoTa = "கிண்டி மெட்ரோவிற்கு நேரடி இணைப்பு பேருந்து 19B."
            ),
            accessibilityVerdictEn = "Bus stops on ECR have ground-level boarding with pedestrian crossings. Metro at Guindy provides full step-free access.",
            accessibilityVerdictTa = "ECR பேருந்து நிறுத்தங்கள் தரைத்தள அணுகல் கொண்டவை. கிண்டி மெட்ரோவில் மின்தூக்கி வசதி உள்ளது.",
            recommendedBestOptionEn = "Take MTC Bus 19B or 102 directly to Thiruvanmiyur, or take Share Auto for ₹20.",
            recommendedBestOptionTa = "திருவான்மியூர் செல்ல MTC 19B / 102 பேருந்து அல்லது ₹20 ஷேர் ஆட்டோவை தேர்வு செய்யவும்.",
            suggestedOriginStationId = "ecr_neelankarai",
            isAiGenerated = false,
            isDemoFallback = isDemo
        )
    }

    private fun buildTriplicaneCommuteRecommendation(query: String, isDemo: Boolean): GeminiTransitRecommendation {
        return GeminiTransitRecommendation(
            queriedLocation = query,
            summaryEn = "Located in the historic Triplicane / Marina corridor. Served directly by Thiruvallikeni MRTS, Ice House Bus Terminus, and Government Estate Blue Line Metro.",
            summaryTa = "வரலாற்று சிறப்புமிக்க திருவல்லிக்கேணி / மெரினா பகுதி. திருவல்லிக்கேணி MRTS ரயில், ஐஸ் அவுஸ் பேருந்து மற்றும் மெட்ரோ இணைப்பு உள்ளது.",
            nearestMetro = NearestTransitPoint(
                type = TransitMode.METRO,
                nameEn = "Government Estate Metro (Blue Line)",
                nameTa = "கவர்ன்மென்ட் எஸ்டேட் மெட்ரோ (நீல வழித்தடம்)",
                distanceEstimate = "850 meters (10 min walk or ₹15 share auto)",
                walkMinutes = 10,
                detailsEn = "Underground station on Anna Salai with functioning lifts directly connecting Airport, Central, and Wimco Nagar.",
                detailsTa = "அண்ணா சாலையில் அமைந்துள்ள நிலத்தடி மெட்ரோ; மின்தூக்கி வசதியுடன் விமான நிலையம் & சென்ட்ரல் இணைப்பு.",
                keyRoutes = listOf("Blue Line (Airport - Wimco Nagar)"),
                hasElevator = true,
                hasRamp = true,
                matchedStationId = "central"
            ),
            nearestBusStop = NearestTransitPoint(
                type = TransitMode.BUS,
                nameEn = "Triplicane Ice House / High Road Bus Stop",
                nameTa = "திருவல்லிக்கேணி ஐஸ் அவுஸ் பேருந்து நிறுத்தம்",
                distanceEstimate = "150 meters (2 min walk)",
                walkMinutes = 2,
                detailsEn = "High-frequency MTC stop connecting to Central, T. Nagar, Adyar, and ECR Neelankarai.",
                detailsTa = "சென்ட்ரல், தி.நகர், அடையாறு, நீலாங்கரை செல்லும் பேருந்து நிறுத்தம்.",
                keyRoutes = listOf("19B", "21G", "11G", "29C"),
                isFreeBusEligible = true,
                matchedStationId = "triplicane"
            ),
            nearestSuburbanRail = NearestTransitPoint(
                type = TransitMode.SUBURBAN_RAIL,
                nameEn = "Thiruvallikeni (Triplicane) MRTS Station",
                nameTa = "திருவல்லிக்கேணி MRTS பறக்கும் ரயில் நிலையம்",
                distanceEstimate = "400 meters (5 min walk)",
                walkMinutes = 5,
                detailsEn = "Elevated MRTS station directly connecting Chennai Beach and Velachery for ₹5.",
                detailsTa = "சென்னை பீச் மற்றும் வேளச்சேரி செல்லும் ₹5 கட்டண பறக்கும் ரயில்.",
                keyRoutes = listOf("Chennai Beach - Velachery MRTS"),
                hasRamp = true,
                matchedStationId = "triplicane"
            ),
            firstLastMile = FirstLastMileCommute(
                shareAutoTipEn = "Share autos operate frequently between Ice House, Triplicane High Road, and Anna Salai / Express Avenue (₹15).",
                shareAutoTipTa = "ஐஸ் அவுஸ் மற்றும் அண்ணா சாலை இடையே ₹15 கட்டணத்தில் தொடர் ஷேர் ஆட்டோ வசதி.",
                estimatedAutoFare = "₹15 - ₹20",
                feederShuttleInfoEn = "Connecting bus 21G to Anna Salai Metro every 5 mins.",
                feederShuttleInfoTa = "மெட்ரோவிற்கு இணைப்பு பேருந்து 21G."
            ),
            accessibilityVerdictEn = "Government Estate Metro provides step-free lift access. Thiruvallikeni MRTS has ramped access to ticket hall.",
            accessibilityVerdictTa = "கவர்ன்மென்ட் எஸ்டேட் மெட்ரோவில் முழு மின்தூக்கி வசதி உள்ளது.",
            recommendedBestOptionEn = "Take Thiruvallikeni MRTS for ₹5 rapid access or MTC Bus 19B / 21G for direct street-level boarding.",
            recommendedBestOptionTa = "₹5 கட்டணத்தில் பறக்கும் ரயில் அல்லது நேரடி MTC பேருந்து 19B / 21G பயன்படுத்தவும்.",
            suggestedOriginStationId = "triplicane",
            isAiGenerated = false,
            isDemoFallback = isDemo
        )
    }

    private fun buildGuindyCommuteRecommendation(query: String, isDemo: Boolean): GeminiTransitRecommendation {
        return GeminiTransitRecommendation(
            queriedLocation = query,
            summaryEn = "Guindy is South Chennai's premier multi-modal interchange linking Metro Blue Line, Suburban EMU, and MTC bus terminals.",
            summaryTa = "கிண்டி: மெட்ரோ நீல வழித்தடம், புறநகர் ரயில் மற்றும் பேருந்து முனையத்தை இணைக்கும் பிரதான சந்திப்பு.",
            nearestMetro = NearestTransitPoint(
                type = TransitMode.METRO,
                nameEn = "Guindy Metro Station",
                nameTa = "கிண்டி மெட்ரோ நிலையம்",
                distanceEstimate = "80 meters (1 min walk)",
                walkMinutes = 1,
                detailsEn = "Elevated station with 3 verified lifts, escalator, and tactile guiding tiles. Direct link to Suburban platform.",
                detailsTa = "3 மின்தூக்கிகள் மற்றும் எஸ்கலேட்டர் வசதியுடன் கூடிய மெட்ரோ நிலையம்.",
                keyRoutes = listOf("Blue Line (Airport - Wimco Nagar)"),
                hasElevator = true,
                hasRamp = true,
                matchedStationId = "guindy"
            ),
            nearestBusStop = NearestTransitPoint(
                type = TransitMode.BUS,
                nameEn = "Guindy Industrial Estate Bus Terminus",
                nameTa = "கிண்டி எஸ்டேட் பேருந்து முனையம்",
                distanceEstimate = "150 meters (2 min walk)",
                walkMinutes = 2,
                detailsEn = "Major origin terminus for buses across Chennai. Vidiyal Payanam free bus scheme available on ordinary buses.",
                detailsTa = "நகரெங்கும் செல்லும் மாநகர பேருந்துகளின் தொடக்க முனையம்.",
                keyRoutes = listOf("18A", "M1", "570", "19B", "21G"),
                isFreeBusEligible = true,
                matchedStationId = "guindy"
            ),
            nearestSuburbanRail = NearestTransitPoint(
                type = TransitMode.SUBURBAN_RAIL,
                nameEn = "Guindy Suburban Railway Station",
                nameTa = "கிண்டி புறநகர் ரயில் நிலையம்",
                distanceEstimate = "120 meters (2 min walk via skywalk)",
                walkMinutes = 2,
                detailsEn = "Frequent EMU trains between Chennai Beach, Tambaram, and Chengalpattu for ₹5 to ₹10.",
                detailsTa = "சென்னை பீச் - தாம்பரம் வழித்தட புறநகர் ரயில்.",
                keyRoutes = listOf("Chennai Beach - Tambaram EMU"),
                hasElevator = true,
                hasRamp = true,
                matchedStationId = "guindy"
            ),
            firstLastMile = FirstLastMileCommute(
                shareAutoTipEn = "Share autos operate from Guindy Kathipara to Porur, DLF IT Park, and Poonamallee (₹20-₹25).",
                shareAutoTipTa = "கிண்டியிலிருந்து போரூர், DLF ஐடி பார்க் செல்லும் ஷேர் ஆட்டோக்கள் (₹20-₹25).",
                estimatedAutoFare = "₹20 - ₹25",
                feederShuttleInfoEn = "CMRL Feeder mini-buses connect to DLF IT Park and Ramapuram every 10 mins.",
                feederShuttleInfoTa = "DLF ஐடி பார்க்கிற்கு மெட்ரோ ஃபீடர் மினி பேருந்துகள் உள்ளன."
            ),
            accessibilityVerdictEn = "Full step-free rating. Working elevators link the metro concourse to street level and suburban skywalk.",
            accessibilityVerdictTa = "முழுமையான சாய்தளம் மற்றும் மின்தூக்கி வசதி. முதியோருக்கான சிறந்த சந்திப்பு.",
            recommendedBestOptionEn = "Guindy Metro Blue Line provides the fastest air-conditioned ride to Central (16 mins) and Airport (9 mins).",
            recommendedBestOptionTa = "சென்ட்ரல் செல்ல கிண்டி மெட்ரோ (16 நிமிடங்கள்), அல்லது தாம்பரம் செல்ல புறநகர் ரயில்.",
            suggestedOriginStationId = "guindy",
            isAiGenerated = false,
            isDemoFallback = isDemo
        )
    }

    private fun buildCentralCommuteRecommendation(query: String, isDemo: Boolean): GeminiTransitRecommendation {
        return GeminiTransitRecommendation(
            queriedLocation = query,
            summaryEn = "Puratchi Thalaivar Dr. M.G.R Central is the ultimate transit nerve center with Blue & Green Metro interchange, suburban terminals, and interstate railways.",
            summaryTa = "புரட்சித் தலைவர் டாக்டர் எம்.ஜி.ஆர் சென்ட்ரல்: மெட்ரோ இரண்டு வழித்தடங்கள், புறநகர் ரயில்கள் மற்றும் பேருந்துகளின் முதன்மை மையம்.",
            nearestMetro = NearestTransitPoint(
                type = TransitMode.METRO,
                nameEn = "Puratchi Thalaivar Dr. M.G.R Central Metro",
                nameTa = "எம்.ஜி.ஆர் சென்ட்ரல் மெட்ரோ",
                distanceEstimate = "50 meters (Underground direct subway)",
                walkMinutes = 1,
                detailsEn = "Largest underground interchange in South India. 4 functional lifts, motorized buggy for seniors, direct subways.",
                detailsTa = "தென்னிந்தியாவின் மிகப்பெரிய நிலத்தடி மெட்ரோ சந்திப்பு; 4 மின்தூக்கிகள் மற்றும் முதியோர் பேட்டரி கார்.",
                keyRoutes = listOf("Blue Line (Airport - Wimco Nagar)", "Green Line (Central - St. Thomas Mount)"),
                hasElevator = true,
                hasRamp = true,
                matchedStationId = "central"
            ),
            nearestBusStop = NearestTransitPoint(
                type = TransitMode.BUS,
                nameEn = "Central Railway Station Bus Stop (Poonamallee High Rd)",
                nameTa = "சென்ட்ரல் ரயில் நிலைய பேருந்து நிறுத்தம்",
                distanceEstimate = "100 meters (1 min walk)",
                walkMinutes = 1,
                detailsEn = "Sheltered bus bays with digital countdown displays and CCTV. High frequency to all parts of Chennai.",
                detailsTa = "டிஜிட்டல் பலகைகளுடன் கூடிய நவீன பேருந்து நிழற்குடை.",
                keyRoutes = listOf("11H", "17D", "29C", "21G", "19B"),
                isFreeBusEligible = true,
                matchedStationId = "central"
            ),
            nearestSuburbanRail = NearestTransitPoint(
                type = TransitMode.SUBURBAN_RAIL,
                nameEn = "Chennai Central Suburban (Moore Market Complex)",
                nameTa = "சென்னை சென்ட்ரல் புறநகர் முனையம் (MMC)",
                distanceEstimate = "150 meters (2 min walk)",
                walkMinutes = 2,
                detailsEn = "Suburban EMU trains to Avadi, Tiruvallur, Arakkonam, and Gummidipoondi starting every 7-10 minutes.",
                detailsTa = "ஆவடி, திருவள்ளூர், அரக்கோணம், கும்மிடிப்பூண்டி செல்லும் புறநகர் ரயில்கள்.",
                keyRoutes = listOf("Central - Arakkonam EMU", "Central - Gummidipoondi EMU"),
                hasElevator = true,
                hasRamp = true,
                matchedStationId = "central"
            ),
            firstLastMile = FirstLastMileCommute(
                shareAutoTipEn = "Prepaid auto stand available at Central exit gate. Share autos to Parrys and Royapuram available along EVR Periyar Salai.",
                shareAutoTipTa = "சென்ட்ரல் வாயிலில் ப்ரீபெய்ட் ஆட்டோ மற்றும் பாரிஸ் செல்லும் ஷேர் ஆட்டோக்கள் உள்ளன.",
                estimatedAutoFare = "₹15 - ₹20",
                feederShuttleInfoEn = "CMRL Feeder buses connect to Ripon Building and Secretariat every 10 mins.",
                feederShuttleInfoTa = "தலைமைச் செயலகத்திற்கு மெட்ரோ ஃபீடர் சேவை உள்ளது."
            ),
            accessibilityVerdictEn = "Highest accessibility rating in Tamil Nadu. Verified step-free underground subways with audio cues.",
            accessibilityVerdictTa = "தமிழ்நாட்டின் மிகச்சிறந்த அணுகல் வசதி கொண்ட நிலையம். பார்வை மாற்றுத்திறனாளிகளுக்கான தொடு உணர் வழிகாட்டிகள்.",
            recommendedBestOptionEn = "Use Metro Green Line for Koyambedu/Airport, Blue Line for Anna Salai/Guindy, and Suburban for West Chennai.",
            recommendedBestOptionTa = "கோயம்பேடு செல்ல மெட்ரோ கிரீன் லைன், அண்ணா சாலை செல்ல ப்ளூ லைன் சிறந்தவை.",
            suggestedOriginStationId = "central",
            isAiGenerated = false,
            isDemoFallback = isDemo
        )
    }
}
