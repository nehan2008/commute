package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.StationSelectorCard
import com.example.ui.theme.ChennaiGreen
import com.example.ui.theme.ChennaiNavy
import com.example.ui.theme.ElderlyYellowAccent

@Composable
fun ElderlyModeScreen(
    origin: Station,
    destination: Station,
    journeyResults: List<JourneyOption>,
    selectedJourney: JourneyOption?,
    activeStepIndex: Int,
    isSpeaking: Boolean,
    language: Language,
    onSelectOrigin: (Station) -> Unit,
    onSelectDestination: (Station) -> Unit,
    onSwapStations: () -> Unit,
    onSelectJourney: (JourneyOption) -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onSpeakDirections: () -> Unit,
    onStopSpeaking: () -> Unit,
    onShareWithFamily: () -> Unit,
    onOpenAiTransitFinder: (() -> Unit)? = null
) {
    val isTa = language == Language.TAMIL
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("elderly_screen_lazy_column")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
    ) {
        // High-Contrast Assistance Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ChennaiNavy),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(ElderlyYellowAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessibilityNew,
                            contentDescription = null,
                            tint = ChennaiNavy,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isTa) "முதியோர் உதவி வழிகாட்டி" else "Senior Citizen Commute Guide",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = if (isTa) "குறைந்த நடை • மின்தூக்கி (Lift) வசதி முன்னுரிமை" else "Minimum Walking • 100% Lift Prioritized",
                            color = ElderlyYellowAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Station Selector Card
        item {
            StationSelectorCard(
                origin = origin,
                destination = destination,
                userMode = UserExperienceMode.ELDERLY,
                language = language,
                onSelectOrigin = onSelectOrigin,
                onSelectDestination = onSelectDestination,
                onSwap = onSwapStations
            )
        }

        if (onOpenAiTransitFinder != null) {
            item {
                OutlinedButton(
                    onClick = onOpenAiTransitFinder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("elderly_ai_locate_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, ChennaiNavy),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ChennaiNavy.copy(alpha = 0.06f),
                        contentColor = ChennaiNavy
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp), tint = ChennaiNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTa) "✨ அருகிலுள்ள நிலையத்தை கண்டறிய AI உதவி" else "✨ Locate Nearest Station / Bus Stop with AI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick Action Buttons: Speak Aloud & Share with Family
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large Speak button
                Button(
                    onClick = {
                        if (isSpeaking) onStopSpeaking() else onSpeakDirections()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .testTag("elderly_speak_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpeaking) MaterialTheme.colorScheme.error else ChennaiNavy,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Voice Guide",
                        tint = ElderlyYellowAccent,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSpeaking) {
                            if (isTa) "நிறுத்து" else "Stop Voice"
                        } else {
                            if (isTa) "குரல் வழிகாட்டி" else "Listen Voice"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Large Family Share button
                Button(
                    onClick = onShareWithFamily,
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .testTag("elderly_share_family_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChennaiGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTa) "குடும்பத்துடன் பகிர்" else "Share Route",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Recommended Routes Header
        item {
            Text(
                text = if (isTa) "பரிந்துரைக்கப்பட்ட எளிய வழிகள் (வழியைத் தேர்ந்தெடுக்கவும்):" else "Recommended Routes (Tap to Select Route):",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Route Cards with High-Contrast Selection Design
        items(journeyResults) { option ->
            val isSelected = option.id == selectedJourney?.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectJourney(option) }
                    .testTag("journey_option_${option.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFFEF9C3) else MaterialTheme.colorScheme.surface
                ),
                elevation = if (isSelected) CardDefaults.cardElevation(defaultElevation = 6.dp) else CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(
                    width = if (isSelected) 3.5.dp else 1.5.dp,
                    color = if (isSelected) ChennaiNavy else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Selection Badge Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Surface(
                                color = ChennaiNavy,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElderlyYellowAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTa) "தேர்ந்தெடுக்கப்பட்ட வழி" else "CURRENT SELECTED ROUTE",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isTa) "தேர்ந்தெடுக்க தொடவும்" else "Tap to Select",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (option.isDirect) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isTa) "நேரடி பயணம்" else "DIRECT ROUTE",
                                    color = Color(0xFF166534),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isTa) option.titleTa else option.titleEn,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metrics: Time, Walk, Fare
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = ChennaiNavy, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${option.totalDurationMins} ${if (isTa) "நிமிடங்கள்" else "mins"}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = ChennaiGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${option.totalWalkMeters} m ${if (isTa) "நடை" else "walk"}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                            Text(
                                text = "${option.totalFareRupees}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isTa) option.dataSourcingNoteTa else option.dataSourcingNoteEn,
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            }
        }

        // Active Step-by-Step Guidance Card
        if (selectedJourney != null && selectedJourney.legs.isNotEmpty()) {
            val currentLeg = selectedJourney.legs.getOrNull(activeStepIndex) ?: selectedJourney.legs.first()

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("elderly_step_by_step_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.5.dp, ChennaiGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ChennaiGreen,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isTa) "படி ${activeStepIndex + 1} / ${selectedJourney.legs.size}" else "STEP ${activeStepIndex + 1} OF ${selectedJourney.legs.size}",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            Text(
                                text = "${currentLeg.durationMins} ${if (isTa) "நிமிடங்கள்" else "mins"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isTa) "${currentLeg.fromStationTa} ➔ ${currentLeg.toStationTa}" else "${currentLeg.fromStation} ➔ ${currentLeg.toStation}",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "${currentLeg.routeCode} • " + (if (isTa) currentLeg.routeDescriptionTa else currentLeg.routeDescriptionEn),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Verified Accessibility Callout
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Accessibility",
                                    tint = ChennaiGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isTa) currentLeg.accessibilityNoteTa else currentLeg.accessibilityNoteEn,
                                    fontSize = 14.sp,
                                    color = Color(0xFF166534),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Next / Prev Step Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = onPrevStep,
                                enabled = activeStepIndex > 0,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("step_prev_button")
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTa) "முந்தைய படி" else "Previous Step")
                            }

                            Button(
                                onClick = onNextStep,
                                enabled = activeStepIndex < selectedJourney.legs.size - 1,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ChennaiNavy),
                                modifier = Modifier.testTag("step_next_button")
                            ) {
                                Text(if (isTa) "அடுத்த படி" else "Next Step")
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                            }
                        }
                    }
                }
            }
        }

        // Eye-Appealing & Accessible Emergency Helplines for Senior Citizens
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, ChennaiNavy.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ChennaiNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isTa) "அவசர உதவி & பயணிகள் வழிகாட்டி எண்கள்" else "Chennai Transit & Senior Helplines",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isTa) "நேரடியாக தொடர்பு கொள்ள பட்டனை அழுத்தவும்" else "One-tap direct dial assistance",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. CMRL Metro Helpline
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = if (isTa) "சென்னை மெட்ரோ உதவி (24x7)" else "Chennai Metro Helpline",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "1860-425-1515",
                                    fontSize = 14.sp,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18604251515"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.widthIn(min = 96.dp).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ChennaiNavy)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isTa) "அழை" else "Call", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 2. MTC Bus Control Room
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = if (isTa) "மாநகரப் பேருந்து உதவி (MTC)" else "MTC Bus Control Room",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Toll Free: 149",
                                    fontSize = 14.sp,
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:149"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.widthIn(min = 96.dp).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ChennaiGreen)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isTa) "அழை" else "Call", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 3. National Elderline
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = if (isTa) "மூத்த குடிமக்கள் உதவி (Elderline)" else "Senior Citizen Elderline",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Toll Free: 14567",
                                    fontSize = 14.sp,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:14567"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.widthIn(min = 96.dp).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isTa) "அழை" else "Call", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
