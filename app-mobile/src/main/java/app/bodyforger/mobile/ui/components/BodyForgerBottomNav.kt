package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.navigation.Tab
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextSecondary

@Composable
fun BodyForgerBottomNav(
    currentTab: Tab?,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 0.dp,
        modifier = modifier.border(width = 1.dp, color = SurfaceBorder)
    ) {
        Tab.entries.forEach { item ->
            val isSelected = item == currentTab
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = NeonLime,
                    indicatorColor = NeonLime,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
