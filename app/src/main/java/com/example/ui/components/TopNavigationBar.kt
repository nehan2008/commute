package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.UserExperienceMode
import com.example.ui.theme.ChennaiGreen
import com.example.ui.theme.ChennaiNavy
import com.example.ui.theme.ElderlyYellowAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    userMode: UserExperienceMode,
    language: Language,
    onModeChange: (UserExperienceMode) -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenTransparency: () -> Unit
) {
    val isTa = language == Language.TAMIL

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Main App Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ChennaiNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CM",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isTa) "சென்னை மூவ்" else "Chennai Move",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isTa) "CUMTA ஒருங்கிணைந்த போக்குவரத்து" else "CUMTA Unified Mobility",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Action buttons: Tamil Toggle & Transparency Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier
                            .testTag("language_toggle_button")
                            .height(38.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTa) "English" else "தமிழ்",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onOpenTransparency,
                        modifier = Modifier
                            .testTag("info_transparency_button")
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Data Sources & Licenses",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual-Mode Selector Bar (Elderly vs Youth)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Elderly Mode Tab
                    val isElderly = userMode == UserExperienceMode.ELDERLY
                    Button(
                        onClick = { onModeChange(UserExperienceMode.ELDERLY) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_elderly_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isElderly) ChennaiNavy else Color.Transparent,
                            contentColor = if (isElderly) ElderlyYellowAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        elevation = if (isElderly) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTa) "முதியோர் பயன்முறை" else "Elderly Mode",
                            fontWeight = if (isElderly) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }

                    // Youth Mode Tab
                    val isYouth = userMode == UserExperienceMode.YOUTH
                    Button(
                        onClick = { onModeChange(UserExperienceMode.YOUTH) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_youth_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isYouth) ChennaiGreen else Color.Transparent,
                            contentColor = if (isYouth) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        elevation = if (isYouth) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTa) "இளைஞர் பயன்முறை" else "Youth Mode",
                            fontWeight = if (isYouth) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
