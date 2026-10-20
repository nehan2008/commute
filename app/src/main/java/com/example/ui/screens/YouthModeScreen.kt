package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.model.*
import com.example.ui.components.StationSelectorCard
import com.example.ui.theme.ChennaiGreen
import com.example.ui.theme.ChennaiNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouthModeScreen(
    origin: Station,
    destination: Station,
    activeFilter: JourneyFilter,
    journeyResults: List<JourneyOption>,
    selectedJourney: JourneyOption?,
    disruptions: List<DisruptionReport>,
    language: Language,
    onSelectOrigin: (Station) -> Unit,
    onSelectDestination: (Station) -> Unit,
    onSwapStations: () -> Unit,
    onSetFilter: (JourneyFilter) -> Unit,
    onSelectJourney: (JourneyOption) -> Unit,
    onUpvoteDisruption: (String) -> Unit,
    onOpenReportDialog: () -> Unit,
    onShareRoute: () -> Unit,
    onOpenAiTransitFinder: (() -> Unit)? = null
) {
    val isTa = language == Language.TAMIL
    var activeTab by remember { mutableStateOf(0) } // 0: Route Compare, 1: Live Transit Advisories & Reports

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("youth_screen_lazy_column")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
    ) {
        // Hero Visual Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.chennai_hero_banner),
                        contentDescription = "Chennai Public Mobility",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xE60A3663))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = if (isTa) "வேகமான மல்டி-மோடல் சென்னை பயணம்" else "Fast Multi-Modal Transit Planner",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (isTa) "மெட்ரோ • மாநகரப் பேருந்து • லோக்கல் ரயில்" else "CMRL Metro • MTC Bus • Local Electric Train",
                            color = Color(0xFFD6E4FF),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Sub-tabs: Route Comparison vs Advisories & Reports
        item {
            SecondaryTabRow(
                selectedTabIndex = activeTab,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTa) "வழிகள் ஒப்பீடு" else "Route Compare",
                                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTa) "அறிக்கைகள் & புகார்கள்" else "Transit Advisories",
                                fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }

        if (activeTab == 0) {
            // Station Selector Card
            item {
                StationSelectorCard(
                    origin = origin,
                    destination = destination,
                    userMode = UserExperienceMode.YOUTH,
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
                            .height(44.dp)
                            .testTag("youth_ai_locate_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTa) "✨ அருகிலுள்ள நிலையம்/பேருந்து நிறுத்தத்தை AI மூலம் கண்டறி" else "✨ Locate Nearest Station / Bus Stop with Gemini AI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Transit Filter Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(
                        JourneyFilter.ALL to (if (isTa) "அனைத்தும்" else "All Modes"),
                        JourneyFilter.METRO_ONLY to (if (isTa) "மெட்ரோ மட்டும்" else "Metro Only"),
                        JourneyFilter.BUS_ONLY to (if (isTa) "பேருந்து மட்டும்" else "Bus Only"),
                        JourneyFilter.SUBURBAN_ONLY to (if (isTa) "லோக்கல் ரயில் மட்டும்" else "Local Train Only"),
                        JourneyFilter.CHEAPEST to (if (isTa) "குறைந்த கட்டணம்" else "Cheapest"),
                        JourneyFilter.FEWEST_TRANSFERS to (if (isTa) "குறைந்த மாற்றங்கள்" else "Fewest Transfers"),
                        JourneyFilter.LEAST_WALKING to (if (isTa) "குறைந்த நடை" else "Least Walking")
                    )

                    items(filters) { (filter, label) ->
                        FilterChip(
                            selected = activeFilter == filter,
                            onClick = { onSetFilter(filter) },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_chip_${filter.name}")
                        )
                    }
                }
            }

            // Route Comparison List
            items(journeyResults) { option ->
                val isSelected = option.id == selectedJourney?.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectJourney(option) }
                        .testTag("youth_journey_card_${option.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = if (isSelected) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = if (isSelected) "Selected" else "Not Selected",
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTa) option.titleTa else option.titleEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Fare Chip
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF92400E))
                                    Text(
                                        text = "${option.totalFareRupees}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Duration, Transfers, Walk, CO2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(17.dp), tint = ChennaiNavy)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${option.totalDurationMins} ${if (isTa) "நிமி" else "min"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TransferWithinAStation, contentDescription = null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${option.totalTransfers} ${if (isTa) "மாற்றம்" else "transfer"}",
                                    fontSize = 13.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(17.dp), tint = ChennaiGreen)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${option.totalWalkMeters}m",
                                    fontSize = 13.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(17.dp), tint = ChennaiGreen)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${option.co2SavedKg}kg CO₂",
                                    fontSize = 12.sp,
                                    color = ChennaiGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Route Legs preview
                        option.legs.forEach { leg ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icon = when (leg.mode) {
                                        TransitMode.METRO -> Icons.Default.Subway
                                        TransitMode.BUS -> Icons.Default.DirectionsBus
                                        TransitMode.SUBURBAN_RAIL -> Icons.Default.Train
                                        TransitMode.WALK -> Icons.Default.DirectionsWalk
                                    }
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = ChennaiNavy)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${leg.routeCode}: " + (if (isTa) "${leg.fromStationTa} ➔ ${leg.toStationTa}" else "${leg.fromStation} ➔ ${leg.toStation}"),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isTa) option.dataSourcingNoteTa else option.dataSourcingNoteEn,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Share Route Button
            item {
                OutlinedButton(
                    onClick = onShareRoute,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("youth_share_route_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTa) "பயண வழியை நண்பர்களுடன் பகிரவும்" else "Share Journey Itinerary", fontSize = 14.sp)
                }
            }
        } else {
            // Live Transit Advisories & Reports Tab
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTa) "அதிகாரப்பூர்வ அறிவிப்புகள் & பயணிகள் பதிவுகள்" else "Official Notices & Commuter Feed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Button(
                        onClick = onOpenReportDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("report_issue_button")
                    ) {
                        Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTa) "பதிவிடு" else "Report Issue", fontSize = 12.sp)
                    }
                }
            }

            // Real Disruption Items (Official Advisories & Real Reports)
            items(disruptions) { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("disruption_card_${report.id}"),
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
                            Surface(
                                color = if (report.isOfficialAdvisory) Color(0xFFDCFCE7) else Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (report.isOfficialAdvisory) (if (isTa) "அதிகாரப்பூர்வ அறிவிப்பு" else "Official Advisory") else (if (isTa) "பயணிகள் பதிவு" else "Commuter Report"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (report.isOfficialAdvisory) Color(0xFF166534) else Color(0xFF991B1B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = report.reportedTimeAgo,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = report.routeOrStation,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = if (isTa) report.summaryTa else report.summaryEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (isTa) report.detailTa else report.detailEn,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        if (!report.isOfficialAdvisory) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (report.upvotes > 0) "${report.upvotes} " + (if (isTa) "பயணிகள் உறுதிசெய்துள்ளனர்" else "commuter confirmed") else (if (isTa) "உறுதிப்படுத்த காத்திருக்கிறது" else "Awaiting confirmations"),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                FilledTonalButton(
                                    onClick = { onUpvoteDisruption(report.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("upvote_disruption_${report.id}")
                                ) {
                                    Icon(Icons.Default.ThumbUp, contentDescription = "Confirm", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isTa) "உறுதி செய்" else "Confirm", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
