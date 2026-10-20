package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.TransitMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisruptionReportDialog(
    language: Language,
    onDismiss: () -> Unit,
    onSubmit: (TransitMode, String, String, String, String, String, String) -> Unit
) {
    val isTa = language == Language.TAMIL

    var selectedMode by remember { mutableStateOf(TransitMode.BUS) }
    var routeOrStation by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Delay") }
    var summary by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isTa) "பயணத் தடங்கலைப் புகாரளிக்கவும்" else "Report Transport Disruption",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isTa) "போக்குவரத்து முறை" else "Transport Mode",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Mode Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedMode == TransitMode.BUS,
                        onClick = { selectedMode = TransitMode.BUS },
                        label = { Text("MTC Bus", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedMode == TransitMode.METRO,
                        onClick = { selectedMode = TransitMode.METRO },
                        label = { Text("Metro", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedMode == TransitMode.SUBURBAN_RAIL,
                        onClick = { selectedMode = TransitMode.SUBURBAN_RAIL },
                        label = { Text("Suburban Rail", fontSize = 12.sp) }
                    )
                }

                OutlinedTextField(
                    value = routeOrStation,
                    onValueChange = {
                        routeOrStation = it
                        hasError = false
                    },
                    label = { Text(if (isTa) "வழித்தடம் அல்லது நிலையம் (எ.கா. 29C / Guindy)" else "Route or Station (e.g., 29C / Guindy)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_route_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = summary,
                    onValueChange = {
                        summary = it
                        hasError = false
                    },
                    label = { Text(if (isTa) "சுருக்கம் (எ.கா. 15 நிமிடம் தாமதம்)" else "Headline (e.g., 15 min delay)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_summary_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text(if (isTa) "கூடுதல் விவரம் (காரணம் / மாற்று வழி)" else "Details (cause, alternate advice)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_detail_input"),
                    minLines = 2,
                    maxLines = 4
                )

                if (hasError) {
                    Text(
                        text = if (isTa) "தயவுசெய்து வழித்தடம் மற்றும் சுருக்கத்தை நிரப்பவும்." else "Please enter the route and a brief summary.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (routeOrStation.isBlank() || summary.isBlank()) {
                        hasError = true
                    } else {
                        onSubmit(selectedMode, routeOrStation, category, summary, summary, detail, detail)
                    }
                },
                modifier = Modifier.testTag("submit_report_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isTa) "பதிவேற்று" else "Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isTa) "ரத்து செய்" else "Cancel")
            }
        }
    )
}
