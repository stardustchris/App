package com.enduranceflow.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.enduranceflow.onboarding.ui.OnboardingScreen
import com.enduranceflow.onboarding.ui.SportsSelectionScreen
import com.enduranceflow.onboarding.ui.AvailabilityConfigScreen
import com.enduranceflow.calibration.ui.CalibrationScreen
import com.enduranceflow.workout.ui.WorkoutListScreen
import com.enduranceflow.profile.ui.ProfileScreen

/**
 * Navigation principale de l'application EnduranceFlow
 *
 * Architecture de navigation :
 * - OnboardingScreen : Premier démarrage (collecte infos utilisateur)
 * - CalibrationScreen : Tests VMA/FTP si nécessaire
 * - WorkoutListScreen : Écran principal (liste des séances)
 * - ProfileScreen : Profil athlète et paramètres
 *
 * Organisation par fonctionnalité :
 * Chaque écran est dans son propre module (onboarding/, calibration/, workout/, profile/)
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        // 🎯 Écran 1 : Onboarding (genre + équipement + VMA/FTP)
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToCalibration = {
                    // Ne sera plus utilisé, on passe toujours par Sports Selection
                    navController.navigate(Screen.SportsSelection.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = false }
                    }
                },
                onNavigateToWorkouts = {
                    // Passer par Sports Selection au lieu d'aller directement aux workouts
                    navController.navigate(Screen.SportsSelection.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = false }
                    }
                }
            )
        }

        // 🏃🚴 Écran 2 : Sélection des sports
        composable(route = Screen.SportsSelection.route) {
            SportsSelectionScreen(
                onNavigateNext = {
                    navController.navigate(Screen.AvailabilityConfig.route) {
                        popUpTo(Screen.SportsSelection.route) { inclusive = false }
                    }
                }
            )
        }

        // 📅 Écran 3 : Configuration des disponibilités
        composable(route = Screen.AvailabilityConfig.route) {
            AvailabilityConfigScreen(
                onNavigateNext = {
                    // Vérifier si calibration nécessaire (via ViewModel)
                    // Pour l'instant, aller directement aux workouts
                    navController.navigate(Screen.WorkoutList.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // 📊 Écran 2 : Calibration (tests VMA/FTP)
        composable(route = Screen.Calibration.route) {
            CalibrationScreen(
                onNavigateToWorkouts = {
                    navController.navigate(Screen.WorkoutList.route) {
                        popUpTo(Screen.Calibration.route) { inclusive = true }
                    }
                }
            )
        }

        // 🏃 Écran 3 : Liste des séances (écran principal)
        composable(route = Screen.WorkoutList.route) {
            WorkoutListScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        // 👤 Écran 4 : Profil et paramètres
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Définition des routes de navigation
 *
 * Sealed class pour garantir la sécurité de type (pas d'erreur de route)
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object SportsSelection : Screen("sports_selection")
    data object AvailabilityConfig : Screen("availability_config")
    data object Calibration : Screen("calibration")
    data object WorkoutList : Screen("workout_list")
    data object Profile : Screen("profile")
}
