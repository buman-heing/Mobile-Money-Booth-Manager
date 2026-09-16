package com.moneybooth.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.ui.auth.PinLoginScreen
import com.moneybooth.app.ui.auth.PinSetupScreen
import com.moneybooth.app.ui.booths.BoothFormScreen
import com.moneybooth.app.ui.employees.EmployeeFormScreen
import com.moneybooth.app.ui.onboarding.BusinessSetupScreen
import com.moneybooth.app.ui.transactions.TransactionDetailScreen
import com.moneybooth.app.ui.transactions.TransactionFormScreen
import com.moneybooth.app.ui.transactions.UnknownSmsReviewScreen

/**
 * Top-level gating (PIN set? authenticated? business exists?) is done with early returns rather
 * than nav routes, since each stage's completion is driven by reactive state (PIN store,
 * SessionManager, the businesses table) rather than a one-off navigation event. The NavController
 * only manages in-app stack navigation once the user is inside the authenticated app shell.
 */
@Composable
fun NavGraph(container: AppContainer) {
    var pinJustConfirmedSet by remember { mutableStateOf(container.pinCredentialStore.isPinSet()) }
    if (!pinJustConfirmedSet) {
        PinSetupScreen(container = container, onPinSet = { pinJustConfirmedSet = true })
        return
    }

    val isAuthenticated by container.sessionManager.isAuthenticated.collectAsStateWithLifecycle()
    if (!isAuthenticated) {
        PinLoginScreen(container = container, onAuthenticated = {})
        return
    }

    val business by container.businessRepository.observeFirst().collectAsStateWithLifecycle(initialValue = null)
    val currentBusiness = business
    if (currentBusiness == null) {
        BusinessSetupScreen(container = container)
        return
    }

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destinations.HOME) {
        composable(Destinations.HOME) {
            HomeScreen(
                navController = navController,
                container = container,
                business = currentBusiness,
                onPinReset = {
                    container.pinCredentialStore.clearPin()
                    container.sessionManager.signOut()
                    pinJustConfirmedSet = false
                },
            )
        }
        composable(Destinations.BOOTH_ADD) {
            BoothFormScreen(
                businessId = currentBusiness.id,
                container = container,
                onDone = { navController.popBackStack() },
            )
        }
        composable(Destinations.EMPLOYEE_ADD) {
            EmployeeFormScreen(
                businessId = currentBusiness.id,
                container = container,
                onDone = { navController.popBackStack() },
            )
        }
        composable(Destinations.TRANSACTION_ADD) {
            TransactionFormScreen(
                businessId = currentBusiness.id,
                container = container,
                onDone = { navController.popBackStack() },
            )
        }
        composable(
            route = Destinations.TRANSACTION_DETAIL,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: return@composable
            TransactionDetailScreen(transactionId = transactionId, container = container)
        }
        composable(Destinations.REVIEW) {
            UnknownSmsReviewScreen(businessId = currentBusiness.id, container = container)
        }
    }
}
