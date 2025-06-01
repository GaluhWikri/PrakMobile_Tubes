package com.example.tubespm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.uiscreens.ArticleDetailScreen
import com.example.tubespm.ui.uiscreens.ArticleListScreen
import com.example.tubespm.ui.uiscreens.CreateEditArticleScreen
import com.example.tubespm.ui.uiscreens.LoginScreen
import com.example.tubespm.ui.uiscreens.RegisterScreen
import com.example.tubespm.ui.uiscreens.ProfileScreen

@Composable
fun BlogNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // ... (rute login dan register tetap sama) ...
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("article_list") {
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
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("article_list") {
            ArticleListScreen(
                onNavigateToDetail = { articleId ->
                    navController.navigate("article_detail/$articleId")
                },
                onNavigateToCreate = {
                    navController.navigate("create_article")
                },
                onNavigateToEdit = { articleId ->
                    navController.navigate("edit_article/$articleId")
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                    }
                },
                onNavigateToChat = { /* TODO: Implement Chat Navigation */ },
                onNavigateToNotifications = { /* TODO: Implement Notifications Navigation */ }
            )
        }

        composable("profile") {
            ProfileScreen(
                onNavigateToHome = {
                    navController.navigate("article_list") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToChat = { /* TODO: Implement Chat Navigation */ },
                onNavigateToNotifications = { /* TODO: Implement Notifications Navigation */ },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToDetail = { articleId ->
                    navController.navigate("article_detail/$articleId")
                },
                onNavigateToEdit = { articleId ->
                    navController.navigate("edit_article/$articleId")
                },
                onNavigateToCreate = {
                    navController.navigate("create_article")
                }
            )
        }


        composable("article_detail/{articleId}") { backStackEntry ->
            // val articleId = backStackEntry.arguments?.getString("articleId") ?: "" // Tidak perlu lagi
            ArticleDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
                // onNavigateToEdit tidak lagi diteruskan karena fitur edit dihapus dari layar ini
                // onNavigateToEdit = { articleIdToEdit ->
                //     navController.navigate("edit_article/$articleIdToEdit")
                // }
            )
        }

        composable("create_article") {
            CreateEditArticleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                isEditing = false
            )
        }

        composable("edit_article/{articleId}") {
            CreateEditArticleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                isEditing = true
            )
        }
    }
}