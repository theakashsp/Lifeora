package com.liferise.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Diary : Screen("diary")
    data object DiaryEdit : Screen("diary_edit/{entryId}") {
        fun createRoute(entryId: Long = -1L) = "diary_edit/$entryId"
    }
    data object DiaryDetail : Screen("diary_detail/{entryId}") {
        fun createRoute(entryId: Long) = "diary_detail/$entryId"
    }
    data object AiAnalysis : Screen("ai_analysis/{entryId}") {
        fun createRoute(entryId: Long) = "ai_analysis/$entryId"
    }
    data object Alarm : Screen("alarm")
    data object AlarmEdit : Screen("alarm_edit/{alarmId}") {
        fun createRoute(alarmId: Long = -1L) = "alarm_edit/$alarmId"
    }
    data object Community : Screen("community")
    data object CreatePost : Screen("create_post")
    data object Progress : Screen("progress")
    data object Tasks : Screen("tasks")
    data object Settings : Screen("settings")
}

sealed class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val title: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(Screen.Home.route, 0, "Home", Icons.Default.Home)
    data object Diary : BottomNavItem(Screen.Diary.route, 0, "Diary", Icons.Default.Book)
    data object Alarm : BottomNavItem(Screen.Alarm.route, 0, "Alarm", Icons.Default.Alarm)
    data object Community : BottomNavItem(Screen.Community.route, 0, "Community", Icons.Default.People)
    data object Progress : BottomNavItem(Screen.Progress.route, 0, "Progress", Icons.Default.TrendingUp)

    companion object {
        val items = listOf(Home, Diary, Alarm, Community, Progress)
    }
}
