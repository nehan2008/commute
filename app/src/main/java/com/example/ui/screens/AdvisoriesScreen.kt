package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DisruptionReport
import com.example.model.Language
import com.example.ui.theme.ChennaiGreen
import com.example.ui.theme.ChennaiNavy

@Composable
fun AdvisoriesScreen(
    disruptions: List<DisruptionReport>,
    language: Language,
    onUpvoteDisruption: (String) -> Unit,
    onOpenReportDialog: () -> Unit
) {
    val isTa = language == Language.TAMIL

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("advisories_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ChennaiNavy),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTa) "சென்னை போக்குவரத்து அறிவிப்புகள்" else "Chennai Transit Live Advisories",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (isTa) "அதிகாரப்பூர்வ CMRL & MTC தகவல்கள்" else "Official CMRL & MTC updates + Verified commuter feed",
                            color = Color(0xFFD6E4FF),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Feed Sources & Transparency Disclosure Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isTa) "அறிக்கைகளின் தரவு மூலங்கள் (Where is this feed from?):" else "Feed Origin & Real-Time Integrity:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isTa) {
                            "• அதிகாரப்பூர்வ மெட்ரோ தகவல்கள்: சென்னை மெட்ரோ ரயில் நிறுவனம் (CMRL - chennaimetrorail.org) வெளியிடும் அதிகாரப்பூர்வ கால அட்டவணை.\n" +
                            "• மாநகரப் பேருந்து தகவல்கள்: தமிழக அரசு மற்றும் MTC சென்னை (mtcbus.tn.gov.in) விடியல் பயணம் கொள்கை.\n" +
                            "• பயணிகள் பதிவுகள்: இந்த செயலியைப் பயன்படுத்தும் சென்னை பயணிகள் நேரடியாகப் பதிவு செய்தவை (உறுதிப்படுத்தும் வாக்குகளுடன்)."
                        } else {
                            "• Official Metro Updates: Chennai Metro Rail Ltd (CMRL - chennaimetrorail.org) published timetables and facility notices.\n" +
                            "• MTC Bus Updates: Metropolitan Transport Corp (mtcbus.tn.gov.in) fare structures and Vidiyal Payanam zero-fare rules.\n" +
                            "• Commuter Reports: Submitted in real-time by commuters via this Chennai Move app, with genuine confirmation counts (no fake stats)."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Action row: Post issue button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTa) "அறிக்கைகள் பட்டியல்" else "Transit Advisories Feed",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Button(
                    onClick = onOpenReportDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("report_issue_button_advisories")
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isTa) "புதிய பதிவு" else "Report Issue", fontSize = 13.sp)
                }
            }
        }

        // Reports list
        items(disruptions) { report ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("advisory_card_${report.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (report.isOfficialAdvisory) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                text = if (report.isOfficialAdvisory) (if (isTa) "அதிகாரப்பூர்வ தகவல்" else "Official Notice") else (if (isTa) "பயணிகள் பதிவு" else "Commuter Report"),
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
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (isTa) report.detailTa else report.detailEn,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (!report.isOfficialAdvisory) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (report.upvotes > 0) "${report.upvotes} " + (if (isTa) "பயணிகள் உறுதி செய்துள்ளனர்" else "commuter confirmed") else (if (isTa) "உறுதிப்படுத்த காத்திருக்கிறது" else "Awaiting confirmations"),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FilledTonalButton(
                                onClick = { onUpvoteDisruption(report.id) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "Confirm", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTa) "உறுதி செய்" else "Confirm", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
