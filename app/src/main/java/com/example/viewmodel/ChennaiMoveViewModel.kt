package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChennaiTransitRepository
import com.example.model.*
import com.example.speech.ChennaiSpeechManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChennaiMoveViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("chennai_move_prefs", Context.MODE_PRIVATE)

    private val speechManager = ChennaiSpeechManager(application)
    val isSpeaking: StateFlow<Boolean> = speechManager.isSpeaking

    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _userMode = MutableStateFlow(UserExperienceMode.ELDERLY)
    val userMode: StateFlow<UserExperienceMode> = _userMode.asStateFlow()

    private val _language = MutableStateFlow(Language.ENGLISH)
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _origin = MutableStateFlow(ChennaiTransitRepository.STATIONS[0]) // Central
    val origin: StateFlow<Station> = _origin.asStateFlow()

    private val _destination = MutableStateFlow(ChennaiTransitRepository.STATIONS[2]) // Guindy
    val destination: StateFlow<Station> = _destination.asStateFlow()

    private val _activeFilter = MutableStateFlow(JourneyFilter.ALL)
    val activeFilter: StateFlow<JourneyFilter> = _activeFilter.asStateFlow()

    private val _journeyResults = MutableStateFlow<List<JourneyOption>>(emptyList())
    val journeyResults: StateFlow<List<JourneyOption>> = _journeyResults.asStateFlow()

    private val _selectedJourney = MutableStateFlow<JourneyOption?>(null)
    val selectedJourney: StateFlow<JourneyOption?> = _selectedJourney.asStateFlow()

    private val _activeStepIndex = MutableStateFlow(0)
    val activeStepIndex: StateFlow<Int> = _activeStepIndex.asStateFlow()

    private val _showReportDialog = MutableStateFlow(false)
    val showReportDialog: StateFlow<Boolean> = _showReportDialog.asStateFlow()

    private val _themeMode = MutableStateFlow(
        when (prefs.getString("theme_mode", "SYSTEM")) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    fun setShowSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    val disruptions: StateFlow<List<DisruptionReport>> = ChennaiTransitRepository.disruptions
    val metroFacilities = ChennaiTransitRepository.METRO_FACILITIES

    // User Profile State
    private val _userProfile = MutableStateFlow(loadSavedProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _phoneInput = MutableStateFlow("")
    val phoneInput: StateFlow<String> = _phoneInput.asStateFlow()

    private val _otpInput = MutableStateFlow("")
    val otpInput: StateFlow<String> = _otpInput.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Gemini AI Transit State
    private val _aiLocationQuery = MutableStateFlow("Marina Beach Lighthouse")
    val aiLocationQuery: StateFlow<String> = _aiLocationQuery.asStateFlow()

    private val _aiCommutePreference = MutableStateFlow(CommutePreference.ALL_MODES)
    val aiCommutePreference: StateFlow<CommutePreference> = _aiCommutePreference.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiRecommendation = MutableStateFlow<GeminiTransitRecommendation?>(null)
    val aiRecommendation: StateFlow<GeminiTransitRecommendation?> = _aiRecommendation.asStateFlow()

    private val _aiErrorMessage = MutableStateFlow<String?>(null)
    val aiErrorMessage: StateFlow<String?> = _aiErrorMessage.asStateFlow()

    // User Location & Auto-Boarding State
    private val _userLocation = MutableStateFlow<com.example.data.location.UserLocationResult?>(null)
    val userLocation: StateFlow<com.example.data.location.UserLocationResult?> = _userLocation.asStateFlow()

    private val _isDetectingLocation = MutableStateFlow(false)
    val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation.asStateFlow()

    init {
        recalculateJourneys()
        // Default initial AI prompt: "I wanna go from Neelankarai to Triplicane" for instant response
        searchNearestTransitWithGemini("i wanna go from neelankari to triplicane", CommutePreference.ALL_MODES)
    }

    private fun loadSavedProfile(): UserProfile {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val phone = prefs.getString("phone", "") ?: ""
        val name = prefs.getString("name", "Chennai Commuter") ?: "Chennai Commuter"
        val cat = prefs.getString("category", CommuterCategory.GENERAL.name) ?: CommuterCategory.GENERAL.name
        val emName = prefs.getString("em_name", "Family Member") ?: "Family Member"
        val emPhone = prefs.getString("em_phone", "9876543210") ?: "9876543210"

        val categoryEnum = try {
            CommuterCategory.valueOf(cat)
        } catch (e: Exception) {
            CommuterCategory.GENERAL
        }

        return UserProfile(
            phoneNumber = phone,
            name = name,
            category = categoryEnum,
            emergencyContactName = emName,
            emergencyContactPhone = emPhone,
            isLoggedIn = isLoggedIn
        )
    }

    private fun persistProfile(profile: UserProfile) {
        prefs.edit().apply {
            putBoolean("is_logged_in", profile.isLoggedIn)
            putString("phone", profile.phoneNumber)
            putString("name", profile.name)
            putString("category", profile.category.name)
            putString("em_name", profile.emergencyContactName)
            putString("em_phone", profile.emergencyContactPhone)
            apply()
        }
    }

    fun setPhoneInput(input: String) {
        if (input.length <= 10 && input.all { it.isDigit() }) {
            _phoneInput.value = input
            _authError.value = null
        }
    }

    fun setOtpInput(input: String) {
        if (input.length <= 6 && input.all { it.isDigit() }) {
            _otpInput.value = input
            _authError.value = null
        }
    }

    fun requestOtp() {
        val phone = _phoneInput.value.trim()
        if (phone.length != 10) {
            _authError.value = "Please enter a valid 10-digit mobile number."
            return
        }
        _isOtpSent.value = true
        _authError.value = null
        // Auto-fill convenience OTP for immediate testing
        _otpInput.value = "123456"
    }

    fun verifyOtp() {
        val otp = _otpInput.value.trim()
        if (otp.length != 6) {
            _authError.value = "Please enter the 6-digit verification code."
            return
        }

        val phone = _phoneInput.value.trim()
        val updated = _userProfile.value.copy(
            phoneNumber = phone,
            name = if (phone.startsWith("9")) "Thiru. Arumugam" else "Chennai Traveler",
            category = if (_userMode.value == UserExperienceMode.ELDERLY) CommuterCategory.SENIOR_CITIZEN else CommuterCategory.GENERAL,
            isLoggedIn = true
        )
        _userProfile.value = updated
        persistProfile(updated)
        _isOtpSent.value = false
        _phoneInput.value = ""
        _otpInput.value = ""
        _authError.value = null
        _currentTab.value = NavigationTab.HOME
    }

    fun logout() {
        val cleared = UserProfile(isLoggedIn = false)
        _userProfile.value = cleared
        persistProfile(cleared)
        _isOtpSent.value = false
    }

    fun updateProfile(name: String, category: CommuterCategory, emName: String, emPhone: String) {
        val updated = _userProfile.value.copy(
            name = name,
            category = category,
            emergencyContactName = emName,
            emergencyContactPhone = emPhone
        )
        _userProfile.value = updated
        persistProfile(updated)
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun quickSearchDestination(dest: Station) {
        _destination.value = dest
        recalculateJourneys()
        _currentTab.value = NavigationTab.PLANNER
    }

    fun setUserMode(mode: UserExperienceMode) {
        _userMode.value = mode
        recalculateJourneys()
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == Language.ENGLISH) Language.TAMIL else Language.ENGLISH
    }

    fun setOrigin(station: Station) {
        _origin.value = station
        recalculateJourneys()
    }

    fun setDestination(station: Station) {
        _destination.value = station
        recalculateJourneys()
    }

    fun swapOriginDestination() {
        val currentOrigin = _origin.value
        val currentDest = _destination.value
        _origin.value = currentDest
        _destination.value = currentOrigin
        recalculateJourneys()
    }

    fun setFilter(filter: JourneyFilter) {
        _activeFilter.value = filter
        recalculateJourneys()
    }

    fun selectJourney(journey: JourneyOption?) {
        _selectedJourney.value = journey
        _activeStepIndex.value = 0
    }

    fun nextStep() {
        val journey = _selectedJourney.value ?: return
        if (_activeStepIndex.value < journey.legs.size - 1) {
            _activeStepIndex.value += 1
        }
    }

    fun prevStep() {
        if (_activeStepIndex.value > 0) {
            _activeStepIndex.value -= 1
        }
    }

    fun setShowReportDialog(show: Boolean) {
        _showReportDialog.value = show
    }

    fun setShowTransparencyDialog(show: Boolean) {
        _showTransparencyDialog.value = show
    }

    fun upvoteDisruption(id: String) {
        ChennaiTransitRepository.upvoteDisruption(id)
    }

    fun submitDisruption(
        mode: TransitMode,
        routeOrStation: String,
        category: String,
        summaryEn: String,
        summaryTa: String,
        detailEn: String,
        detailTa: String
    ) {
        ChennaiTransitRepository.submitDisruptionReport(
            mode = mode,
            routeOrStation = routeOrStation,
            category = category,
            summaryEn = summaryEn,
            summaryTa = summaryTa,
            detailEn = detailEn,
            detailTa = detailTa
        )
        _showReportDialog.value = false
    }

    fun speakJourneyInstructions() {
        val journey = _selectedJourney.value ?: _journeyResults.value.firstOrNull() ?: return
        val isTa = _language.value == Language.TAMIL

        val speechText = if (isTa) {
            buildString {
                append("சென்னை மூவ் பயண வழிகாட்டி. ")
                append("${_origin.value.nameTa} முதல் ${_destination.value.nameTa} வரை. ")
                append("${journey.titleTa}. மொத்த நேரம் ${journey.totalDurationMins} நிமிடங்கள். கட்டணம் ரூபாய் ${journey.totalFareRupees}. ")
                journey.legs.forEachIndexed { index, leg ->
                    append("படி ${index + 1}: ${leg.mode} வழியாக ${leg.fromStationTa}-ல் ஏறி ${leg.toStationTa}-ல் இறங்கவும். ")
                }
            }
        } else {
            buildString {
                append("Chennai Move journey guide. ")
                append("Traveling from ${_origin.value.nameEn} to ${_destination.value.nameEn}. ")
                append("${journey.titleEn}. Total duration ${journey.totalDurationMins} minutes. Total fare rupees ${journey.totalFareRupees}. ")
                journey.legs.forEachIndexed { index, leg ->
                    append("Step ${index + 1}: Take ${leg.routeCode} from ${leg.fromStation} to ${leg.toStation}. ${leg.accessibilityNoteEn}. ")
                }
            }
        }

        speechManager.speak(speechText, isTa)
    }

    fun stopSpeaking() {
        speechManager.stop()
    }

    fun shareJourneyWithFamily() {
        val journey = _selectedJourney.value ?: _journeyResults.value.firstOrNull() ?: return
        val isTa = _language.value == Language.TAMIL

        val shareMessage = if (isTa) {
            """
            *சென்னை மூவ் - எனது பயண விவரங்கள்*
            📍 புறப்படும் இடம்: ${_origin.value.nameTa}
            🎯 சேருமிடம்: ${_destination.value.nameTa}
            🚆 வழித்தடம்: ${journey.titleTa}
            ⏱️ எதிர்பார்க்கப்படும் நேரம்: ${journey.totalDurationMins} நிமிடங்கள்
            💰 கட்டணம்: ₹${journey.totalFareRupees}
            🛡️ அணுகல் தரம்: ${if (journey.accessibilityBadge == AccessibilityBadgeLevel.FULL_STEP_FREE) "முழு மின்தூக்கி வசதி (Step-Free)" else "சாய்தள வசதி"}
            
            _CUMTA Chennai Move செயலியில் பகிரப்பட்டது._
            """.trimIndent()
        } else {
            """
            *Chennai Move - My Journey Details*
            📍 From: ${_origin.value.nameEn}
            🎯 To: ${_destination.value.nameEn}
            🚆 Route: ${journey.titleEn}
            ⏱️ Expected Duration: ${journey.totalDurationMins} mins
            💰 Total Fare: ₹${journey.totalFareRupees}
            🛡️ Accessibility: ${if (journey.accessibilityBadge == AccessibilityBadgeLevel.FULL_STEP_FREE) "Full Step-Free Access (Lifts)" else "Ramped Boarding"}
            
            _Shared via Chennai Move - CUMTA Reimagine Chennai._
            """.trimIndent()
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, if (isTa) "பயணத்தை குடும்பத்தினருடன் பகிரவும்" else "Share Journey with Family")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        getApplication<Application>().startActivity(shareIntent)
    }

    // Gemini AI Transit Methods
    fun setAiLocationQuery(query: String) {
        _aiLocationQuery.value = query
    }

    fun setAiCommutePreference(pref: CommutePreference) {
        _aiCommutePreference.value = pref
    }

    fun searchNearestTransitWithGemini(
        query: String? = null,
        pref: CommutePreference? = null
    ) {
        val targetQuery = query ?: _aiLocationQuery.value
        val targetPref = pref ?: _aiCommutePreference.value

        if (targetQuery.isBlank()) return

        _aiLocationQuery.value = targetQuery
        _aiCommutePreference.value = targetPref
        _isAiLoading.value = true
        _aiErrorMessage.value = null

        viewModelScope.launch {
            try {
                val recommendation = com.example.data.gemini.GeminiTransitService.findNearestCommuteOptions(
                    locationQuery = targetQuery,
                    preference = targetPref
                )
                _aiRecommendation.value = recommendation

                // Automatically synchronize origin and destination to planner if within standard boarding distance
                if (recommendation.parsedTripPlan != null) {
                    val trip = recommendation.parsedTripPlan
                    val originId = trip.resolvedOriginStationId ?: trip.nearestBoardingPoint.matchedStationId
                    val destId = trip.resolvedDestinationStationId ?: trip.nearestDestinationPoint.matchedStationId

                    val originStation = ChennaiTransitRepository.STATIONS.firstOrNull { it.id == originId }
                        ?: ChennaiTransitRepository.searchStationsAndStops(trip.originQuery).firstOrNull()
                    val destStation = ChennaiTransitRepository.STATIONS.firstOrNull { it.id == destId }
                        ?: ChennaiTransitRepository.searchStationsAndStops(trip.destinationQuery).firstOrNull()

                    if (originStation != null) _origin.value = originStation
                    if (destStation != null) _destination.value = destStation
                    recalculateJourneys()
                }
            } catch (e: Exception) {
                _aiErrorMessage.value = e.message ?: "Failed to locate nearest transit options."
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun planNaturalTripFromSearch(query: String) {
        if (query.isBlank()) {
            setTab(NavigationTab.PLANNER)
            return
        }
        val endpoints = com.example.data.gemini.GeminiTransitService.extractJourneyEndpoints(query)
        if (endpoints != null) {
            val (originText, destText) = endpoints
            val originStation = ChennaiTransitRepository.STATIONS.firstOrNull {
                it.nameEn.contains(originText, ignoreCase = true) || it.nameTa.contains(originText, ignoreCase = true) || it.id.contains(originText, ignoreCase = true)
            } ?: ChennaiTransitRepository.searchStationsAndStops(originText).firstOrNull()
              ?: ChennaiTransitRepository.STATIONS.first { it.id == "ecr_neelankarai" }

            val destStation = ChennaiTransitRepository.STATIONS.firstOrNull {
                it.nameEn.contains(destText, ignoreCase = true) || it.nameTa.contains(destText, ignoreCase = true) || it.id.contains(destText, ignoreCase = true)
            } ?: ChennaiTransitRepository.searchStationsAndStops(destText).firstOrNull()
              ?: ChennaiTransitRepository.STATIONS.first { it.id == "airport" }

            setOrigin(originStation)
            setDestination(destStation)
            searchNearestTransitWithGemini(query, _aiCommutePreference.value)
            setTab(NavigationTab.PLANNER)
        } else {
            val matched = ChennaiTransitRepository.searchStationsAndStops(query).firstOrNull()
            if (matched != null) {
                setDestination(matched)
            }
            setTab(NavigationTab.PLANNER)
        }
    }

    fun applyNearestStationAsOrigin(stationId: String?) {
        if (stationId.isNullOrBlank()) return
        val matchedStation = ChennaiTransitRepository.STATIONS.firstOrNull { it.id == stationId }
            ?: ChennaiTransitRepository.searchStationsAndStops(stationId).firstOrNull()
        if (matchedStation != null) {
            setOrigin(matchedStation)
            setTab(NavigationTab.PLANNER)
        }
    }

    fun applyNearestStationAsDestination(stationId: String?) {
        if (stationId.isNullOrBlank()) return
        val matchedStation = ChennaiTransitRepository.STATIONS.firstOrNull { it.id == stationId }
            ?: ChennaiTransitRepository.searchStationsAndStops(stationId).firstOrNull()
        if (matchedStation != null) {
            setDestination(matchedStation)
            setTab(NavigationTab.PLANNER)
        }
    }

    fun detectUserLocation(context: Context) {
        _isDetectingLocation.value = true
        viewModelScope.launch {
            try {
                val result = com.example.data.location.ChennaiLocationService.getCurrentLocation(context)
                _userLocation.value = result
                if (result != null) {
                    if (result.nearestStation != null) {
                        setOrigin(result.nearestStation)
                    }
                }
            } catch (e: Exception) {
                Log.e("ChennaiMoveVM", "Error detecting location: ${e.message}", e)
            } finally {
                _isDetectingLocation.value = false
            }
        }
    }

    fun applyParsedTripToPlanner(tripPlan: com.example.model.GeminiParsedTripPlan) {
        val originId = tripPlan.resolvedOriginStationId ?: tripPlan.nearestBoardingPoint.matchedStationId
        val destId = tripPlan.resolvedDestinationStationId ?: tripPlan.nearestDestinationPoint.matchedStationId

        val originStation = ChennaiTransitRepository.STATIONS.firstOrNull { it.id == originId }
            ?: ChennaiTransitRepository.searchStationsAndStops(tripPlan.originQuery).firstOrNull()
            ?: ChennaiTransitRepository.STATIONS.first { it.id == "ecr_neelankarai" }

        val destStation = ChennaiTransitRepository.STATIONS.firstOrNull { it.id == destId }
            ?: ChennaiTransitRepository.searchStationsAndStops(tripPlan.destinationQuery).firstOrNull()
            ?: ChennaiTransitRepository.STATIONS.first { it.id == "triplicane" }

        setOrigin(originStation)
        setDestination(destStation)
        setTab(NavigationTab.PLANNER)
    }

    fun speakAiRecommendation() {
        val rec = _aiRecommendation.value ?: return
        val isTa = _language.value == Language.TAMIL
        val spokenText = if (isTa) {
            "${rec.summaryTa}. ${rec.recommendedBestOptionTa}. முதியோர் மற்றும் நடைபாதை தகவல்: ${rec.accessibilityVerdictTa}"
        } else {
            "${rec.summaryEn}. ${rec.recommendedBestOptionEn}. Accessibility and transit advisory: ${rec.accessibilityVerdictEn}"
        }
        speechManager.speak(spokenText, isTa)
    }

    private fun recalculateJourneys() {
        viewModelScope.launch {
            val results = ChennaiTransitRepository.planJourneys(
                origin = _origin.value,
                destination = _destination.value,
                userMode = _userMode.value,
                filter = _activeFilter.value
            )
            _journeyResults.value = results
            if (_selectedJourney.value == null || !results.contains(_selectedJourney.value)) {
                _selectedJourney.value = results.firstOrNull()
            }
            _activeStepIndex.value = 0
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }
}
