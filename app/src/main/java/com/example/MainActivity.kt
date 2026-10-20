package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.NavigationTab
import com.example.model.UserExperienceMode
import com.example.ui.components.ChennaiBottomNav
import com.example.ui.components.TopNavigationBar
import com.example.ui.dialogs.DataTransparencyDialog
import com.example.ui.dialogs.DisruptionReportDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ChennaiMoveViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ChennaiMoveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ChennaiMoveMainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ChennaiMoveMainScreen(viewModel: ChennaiMoveViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val userMode by viewModel.userMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val origin by viewModel.origin.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val journeyResults by viewModel.journeyResults.collectAsStateWithLifecycle()
    val selectedJourney by viewModel.selectedJourney.collectAsStateWithLifecycle()
    val activeStepIndex by viewModel.activeStepIndex.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val disruptions by viewModel.disruptions.collectAsStateWithLifecycle()
    val showReportDialog by viewModel.showReportDialog.collectAsStateWithLifecycle()
    val showTransparencyDialog by viewModel.showTransparencyDialog.collectAsStateWithLifecycle()

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val phoneInput by viewModel.phoneInput.collectAsStateWithLifecycle()
    val otpInput by viewModel.otpInput.collectAsStateWithLifecycle()
    val isOtpSent by viewModel.isOtpSent.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    val aiLocationQuery by viewModel.aiLocationQuery.collectAsStateWithLifecycle()
    val aiCommutePreference by viewModel.aiCommutePreference.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiRecommendation by viewModel.aiRecommendation.collectAsStateWithLifecycle()
    val aiErrorMessage by viewModel.aiErrorMessage.collectAsStateWithLifecycle()

    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val isDetectingLocation by viewModel.isDetectingLocation.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            viewModel.detectUserLocation(context)
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.detectUserLocation(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopNavigationBar(
                userMode = userMode,
                language = language,
                onModeChange = { viewModel.setUserMode(it) },
                onToggleLanguage = { viewModel.toggleLanguage() },
                onOpenTransparency = { viewModel.setShowTransparencyDialog(true) }
            )
        },
        bottomBar = {
            ChennaiBottomNav(
                currentTab = currentTab,
                language = language,
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.HOME -> {
                    RapidoHomeScreen(
                        userProfile = userProfile,
                        userMode = userMode,
                        language = language,
                        origin = origin,
                        destination = destination,
                        userLocation = userLocation,
                        onSelectOrigin = { viewModel.setOrigin(it) },
                        onSelectDestination = { viewModel.setDestination(it) },
                        onNavigateToPlanner = { viewModel.setTab(NavigationTab.PLANNER) },
                        onNavigateToAiCommute = { viewModel.setTab(NavigationTab.AI_COMMUTE) },
                        onNavigateToMetroGuide = { viewModel.setTab(NavigationTab.METRO_GUIDE) },
                        onNavigateToAdvisories = { viewModel.setTab(NavigationTab.ADVISORIES) },
                        onNavigateToProfile = { viewModel.setTab(NavigationTab.PROFILE) },
                        onModeChange = { viewModel.setUserMode(it) }
                    )
                }

                NavigationTab.AI_COMMUTE -> {
                    GeminiAiTransitScreen(
                        aiLocationQuery = aiLocationQuery,
                        aiCommutePreference = aiCommutePreference,
                        isAiLoading = isAiLoading,
                        aiRecommendation = aiRecommendation,
                        aiErrorMessage = aiErrorMessage,
                        isSpeaking = isSpeaking,
                        userLocation = userLocation,
                        isDetectingLocation = isDetectingLocation,
                        userMode = userMode,
                        language = language,
                        onQueryChange = { viewModel.setAiLocationQuery(it) },
                        onPreferenceChange = { viewModel.setAiCommutePreference(it) },
                        onSearchNearest = { q, p -> viewModel.searchNearestTransitWithGemini(q, p) },
                        onApplyOrigin = { viewModel.applyNearestStationAsOrigin(it) },
                        onApplyDestination = { viewModel.applyNearestStationAsDestination(it) },
                        onApplyTripToPlanner = { viewModel.applyParsedTripToPlanner(it) },
                        onRequestLocation = { viewModel.detectUserLocation(context) },
                        onSpeakDirections = { viewModel.speakAiRecommendation() },
                        onStopSpeaking = { viewModel.stopSpeaking() },
                        onNavigateToPlanner = { viewModel.setTab(NavigationTab.PLANNER) }
                    )
                }

                NavigationTab.PLANNER -> {
                    when (userMode) {
                        UserExperienceMode.ELDERLY -> {
                            ElderlyModeScreen(
                                origin = origin,
                                destination = destination,
                                journeyResults = journeyResults,
                                selectedJourney = selectedJourney,
                                activeStepIndex = activeStepIndex,
                                isSpeaking = isSpeaking,
                                language = language,
                                onSelectOrigin = { viewModel.setOrigin(it) },
                                onSelectDestination = { viewModel.setDestination(it) },
                                onSwapStations = { viewModel.swapOriginDestination() },
                                onSelectJourney = { viewModel.selectJourney(it) },
                                onNextStep = { viewModel.nextStep() },
                                onPrevStep = { viewModel.prevStep() },
                                onSpeakDirections = { viewModel.speakJourneyInstructions() },
                                onStopSpeaking = { viewModel.stopSpeaking() },
                                onShareWithFamily = { viewModel.shareJourneyWithFamily() },
                                onOpenAiTransitFinder = { viewModel.setTab(NavigationTab.AI_COMMUTE) }
                            )
                        }
                        UserExperienceMode.YOUTH -> {
                            YouthModeScreen(
                                origin = origin,
                                destination = destination,
                                activeFilter = activeFilter,
                                journeyResults = journeyResults,
                                selectedJourney = selectedJourney,
                                disruptions = disruptions,
                                language = language,
                                onSelectOrigin = { viewModel.setOrigin(it) },
                                onSelectDestination = { viewModel.setDestination(it) },
                                onSwapStations = { viewModel.swapOriginDestination() },
                                onSetFilter = { viewModel.setFilter(it) },
                                onSelectJourney = { viewModel.selectJourney(it) },
                                onUpvoteDisruption = { viewModel.upvoteDisruption(it) },
                                onOpenReportDialog = { viewModel.setShowReportDialog(true) },
                                onShareRoute = { viewModel.shareJourneyWithFamily() },
                                onOpenAiTransitFinder = { viewModel.setTab(NavigationTab.AI_COMMUTE) }
                            )
                        }
                    }
                }

                NavigationTab.METRO_GUIDE -> {
                    ChennaiMetroGuideScreen(
                        metroFacilities = viewModel.metroFacilities,
                        language = language
                    )
                }

                NavigationTab.ADVISORIES -> {
                    AdvisoriesScreen(
                        disruptions = disruptions,
                        language = language,
                        onUpvoteDisruption = { viewModel.upvoteDisruption(it) },
                        onOpenReportDialog = { viewModel.setShowReportDialog(true) }
                    )
                }

                NavigationTab.PROFILE -> {
                    ProfileAuthScreen(
                        userProfile = userProfile,
                        phoneInput = phoneInput,
                        otpInput = otpInput,
                        isOtpSent = isOtpSent,
                        authError = authError,
                        language = language,
                        onPhoneChange = { viewModel.setPhoneInput(it) },
                        onOtpChange = { viewModel.setOtpInput(it) },
                        onRequestOtp = { viewModel.requestOtp() },
                        onVerifyOtp = { viewModel.verifyOtp() },
                        onLogout = { viewModel.logout() },
                        onUpdateProfile = { name, cat, emName, emPhone ->
                            viewModel.updateProfile(name, cat, emName, emPhone)
                        }
                    )
                }
            }
        }
    }

    if (showReportDialog) {
        DisruptionReportDialog(
            language = language,
            onDismiss = { viewModel.setShowReportDialog(false) },
            onSubmit = { mode, route, category, summaryEn, summaryTa, detailEn, detailTa ->
                viewModel.submitDisruption(mode, route, category, summaryEn, summaryTa, detailEn, detailTa)
            }
        )
    }

    if (showTransparencyDialog) {
        DataTransparencyDialog(
            language = language,
            onDismiss = { viewModel.setShowTransparencyDialog(false) }
        )
    }
}
