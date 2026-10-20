package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.Language
import com.example.model.NavigationTab
import com.example.ui.theme.ChennaiGreen
import com.example.ui.theme.ChennaiNavy

@Composable
fun ChennaiBottomNav(
    currentTab: NavigationTab,
    language: Language,
    onTabSelected: (NavigationTab) -> Unit
) {
    val isTa = language == Language.TAMIL

    NavigationBar(
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("chennai_bottom_nav")
    ) {
        NavigationBarItem(
            selected = currentTab == NavigationTab.HOME,
            onClick = { onTabSelected(NavigationTab.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text(if (isTa) "முகப்பு" else "Home") },
            modifier = Modifier.testTag("nav_item_home")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.AI_COMMUTE,
            onClick = { onTabSelected(NavigationTab.AI_COMMUTE) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Transit") },
            label = { Text(if (isTa) "✨ AI வழிகாட்டி" else "✨ AI Transit") },
            modifier = Modifier.testTag("nav_item_ai_transit")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.PLANNER,
            onClick = { onTabSelected(NavigationTab.PLANNER) },
            icon = { Icon(Icons.Default.Route, contentDescription = "Planner") },
            label = { Text(if (isTa) "பயணம்" else "Planner") },
            modifier = Modifier.testTag("nav_item_planner")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.METRO_GUIDE,
            onClick = { onTabSelected(NavigationTab.METRO_GUIDE) },
            icon = { Icon(Icons.Default.Subway, contentDescription = "Metro Guide") },
            label = { Text(if (isTa) "மெட்ரோ" else "Metro") },
            modifier = Modifier.testTag("nav_item_metro")
        )

        NavigationBarItem(
            selected = currentTab == NavigationTab.ADVISORIES || currentTab == NavigationTab.PROFILE,
            onClick = { onTabSelected(NavigationTab.ADVISORIES) },
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = "Advisories") },
            label = { Text(if (isTa) "அறிக்கைகள்" else "Alerts") },
            modifier = Modifier.testTag("nav_item_advisories")
        )
    }
}
