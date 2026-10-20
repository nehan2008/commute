package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language

@Composable
fun DataTransparencyDialog(
    language: Language,
    onDismiss: () -> Unit
) {
    val isTa = language == Language.TAMIL

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = if (isTa) "தரவு மூலங்கள் & வெளிப்படைத்தன்மை" else "Data Lineage & Transparency",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: CUMTA 24-Hour Challenge Context
                Text(
                    text = if (isTa) "CUMTA 24-Hour Challenge — Reimagine Chennai" else "CUMTA 24-Hour Challenge — Reimagine Chennai",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isTa) {
                        "சென்னை பெருநகர ஒருங்கிணைந்த போக்குவரத்து ஆணையத்திற்காக (CUMTA) முதியோர் மற்றும் இளைய தலைமுறை பயணிகளுக்கான பிரத்யேக வசதிகளுடன் உருவாக்கப்பட்டது."
                    } else {
                        "Built for the Chennai Unified Metropolitan Transport Authority (CUMTA) 24-Hour Challenge to offer personalized mobility for elderly citizens and young commuters sharing a unified routing engine."
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider()

                // Section 2: Datasets & Verification
                Text(
                    text = if (isTa) "பயன்படுத்தப்பட்ட தரவுத்தொகுப்புகள் (Datasets)" else "Verified Data Sourcing",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isTa) {
                        "• சென்னை மெட்ரோ (CMRL): நீல மற்றும் பச்சை வழித்தட நிலையங்கள், கட்டண அட்டவணை (₹10-₹50), மற்றும் சரிபார்க்கப்பட்ட மின்தூக்கி வசதிகள்.\n" +
                        "• தெற்கு ரயில்வே: சென்னை கடற்கரை - தாம்பரம் புறநகர் மின்சார ரயில் மற்றும் MRTS வழித்தடங்கள் (இரண்டாம் வகுப்பு ₹5-₹10).\n" +
                        "• மாநகரப் பேருந்து (MTC): 29C, 570, 21G, 11G, 18A உள்ளிட்ட முக்கிய இணைப்பு வழித்தடங்கள் மற்றும் தமிழக அரசின் 'விடியல் பயணம்' இலவச பேருந்து திட்டம்."
                    } else {
                        "• Chennai Metro (CMRL): Blue & Green Line alignments, verified step-free elevators, and official distance fare slabs (₹10-₹50).\n" +
                        "• Southern Railway: Chennai Beach to Tambaram EMU mainline & MRTS network with standard unreserved fares (₹5-₹10).\n" +
                        "• MTC Chennai: Arterial corridors (29C, 570, 21G, 11G, 18A), low-floor electric bus tags, and TN 'Vidiyal Payanam' zero-fare scheme for women commuters."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                // Section 3: Limitations & Sourcing Honesty
                Text(
                    text = if (isTa) "வெளிப்படைத்தன்மை குறிப்பு (Limitations)" else "Reported Limitations & Fallbacks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isTa) {
                        "• நேரலை GPS வருகை தரவுகள் (Live Vehicle Feeds) பொதுவெளியில் கிடைக்காததால், பயண நேரங்கள் நிலையான கால அட்டவணை மற்றும் இடைவெளிகளின் அடிப்படையில் கணக்கிடப்பட்டுள்ளன.\n" +
                        "• பயணிகள் தடங்கல் அறிவிப்புகள் மாதிரிக்காக உருவாக்கப்பட்டவை (Community Crowd-Sourced Demo)."
                    } else {
                        "• Live GPS vehicle feeds are not available in public static GTFS feeds. Arrival times and headway intervals (5-15 mins) are calculated from static timetables.\n" +
                        "• Disruption feeds are community crowd-sourced with commuter verification votes."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Text(
                    text = "License: ChennaiGTFS Open Transit Data (MIT License). Attribution to UngalSoththu / Chennai Open Data Contributors.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isTa) "சரி" else "Understood")
            }
        }
    )
}
