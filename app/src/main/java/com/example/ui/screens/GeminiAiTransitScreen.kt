package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.location.UserLocationResult
import com.example.model.*
import com.example.ui.theme.ChennaiGreen
import com.example.ui.theme.ChennaiNavy
import com.example.ui.theme.ElderlyYellowAccent
import kotlinx.coroutines.launch

@Composable
fun GeminiAiTransitScreen(
    aiLocationQuery: String,
    aiCommutePreference: CommutePreference,
    isAiLoading: Boolean,
    aiRecommendation: GeminiTransitRecommendation?,
    aiErrorMessage: String?,
    isSpeaking: Boolean,
    userLocation: UserLocationResult?,
    isDetectingLocation: Boolean,
    userMode: UserExperienceMode,
    language: Language,
    onQueryChange: (String) -> Unit,
    onPreferenceChange: (CommutePreference) -> Unit,
    onSearchNearest: (String, CommutePreference) -> Unit,
    onApplyOrigin: (String?) -> Unit,
    onApplyDestination: (String?) -> Unit,
    onApplyTripToPlanner: (GeminiParsedTripPlan) -> Unit,
    onRequestLocation: () -> Unit,
    onSpeakDirections: () -> Unit,
    onStopSpeaking: () -> Unit,
    onNavigateToPlanner: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val isTa = language == Language.TAMIL
    val isElderly = userMode == UserExperienceMode.ELDERLY

    // Permission Launcher for Location
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            hasLocationPermission = true
            onRequestLocation()
        }
    }

    // Automatically check permission & request location when screen loads
    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            if (userLocation == null) {
                onRequestLocation()
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Auto-scroll down when new recommendation or parsed trip arrives
    LaunchedEffect(aiRecommendation) {
        if (aiRecommendation != null) {
            coroutineScope.launch {
                // Scroll down to the boarding & destination results section
                listState.animateScrollToItem(index = 3)
            }
        }
    }

    val conversationalPrompts = listOf(
        "I wanna go from Neelankarai to Triplicane" to "நீலாங்கரையிலிருந்து திருவல்லிக்கேணி செல்ல வேண்டும்",
        "Neelankarai to Central" to "நீலாங்கரை to சென்ட்ரல்",
        "Thiruvanmiyur to Guindy Metro" to "திருவான்மியூர் to கிண்டி மெட்ரோ",
        "Tambaram to T. Nagar" to "தாம்பரம் to தி.நகர்",
        "Velachery to Koyambedu" to "வேளச்சேரி to கோயம்பேடு",
        "Adyar to Marina Beach" to "அடையாறு to மெரினா கடற்கரை"
    )

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("gemini_ai_transit_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
    ) {
        // Hero Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isElderly) ChennaiNavy else MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isElderly) ElderlyYellowAccent else ChennaiNavy
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Gemini AI",
                                    tint = if (isElderly) ChennaiNavy else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isTa) "ஜெமினி AI பயண வழிகாட்டி" else "Gemini AI Transit Planner",
                                    fontSize = if (isElderly) 20.sp else 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isElderly) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (isTa) "அருகிலுள்ள ஏறும் இடம் & சேரும் இடம்" else "Automatic Boarding & Destination Locator",
                                    fontSize = 12.sp,
                                    color = if (isElderly) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Audio Speech Toggle Button
                        IconButton(
                            onClick = {
                                if (isSpeaking) onStopSpeaking() else onSpeakDirections()
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isSpeaking) ChennaiGreen else (if (isElderly) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                )
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Voice Guidance",
                                tint = if (isSpeaking) Color.White else (if (isElderly) ElderlyYellowAccent else MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isTa) {
                            "உங்கள் அன்றைய பயண திட்டத்தை கேட்கவும் அல்லது தட்டச்சு செய்யவும். ஏறும் நிறுத்தம் மற்றும் சேரும் இடத்தை தானாக கண்டறிந்து வழிகாட்டும்."
                        } else {
                            "Tell AI your travel plan (e.g., 'I wanna go from Neelankarai to Triplicane'). We will locate your nearest boarding stop and destination automatically!"
                        },
                        fontSize = 13.sp,
                        color = if (isElderly) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Automatic User Location & Nearest Boarding Stop Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_location_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (userLocation != null) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, if (userLocation != null) ChennaiGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (userLocation != null) Icons.Default.MyLocation else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (userLocation != null) ChennaiGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (userLocation != null) {
                                    if (isTa) "📍 தானாக கண்டறியப்பட்ட இருப்பிடம்" else "📍 Auto-Detected User Location"
                                } else {
                                    if (isTa) "📍 இருப்பிட அனுமதி தேவை" else "📍 GPS Location Detection"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (userLocation != null) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isDetectingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = ChennaiGreen
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    if (hasLocationPermission) {
                                        onRequestLocation()
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (userLocation != null) (if (isTa) "புதுப்பி" else "Re-check") else (if (isTa) "அனுமதி கொடு" else "Enable GPS"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (userLocation != null) {
                        Text(
                            text = if (isTa) {
                                "நடப்பு பகுதி: ${userLocation.localityNameTa} • அருகிலுள்ள ஏறும் நிறுத்தம்: ${userLocation.nearestBoardingPoint.nameTa}"
                            } else {
                                "Area: ${userLocation.localityName} • Nearest Boarding: ${userLocation.nearestBoardingPoint.nameEn} (${userLocation.nearestBoardingPoint.distanceEstimate})"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    onApplyOrigin(userLocation.nearestStation?.id)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isTa) "தொடக்க நிலையமாக வை" else "Set as Boarding Point", fontSize = 11.sp)
                            }

                            AssistChip(
                                onClick = {
                                    val query = "${userLocation.localityName} to Triplicane"
                                    onQueryChange(query)
                                    onSearchNearest(query, aiCommutePreference)
                                },
                                label = { Text(if (isTa) "திருவல்லிக்கேணிக்கு வழி" else "Route to Triplicane", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    } else {
                        Text(
                            text = if (isTa) {
                                "செயலி தொடங்கும் போது அருகிலுள்ள பேருந்து அல்லது மெட்ரோ நிறுத்தத்தை தானாக கண்டறிய GPS அனுமதியை இயக்கவும்."
                            } else {
                                "Allow location access to find your closest bus stop or metro boarding point automatically on app start."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Conversational Plan Query Box: "AI --> Tell me what's today's plan"
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("conversational_prompt_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // AI Greeting Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ChennaiNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isTa) "இன்றைய பயண திட்டம் என்ன?" else "Tell me what's today's plan?",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isElderly) 17.sp else 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isTa) "எ.கா: 'நீலாங்கரையிலிருந்து திருவல்லிக்கேணி செல்ல வேண்டும்'" else "e.g., 'I wanna go from Neelankarai to Triplicane'",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Text Input Field
                    OutlinedTextField(
                        value = aiLocationQuery,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_conversational_input"),
                        placeholder = {
                            Text(
                                text = if (isTa) "எங்கே இருந்து எங்கே செல்ல வேண்டும்?" else "Where do you want to travel from and to?",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = ChennaiNavy)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (aiLocationQuery.isNotBlank()) {
                                    IconButton(onClick = { onQueryChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        // Quick set prompt
                                        val sample = "i wanna go from neelankari to triplicane"
                                        onQueryChange(sample)
                                        onSearchNearest(sample, aiCommutePreference)
                                    }
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = ChennaiNavy)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Prompt Suggestions
                    Text(
                        text = if (isTa) "விரைவு பயண கேள்விகள்:" else "Quick Journey Prompts:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(conversationalPrompts) { (en, ta) ->
                            SuggestionChip(
                                onClick = {
                                    val promptText = if (isTa) ta else en
                                    onQueryChange(promptText)
                                    onSearchNearest(promptText, aiCommutePreference)
                                },
                                label = {
                                    Text(
                                        text = if (isTa) ta else en,
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Commute Preference Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val preferences = listOf(
                            CommutePreference.ALL_MODES to (if (isTa) "அனைத்தும்" else "All Modes"),
                            CommutePreference.STEP_FREE_ELDERLY to (if (isTa) "மின்தூக்கி (முதியோர்)" else "Step-Free Lift"),
                            CommutePreference.FASTEST_TRANSIT to (if (isTa) "வேகமானது" else "Fastest"),
                            CommutePreference.BUDGET_SAVING to (if (isTa) "இலவச/சிக்கனம்" else "Budget Saver")
                        )

                        preferences.forEach { (pref, label) ->
                            val isSelected = aiCommutePreference == pref
                            FilterChip(
                                selected = isSelected,
                                onClick = { onPreferenceChange(pref) },
                                label = { Text(label, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Submit Button: "Find Nearest Boarding & Commute"
                    Button(
                        onClick = {
                            onSearchNearest(aiLocationQuery, aiCommutePreference)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ai_plan_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ChennaiNavy),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAiLoading && aiLocationQuery.isNotBlank()
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(if (isTa) "AI வழிகளை கணக்கிடுகிறது..." else "Locating Transit Options...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTa) "ஏறும் இடம் & வழிகளை கண்டறி" else "Find Nearest Boarding & Destination",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Error message if any
        if (aiErrorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = aiErrorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // RESULTS SECTION: Parsed Trip Plan (Boarding Point & Destination breakdown)
        if (aiRecommendation?.parsedTripPlan != null) {
            val trip = aiRecommendation.parsedTripPlan

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parsed_trip_header_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTa) "பயண திட்டம்: ${trip.originQuery} ➔ ${trip.destinationQuery}" else "Trip Plan: ${trip.originQuery} ➔ ${trip.destinationQuery}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ChennaiGreen
                            ) {
                                Text(
                                    text = if (isTa) "வழிகள் தயார்" else "Routes Ready",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTa) trip.bestAdviceTa else trip.bestAdviceEn,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // 1. NEAREST BOARDING POINT (ORIGIN)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nearest_boarding_point_card"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, ChennaiGreen),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ChennaiGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isTa) "📍 அருகிலுள்ள ஏறும் இடம் (ORIGIN)" else "📍 NEAREST BOARDING POINT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChennaiGreen
                                )
                                Text(
                                    text = if (isTa) trip.nearestBoardingPoint.nameTa else trip.nearestBoardingPoint.nameEn,
                                    fontSize = if (isElderly) 18.sp else 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🚶 ${trip.nearestBoardingPoint.distanceEstimate}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTa) trip.nearestBoardingPoint.detailsTa else trip.nearestBoardingPoint.detailsEn,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            trip.nearestBoardingPoint.keyRoutes.forEach { route ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = route,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                onApplyOrigin(trip.resolvedOriginStationId ?: trip.nearestBoardingPoint.matchedStationId)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTa) "இதை தொடக்க நிலையமாக வை" else "Set as Origin in Planner")
                        }
                    }
                }
            }

            // 2. NEAREST ARRIVAL POINT (DESTINATION)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nearest_destination_point_card"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, ChennaiNavy),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ChennaiNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isTa) "🏁 சேரும் இடம் (DESTINATION)" else "🏁 NEAREST ARRIVAL POINT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChennaiNavy
                                )
                                Text(
                                    text = if (isTa) trip.nearestDestinationPoint.nameTa else trip.nearestDestinationPoint.nameEn,
                                    fontSize = if (isElderly) 18.sp else 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🚶 ${trip.nearestDestinationPoint.distanceEstimate}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ChennaiNavy
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTa) trip.nearestDestinationPoint.detailsTa else trip.nearestDestinationPoint.detailsEn,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            trip.nearestDestinationPoint.keyRoutes.forEach { route ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = route,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                onApplyDestination(trip.resolvedDestinationStationId ?: trip.nearestDestinationPoint.matchedStationId)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTa) "இதை சேரும் நிலையமாக வை" else "Set as Destination in Planner")
                        }
                    }
                }
            }

            // 3. COMMUTE TRIP OPTIONS
            item {
                Text(
                    text = if (isTa) "பரிந்துரைக்கப்பட்ட பயண வழிகள்:" else "Recommended Transit Routes:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            items(trip.tripOptions) { option ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (option.isRecommended) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        if (option.isRecommended) 1.5.dp else 1.dp,
                        if (option.isRecommended) ChennaiGreen else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (option.mode) {
                                        TransitMode.METRO -> Icons.Default.DirectionsSubway
                                        TransitMode.SUBURBAN_RAIL -> Icons.Default.Train
                                        else -> Icons.Default.DirectionsBus
                                    },
                                    contentDescription = null,
                                    tint = if (option.isRecommended) ChennaiGreen else ChennaiNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTa) option.titleTa else option.titleEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            if (option.isRecommended) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ChennaiGreen
                                ) {
                                    Text(
                                        text = if (isTa) "பரிந்துரை" else "Best Choice",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱ ${option.durationMins} mins",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "🏷 ${option.fare}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ChennaiGreen
                            )
                            if (option.isStepFree) {
                                Text(
                                    text = "♿ Step-Free",
                                    fontSize = 11.sp,
                                    color = ChennaiNavy
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTa) option.instructionsTa else option.instructionsEn,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Full Journey Plan Action
            item {
                Button(
                    onClick = { onApplyTripToPlanner(trip) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("apply_trip_to_planner_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ChennaiGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTa) "பயண வழிகாட்டியில் பார்க்க (Open Journey Planner)" else "Open Route in Journey Planner",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        } else if (aiRecommendation != null) {
            // Standard single-location commute options
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isTa) "பயண சுருக்கம்" else "Commute Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTa) aiRecommendation.summaryTa else aiRecommendation.summaryEn,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTa) aiRecommendation.recommendedBestOptionTa else aiRecommendation.recommendedBestOptionEn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChennaiGreen
                        )
                    }
                }
            }

            aiRecommendation.nearestBusStop?.let { bus ->
                item {
                    TransitPointCard(
                        title = if (isTa) "அருகிலுள்ள பேருந்து நிறுத்தம்" else "Nearest Bus Stop",
                        point = bus,
                        icon = Icons.Default.DirectionsBus,
                        badgeColor = ChennaiGreen,
                        isTa = isTa,
                        onApplyStation = onApplyOrigin
                    )
                }
            }

            aiRecommendation.nearestMetro?.let { metro ->
                item {
                    TransitPointCard(
                        title = if (isTa) "அருகிலுள்ள மெட்ரோ நிலையம்" else "Nearest Metro Station",
                        point = metro,
                        icon = Icons.Default.DirectionsSubway,
                        badgeColor = ChennaiNavy,
                        isTa = isTa,
                        onApplyStation = onApplyOrigin
                    )
                }
            }

            aiRecommendation.nearestSuburbanRail?.let { rail ->
                item {
                    TransitPointCard(
                        title = if (isTa) "அருகிலுள்ள புறநகர் ரயில்" else "Nearest Suburban Rail",
                        point = rail,
                        icon = Icons.Default.Train,
                        badgeColor = Color(0xFFC2185B),
                        isTa = isTa,
                        onApplyStation = onApplyOrigin
                    )
                }
            }
        }
    }
}

@Composable
private fun TransitPointCard(
    title: String,
    point: NearestTransitPoint,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    isTa: Boolean,
    onApplyStation: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(title, fontSize = 11.sp, color = badgeColor, fontWeight = FontWeight.Bold)
                        Text(if (isTa) point.nameTa else point.nameEn, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(point.distanceEstimate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(if (isTa) point.detailsTa else point.detailsEn, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    point.keyRoutes.take(3).forEach { r ->
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(r, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                TextButton(onClick = { onApplyStation(point.matchedStationId) }) {
                    Text(if (isTa) "பயணத்திற்கு பயன்படுத்து" else "Use as Origin", fontSize = 11.sp)
                }
            }
        }
    }
}
