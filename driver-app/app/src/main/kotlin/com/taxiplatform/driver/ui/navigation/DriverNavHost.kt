package com.taxiplatform.driver.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.taxiplatform.driver.ui.auth.LoginScreen
import com.taxiplatform.driver.ui.auth.RegisterScreen
import com.taxiplatform.driver.ui.home.HomeScreen
import com.taxiplatform.driver.ui.offer.IncomingOfferScreen
import com.taxiplatform.driver.ui.ride.ActiveRideScreen

private object Routes {
	const val LOGIN = "login"
	const val REGISTER = "register"
	const val HOME = "home"
	const val OFFER = "offer"
	const val RIDE = "ride/{rideId}"
	fun ride(rideId: String) = "ride/$rideId"
}

@Composable
fun DriverNavHost(navController: NavHostController = rememberNavController()) {
	NavHost(navController = navController, startDestination = Routes.LOGIN) {
		composable(Routes.LOGIN) {
			LoginScreen(
				onLoggedIn = {
					navController.navigate(Routes.HOME) {
						popUpTo(Routes.LOGIN) { inclusive = true }
					}
				},
				onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
			)
		}
		composable(Routes.REGISTER) {
			RegisterScreen(
				onRegistered = {
					navController.navigate(Routes.HOME) {
						popUpTo(Routes.LOGIN) { inclusive = true }
					}
				},
				onNavigateToLogin = { navController.popBackStack() },
			)
		}
		composable(Routes.HOME) {
			HomeScreen(
				onOfferReceived = { navController.navigate(Routes.OFFER) },
			)
		}
		composable(Routes.OFFER) {
			IncomingOfferScreen(
				onAccepted = { ride ->
					navController.navigate(Routes.ride(ride.id)) {
						popUpTo(Routes.OFFER) { inclusive = true }
					}
				},
				onDismissed = { navController.popBackStack() },
			)
		}
		composable(
			route = Routes.RIDE,
			arguments = listOf(navArgument("rideId") { type = NavType.StringType }),
		) {
			ActiveRideScreen(
				onCompleted = { navController.popBackStack(Routes.HOME, inclusive = false) },
			)
		}
	}
}
