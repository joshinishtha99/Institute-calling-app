package com.institute.calling.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.institute.calling.domain.model.Role
import com.institute.calling.ui.auth.BranchScreen
import com.institute.calling.ui.auth.CallerPickScreen
import com.institute.calling.ui.auth.CityScreen
import com.institute.calling.ui.auth.LandingScreen
import com.institute.calling.ui.auth.OwnerPickScreen
import com.institute.calling.ui.auth.PinScreen
import com.institute.calling.ui.caller.CallerScreen
import com.institute.calling.ui.owner.BranchDetailScreen
import com.institute.calling.ui.owner.BranchStaffScreen
import com.institute.calling.ui.owner.CallReviewScreen
import com.institute.calling.ui.owner.ManagementScreen
import com.institute.calling.ui.owner.OwnerHomeScreen
import com.institute.calling.ui.session.SessionViewModel

private object Routes {
    const val LANDING = "landing"
    const val CITIES = "cities"
    const val BRANCHES = "branches"
    const val CALLER_PICK = "callerPick"
    const val OWNER_PICK = "ownerPick"
    const val PIN = "pin"
    const val CALLER_HOME = "callerHome"
    const val OWNER_HOME = "ownerHome"
    const val OWNER_BRANCH = "ownerBranch"
    const val OWNER_MANAGE = "ownerManage"
    const val OWNER_CALLS = "ownerCalls"
    const val OWNER_STAFF = "ownerStaff"
}

@Composable
fun CallingNavHost(
    navController: NavHostController = rememberNavController(),
    // Scoped to the Activity (created in MainActivity), so all auth screens
    // share the same SessionViewModel and its selections persist across steps.
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val session by sessionViewModel.state.collectAsStateWithLifecycle()

    fun logout() {
        sessionViewModel.logout()
        navController.navigate(Routes.LANDING) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = Routes.LANDING) {

        composable(Routes.LANDING) {
            LandingScreen(
                onOwner = { navController.navigate(Routes.OWNER_PICK) },
                onEmployee = { navController.navigate(Routes.CITIES) },
            )
        }

        composable(Routes.CITIES) {
            CityScreen(
                sessionViewModel = sessionViewModel,
                onBack = { navController.popBackStack() },
                onCitySelected = { navController.navigate(Routes.BRANCHES) },
            )
        }

        composable(Routes.BRANCHES) {
            BranchScreen(
                sessionViewModel = sessionViewModel,
                onBack = { navController.popBackStack() },
                onBranchSelected = { navController.navigate(Routes.CALLER_PICK) },
            )
        }

        composable(Routes.CALLER_PICK) {
            CallerPickScreen(
                sessionViewModel = sessionViewModel,
                onBack = { navController.popBackStack() },
                onCallerSelected = { navController.navigate(Routes.PIN) },
            )
        }

        composable(Routes.OWNER_PICK) {
            OwnerPickScreen(
                sessionViewModel = sessionViewModel,
                onBack = { navController.popBackStack() },
                onOwnerSelected = { navController.navigate(Routes.PIN) },
            )
        }

        composable(Routes.PIN) {
            PinScreen(
                sessionViewModel = sessionViewModel,
                onBack = { navController.popBackStack() },
                onLoggedIn = { role ->
                    val dest = if (role == Role.OWNER) Routes.OWNER_HOME else Routes.CALLER_HOME
                    navController.navigate(dest) { popUpTo(Routes.LANDING) { inclusive = true } }
                },
            )
        }

        composable(Routes.CALLER_HOME) {
            val user = session.currentUser
            if (user == null) {
                LandingScreen(onOwner = {}, onEmployee = {})
            } else {
                CallerScreen(user = user, onLogout = { logout() })
            }
        }

        composable(Routes.OWNER_HOME) {
            OwnerHomeScreen(
                user = session.currentUser,
                onLogout = { logout() },
                onOpenBranch = { branchId -> navController.navigate("${Routes.OWNER_BRANCH}/$branchId") },
                onOpenManage = { navController.navigate(Routes.OWNER_MANAGE) },
                onOpenCalls = { navController.navigate(Routes.OWNER_CALLS) },
            )
        }

        composable(Routes.OWNER_CALLS) {
            CallReviewScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.OWNER_MANAGE) {
            ManagementScreen(
                onBack = { navController.popBackStack() },
                onOpenStaff = { branchId -> navController.navigate("${Routes.OWNER_STAFF}/$branchId") },
            )
        }

        composable(
            route = "${Routes.OWNER_STAFF}/{branchId}",
            arguments = listOf(navArgument("branchId") { type = NavType.StringType }),
        ) {
            BranchStaffScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "${Routes.OWNER_BRANCH}/{branchId}",
            arguments = listOf(navArgument("branchId") { type = NavType.StringType }),
        ) {
            BranchDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
