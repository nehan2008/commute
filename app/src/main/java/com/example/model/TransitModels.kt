package com.example.model

enum class TransitMode {
    METRO,
    BUS,
    SUBURBAN_RAIL,
    WALK
}

enum class UserExperienceMode {
    ELDERLY,
    YOUTH
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class Language {
    ENGLISH,
    TAMIL
}

enum class JourneyFilter {
    ALL,
    METRO_ONLY,
    BUS_ONLY,
    SUBURBAN_ONLY,
    CHEAPEST,
    FEWEST_TRANSFERS,
    LEAST_WALKING
}

enum class AccessibilityBadgeLevel {
    FULL_STEP_FREE,   // Verified: Lift, tactile tiles, level boarding
    MODERATE_ACCESS,  // Verified: Ramps present, assistance recommended
    STEP_ENTRY        // Standard bus/train with steps
}

enum class CommuterCategory {
    SENIOR_CITIZEN,
    STUDENT,
    GENERAL
}

enum class NavigationTab {
    HOME,
    PLANNER,
    AI_COMMUTE,
    METRO_GUIDE,
    ADVISORIES,
    PROFILE
}

data class UserProfile(
    val phoneNumber: String = "",
    val name: String = "",
    val category: CommuterCategory = CommuterCategory.GENERAL,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val isLoggedIn: Boolean = false,
    val savedHomeStationId: String = "central",
    val savedWorkStationId: String = "guindy"
)

data class MetroStationFacility(
    val stationId: String,
    val nameEn: String,
    val nameTa: String,
    val line: String,
    val firstTrain: String,
    val lastTrain: String,
    val hasLift: Boolean = true,
    val hasEscalator: Boolean = true,
    val hasParking: Boolean = true,
    val hasRestroom: Boolean = true,
    val hasDrinkingWater: Boolean = true,
    val wheelchairAvailable: Boolean = true,
    val exits: List<String>,
    val feederServices: List<String>
)

data class Station(
    val id: String,
    val nameEn: String,
    val nameTa: String,
    val locality: String,
    val supportedModes: List<TransitMode>,
    val hasElevator: Boolean = false,
    val hasRamp: Boolean = false,
    val hasTactilePaving: Boolean = false,
    val wheelchairAssistanceAvailable: Boolean = false,
    val accessibilityNotesEn: String = "",
    val accessibilityNotesTa: String = ""
)

data class RouteLeg(
    val fromStation: String,
    val fromStationTa: String,
    val toStation: String,
    val toStationTa: String,
    val mode: TransitMode,
    val routeCode: String,          // e.g. "Blue Line", "29C", "Local Train"
    val routeDescriptionEn: String,
    val routeDescriptionTa: String,
    val durationMins: Int,
    val walkDistanceMeters: Int = 0,
    val fareRupees: Int = 0,
    val isElectricBus: Boolean = false,
    val isFreeForWomen: Boolean = false,
    val accessibilityNoteEn: String = "",
    val accessibilityNoteTa: String = "",
    val intermediateStopsEn: List<String> = emptyList(),
    val intermediateStopsTa: List<String> = emptyList()
)

data class JourneyOption(
    val id: String,
    val titleEn: String,
    val titleTa: String,
    val legs: List<RouteLeg>,
    val totalDurationMins: Int,
    val totalWalkMeters: Int,
    val totalTransfers: Int,
    val totalFareRupees: Int,
    val isDirect: Boolean,
    val accessibilityBadge: AccessibilityBadgeLevel,
    val co2SavedKg: Double,
    val crowdingEstimate: String,     // Low, Medium, High
    val dataSourcingNoteEn: String,
    val dataSourcingNoteTa: String
)

data class DisruptionReport(
    val id: String,
    val affectedMode: TransitMode,
    val routeOrStation: String,
    val category: String,             // Delay, Maintenance, Crowding, Route Diversion
    val summaryEn: String,
    val summaryTa: String,
    val detailEn: String,
    val detailTa: String,
    val reportedTimeAgo: String,
    val upvotes: Int,
    val isOfficialAdvisory: Boolean = false,
    val isUserReported: Boolean = false,
    val verifiedByCommuters: Boolean = true
)
