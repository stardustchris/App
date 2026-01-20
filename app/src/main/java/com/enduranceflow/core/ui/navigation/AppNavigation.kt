package com.enduranceflow.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.enduranceflow.onboarding.ui.*
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
        startDestination = Screen.Welcome.route
    ) {
        // 👋 Écran 1 : Welcome - Présentation de l'app
        composable(route = Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateNext = {
                    navController.navigate(Screen.PersonalProfile.route)
                }
            )
        }

        // 📊 Écran 2 : Personal Profile - Âge, poids, taille, FC Max
        composable(route = Screen.PersonalProfile.route) {
            PersonalProfileScreen(
                onNavigateNext = {
                    navController.navigate(Screen.Onboarding.route)
                }
            )
        }

        // 🎯 Écran 3 : Onboarding - Genre + Équipement + VMA/FTP (optionnel)
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToCalibration = {
                    // Continue to sports selection, calibration will come later if needed
                    navController.navigate(Screen.SportsSelection.route)
                },
                onNavigateToWorkouts = {
                    // Continue to sports selection
                    navController.navigate(Screen.SportsSelection.route)
                }
            )
        }

        // 🏃🚴 Écran 4 : Sports Selection - Course, Vélo, ou les deux
        composable(route = Screen.SportsSelection.route) {
            SportsSelectionScreen(
                onNavigateNext = {
                    navController.navigate(Screen.GoalsSelection.route)
                }
            )
        }

        // 🎖️ Écran 5 : Goals Selection - Compétition, Forme, Perte de poids, Maintenance
        composable(route = Screen.GoalsSelection.route) {
            GoalsSelectionScreen(
                onNavigateNext = { hasCompetition ->
                    if (hasCompetition) {
                        // Si compétition choisie → écran événement cible
                        navController.navigate(Screen.TargetEvent.route)
                    } else {
                        // Sinon → disponibilités directement
                        navController.navigate(Screen.AvailabilityConfig.route)
                    }
                }
            )
        }

        // 🏁 Écran 6 : Target Event - Détails de la compétition (conditionnel)
        composable(route = Screen.TargetEvent.route) {
            TargetEventScreen(
                onNavigateNext = {
                    navController.navigate(Screen.AvailabilityConfig.route)
                }
            )
        }

        // 📅 Écran 7 : Availability Config - Jours disponibles
        composable(route = Screen.AvailabilityConfig.route) {
            AvailabilityConfigScreen(
                onNavigateNext = {
                    // Finaliser l'onboarding et créer le profil
                    navController.navigate(Screen.WorkoutList.route) {
                        // Clear entire backstack - prevent going back to onboarding
                        popUpTo(Screen.Welcome.route) { inclusive = true }
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
 *
 * Onboarding flow (7+ screens):
 * Welcome → PersonalProfile → Onboarding → SportsSelection → GoalsSelection
 * → (TargetEvent if competition) → AvailabilityConfig → WorkoutList
 */
sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object PersonalProfile : Screen("personal_profile")
    data object Onboarding : Screen("onboarding")
    data object SportsSelection : Screen("sports_selection")
    data object GoalsSelection : Screen("goals_selection")
    data object TargetEvent : Screen("target_event")
    data object AvailabilityConfig : Screen("availability_config")
    data object Calibration : Screen("calibration")
    data object WorkoutList : Screen("workout_list")
    data object Profile : Screen("profile")
}
