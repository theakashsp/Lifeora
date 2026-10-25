package com.liferise.app.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.liferise.app.ai.AiAnalysisScreen
import com.liferise.app.ai.AiViewModel
import com.liferise.app.alarms.AlarmEditScreen
import com.liferise.app.alarms.AlarmListScreen
import com.liferise.app.alarms.AlarmViewModel
import com.liferise.app.community.CommunityScreen
import com.liferise.app.community.CreatePostScreen
import com.liferise.app.community.SocialViewModel
import com.liferise.app.core.di.AppContainer
import com.liferise.app.diary.DiaryDetailScreen
import com.liferise.app.diary.DiaryEditScreen
import com.liferise.app.diary.DiaryListScreen
import com.liferise.app.diary.DiaryViewModel
import com.liferise.app.home.HomeScreen
import com.liferise.app.home.HomeViewModel
import com.liferise.app.progress.ProgressScreen
import com.liferise.app.progress.ProgressViewModel
import com.liferise.app.settings.SettingsScreen
import com.liferise.app.settings.SettingsViewModel
import com.liferise.app.tasks.TaskListScreen
import com.liferise.app.tasks.TaskViewModel

@Composable
fun LifeRiseNavGraph(
    navController: NavHostController,
    container: AppContainer,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.padding(innerPadding)
    ) {
        // 1. Home
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.provideFactory(
                    container.progressRepository,
                    container.missionRepository,
                    container.taskRepository,
                    container.alarmRepository,
                    container.diaryRepository
                )
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToDiary = { navController.navigate(Screen.Diary.route) },
                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                onNavigateToAlarms = { navController.navigate(Screen.Alarm.route) },
                onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // 2. Diary List
        composable(Screen.Diary.route) {
            val diaryViewModel: DiaryViewModel = viewModel(
                factory = DiaryViewModel.provideFactory(container.diaryRepository)
            )
            DiaryListScreen(
                viewModel = diaryViewModel,
                onNavigateToCreate = { navController.navigate(Screen.DiaryEdit.createRoute(-1L)) },
                onNavigateToDetail = { entryId -> navController.navigate(Screen.DiaryDetail.createRoute(entryId)) }
            )
        }

        // 2b. Diary Edit
        composable(
            route = Screen.DiaryEdit.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
            val diaryViewModel: DiaryViewModel = viewModel(
                factory = DiaryViewModel.provideFactory(container.diaryRepository)
            )
            DiaryEditScreen(
                entryId = entryId,
                viewModel = diaryViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 2c. Diary Detail
        composable(
            route = Screen.DiaryDetail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
            val diaryViewModel: DiaryViewModel = viewModel(
                factory = DiaryViewModel.provideFactory(container.diaryRepository)
            )
            DiaryDetailScreen(
                entryId = entryId,
                viewModel = diaryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.DiaryEdit.createRoute(id)) },
                onNavigateToAiAnalysis = { id -> navController.navigate(Screen.AiAnalysis.createRoute(id)) }
            )
        }

        // 2d. AI Insights
        composable(
            route = Screen.AiAnalysis.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
            val aiViewModel: AiViewModel = viewModel(
                factory = AiViewModel.provideFactory(
                    container.aiDiaryAnalysisRepository,
                    container.diaryRepository,
                    container.settingsRepository
                )
            )
            AiAnalysisScreen(
                entryId = entryId,
                viewModel = aiViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. Alarm List
        composable(Screen.Alarm.route) {
            val alarmViewModel: AlarmViewModel = viewModel(
                factory = AlarmViewModel.provideFactory(container.alarmRepository, container.alarmScheduler)
            )
            AlarmListScreen(
                viewModel = alarmViewModel,
                onNavigateToCreate = { navController.navigate(Screen.AlarmEdit.createRoute(-1L)) },
                onNavigateToEdit = { alarmId -> navController.navigate(Screen.AlarmEdit.createRoute(alarmId)) }
            )
        }

        // 3b. Alarm Edit
        composable(
            route = Screen.AlarmEdit.route,
            arguments = listOf(navArgument("alarmId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: -1L
            val alarmViewModel: AlarmViewModel = viewModel(
                factory = AlarmViewModel.provideFactory(container.alarmRepository, container.alarmScheduler)
            )
            AlarmEditScreen(
                alarmId = alarmId,
                viewModel = alarmViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. Community Feed
        composable(Screen.Community.route) {
            val socialViewModel: SocialViewModel = viewModel(
                factory = SocialViewModel.provideFactory(container.socialRepository)
            )
            CommunityScreen(
                viewModel = socialViewModel,
                onNavigateToCreatePost = { navController.navigate(Screen.CreatePost.route) }
            )
        }

        // 4b. Create Social Post
        composable(Screen.CreatePost.route) {
            val socialViewModel: SocialViewModel = viewModel(
                factory = SocialViewModel.provideFactory(container.socialRepository)
            )
            CreatePostScreen(
                viewModel = socialViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Progress Dashboard
        composable(Screen.Progress.route) {
            val progressViewModel: ProgressViewModel = viewModel(
                factory = ProgressViewModel.provideFactory(container.progressRepository)
            )
            ProgressScreen(viewModel = progressViewModel)
        }

        // Tasks Screen
        composable(Screen.Tasks.route) {
            val taskViewModel: TaskViewModel = viewModel(
                factory = TaskViewModel.provideFactory(container.taskRepository, container.taskAlarmScheduler)
            )
            TaskListScreen(
                viewModel = taskViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.provideFactory(
                    container.settingsRepository,
                    container.diaryRepository,
                    container.taskRepository,
                    container.securityManager
                )
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
