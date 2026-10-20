package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChennaiTransitRepository
import com.example.model.Language
import com.example.model.Station
import com.example.model.TransitMode
import com.example.model.UserExperienceMode
import com.example.ui.theme.ChennaiGreen
import com.example.ui.theme.ChennaiNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSelectorCard(
    origin: Station,
    destination: Station,
    userMode: UserExperienceMode,
    language: Language,
    onSelectOrigin: (Station) -> Unit,
    onSelectDestination: (Station) -> Unit,
    onSwap: () -> Unit
) {
    val isTa = language == Language.TAMIL
    val isElderly = userMode == UserExperienceMode.ELDERLY

    var pickingForOrigin by remember { mutableStateOf<Boolean?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("station_selector_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isElderly) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isElderly) BorderStroke(2.dp, ChennaiNavy) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Origin Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pickingForOrigin = true }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Origin",
                    tint = ChennaiGreen,
                    modifier = Modifier.size(if (isElderly) 28.dp else 22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTa) "புறப்படும் இடம் (From)" else "Starting From (Origin)",
                        fontSize = if (isElderly) 14.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isTa) origin.nameTa else origin.nameEn,
                        fontSize = if (isElderly) 20.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = origin.locality,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { pickingForOrigin = true }) {
                    Text(if (isTa) "மாற்று" else "Change", fontWeight = FontWeight.Bold)
                }
            }

            // Dedicated Prominent Swap Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                FilledTonalButton(
                    onClick = onSwap,
                    modifier = Modifier
                        .testTag("swap_stations_button")
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ChennaiNavy.copy(alpha = 0.12f),
                        contentColor = ChennaiNavy
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Origin and Destination",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTa) "மாற்றுக (Swap)" else "Swap Stations",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // Destination Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pickingForOrigin = false }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Destination",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(if (isElderly) 28.dp else 22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTa) "சேருமிடம் (To)" else "Going To (Destination)",
                        fontSize = if (isElderly) 14.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isTa) destination.nameTa else destination.nameEn,
                        fontSize = if (isElderly) 20.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = destination.locality,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { pickingForOrigin = false }) {
                    Text(if (isTa) "மாற்று" else "Change", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal Bottom Sheet or Dialog to pick station
    if (pickingForOrigin != null) {
        val isSelectingOrigin = pickingForOrigin == true
        AlertDialog(
            onDismissRequest = { pickingForOrigin = null },
            title = {
                Text(
                    text = if (isTa) {
                        if (isSelectingOrigin) "புறப்படும் நிலையத்தைத் தேர்ந்தெடுக்கவும்" else "சேரும் நிலையத்தைத் தேர்ந்தெடுக்கவும்"
                    } else {
                        if (isSelectingOrigin) "Choose Starting Station" else "Choose Destination Station"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(ChennaiTransitRepository.STATIONS) { station ->
                        val isSelected = if (isSelectingOrigin) station.id == origin.id else station.id == destination.id
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = if (isTa) station.nameTa else station.nameEn,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = if (isElderly) 18.sp else 15.sp
                                )
                            },
                            supportingContent = {
                                Text("${station.locality} • " + station.supportedModes.joinToString(", ") { mode ->
                                    when (mode) {
                                        TransitMode.METRO -> "Metro"
                                        TransitMode.SUBURBAN_RAIL -> "Suburban"
                                        TransitMode.BUS -> "MTC Bus"
                                        TransitMode.WALK -> "Walk"
                                    }
                                })
                            },
                            leadingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    if (station.supportedModes.contains(TransitMode.METRO)) {
                                        Icon(Icons.Default.Subway, contentDescription = "Metro", modifier = Modifier.size(16.dp), tint = ChennaiNavy)
                                    }
                                    if (station.supportedModes.contains(TransitMode.BUS)) {
                                        Icon(Icons.Default.DirectionsBus, contentDescription = "Bus", modifier = Modifier.size(16.dp), tint = ChennaiGreen)
                                    }
                                    if (station.supportedModes.contains(TransitMode.SUBURBAN_RAIL)) {
                                        Icon(Icons.Default.Train, contentDescription = "Rail", modifier = Modifier.size(16.dp), tint = Color(0xFFC05621))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelectingOrigin) {
                                        onSelectOrigin(station)
                                    } else {
                                        onSelectDestination(station)
                                    }
                                    pickingForOrigin = null
                                }
                        )
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickingForOrigin = null }) {
                    Text(if (isTa) "மூடு" else "Close")
                }
            }
        )
    }
}
