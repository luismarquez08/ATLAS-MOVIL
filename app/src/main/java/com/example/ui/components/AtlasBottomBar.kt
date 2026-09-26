package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AtlasTab
import com.example.ui.theme.AtlasBgDark
import com.example.ui.theme.AtlasBorder
import com.example.ui.theme.AtlasCyan
import com.example.ui.theme.AtlasTextMuted

data class NavItem(
    val tab: AtlasTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

@Composable
fun AtlasBottomBar(
    currentTab: AtlasTab,
    onTabSelected: (AtlasTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(AtlasTab.ASSISTANT, Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq, "Inicio"),
        NavItem(AtlasTab.COMMANDS, Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Filled.MenuBook, "Herramientas"),
        NavItem(AtlasTab.CONTACTS, Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat, "Contactos"),
        NavItem(AtlasTab.HISTORY, Icons.Filled.History, Icons.Outlined.History, "Memoria"),
        NavItem(AtlasTab.SETTINGS, Icons.Filled.Settings, Icons.Outlined.Settings, "Ajustes")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AtlasBgDark)
            .border(width = 1.dp, color = AtlasBorder)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentTab == item.tab
                val tint = if (isSelected) AtlasCyan else AtlasTextMuted

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(item.tab) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("nav_tab_${item.tab.name.lowercase()}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AtlasCyan.copy(alpha = 0.16f) else androidx.compose.ui.graphics.Color.Transparent)
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = tint
                    )
                }
            }
        }
    }
}
