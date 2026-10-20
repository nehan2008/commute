package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.MetroStationFacility
import com.example.ui.theme.ChennaiGreen
import com.example.ui.theme.ChennaiNavy

@Composable
fun ChennaiMetroGuideScreen(
    metroFacilities: List<MetroStationFacility>,
    language: Language
) {
    val isTa = language == Language.TAMIL
    var selectedFacilityId by remember { mutableStateOf<String?>(metroFacilities.firstOrNull()?.stationId) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("metro_guide_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Metro Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ChennaiNavy),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Subway, contentDescription = null, tint = ChennaiNavy, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isTa) "சென்னை மெட்ரோ அதிகாரப்பூர்வ வழிகாட்டி" else "Chennai Metro (CMRL) Guide",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = if (isTa) "முதல் ரயில்: 05:00 AM • கடைசி ரயில்: 11:00 PM" else "First Train: 05:00 AM • Last Train: 11:00 PM",
                                color = Color(0xFFD6E4FF),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Fare Chart & 20% Discount Callout (from Chennai Metro App)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Discount, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTa) "சிங்கார சென்னை / NCMC கார்டில் 20% தள்ளுபடி" else "Singara Chennai / NCMC 20% Fare Discount",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isTa) {
                            "மெட்ரோ கட்டண அட்டவணை:\n• 0-2 கி.மீ: ₹10\n• 2-5 கி.மீ: ₹20\n• 5-12 கி.மீ: ₹30\n• 12-21 கி.மீ: ₹40\n• 21 கி.மீ மேல்: ₹50\n(NCMC / மெட்ரோ பயண அட்டையில் 20% சேமிப்பு)."
                        } else {
                            "Official Distance Slabs:\n• 0-2 km: ₹10\n• 2-5 km: ₹20\n• 5-12 km: ₹30\n• 12-21 km: ₹40\n• Beyond 21 km: ₹50\n(Save 20% with National Common Mobility Card & QR tickets)."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }

        // Station Facilities Directory Title
        item {
            Text(
                text = if (isTa) "முக்கிய மெட்ரோ நிலைய வசதிகள் & நுழைவு வாயில்கள்" else "Station Facilities & Exit Gates",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Station Cards with Expansion
        items(metroFacilities) { facility ->
            val isExpanded = selectedFacilityId == facility.stationId

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("metro_station_${facility.stationId}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(
                    width = if (isExpanded) 2.dp else 1.dp,
                    color = if (isExpanded) ChennaiNavy else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTa) facility.nameTa else facility.nameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = facility.line,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(onClick = {
                            selectedFacilityId = if (isExpanded) null else facility.stationId
                        }) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Key Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Elevator, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF166534))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lift / Escalator", fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Surface(
                            color = Color(0xFFE0E7FF),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocalParking, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF3730A3))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("2W & 4W Parking", fontSize = 11.sp, color = Color(0xFF3730A3), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Surface(
                            color = Color(0xFFF3E8FF),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Accessible, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6B21A8))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Wheelchair", fontSize = 11.sp, color = Color(0xFF6B21A8), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // First/Last Timings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "⏰ " + (if (isTa) "முதல் ரயில்: ${facility.firstTrain}" else "First Train: ${facility.firstTrain}"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "🌙 " + (if (isTa) "கடைசி ரயில்: ${facility.lastTrain}" else "Last Train: ${facility.lastTrain}"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Gates & Exits
                        Text(
                            text = if (isTa) "வெளியேறும் வழிகள் & வாசல்கள் (Exit Gates):" else "Station Gates & Exits:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        facility.exits.forEach { exit ->
                            Text(
                                text = "• $exit",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Feeder Services
                        Text(
                            text = if (isTa) "இணைப்புப் பேருந்து & ஷேர் ஆட்டோ சேவைகள்:" else "Feeder Mini-buses & Share Autos:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        facility.feederServices.forEach { feeder ->
                            Text(
                                text = "• $feeder",
                                fontSize = 12.sp,
                                color = ChennaiGreen,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
