package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChennaiTransitRepository
import com.example.model.*
import com.example.ui.theme.ChennaiGreen
import com.example.ui.theme.ChennaiNavy
import com.example.ui.theme.ElderlyYellowAccent

@Composable
fun RapidoHomeScreen(
    userProfile: UserProfile,
    userMode: UserExperienceMode,
    language: Language,
    origin: Station,
    destination: Station,
    userLocation: com.example.data.location.UserLocationResult? = null,
    onSelectOrigin: (Station) -> Unit,
    onSelectDestination: (Station) -> Unit,
    onNavigateToPlanner: () -> Unit,
    onNavigateToAiCommute: () -> Unit,
    onNavigateToMetroGuide: () -> Unit,
    onNavigateToAdvisories: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onModeChange: (UserExperienceMode) -> Unit
) {
    val isTa = language == Language.TAMIL
    val isElderly = userMode == UserExperienceMode.ELDERLY

    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else ChennaiTransitRepository.searchStationsAndStops(searchQuery)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("rapido_home_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
    ) {
        // Top Profile Greeting & Persona Mode Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Greeting & Profile
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToProfile() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (userProfile.isLoggedIn) ChennaiGreen else ChennaiNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (userProfile.isLoggedIn) Icons.Default.Person else Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (userProfile.isLoggedIn) {
                                if (isTa) "வணக்கம், ${userProfile.name}" else "Vanakkam, ${userProfile.name}"
                            } else {
                                if (isTa) "வணக்கம், சென்னை பயணி" else "Hello, Chennai Commuter"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (userProfile.isLoggedIn) {
                                "+91 ${userProfile.phoneNumber} • ${userProfile.category.name.replace("_", " ")}"
                            } else {
                                if (isTa) "மொபைல் எண் மூலம் உள்நுழைக" else "Tap to login with mobile"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Mode switch pill
                FilledTonalButton(
                    onClick = {
                        onModeChange(if (isElderly) UserExperienceMode.YOUTH else UserExperienceMode.ELDERLY)
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isElderly) ChennaiNavy else ChennaiGreen.copy(alpha = 0.2f),
                        contentColor = if (isElderly) ElderlyYellowAccent else ChennaiGreen
                    )
                ) {
                    Icon(
                        imageVector = if (isElderly) Icons.Default.AccessibilityNew else Icons.Default.ElectricBolt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isElderly) (if (isTa) "முதியோர்" else "Elderly") else (if (isTa) "இளைஞர்" else "Youth"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Mode Explanation Subtitle
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isElderly) Icons.Default.DirectionsWalk else Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isElderly) ChennaiNavy else ChennaiGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isElderly) {
                            if (isTa) "முதியோர் முறை: குறைந்த நடைபயிற்சி, மாடிப்படி தவிர்ப்பு, மின்தூக்கி வசதி" else "Elderly Mode: Zero transfers, minimal walking, and verified step-free lifts"
                        } else {
                            if (isTa) "இளைஞர் முறை: அதிவிரைவு பயணம், ₹5 குறைந்த ரயில் கட்டணம், கார்பன் சேமிப்பு" else "Youth Mode: Fastest multi-modal transit, ₹5 student fares & CO₂ savings"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Auto-detected User Location Badge
        if (userLocation != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAiCommute() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = BorderStroke(1.dp, ChennaiGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = ChennaiGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isTa) "📍 நடப்பு இடம்: ${userLocation.localityNameTa}" else "📍 Current Area: ${userLocation.localityName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = if (isTa) "அருகிலுள்ள ஏறும் நிறுத்தம்: ${userLocation.nearestBoardingPoint.nameTa}" else "Nearest Boarding: ${userLocation.nearestBoardingPoint.nameEn}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ChennaiGreen)
                    }
                }
            }
        }

        // Gemini AI Transit Finder Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAiCommute() }
                    .testTag("rapido_gemini_ai_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isElderly) ChennaiNavy else MaterialTheme.colorScheme.primaryContainer
                ),
                border = BorderStroke(1.5.dp, if (isElderly) ElderlyYellowAccent else ChennaiGreen.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isElderly) ElderlyYellowAccent else ChennaiNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Gemini AI",
                                    tint = if (isElderly) ChennaiNavy else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isTa) "ஜெமினி AI வழிகாட்டி" else "Gemini AI Transit Finder",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isElderly) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (isElderly) ElderlyYellowAccent else ChennaiGreen,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "NEW",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isElderly) ChennaiNavy else Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isTa) "அருகிலுள்ள பேருந்து நிறுத்தம், மெட்ரோ & ஷேர் ஆட்டோ" else "Locate nearest Metro, Bus stop & Share Auto",
                                    fontSize = 12.sp,
                                    color = if (isElderly) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = if (isElderly) ElderlyYellowAccent else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isTa) {
                            "எந்த நிலையம் அல்லது பேருந்து நிறுத்தம் அருகில் உள்ளது என்று தெரியவில்லையா? மெரினா, பெசன்ட் நகர், அப்பல்லோ என எதையும் தட்டச்சு செய்து கண்டறியுங்கள்."
                        } else {
                            "Not sure which station or bus stop is closest? Tell Gemini your landmark (e.g. Marina Beach, Apollo Hospital, TIDEL Park) to find the best route!"
                        },
                        fontSize = 12.sp,
                        color = if (isElderly) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = if (isElderly) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isTa) "♿ லிப்ட் வசதி" else "♿ Step-Free Lifts",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    color = if (isElderly) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                color = if (isElderly) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isTa) "🚌 விடியல் பயணம்" else "🚌 Free MTC Bus",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    color = if (isElderly) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Text(
                            text = if (isTa) "திறக்க தட்டவும் →" else "Tap to open →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isElderly) ElderlyYellowAccent else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // In-Place Search Box (Does NOT relocate unexpectedly!)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rapido_search_box"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.5.dp, if (isElderly) ChennaiNavy else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isTa) "எங்கு செல்ல விரும்புகிறீர்கள்?" else "Where are you going?",
                        fontSize = if (isElderly) 20.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Search TextField
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input"),
                        placeholder = {
                            Text(
                                text = if (isTa) "ECR, OMR, பேருந்து நிறுத்தம், மெட்ரோ..." else "Search ECR, OMR, bus stops, metro...",
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Current Selected Origin & Destination
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📍 " + (if (isTa) "புறப்படும் இடம்: ${origin.nameTa}" else "From: ${origin.nameEn}"),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "🎯 " + (if (isTa) "சேருமிடம்: ${destination.nameTa}" else "To: ${destination.nameEn}"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = onNavigateToPlanner,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (isTa) "பயணத்தை திட்டமிடு" else "Plan Journey", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Live In-Place Search Results (when typing)
        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    text = if (isTa) "தேடல் முடிவுகள் (${searchResults.size}):" else "Search Results for \"$searchQuery\" (${searchResults.size}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (searchResults.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isTa) "\"$searchQuery\" தொடர்புடைய நிலையங்கள் கிடைக்கவில்லை. ECR, OMR, அல்லது மெட்ரோ பெயரை முயற்சிக்கவும்." else "No stations or bus stops found for \"$searchQuery\". Try searching 'ECR', 'OMR', 'Guindy', or 'Bus'.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(searchResults) { station ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_result_${station.id}"),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isTa) station.nameTa else station.nameEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = station.locality,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Mode Icons
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (station.supportedModes.contains(TransitMode.BUS)) {
                                        Surface(
                                            color = ChennaiGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(14.dp), tint = ChennaiGreen)
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Bus", fontSize = 11.sp, color = ChennaiGreen, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    if (station.supportedModes.contains(TransitMode.METRO)) {
                                        Surface(
                                            color = ChennaiNavy.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Subway, contentDescription = null, modifier = Modifier.size(14.dp), tint = ChennaiNavy)
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Metro", fontSize = 11.sp, color = ChennaiNavy, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    if (station.supportedModes.contains(TransitMode.SUBURBAN_RAIL)) {
                                        Surface(
                                            color = Color(0xFFEA580C).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Train, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFC05621))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Rail", fontSize = 11.sp, color = Color(0xFFC05621), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: Set as Origin or Set as Destination
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onSelectOrigin(station)
                                        searchQuery = ""
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isTa) "புறப்படும் இடம்" else "Set as Origin", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        onSelectDestination(station)
                                        searchQuery = ""
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChennaiNavy),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isTa) "சேருமிடம்" else "Set as Destination", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Popular Chennai Hubs
        item {
            Column {
                Text(
                    text = if (isTa) "விரைவு சேருமிடங்கள்" else "Popular Transit Hubs",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val quickStops = ChennaiTransitRepository.STATIONS.filter {
                        it.id in listOf("ecr_thiruvanmiyur", "guindy", "cmbt", "sholinganallur", "tnagar", "airport", "tambaram")
                    }
                    items(quickStops) { station ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.clickable { onSelectDestination(station) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = ChennaiNavy,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isTa) station.nameTa else station.nameEn,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Rapido-Style Service Category Grid
        item {
            Text(
                text = if (isTa) "சென்னை பொதுப் போக்குவரத்து சேவைகள்" else "Chennai Transit Network",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CMRL Metro Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToMetroGuide() }
                        .testTag("rapido_metro_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ChennaiNavy.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Subway, contentDescription = "Metro", tint = ChennaiNavy, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isTa) "சென்னை மெட்ரோ" else "CMRL Metro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isTa) "விரைவு • மின்தூக்கி வசதி • ₹10-₹50" else "Fast • 100% Lifts • ₹10-₹50",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // MTC City Bus Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToPlanner() }
                        .testTag("rapido_bus_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ChennaiGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsBus, contentDescription = "Bus", tint = ChennaiGreen, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isTa) "மாநகரப் பேருந்து" else "MTC City Bus",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isTa) "விடியல் பயணம் (இலவசம்) • ₹15" else "Vidiyal Payanam Free • ₹15",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Chennai Local Train Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToPlanner() }
                        .testTag("rapido_train_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEA580C).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Train, contentDescription = "Train", tint = Color(0xFFC05621), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isTa) "லோக்கல் ரயில்" else "Local Train",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isTa) "கடற்கரை - தாம்பரம் • ₹5 கட்டணம்" else "Beach - Tambaram • ₹5 Fare",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Official Metro Guide Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToMetroGuide() }
                        .testTag("rapido_guide_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Map, contentDescription = "Guide", tint = Color(0xFF7C3AED), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isTa) "மெட்ரோ வழிகாட்டி" else "Metro Guide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isTa) "நிலைய வசதிகள் • நேரங்கள் • கட்டணம்" else "Facilities • Timings • Fares",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Live Service Advisory Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAdvisories() },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTa) "அதிகாரப்பூர்வ CMRL அறிக்கை" else "Official CMRL Advisory",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            text = if (isTa) "அலுவலக நேரங்களில் 6 நிமிடங்களுக்கு ஒருமுறை ரயில்கள் இயக்கப்படுகின்றன." else "Peak hour trains operating at 6-minute frequency. All station lifts functional.",
                            fontSize = 12.sp,
                            color = Color(0xFF166534)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View",
                        tint = Color(0xFF16A34A)
                    )
                }
            }
        }
    }
}
