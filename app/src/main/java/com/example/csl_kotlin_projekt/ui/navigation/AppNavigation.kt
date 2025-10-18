package com.example.csl_kotlin_projekt.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.csl_kotlin_projekt.ui.screens.splash.SplashScreen
import com.example.csl_kotlin_projekt.ui.screens.login.LoginScreen
import com.example.csl_kotlin_projekt.ui.screens.register.RegisterScreen
import com.example.csl_kotlin_projekt.ui.screens.home.HomeScreen
import com.example.csl_kotlin_projekt.ui.screens.createschedule.CreateScheduleScreen
import com.example.csl_kotlin_projekt.ui.screens.addhabit.AddHabitScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.csl_kotlin_projekt.ui.screens.scheduledetails.ScheduleDetailsScreen
import com.example.csl_kotlin_projekt.ui.screens.editschedule.EditScheduleScreen
import com.example.csl_kotlin_projekt.ui.screens.profile.ProfileScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "splash"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        composable("add_schedule") {
            CreateScheduleScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddHabit = { navController.navigate("add_habit") }
            )
        }
        composable("add_habit") {
            AddHabitScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToAddSchedule = {
                    navController.navigate("add_schedule")
                },
                onNavigateToAddHabit = {
                    navController.navigate("add_habit")
                },
                onNavigateToScheduleDetails = { id ->
                    navController.navigate("schedule_details/$id")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        composable(
            route = "schedule_details/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            ScheduleDetailsScreen(
                scheduleId = id,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { editId -> navController.navigate("edit_schedule/$editId") }
            )
        }
        composable(
            route = "edit_schedule/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            EditScheduleScreen(
                scheduleId = id,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { updatedId ->
                    // Replace details with a refreshed instance
                    navController.navigate("schedule_details/$updatedId") {
                        popUpTo("schedule_details/$updatedId") { inclusive = true }
                    }
                }
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddHabit = { navController.navigate("add_habit") },
                onLoggedOut = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
