package app.bodyforger.mobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : NavItem("Accueil", Icons.Filled.Home, Icons.Outlined.Home)
    object Planner : NavItem("Programme", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter)
    object Analytics : NavItem("Stats", Icons.Filled.MonitorWeight, Icons.Outlined.MonitorWeight)
    object Profile : NavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person)
}
