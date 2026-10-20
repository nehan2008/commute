package com.example.model

enum class CommutePreference {
    ALL_MODES,
    STEP_FREE_ELDERLY,    // Minimal walking, lift verified
    FASTEST_TRANSIT,      // Metro / Suburban express
    BUDGET_SAVING         // MTC Free bus / ₹5 Suburban
}

data class NearestTransitPoint(
    val type: TransitMode,
    val nameEn: String,
    val nameTa: String,
    val distanceEstimate: String,   // e.g. "250 meters (3 min walk)"
    val walkMinutes: Int,
    val detailsEn: String,          // e.g. "Platform 1, Gate B has working elevator. Blue Line to Airport."
    val detailsTa: String,
    val keyRoutes: List<String> = emptyList(), // e.g. ["19B", "102", "588"] or ["Blue Line"]
    val hasElevator: Boolean = false,
    val hasRamp: Boolean = false,
    val isFreeBusEligible: Boolean = false,
    val matchedStationId: String? = null
)

data class FirstLastMileCommute(
    val shareAutoTipEn: String,
    val shareAutoTipTa: String,
    val estimatedAutoFare: String,  // e.g. "₹15 - ₹20"
    val feederShuttleInfoEn: String,
    val feederShuttleInfoTa: String
)

data class CommuteTripOption(
    val titleEn: String,
    val titleTa: String,
    val mode: TransitMode,
    val routeCode: String,          // e.g. "MTC 19B / 21G"
    val durationMins: Int,
    val fare: String,               // e.g. "₹20 (Free for Women)"
    val instructionsEn: String,
    val instructionsTa: String,
    val isRecommended: Boolean = false,
    val isStepFree: Boolean = true
)

data class GeminiParsedTripPlan(
    val promptText: String,
    val originQuery: String,
    val destinationQuery: String,
    val nearestBoardingPoint: NearestTransitPoint,
    val nearestDestinationPoint: NearestTransitPoint,
    val tripOptions: List<CommuteTripOption>,
    val bestAdviceEn: String,
    val bestAdviceTa: String,
    val resolvedOriginStationId: String? = null,
    val resolvedDestinationStationId: String? = null
)

data class GeminiTransitRecommendation(
    val queriedLocation: String,
    val summaryEn: String,
    val summaryTa: String,
    val nearestMetro: NearestTransitPoint?,
    val nearestBusStop: NearestTransitPoint?,
    val nearestSuburbanRail: NearestTransitPoint?,
    val firstLastMile: FirstLastMileCommute?,
    val accessibilityVerdictEn: String,
    val accessibilityVerdictTa: String,
    val recommendedBestOptionEn: String,
    val recommendedBestOptionTa: String,
    val suggestedOriginStationId: String?,
    val isAiGenerated: Boolean = true,
    val isDemoFallback: Boolean = false,
    val parsedTripPlan: GeminiParsedTripPlan? = null
)
