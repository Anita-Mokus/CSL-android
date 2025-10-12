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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.csl_kotlin_projekt.ui.screens.createschedule.CreateScheduleScreen
import com.example.csl_kotlin_projekt.ui.screens.addhabit.AddHabitScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.csl_kotlin_projekt.ui.screens.scheduledetails.ScheduleDetailsScreen

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
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
