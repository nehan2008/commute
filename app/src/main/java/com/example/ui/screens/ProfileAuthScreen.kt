package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CommuterCategory
import com.example.model.Language
import com.example.model.UserProfile
import com.example.ui.theme.ChennaiGreen
import com.example.ui.theme.ChennaiNavy
import com.example.ui.theme.ElderlyYellowAccent

@Composable
fun ProfileAuthScreen(
    userProfile: UserProfile,
    phoneInput: String,
    otpInput: String,
    isOtpSent: Boolean,
    authError: String?,
    language: Language,
    onPhoneChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onRequestOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: (String, CommuterCategory, String, String) -> Unit
) {
    val isTa = language == Language.TAMIL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_auth_screen")
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (!userProfile.isLoggedIn) {
            // Phone Authentication Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(ChennaiNavy.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = ChennaiNavy,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isTa) "மொபைல் எண் மூலம் உள்நுழைக" else "Login with Mobile Number",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isTa) "பயண விவரங்களை குடும்பத்தினருடன் எளிதாகப் பகிரவும், முன்னுரிமை கட்டணங்களை அணுகவும் உள்நுழையவும்." else "Login to manage family emergency contacts, trip history, and commuter passes.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isOtpSent) {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = onPhoneChange,
                            label = { Text(if (isTa) "10 இலக்க மொபைல் எண்" else "10-digit Mobile Number") },
                            leadingIcon = {
                                Text(
                                    text = "🇮🇳 +91 ",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input_field")
                        )

                        if (authError != null) {
                            Text(
                                text = authError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onRequestOtp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("send_otp_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ChennaiNavy),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isTa) "சரிபார்ப்பு குறியீட்டைப் பெறவும் (OTP)" else "Get Verification Code (OTP)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // OTP Verification view
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChennaiGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTa) "+91 $phoneInput-க்கு OTP அனுப்பப்பட்டுள்ளது (மாதிரி: 123456)" else "OTP sent to +91 $phoneInput (Demo OTP: 123456)",
                                    fontSize = 13.sp,
                                    color = Color(0xFF166534)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = onOtpChange,
                            label = { Text(if (isTa) "6 இலக்க OTP குறியீடு" else "6-digit OTP Code") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input_field")
                        )

                        if (authError != null) {
                            Text(
                                text = authError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onVerifyOtp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("verify_otp_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ChennaiGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isTa) "உள்நுழைவை உறுதி செய்" else "Verify & Sign In", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Logged-in Profile View
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(ChennaiNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userProfile.name.take(1),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = userProfile.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "+91 ${userProfile.phoneNumber}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onLogout,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isTa) "வெளியேறு" else "Logout", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // Commuter Category Selector
                    Text(
                        text = if (isTa) "பயணி வகை (Commuter Category - மாற்ற தேர்வு செய்யவும்):" else "Commuter Category (Tap to switch):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = userProfile.category == CommuterCategory.GENERAL,
                            onClick = {
                                onUpdateProfile(userProfile.name, CommuterCategory.GENERAL, userProfile.emergencyContactName, userProfile.emergencyContactPhone)
                            },
                            label = { Text(if (isTa) "தினசரி பயணி" else "General", fontSize = 12.sp) }
                        )

                        FilterChip(
                            selected = userProfile.category == CommuterCategory.STUDENT,
                            onClick = {
                                onUpdateProfile(userProfile.name, CommuterCategory.STUDENT, userProfile.emergencyContactName, userProfile.emergencyContactPhone)
                            },
                            label = { Text(if (isTa) "மாணவர்" else "Student", fontSize = 12.sp) }
                        )

                        FilterChip(
                            selected = userProfile.category == CommuterCategory.SENIOR_CITIZEN,
                            onClick = {
                                onUpdateProfile(userProfile.name, CommuterCategory.SENIOR_CITIZEN, userProfile.emergencyContactName, userProfile.emergencyContactPhone)
                            },
                            label = { Text(if (isTa) "முதியோர் (60+)" else "Senior (60+)", fontSize = 12.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Emergency Contact Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContactPhone, contentDescription = null, tint = ChennaiNavy, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isTa) "குடும்ப அவசர தொடர்பு (Trusted Contact)" else "Family Emergency Contact",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${userProfile.emergencyContactName} • +91 ${userProfile.emergencyContactPhone}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isTa) "பயணத்தை பகிரும்போது இந்த எண்ணுக்கு விவரங்கள் அனுப்பப்படும்." else "Your route steps and ETA are shared directly with this contact.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
